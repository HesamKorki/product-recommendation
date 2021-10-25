import utils.{FileOperation, CommandLineOptions, DataUtil}
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.spark.rdd.RDD
import spray.json._

object Recommendation {
  def main(args: Array[String]): Unit = {

    // Parse command line arguments
    val options: CommandLineOptions = CommandLineOptions(
      this.getClass.getSimpleName,
      CommandLineOptions.inputPath("data/products.json"),
      CommandLineOptions.outputPath("output"),
      CommandLineOptions.productSKU(""),
      CommandLineOptions.quiet
    )
    val argz: Map[String, String] = options(args.toList)
    val out: String = argz("output-path")
    val dataPath: String = argz("input-path")
    val quiet: Boolean = argz("quiet").toBoolean
    val productSKU: String = argz("product-sku")

    val spark = SparkSession.builder
      .master("local[*]")
      .appName("Recommendation")
      .config("spark.app.id", "Recommendation")
      .config("spark.driver.host", "localhost")
      .getOrCreate()

    try {
      // verify input file exists
      FileOperation.verify(dataPath)
      // remove previous output
      FileOperation.verify_dir(out)
      FileOperation.rmrf(out)
      FileOperation.mkdir(out)

      // read in the data
      val data: DataFrame = spark.read.json(dataPath)

      // collect the input product to create recommendations for
      data.createOrReplaceTempView("products")
      val inQuery: DataFrame =
        spark.sql(s"select * from products where sku='$productSKU'")
      if (inQuery.rdd.take(1).length == 0) {
        throw new IllegalArgumentException("""
                Bad argument for product-sku (-p).
                run sbt "runMain Recommendation --help" for more information""")
        sys.exit(1)
      }
      val inProduct: (String, Array[Int]) =
        inQuery.rdd.map(DataUtil.parseRow).collect()(0)
      val attributeNum = inProduct._2.length

      // parse the data into a more usable form,
      // and save attribute differences from the input product
      val parsedProducts: RDD[(String, Array[Int])] =
        data.rdd.map(DataUtil.parseRow)
      val productsRelative: RDD[(String, Array[Int])] = parsedProducts.map {
        case (sku, attValues) =>
          val (inSKU, inAttValues) = inProduct
          (sku, (attValues, inAttValues).zipped.map(_ - _))

      }

      // score products by counting zeros in attribute differences
      val products: RDD[(String, Double)] = productsRelative
        .map { case (sku, values) =>
          var score: Double = 0.0
          for (i <- 0 to attributeNum - 1) {
            if (values(i) == 0) {
              // add 1/10+i to acount for alphabetic ordering value
              score += 1 + 1 / (i + attributeNum).toDouble
            }
          }
          val orgScore = DataUtil.roundAt2(score.toInt / attributeNum.toDouble)
          ((sku, orgScore), score)
        }
        .sortBy(_._2, false) // sort by weighted score
        .map { case (pr, score) => pr } // keep original scores
        .filter { case (sku, score) =>
          sku != productSKU
        } // not recommending the product itself

      case class Recom(sku: String, score: Double, rank: Int)
      object RecomFormatter extends DefaultJsonProtocol {
        implicit val recomFormat = jsonFormat3(Recom.apply)
      }
      import RecomFormatter._
      val recomList = new Array[String](10)
      var i: Int = 0
      products.take(10).foreach { case (sku, score) =>
        recomList(i) = Recom(sku, score, i + 1).toJson.compactPrint
        i += 1
      }

      // write out the recommendations
      if (!quiet) recomList.foreach(println)
      if (!quiet) println(s"Writing output to: $out/recommendations.json")

      FileOperation.write(out, "recommendations.json", recomList)

    } finally {
      spark.stop()
    }
  }
}

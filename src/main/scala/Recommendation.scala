import utils.FileOperation
import org.apache.spark.sql.{DataFrame, Row, SparkSession}
import org.apache.spark.rdd.RDD



object Recommendation {
    def main(args: Array[String]): Unit = {
        
        val spark = SparkSession.builder.
            master("local[*]").
            appName("Recommendation").
            config("spark.app.id", "Recommendation").
            getOrCreate()

        val sc = spark.sparkContext
        val sqlContext = spark.sqlContext
        import sqlContext.implicits._

        
        try {
            val out : String = "output/sku-recom1"
            FileOperation.rmrf(out)

            val input : String = "sku-123"
            val data : DataFrame = spark.read.json("data/products.json")
            data.createOrReplaceTempView("products")
            val inQuery : DataFrame = spark.sql(s"select * from products where sku='$input'")

            def parseRow(product: Row): (String, Array[Int]) = {
                val atts = product.getStruct(0)
                var attValues = new Array[Int](atts.length)
                for (i <- 0 to atts.length -1){
                    attValues(i) = atts.getString(i).split("-")(2).toInt
                }
                //val Array(a,b,c,d,e,f,g,h,i,j) = attValues
                (product.getString(1), attValues)
            }
            val inProduct = inQuery.rdd.map(parseRow).collect()(0)
            val parsedProducts = data.rdd.map(parseRow)
            val productsRelative = parsedProducts.map{case (sku, attValues) =>
                val (inSKU, inAttValues) = inProduct 
                (sku, (attValues, inAttValues).zipped.map(_-_))
                
            }
            val products = productsRelative.map{case (sku, values) => 
                var score : Double = 0.0
                for (i <- 0 to values.length -1) {
                    if (values(i) == 0) {
                        score += 1 + 1/(i+10).toDouble
                    }
                }
                ((sku, score.toInt/10.0), score)
            }.sortBy(_._2, false)
            .map{case(pr, score) => pr}
            .filter{case(sku, score) => sku != input}


            println(s"Writing output to: $out")
            products.saveAsTextFile(out)
            

        } finally {
            spark.stop()
        }
    }
}
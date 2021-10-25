package utils
import org.apache.spark.sql.Row

object DataUtil {

  def parseRow(product: Row): (String, Array[Int]) = {
    val atts = product.getStruct(0)
    val attValues = new Array[Int](atts.length)
    for (i <- 0 to atts.length - 1) {
      attValues(i) = atts.getString(i).split("-")(2).toInt
    }
    (product.getString(1), attValues)
  }

  def roundAt(p: Int)(n: Double): Double = {
    val s = math pow (10, p); (math round n * s) / s
  }
  def roundAt2(n: Double) = roundAt(2)(n)

}

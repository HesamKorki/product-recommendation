import java.io._
import scala.io.Source

// Run in local mode and local data.
object TestUtil {

  import utils.FileOperation._

  def verifyAndClean(actualFile: String, expectedFile: String, dirToDelete: String) =
    try {
      verify(actualFile, expectedFile)
    } finally {
      rmrf(dirToDelete)
    }


  def verify(actualFile: String, expectedFile: String) = {
    val actual   = Source.fromFile(actualFile).getLines.toSeq.sortBy(l => l)
    val expected = Source.fromFile(expectedFile).getLines.toSeq.sortBy(l => l)
    (actual zip expected).zipWithIndex.foreach {
      case ((a, e), i) =>
        val a2 = a.sortBy(c => c)
        val e2 = e.sortBy(c => c)
        assert(a2 == e2, s"$a != $e at line $i")
    }
  }
}
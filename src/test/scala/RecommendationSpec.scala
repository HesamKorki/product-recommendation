import org.scalatest.FunSpec
import utils.FileOperation

// Run in local mode and local data.
class RecommendationSpec extends FunSpec {

  describe ("Recommendation") {
    it ("computes the ten most similar products") {
      val out    = "output/kjv-wc2"
      val out2   = s"$out/part-00000"
      val expected = "expected/recom/part-00000"
      FileOperation.rmrf(out)  // Delete previous runs, if necessary.

      Recommendation.main(Array.empty[String])

      TestUtil.verifyAndClean(out2, golden, out)
    }
  }
}
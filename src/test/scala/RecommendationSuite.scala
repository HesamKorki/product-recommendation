import org.scalatest._
import funsuite._
import utils.FileOperation

// Run in local mode and local data.
class RecommendationSuite extends AnyFunSuite {
  test("providing a directory for input-path should throw FileOperationError") {
    assertThrows[FileOperation.FileOperationError] {
      Recommendation.main(Array("-i", "data", "-p", "sku-1234"))
    }
  }
  test(
    "providing a non-existing file for input-data should throw FileOperationError"
  ) {
    assertThrows[FileOperation.FileOperationError] {
      Recommendation.main(
        Array("-i", "data/non-existing-file.json", "-p", "sku-1234")
      )
    }
  }
  test(
    "providing a file for output-path directory should throw FileOperationError"
  ) {
    assertThrows[FileOperation.FileOperationError] {
      Recommendation.main(Array("-o", "data/products.json", "-p", "sku-1234"))
    }
  }
  test(
    "providing a non-existing directory for the output-path should create output file within that directory"
  ) {
    val outPath = "myOut"
    Recommendation.main(Array("-o", outPath, "-p", "sku-1234"))
    val outFile = FileOperation.join(outPath, "recommendations.json")
    assert(FileOperation.exists(outFile))
    // Clean up
    FileOperation.rmrf(outPath)
  }
  test(
    "providing a product-sku that is not in the right format should throw IllegalArgumentException"
  ) {
    assertThrows[IllegalArgumentException] {
      Recommendation.main(Array("-p", "sku34"))
    }
  }
  test(
    "providing a product-sku that is out of range should throw IllegalArgumentException"
  ) {
    assertThrows[IllegalArgumentException] {
      Recommendation.main(Array("-p", "sku-34000"))
    }
  }
  test(
    "providing a particular sku should generate an output that is identical to the expected result"
  ) {
    val expected = "expected/sku-1234Result.json"
    Recommendation.main(Array("-p", "sku-1234"))
    val actual = "output/recommendations.json"
    TestUtil.verifyAndClean(actual, expected, "output")
  }
  test(
    "The recommendations for the testData1 should be identical to the expected file for sku-1"
  ) {
    val expected = "expected/testResult1.json"
    Recommendation.main(Array("-i", "data/test/testData1.json", "-p", "sku-1"))
    val actual = "output/recommendations.json"
    TestUtil.verifyAndClean(actual, expected, "output")

  }
  test(
    "The recommendations for the testData2 should be identical to the expected file for sku-1"
  ) {
    val expected = "expected/testResult2.json"
    Recommendation.main(Array("-i", "data/test/testData2.json", "-p", "sku-1"))
    val actual = "output/recommendations.json"
    TestUtil.verifyAndClean(actual, expected, "output")

  }

}

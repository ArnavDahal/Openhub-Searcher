
import org.scalatest.FunSuite

import ElasticSearch_Client.printThis

class ElasticSearch_Client$Test extends FunSuite {

  //
  // This test is simply to show that we learned how
  // to write unit tests in scala
  //
  test("PrintThisResult")
  {
    // this is a print call.
    printThis("ID", "Name", "Url", "HTML_URL",
      "description", "main_language", "filename", "C:\\path",
      "query", 1)

    // simply asserts if the above call was sucessful
    assert(true)
  }


}

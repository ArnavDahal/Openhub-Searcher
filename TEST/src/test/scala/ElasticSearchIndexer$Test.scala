

import org.scalatest.FunSuite
import org.scalatest.Matchers._
import org.scalactic.Explicitly._
import org.scalactic.StringNormalizations._
import ElasticSearchIndexer._
import org.apache.commons.io.FileUtils
import org.json4s
import org.json4s.JsonDSL._
import org.json4s.jackson.JsonMethods._
import org.json4s.Xml.toJson
import java.io._

import org.apache.commons.io.FileUtils.deleteDirectory

/**
  * Created by UmairKhan on 11/9/2016.
  */
class ElasticSearchIndexer$Test extends FunSuite {

  test("FileExisting")
  {
    val file = new File("Non_Existing_Folder")

    assert(!file.exists())
  }

  test("RecursiveListOfFiles_With5Files")
  {
    val dir_testing = new File("testing_temp")
    dir_testing.mkdir()

    new PrintWriter(dir_testing.getAbsolutePath +  "\\file1")
    new PrintWriter(dir_testing.getAbsolutePath +  "\\file2")
    new PrintWriter(dir_testing.getAbsolutePath +  "\\file3")
    new PrintWriter(dir_testing.getAbsolutePath +  "\\file4")
    new PrintWriter(dir_testing.getAbsolutePath +  "\\file5")

    assert(getRecursiveListOfFiles(dir_testing).length == 5)

    FileUtils.deleteQuietly(dir_testing)
  }

  test("RecursiveListOfFiles_With0Files")
  {
    val dir_testing = new File("testing_temp2")
    dir_testing.mkdir()

    assert(getRecursiveListOfFiles(dir_testing).length == 0)

    FileUtils.deleteQuietly(dir_testing)
  }

  test("ConvertToJson")
  {
    // Our xml and json format
    val xml = "<note>\n       <to>Tove</to>\n       <from>Jani</from>\n       <heading>Reminder</heading>\n       <body>Don't forget me this weekend!</body>\n</note>"
    val json = "{\n  \"note\": {\n    \"to\": \"Tove\",\n    \"from\": \"Jani\",\n    \"heading\": \"Reminder\",\n    \"body\": \"Don't forget me this weekend!\"\n  }\n}"

    // testing the XmlToJson method by loading the xml to an Xml content
    val xml_content = scala.xml.XML.loadString(xml)
    val observed_json = toJson(xml_content)

    // converting our expected json to the methods json object
    val expected_json = parse(json)

    // Assert both expected_json object with the observed_json object
    assert(expected_json == observed_json)

  }

}

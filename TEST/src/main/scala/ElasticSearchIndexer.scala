
import java.io._
import java.net.{HttpURLConnection, URL}

import org.json4s
import org.json4s.JsonDSL._
import org.json4s.jackson.JsonMethods._
import org.json4s.Xml.{toJson}


object ElasticSearchIndexer extends App {

  //
  // This method is to add a document to Elastic Search
  // using the CURL command and adding the built document
  // to the POST request
  //
  def addToElasticSearch(data:String): Unit = {
    try {

      // url for local instance of Elastic Search
      val url = "http://localhost:9200/documents/document/"

      // Instantiate URL object and open HttpURLConnection
      val obj = new URL(url)
      val conn = obj.openConnection().asInstanceOf[HttpURLConnection]

      conn.setDoOutput(true)

      // post curl
      conn.setRequestMethod("POST")

      val out = new OutputStreamWriter(conn.getOutputStream())
      out.write(data)
      out.close()

      new InputStreamReader(conn.getInputStream())

    } catch {
      case e: Exception =>
    }
  }

  //
  // This method recursively gets all the list
  // of files from a starting directory File
  //
  def getRecursiveListOfFiles(dir: File): Array[File] = {
    val these = dir.listFiles
    these ++ these.filter(_.isDirectory).flatMap(getRecursiveListOfFiles)
  }

  //
  // Create new instance of XML dir in Downloaded Projects directory
  // If it does not exist then throw an error b/c it should have been
  // created previously by the GitDownloader
  //
  var xml_dir = new File("DownloadedProjects\\XML")

  if (!xml_dir.exists())
    throw new FileNotFoundException("Could not find folder " + xml_dir.getName + ". Did you forget to run part 1?")

  //
  // Create new instance of JSON dir in Downloaded Projects directory
  // If it does not exist then make the dir. This is where our JSONs
  // will be stored. This can be commented out later given that we
  // really have no use to save the JSON before adding to Elastic Search
  //
  var json_dir = new File("DownloadedProjects\\JSON")

  if (!json_dir.exists())
    json_dir.mkdir()

  //
  // For Loop: For every xml file, there is a corresponding project folder
  // in the Git dir. Create the Json for the XML as a generic for the files
  // inside the project folder.
  //
  for (file <- xml_dir.listFiles if file.getName endsWith ".xml") {


    val repo_id = file.getName.substring(0, file.getName.length - 4)
    val newFileName = file.getName().substring(0, file.getName().length-4)

    val xml_content = xml.XML.loadFile(file)
    val json = toJson(xml_content)

    //
    // Open the project dir  in DownloadProjects dir
    //
    val project_dir = new File("DownloadedProjects\\Git\\" + repo_id)
    // Verify the dir exists
    if (!project_dir.exists())
      throw new FileNotFoundException("Could not find folder in the \"Git\" "
        + repo_id + "directory. Did you forget to run part 1?")

    // recursively traverse that id's Git Folder to retrieve a list of every file and folder in there
    val entire_directory_list = getRecursiveListOfFiles(project_dir)

    var code = List[json4s.JValue]()
    //Go through the list
    for (x <- entire_directory_list) {
      if (x.isFile) {
        try {

          // get filename and data inside
          val files = render("filename", x.getName) merge render("Code", scala.io.Source.fromFile(x).mkString) merge
            render("absolute_path", x.getAbsolutePath())

          //  concatenate it to the list
          code = (json merge files) :: code

        }
        catch {
          case e: Exception =>
        }
      }
    }

    // for all code files upload to elastic search with extra file name
    var i = 1
    for(readable_json <- code)
      {
        addToElasticSearch(pretty(readable_json))//, (newFileName + "(" + Integer.toString(i) + ")"))
        i+=1
      }
    println("================================" + newFileName +  "========================================")

  }
}



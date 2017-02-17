
import java.io.{BufferedReader, InputStreamReader, OutputStreamWriter}
import java.net.{HttpURLConnection, URL}

import com.jayway.jsonpath.{JsonPath, ReadContext}
import net.minidev.json.JSONArray

object ElasticSearch_Client extends App {

  //
  // START_OF_MAIN_CODE() ======================================================================================


  println("\nWelcome to the Elastic Search Engine.\n\n")

  while(true) {
    // Get IP:Port for elastic search instance, default to "localhost:9200"
    println("Enter an \"IP:Port\" for Elastic Search Instance (or press enter to use \"localhost:9200\"): ")
    val ip = scala.io.StdIn.readLine()

    // Get a search query to search through the elastic search index
    println("Enter a search query: ")
    val query = scala.io.StdIn.readLine()

    // Get a search query to search through the elastic search index
    println("Enter an attribute, or press enter to search all attributes: ")
    val attribute = scala.io.StdIn.readLine()

    println("Enter the number of responses that you want to receive: ")
    val num_results = scala.io.StdIn.readLine()

    val searchResultString = queryElasticSearch(query, ip, attribute)

    // check if there was in fact a result of the search
    if (!searchResultString.isEmpty) {
      val searchResult = JsonPath.parse(searchResultString).asInstanceOf[ReadContext]

      PrintResults(searchResult, query, num_results)
    }
    else {
      println("=============================================================================")
      println("\nOops it seems like there were no documents with that search query.\n" +
        "Please try a different search\n")
      println("=============================================================================\n")

    }

  }


  //
  // END_OF_MAIN_CODE() ========================================================================================
  //

  //
  // Method to query elastic search instance and obtain results
  //
  def queryElasticSearch(query:String, ip_port:String, attribute:String): String = {

    // the url for local host
    var url = "http://localhost:9200/_search"

    // if the user selected to pass in a different external host
    if(!ip_port.isEmpty)
    {
      url = "http://" + ip_port + "/_search"
    }

    //
    // set up the data in json format to query
    // Be careful to not change this query unless you know what your doing
    //
    var data = new String()
    if (attribute.isEmpty)
      data = "{\n    \"query\": {\n        \"query_string\": {\n            \"query\": \"" + query + "\"\n        }\n    }\n}"
    else
      data = "{\n    \"query\": {\n        \"query_string\": {\n            \"query\": \"" + query + "\",\n            \"fields\": [\"" + attribute + "\"]\n        }\n    }\n}"


    //
    // Try-Catch Clause
    //
    try {

      //
      // url obj with properties set to retrieve to POST and retrieve JSON input
      //
      val obj = new URL(url)
      val conn = obj.openConnection().asInstanceOf[HttpURLConnection]
      conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");

      conn.setDoOutput(true)
      conn.setDoInput(true)

      conn.setRequestMethod("POST")

      // write out the curl request
      val out = new OutputStreamWriter(conn.getOutputStream())
      out.write(data)
      out.close()


      // read in all the lines that the elasticsearch api returns with the call
      var lines = ""
      val reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))

      lines = Stream.continually(reader.readLine()).takeWhile(_ != null).mkString("\n")

      // return the search result
      return lines

    } catch {
      case e: Exception =>
        // return an empty search result
        if(e.toString == "java.net.ConnectException: Connection refused: connect") {
          println(e.toString)
          println("Connecting to the Elastic Search instance at: " + url + " failed.")
          println("Please try correcting the IP:port or ensuring an Elastic Search is running locally.")
          println("")
        }
        return ""
    }
  }

  //
  // Method to print results or search in pretty format
  //
  def PrintResults(searchResult:ReadContext, query:String, num_results:String){

    // query the json
    val ids = searchResult.read("$..[1]._source.response.result.project.id").asInstanceOf[JSONArray]
    val names = searchResult.read("$..[1]._source.response.result.project.name").asInstanceOf[JSONArray]
    val urls = searchResult.read("$..[1]._source.response.result.project.url").asInstanceOf[JSONArray]
    val html_urls = searchResult.read("$..[1]._source.response.result.project.html_url").asInstanceOf[JSONArray]
    val descriptions = searchResult.read("$..[1]._source.response.result.project.description").asInstanceOf[JSONArray]
    val main_language = searchResult.read("$..[1]._source.response.result.project.analysis.main_language_name").asInstanceOf[JSONArray]
    val filenames = searchResult.read("$..[1]._source.filename").asInstanceOf[JSONArray]
    val absolute_paths = searchResult.read("$..[1]._source.absolute_path").asInstanceOf[JSONArray]

    if (ids.size <= 0)
      {
        println("")
        println("Your search query: \"" + query + "\" obtained no results please try another query.")
        println("")
        return
      }

    // for the number of ids returned print the data
    for(i <- 0 to (ids.size()-1); if i< Integer.valueOf(num_results))
      {
        try {
          printThis(ids.get(i).toString,
            names.get(i).toString,
            urls.get(i).toString,
            html_urls.get(i).toString,
            descriptions.get(i).toString,
            main_language.get(i).toString,
            filenames.get(i).toString,
            absolute_paths.get(i).toString,
            query, (i + 1))
        } catch {
          case e: Exception =>
            // just keep attempting.
        }
      }
  }

  //
  // Method to actually print it out
  //
  def printThis(id:String, name:String, url:String, html_url:String,
              description:String, main_language:String, filename:String, absolute_path:String,
              query:String, i:Int) {

    println(i + ".   Query: \"" + query + "\" was found in file: \"" + filename + "\"")
    println("     File: \"" + filename + "\" can be found at: \"" + absolute_path + "\"")
    println("       Project's ID: " + id)
    println("       Project's Name: " + name)
    println("       Project's Url: " + url )
    println("       Project's Html_Url: " + html_url)
    println("       Project's Description: " + description)
    println("       Project's Main Language: " + main_language)
    println("")

  }

}

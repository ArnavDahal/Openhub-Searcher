import java.io._
import java.net.URL

import akka.actor.{Actor, ActorSystem, Props}

import org.apache.commons.io.FileUtils
import org.eclipse.jgit.api.Git
import scala.xml.XML

/*
API Keys
  a7e0c1c1dfa5e7f16f6a86f48dd9a3e6db991d003528b7278ca5559079799ee0
  a2f21c23e89c5202cbc3efc5984d3b9e48d7ec5a7547ef8918ce913f47dfdf47
  c0b3efa50307b3fbfd33df56bc41b061820b1d328d4d4a1b2221828b6c9a1a56
  a5e2b49a015703b5bc1c13d3a51944df6ee8a7ee37b3985faf66eca9846146fe
  b3292c5f94bbd4a0d5580d17b5cceaf8a1f390d22017c2d20c3a90f2c5abd853
  888b8c23846f29cc56fae4246c1664823594c697ad7f99e7217fb8b795e62e5c
*/


object GitDownloader extends App {

  // GetProjectDownloader case for getting downloads
  case class GetProjectDownloader()

  // UploadDownloadsToElasticSearch()
  case class UploadDownloadsToElasticSearch()

  class ProjectDownloader extends Actor {

    // object wide static counter variable for GitDownloader
    var counter = 1

    // object wide static counter variable for GitDownloader
    var total_Projects_searched = 0

    // Object-wide Regular Expression that matches anything git://github<wildcard>.git
    val regex = "(git:\\/\\/github).*(.git)"

    // Total projects to download
    val num_downloads = 10

    override def receive: Receive = {
      case GetProjectDownloader() => {

        // Starting and ending range of Project IDs
        val startIndex = 26001
        val endIndex = 27000

        // URLs
        val baseUrl = "https://www.openhub.net/projects/"
        val endUrl = "/enlistments.xml?api_key="
        val apiKey = "a7e0c1c1dfa5e7f16f6a86f48dd9a3e6db991d003528b7278ca5559079799ee0"

        // Directory where saved files go
        val dir = new File("DownloadedProjects")
        val xml_dir = new File("DownloadedProjects\\XML")
        val gir_src_dir = new File("DownloadedProjects\\Git")

        // The request string to Ohloh
        var curlReq = ""

        // Creates the gitSrcs local directory if it doesn't exist
        if (!dir.exists())
          dir.mkdir()
        if (!xml_dir.exists())
          xml_dir.mkdir()
        if (!gir_src_dir.exists())
          gir_src_dir.mkdir()

        for (i <- startIndex to endIndex; if counter <= num_downloads) {

          // Creates a string to send to Ohloh's API
          // Ex. https://www.openhub.net/projects/{i}/enlistments.xml?api_key={apiKey}
          curlReq = baseUrl + i + endUrl + apiKey

          DownloadProject(curlReq, apiKey, i)
        }

        println("")
        println("Total Projects: " + (counter-1))
        println("Projects Searched: " + total_Projects_searched)

        return null
      }
    }

    def DownloadProject(curlReq: String, apiKey: String, repo_id: Int) = {

      // Will send the curlReq query and retrieve the XML payload
      try {
        total_Projects_searched += 1
        val data = XML.load(new URL(curlReq))

        // Get the repository node
        val repoNode = (data \\ "repository") \ "url"
        var repoText = repoNode.text

        // Checks if the repository is github
        if (repoText.matches(regex)) {
          // Grabs the XML file with Analyses from OhLoh API and saves it
          val out = new PrintWriter("DownloadedProjects\\XML\\" + repo_id + ".xml")
          out.println(XML.load(new URL("https://www.openhub.net/projects/" + repo_id + ".xml?api_key=" + apiKey)))
          out.close()

          // Safe handling of two concat git urls, removes the last one
          // Drops the git part of the URI and converts to URL
          repoText = "https" + repoText.substring(3, repoText.indexOf(".git")) + ".git"

          val dir = new File("DownloadedProjects\\Git\\" + repo_id)

          // Uses JGit API to download GitHub repo
          Git.cloneRepository()
            .setURI(repoText)
            .setDirectory(dir)
            .call()

          println(counter + ") Downloaded:" + " (" + repo_id + ") " + repoText)
          counter += 1
        }
      }
      catch {
        case e: Exception => //delete bad file

          FileUtils.deleteQuietly(new File("DownloadedProjects\\Git\\" + repo_id)) //delete the directory in Git
          FileUtils.deleteQuietly(new File("DownloadedProjects\\XML\\" + repo_id + ".xml")) //delete the .xml
      }
    }
  }

  val system = ActorSystem("SimpleSystem")
  val projectDownloaderActor = system.actorOf(Props[ProjectDownloader], "ProjectDownloader")

  projectDownloaderActor ! GetProjectDownloader()

  // Terminate system
  system.terminate()

}
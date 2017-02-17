import GitDownloader.{GetProjectDownloader, ProjectDownloader}
import akka.actor.{ActorRef, ActorSystem, Props}
import org.scalatest.FunSuite

class GitDownloader$Test extends FunSuite {


  test("TestGitDownloader")
  {

    // Initialize the actor system
    val system = ActorSystem("SimpleSystem")
    val projectDownloaderActor = system.actorOf(Props[ProjectDownloader], "ProjectDownloader")

    // Simple Assert of Type
    assert(projectDownloaderActor.isInstanceOf[ActorRef])

  }



}

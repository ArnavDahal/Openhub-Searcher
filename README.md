Information:
	There are three parts: 
		1. a downloader (GitDownloader)
		2. an indexer (ElasticSearchIndexer)
		3. and a client (ElasticSearch_Client)
		
	This project will download Github repositories from OpenHub listings.
	The start ranges we chose were 26000-27000 because through testing we found that this range had the most downloadable GitHub projects.
	
		1.
			The downloader (GitDownloader) will use the Ohloh API to pull enlistments from the range specified and parse through the XML payload to find GitHub URIs.
			Then we will use the JGit API to download the project from GitHub and use Ohloh API to download the XML analysis file of the project. We used scalaXML to load in Ohloh's enlistments. Then, we used a regular expression parser to get the GitHub URI. We then use the GIT URI with JGits downloader.
	
		2.
			From there, our indexer program (ElasticSearchIndexer) will take in all of the files from '/DownloadedProjects/' and convert the XML analysis payloads to JSON.
			Every class will have a base JSON from the XML analysis and as an element the content from a text-readable file. 
	
		3.
			Finally, our client program (ElasticSearch_Client) takes in a set of keywords that the user wants to search for and will pass them as a parameter to a CURL request
			that queries ElasticSearch. 
				It outputs information about the files that contain the search query
	
	
Structure:
    The structure of this program consists of 3 major parts:
	
        Part 1: GitDownloader.scala -
			Downloads .xml and Git src files and folders from a given Github/Openhub URL using Akka Actors.
		
        Part 2: ElasticSearchIndexer.scala -
			Converts XML to JSON, and then concatenates the JSON with all readable project files.
			Then, the entire JSON content is indexed into ElasticSearch.
        
		Part 3: ElasticSearch_Client.scala - 
			User is prompted to enter key terms. These terms are fed into ElasticSearch and a response is received.
		
////////////////////////////////////////////////////////////////////////////////////		
How to run:
    Prerequisites:
   
			The program was developed and deployed on a local machine running the Windows OS with Elastic Search installed.
			We suggest running it on a Windows environment.
			Also ensure you have access to Scala version 2.11.0 and NOT 2.12.0. The latest Akka library 2.4.12 is NOT compatible with latest verision of Scala 2.12.0. The build.sbt already includes 
			the necessary version. (this is just a note)
			
			The program was also tested and found to work in a Linux environment.
				- To run it on a Linux machine, you must go into the source files (GitDownloader, ElasticSearchIndexer) and change every escaped path slash for windows "\\" to the linux "/".
		
	Step 0: There are 3 programs seperated into two scala SBT projects: TEST and Project3_ClientApplication.
			Load TEST into Intellij. Make sure build.sbt has no errors and that all dependencies are loaded.
			Make sure ElasticSearch is installed and running on your machine.
			
	Step 1: From the menu, select:
				Build > Make Project
				Run > Run... 
					choose GitDownloader
			
			Let GitDownloader run (10 projects take approximately 4 minutes on a 30Mb connection to download)
		
	Step 2: Run > Run...
				choose ElasticSearchIndexer
			
			Let ElasticSearchIndexer run (indexing 10 projects into Elastic search takes approximately 1 minute on a 30Mb connection)
			
	Step 3:	Load Project3_ClientApplication into Intellij.
	
	Step 4: From the menu, select:
				Build > Make Project
				Run > Run... 
					choose ElasticSearch_Client
					
			The user will be prompted for an "IP:port" 
				if user is not searching on local ElasticSearch instance, enter IP:port 
					For example: "146.148.96.33:9200" (supplied in 'Information' heading)
				if user is searching on local ElasticSearch instance, simply press ENTER to use "localhost:9200"
			
			The user will then be prompted for a search query.
						Enter a query that will be fed into ElasticSearch.
			
			Then the user will be prompted to enter an attribute that they would like to search the query in. 
				(eg. search for the string "c++" only in the "Languages" attribute. It is necessary for the user to know in advance
				 which attributes are available. A list of the few common ones are listed below. They can also be found in the example 
				 json file named: "sample_json_structure.json")
			If the user wants to search all attributes they must simply press enter key, and the application will search for the query 
			throughout every attribute.
				 
			Then, the user will be prompted for the number of results that the user wants to retrieve.
						Enter the number of results desired.
				The correct number of results will be shown (if that many results exist).


////////////////////////////////////////////////////////////////////////////////////
Limitations:
	-	Only the ElasticSearch Client allows the user to specify an ElasticSearch instance by 
		providing IP:port. To get the server applications inside of the TEST project to target 
		an external ElasticSearch instance, the user must go through the code and change localhost:9200
		to their specific IP:port. In other words, GitDownloader and ElasticSearchIndexer can only 
		add data to a local instance of ElasticSearch (unless IP:port is changed within the code to allow for remote instance of ElasticSearch). 
		The ElasticSearch_Client can query BOTH local and remote instances of ElasticSearch by the user's specification.
	
	-	ElasticSearchIndexer does not parse or discard any information such as language specific keywords.
		It simply adds each project file in its entirety to the ElasticSearch engine. We thought this was the 
		best approach to retain all information about set projects, including the code format. 

////////////////////////////////////////////////////////////////////////////////////
Tests:
	Unit Tests:
		We created instances of Scala Unit Tests from within the IntelliJ IDE. Here is the list of the following 
		unit tests corresponding to the applications:
			- GitDownloader
				+ test("TestGitDownloader")
			- ElasticSearchIndexer
				+ test("ConvertToJson")
				+ test("RecursiveListOfFiles_With5Files")
				+ test("RecursiveListOfFiles_With0Files")
				+ test("FileExisting")
			-ElasticSearch_Client
				+ test("PrintThisResult")
		Most of these tests are pretty basic because our implementation doesn't utilize many methods are simpler 
		means of testing. They are mostly API calls or file and folder manipulation. Therefore, these tests are 
		to show that we researched and learned how to perform Unit Testing in Scala. 
		
		How to run the unit tests:
			- Open up the both projects in IntelliJ. The Scala Unit tests are in the "..\TEST\src\test" directory for both projects.
			- Open up scala test code and then: 
				- RightClick on the name of the class and hit Run..
			- The tests output will be shown in the test runner console.
	
	Load Tests:
		We ran load tests on searching by ID, URL, and by any Term.
		Our results are shown in the LoadTests folder along with the saved project file for SoapUI.
			* When searching by URL it would take up to 250 seconds, and would sometimes run out of memory on SoapUI

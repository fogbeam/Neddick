import grails.util.Environment

import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.document.Document
import org.apache.lucene.index.IndexWriter
import org.apache.lucene.index.IndexWriter.MaxFieldLength
import org.apache.lucene.store.Directory
import org.apache.lucene.store.NIOFSDirectory
import org.apache.lucene.util.Version
import org.fogbeam.neddick.Channel
import org.fogbeam.neddick.User

class BootStrap {

	 def entryCacheService;
	 def siteConfigService;
	 
     def init = { servletContext ->
     
		 this.getClass().classLoader.rootLoader.URLs.each { println it }
		 
	     switch( Environment.current )
	     {
	         case Environment.DEVELOPMENT:
	        	 createDefaultChannel();
	        	 createAdminUser();
	             createSomeUsers();
	             createAnonymousUser();
	             break;
	         case Environment.PRODUCTION:
	             println "No special configuration required";
	             break;
	     }
	

		 /* TODO: Hook into install / upgrade steps here.  Setup default values for siteConfig
		  * options, etc.  We'll use a "class per version" and a value in the DB that maintains
		  * the "current version" to manage upgrades.
		  */
		 
		 		 
		 String indexDirLocation = siteConfigService.getSiteConfigEntry( "indexDirLocation" );
		 println "indexDirLocation: ${indexDirLocation}";
		 if( indexDirLocation )
		 {
			 File indexFile = new java.io.File( indexDirLocation );
			 String[] indexFileChildren = indexFile.list();
			 boolean indexIsInitialized = (indexFileChildren != null && indexFileChildren.length > 0 );
			 if( ! indexIsInitialized )
			 {
				 println "Index not previously initialized, creating empty index";
				 /* initialize empty index */
				 Directory indexDir = new NIOFSDirectory( indexFile );
				 IndexWriter writer = new IndexWriter( indexDir, new StandardAnalyzer(Version.LUCENE_30), true, MaxFieldLength.UNLIMITED);
				 Document doc = new Document();
				 writer.addDocument(doc);
				 writer.close();
			}
			else
			{
				
				println "Index already initialized, skipping...";	
			}
		 }
		      
     }
     
     
     def destroy = {
     
     }
     
     void createDefaultChannel()
     {
    	 if( !Channel.findByName( "default" ) )
    	 {
    		 println "Fresh Database, creating DEFAULT channel";
    		 def channel = new Channel(name:"default");
    		 if( !channel.save() )
    		 {
    			 println( "Saving DEFAULT channel failed!" );
    		 }
    	 }
    	 else
    	 {
    		 println "Existing DEFAULT channel, skipping...";
    	 }
     }
     
     void createSomeUsers()
     {
         if( !User.findByUserId( "prhodes" ))
         {
             println "Fresh Database, creating PRHODES user";
             def user = new User( userId: "prhodes", password: "secret",
                     fullName: "Phillip Rhodes", email: "prhodes@example.com", bio:"" );
             
             if( !user.save() )
             {
                 println( "Saving PRHODES user failed!");
             }
             
         }
         else
         {
             println "Existing PRHODES user, skipping...";
         }     	 
     
         for( int i = 0; i < 20; i++ )
         {
             if( !User.findByUserId( "testuser${i}" ))
             {
                 println "Fresh Database, creating TESTUSER ${i} user";
                 def user = new User( userId: "testuser${i}", password: "secret",
                         fullName: "Test User ${i}", email: "testuser${i}@example.com", bio:"" );
                 
                 if( !user.save() )
                 {
                     println( "Saving TESTUSER ${i} user failed!");
                 }
                 
             }
             else
             {
                 println "Existing TESTUSER ${i} user, skipping...";
             }         	 
         }
     }
     
     void createAnonymousUser()
     {
         if( !User.findByUserId( "anonymous" ))
         {
             println "Fresh Database, creating ANONYMOUS user";
             def user = new User( userId: "anonymous", password: "secret",
                     fullName: "Anonymous User", email: "anonymous@yourhost.com", bio:"" );
             
             if( !user.save() )
             {
                 println( "Saving ANONYMOUS user failed!");
             }
             
         }
         else
         {
             println "Existing ANONYMOUS user, skipping...";
         }    	 
     }
     
     void createAdminUser()
     {
         if( !User.findByUserId( "admin" ))
         {
             println "Fresh Database, creating ADMIN user";
             def user = new User( userId: "admin", password: "secret",
            		 fullName: "Site Administrator", email: "admin@yourhost.com", bio:"" );
             if( !user.save() )
             {
            	 println( "Saving ADMIN user failed!");
             }
             
         }
         else
         {
             println "Existing ADMIN user, skipping...";
         }
         
     }     
     
     
} 
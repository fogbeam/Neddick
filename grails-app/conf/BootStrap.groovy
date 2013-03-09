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
     
		 // this.getClass().classLoader.rootLoader.URLs.each { p rintln it }
		 
	     switch( Environment.current )
	     {
	         case Environment.DEVELOPMENT:
	        	 createDefaultChannel();
	        	 createAdminUser();
	             createSomeUsers();
				 createSomeChannels();
	             createAnonymousUser();
	             break;
	         case Environment.PRODUCTION:
	             log.info( "No special configuration required" );
	             break;
	     }
	

		 /* TODO: Hook into install / upgrade steps here.  Setup default values for siteConfig
		  * options, etc.  We'll use a "class per version" and a value in the DB that maintains
		  * the "current version" to manage upgrades.
		  */
		 
		 		 
		 String indexDirLocation = siteConfigService.getSiteConfigEntry( "indexDirLocation" );
		 log.debug( "indexDirLocation: ${indexDirLocation}" );
		 if( indexDirLocation )
		 {
			 File indexFile = new java.io.File( indexDirLocation );
			 String[] indexFileChildren = indexFile.list();
			 boolean indexIsInitialized = (indexFileChildren != null && indexFileChildren.length > 0 );
			 if( ! indexIsInitialized )
			 {
				 log.debug( "Index not previously initialized, creating empty index" );
				 /* initialize empty index */
				 Directory indexDir = new NIOFSDirectory( indexFile );
				 IndexWriter writer = new IndexWriter( indexDir, new StandardAnalyzer(Version.LUCENE_30), true, MaxFieldLength.UNLIMITED);
				 Document doc = new Document();
				 writer.addDocument(doc);
				 writer.close();
			}
			else
			{
				
				log.info( "Index already initialized, skipping..." );	
			}
		 }
		 else
		 {
		 	log.warn( "No indexDirLocation configured!!");
		 }
		      
     }
     
     
     def destroy = {
     
     }
     
     void createDefaultChannel()
     {
    	 if( !Channel.findByName( "default" ) )
    	 {
    		 log.info( "Fresh Database, creating DEFAULT channel" );
    		 def channel = new Channel(name:"default");
    		 if( !channel.save() )
    		 {
    			 log.error( "Saving DEFAULT channel failed!" );
    		 }
    	 }
    	 else
    	 {
    		 log.info( "Existing DEFAULT channel, skipping..." );
    	 }
     }
     
	 void createSomeChannels()
	 {
		 for( int i = 0; i < 40; i++ )
		 {
			 if( !Channel.findByName( "channel${i}" ))
			 {
				 log.info( "Fresh Database, creating channel${i} channel" );
				 def channel = new Channel( name: "channel${i}", description:"Channel${i}" );
				 
				 if( !channel.save() )
				 {
					 log.error( "Saving channel${i} channel failed!");
				 }
				 
			 }
			 else
			 {
				 log.info( "Existing channel${i} channel, skipping..." );
			 }
		 }
	}
	 
     void createSomeUsers()
     {
         if( !User.findByUserId( "prhodes" ))
         {
             log.info( "Fresh Database, creating PRHODES user" );
             def user = new User( userId: "prhodes", password: "secret",
                     fullName: "Phillip Rhodes", email: "prhodes@example.com", bio:"" );
             
             if( !user.save() )
             {
                 log.error( "Saving PRHODES user failed!");
             }
             
         }
         else
         {
             log.info( "Existing PRHODES user, skipping..." );
         }     	 
     
         for( int i = 0; i < 40; i++ )
         {
             if( !User.findByUserId( "testuser${i}" ))
             {
                 log.info( "Fresh Database, creating TESTUSER ${i} user" );
                 def user = new User( userId: "testuser${i}", password: "secret",
                         fullName: "Test User ${i}", email: "testuser${i}@example.com", bio:"" );
                 
                 if( !user.save() )
                 {
                     log.error( "Saving TESTUSER ${i} user failed!");
                 }
                 
             }
             else
             {
                 log.info( "Existing TESTUSER ${i} user, skipping..." );
             }         	 
         }
     }
     
     void createAnonymousUser()
     {
         if( !User.findByUserId( "anonymous" ))
         {
             log.info( "Fresh Database, creating ANONYMOUS user" );
             def user = new User( userId: "anonymous", password: "secret",
                     fullName: "Anonymous User", email: "anonymous@yourhost.com", bio:"" );
             
             if( !user.save() )
             {
                 log.error( "Saving ANONYMOUS user failed!");
             }
             
         }
         else
         {
             log.info( "Existing ANONYMOUS user, skipping..." );
         }    	 
     }
     
     void createAdminUser()
     {
         if( !User.findByUserId( "admin" ))
         {
             log.info( "Fresh Database, creating ADMIN user" );
             def user = new User( userId: "admin", password: "secret",
            		 fullName: "Site Administrator", email: "admin@yourhost.com", bio:"" );
             if( !user.save() )
             {
            	 log.error( "Saving ADMIN user failed!");
             }
             
         }
         else
         {
             log.info( "Existing ADMIN user, skipping..." );
         }
         
     }     
} 
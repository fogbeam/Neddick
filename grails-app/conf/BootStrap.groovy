import grails.util.Environment

import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.document.Document
import org.apache.lucene.index.IndexWriter
import org.apache.lucene.index.IndexWriter.MaxFieldLength
import org.apache.lucene.store.Directory
import org.apache.lucene.store.NIOFSDirectory
import org.apache.lucene.util.Version
import org.fogbeam.neddick.AccountRole
import org.fogbeam.neddick.Channel
import org.fogbeam.neddick.User

class BootStrap {

	 def entryCacheService;
	 def siteConfigService;
	 def userService;
	 
     def init = { servletContext ->
     
		 // this.getClass().classLoader.rootLoader.URLs.each { p rintln it }
		 
	     switch( Environment.current )
	     {
	         case Environment.DEVELOPMENT:
				 createRoles();
				 createAdminUser();
	             createSomeUsers();
	             createAnonymousUser();
				 createDefaultChannel();
				 createSomeChannels();
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
    		 Channel channel = new Channel(name:"default");
			 channel.privateChannel = false;
			 
			 User userAnon = User.findByUserId( "anonymous" );
			  
			 channel.owner = userAnon;
			 
    		 if( !channel.save() )
    		 {
    			 log.error( "Saving DEFAULT channel failed!" );
				 channel.errors.allErrors.each { println it; };
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
				 
				 
				 channel.privateChannel = false;
				 
				 User userAnon = User.findByUserId( "anonymous" );
				  
				 channel.owner = userAnon;
				 
				 if( !channel.save() )
				 {
					 log.error( "Saving channel${i} channel failed!");
					 channel.errors.allErrors.each { println it; };
				 }
				 
			 }
			 else
			 {
				 log.info( "Existing channel${i} channel, skipping..." );
			 }
		 }
	}
	 
	 
	 void createRoles()
	 {
		 
		 println "Creating roles...";
		 AccountRole userRole = userService.findAccountRoleByName( "user" );
		 if( userRole != null )
		 {
			 println "Existing AccountRole user found";
		 }
		 else
		 {
			 println "No existing AccountRole user found, so creating now...";
			 
			userRole = new AccountRole( name: "user" );
			 // userRole.addToPermissions( "admin:*" );
			 userRole.addToPermissions( "channel:*" );
			 userRole.addToPermissions( "comment:*" );
			 userRole.addToPermissions( "entry:*" );
			 userRole.addToPermissions( "filter:*" );
			 userRole.addToPermissions( "home:*" );
			 // userRole.addToPermissions( "queue:*" );
			 userRole.addToPermissions( "rssFeed:*" );
			 // userRole.addToPermissions( "schedule:*" );
			 userRole.addToPermissions( "search:*" );
			 userRole.addToPermissions( "share:*" );
			 // userRole.addToPermissions( "siteConfigEntry:*" );
			 userRole.addToPermissions( "tag:*" );
			 userRole.addToPermissions( "trigger:*" );
			 userRole.addToPermissions( "user:*" );
			 userRole.addToPermissions( "userHome:*" );
			 userRole.addToPermissions( "vote:*" );
			
			userRole = userService.createAccountRole( userRole );
			
			if( !userRole )
			{
				println "Error creating userRole";
			}

		 }
		 
		 AccountRole adminRole = userService.findAccountRoleByName( "admin" );
		 if( adminRole != null )
		 {
			 println "Existing AccountRole admin found";
		 }
		 else
		 {
			 println "No existing AccountRole admin found, so creating now...";
			 
			 adminRole = new AccountRole( name: "admin" );
			 
			 adminRole.addToPermissions( "admin:*" );
			 adminRole.addToPermissions( "channel:*" );
			 adminRole.addToPermissions( "comment:*" );
			 adminRole.addToPermissions( "entry:*" );
			 adminRole.addToPermissions( "filter:*" );
			 adminRole.addToPermissions( "home:*" );
			 adminRole.addToPermissions( "queue:*" );
			 adminRole.addToPermissions( "rssFeed:*" );
			 adminRole.addToPermissions( "schedule:*" );
			 adminRole.addToPermissions( "search:*" );
			 adminRole.addToPermissions( "share:*" );
			 adminRole.addToPermissions( "siteConfigEntry:*" );
			 adminRole.addToPermissions( "tag:*" );
			 adminRole.addToPermissions( "trigger:*" );
			 adminRole.addToPermissions( "user:*" );
			 adminRole.addToPermissions( "userHome:*" );
			 adminRole.addToPermissions( "vote:*" );
			 
			 
			 adminRole = userService.createAccountRole( adminRole );
		 
			 if( !adminRole )
			 {
				 println "Error creating adminRole";
			 }
			 
		 }

		 
		 
	 }
	 
	 
     void createSomeUsers()
     {  
		 AccountRole userRole = userService.findAccountRoleByName( "user" );
		 
		 if( userRole == null )
		 {
			 println "did not locate user role!";
		 }
		 
		 
		 AccountRole adminRole = userService.findAccountRoleByName( "admin" );
		 if( adminRole == null )
		 {
			 println "did not locate admin role!";
		 }
		 
		 
         if( !User.findByUserId( "prhodes" ))
         {
             log.info( "Fresh Database, creating PRHODES user" );
			 def userPrhodes = new User();
				 
			 println "Could not find prhodes";
			 println "Creating new prhodes user";
			 userPrhodes = new User();
			 userPrhodes.uuid = "abc123";
			 userPrhodes.fullName = "Phillip Rhodes";
			 userPrhodes.email = "motley.crue.fan@gmail.com";
			 userPrhodes.userId = "prhodes";
			 userPrhodes.password = "secret";
			 userPrhodes.bio = "bio";
			  
			  
			 userPrhodes.addToRoles( userRole );
			 userPrhodes.addToRoles( adminRole );
			  
				     
             if( !userPrhodes.save() )
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
				 def user = new User();
				 
				 user = new User();
				 user.fullName = "Test User${i}";
				 user.email = "testuser${i}@example.com";
				 user.userId = "testuser${i}";
				 user.password = "secret";
				 user.bio = "bio";
				  
				  
				 user.addToRoles( userRole );
				
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
		 
		 AccountRole userRole = userService.findAccountRoleByName( "user" );
		 
		 if( userRole == null )
		 {
			 println "did not locate user role!";
		 }
		 
		 
		 AccountRole adminRole = userService.findAccountRoleByName( "admin" );
		 if( adminRole == null )
		 {
			 println "did not locate admin role!";
		 }
		 
         if( !User.findByUserId( "anonymous" ))
         {
             log.info( "Fresh Database, creating ANONYMOUS user" );
             def user = new User();
			 user.userId = "anonymous";
			 user.password = "secret";
			 user.fullName = "Anonymous User";
			 user.email = "anonymous@yourhost.com";
			 user.bio = "";
             
			 user.addToRoles( userRole );
			 
			 
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
		 
		 AccountRole userRole = userService.findAccountRoleByName( "user" );
		 
		 if( userRole == null )
		 {
			 println "did not locate user role!";
		 }
		 
		 
		 AccountRole adminRole = userService.findAccountRoleByName( "admin" );
		 if( adminRole == null )
		 {
			 println "did not locate admin role!";
		 }
		 
         if( !User.findByUserId( "admin" ))
         {
             log.info( "Fresh Database, creating ADMIN user" );
             def user = new User();
			 
			 user.userId = "admin";
			 user.password = "secret";
			 user.fullName = "Site Administrator";
			 user.email = "admin@yourhost.com";
			 user.bio = "";
			 
			 
			 user.addToRoles( userRole );
			 user.addToRoles( adminRole );
			 
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
package neddick_grails3;

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
import org.fogbeam.neddick.UserAccountRoleMapping
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder

import grails.util.Environment


class BootStrap 
{
	 def entryCacheService;
	 def siteConfigService;
	 def userService;
	 
     def init = { servletContext ->
     
		 
		 String neddickHome = System.getProperty( "neddick.home" );
		 if( neddickHome == null || neddickHome.isEmpty())
		 {
			 throw new Exception( "No neddick.home configured!" );
		 }
		 
	     switch( Environment.current )
	     {
	         case Environment.DEVELOPMENT:
				 createRoles();
				 // createAdminUser();
	             createSomeUsers();
	             // createAnonymousUser();
				 createDefaultChannel();
				 createSomeChannels();
	             break;
	         case Environment.PRODUCTION:
	             log.info( "No special configuration required" );
				 createRoles();
				 // createAdminUser();
				 createSomeUsers();
				 // createAnonymousUser();
				 createDefaultChannel();
				 createSomeChannels();
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
				 println( "Index not previously initialized, creating empty index" );
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
		 	log.warn( "No explicit indexDirLocation configured.  Placing index under neddickHome at: ${neddickHome}/index");
			 indexDirLocation = neddickHome + "/index";
			 File indexFile = new java.io.File( indexDirLocation );
			 String[] indexFileChildren = indexFile.list();
			 boolean indexIsInitialized = (indexFileChildren != null && indexFileChildren.length > 0 );
			 if( ! indexIsInitialized )
			 {
				 println( "Index not previously initialized, creating empty index" );
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
			 
			 User userAnon = User.findByUserId( "SYS_anonymous" );
			 
			 if( userAnon )
			 {
				 log.info( "found user \"SYS_anonymous\" ok ");
			 }
			 else
			 {
			 	log.error( "could not find user \"SYS_anonymous\"");
			 }
			 
			 channel.owner = userAnon;
			 
    		 if( !channel.save(flush:true) )
    		 {
    			 println( "Saving DEFAULT channel failed!" );
				 channel.errors.allErrors.each { log.error( it.toString() ) };
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
				 
				 User userAnon = User.findByUserId( "SYS_anonymous" );
				  
				 channel.owner = userAnon;
				 
				 if( !channel.save(flush:true) )
				 {
					 log.error( "Saving channel${i} channel failed!");
					 channel.errors.allErrors.each { log.error( it.toString() ) };
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
		 AccountRole userRole = userService.findAccountRoleByAuthority( "ROLE_USER" );
		 if( userRole != null )
		 {
			 println "Existing AccountRole ROLE_USER found";
		 }
		 else
		 {
			 println "No existing AccountRole ROLE_USER found, so creating now...";
			 
			userRole = new AccountRole( authority: "ROLE_USER" );
			
			userRole = userService.createAccountRole( userRole );
			
			if( !userRole )
			{
				println "Error creating userRole";
			}

		 }
		 
		 AccountRole adminRole = userService.findAccountRoleByAuthority( "ROLE_ADMIN" );
		 if( adminRole != null )
		 {
			 println "Existing AccountRole ROLE_ADMIN found";
		 }
		 else
		 {
			 println "No existing AccountRole ROLE_ADMIN found, so creating now...";
			 
			 adminRole = new AccountRole( authority: "ROLE_ADMIN" );
			 
			 adminRole = userService.createAccountRole( adminRole );
		 
			 if( !adminRole )
			 {
				 println "Error creating role ROLE_ADMIN";
			 }
		 }
	 }
	 
	 
	void createSomeUsers()
	{
        PasswordEncoder encoder = new BCryptPasswordEncoder();
        
		println "Creating some users!";
	
		AccountRole userRole = userService.findAccountRoleByAuthority( "ROLE_USER" );

		if( userRole == null )
		{
				println "did not locate user role!";
		}

		AccountRole adminRole = userService.findAccountRoleByAuthority( "ROLE_ADMIN" );
		if( adminRole == null )
		{
				println "did not locate admin role!";
		}

        
        /* Create default Anonymous user */
        boolean anonUserFound = false;

        User userAnonymous= userService.findUserByUserId( "SYS_anonymous" );

        if( userAnonymous != null )
        {
             println "Found existing SYS_anonymous user!";

        }
        else
        {
            println "Could not find SYS_anonymous";
            println "Creating new SYS_anonymous user";
            userAnonymous = new User();
            userAnonymous.uuid = "-1";
            userAnonymous.displayName = "Anonymous User";
            userAnonymous.firstName = "Anonymous";
            userAnonymous.lastName = "User";
            userAnonymous.email = "anonymous@example.com";
            userAnonymous.userId = "SYS_anonymous";
            userAnonymous.password = "notapplicable";
            userAnonymous.bio = "N/A";
            userAnonymous.disabled = true;
                         
            userAnonymous = userService.updateUser( userAnonymous );
                    
        }
        
		boolean prhodesFound = false;

		User userPrhodes= userService.findUserByUserId( "prhodes" );

		if( userPrhodes != null )
		{
			 println "Found existing prhodes user!";

		}
		else
		{
			println "Could not find prhodes";
			println "Creating new prhodes user";
			userPrhodes = new User();
			userPrhodes.uuid = "abc123";
			userPrhodes.displayName = "Phillip Rhodes";
			userPrhodes.firstName = "Phillip";
			userPrhodes.lastName = "Rhodes";
			userPrhodes.email = "prhodes@fogbeam.com";
			userPrhodes.userId = "prhodes";
			userPrhodes.disabled = false;
           
            String hashedPassword = encoder.encode( "secret" );
            userPrhodes.password = hashedPassword;
			userPrhodes.bio = "bio";
			 
			userPrhodes = userService.updateUser( userPrhodes );
			
			UserAccountRoleMapping prhodesUser_UserRole = null;
			UserAccountRoleMapping.withSession
			{
				prhodesUser_UserRole = UserAccountRoleMapping.findByUserAndRole( userPrhodes, userRole );
				if( !prhodesUser_UserRole )
				{
					prhodesUser_UserRole = new UserAccountRoleMapping( userPrhodes, userRole );
					if( !prhodesUser_UserRole.save( flush: true ) )
					{
						prhodesUser_UserRole.errors.allErrors.each { println it };
						throw new RuntimeException( "Failed to create prhodesUser_UserRole" );
					}
					else
					{
						println "prhodesUser_UserRole created!";
					}
				}
				else
				{
					println "prhodesUser_UserRole already exists!";
				}
			}
 
			 
			// userPrhodes.addToRoles( adminRole );
			UserAccountRoleMapping prhodesUser_AdminRole = null;
			UserAccountRoleMapping.withSession
			{
				 prhodesUser_AdminRole = UserAccountRoleMapping.findByUserAndRole( userPrhodes, adminRole );
				 if( !prhodesUser_AdminRole )
				 {
					 prhodesUser_AdminRole = new UserAccountRoleMapping( userPrhodes, adminRole );
					 if( ! prhodesUser_AdminRole.save( flush: true ) )
					 {
						prhodesUser_AdminRole.errors.allErrors.each { println it };
					 	throw new RuntimeException( "Failed to create prhodesUser_AdminRole" );
					 }
					 else
					 {
						 println "prhodesUser_AdminRole created!";
					 }
				 }
				 else
				 {
					 println "prhodesUser_AdminRole already exists!";
				 }
			 }
		 }

		 
		 User userSarah = userService.findUserByUserId( "sarah" );
 
		 if( userSarah != null )
		 {
			   println "Found existing sarah user!";
 
		 }
		 else
		 {
			 println "Could not find sarah";
			 println "Creating new sarah user";
			 userSarah = new User();
			 userSarah.uuid = "abc124";
			 userSarah.displayName = "Sarah Kahn";
			 userSarah.firstName = "Sarah";
			 userSarah.lastName = "Kahn";
			 userSarah.email = "sarah@fogbeam.com";
			 userSarah.userId = "sarah";
             
             String hashedPassword = encoder.encode( "secret" );
			 userSarah.password = hashedPassword;
			 userSarah.bio = "bio";

			 userSarah = userService.updateUser( userSarah );
			 			   
			 UserAccountRoleMapping sarahUser_UserRole = null;
			 UserAccountRoleMapping.withSession
			 {
				 sarahUser_UserRole = UserAccountRoleMapping.findByUserAndRole( userSarah, userRole );
				 if( !sarahUser_UserRole )
				 {
					 sarahUser_UserRole = new UserAccountRoleMapping( userSarah, userRole );
					 if( ! sarahUser_UserRole.save( flush: true ) )
					 {
						 sarahUser_UserRole.errors.allErrors.each { println it };
						 throw new RuntimeException( "Failed to create sarahUser_UserRole" );
					 }
					 else
					 {
						 println "sarahUser_UserRole created!";
					 }
				 }
				 else
				 {
					 println "sarahUser_UserRole already exists!";
				 }
			 }
   
			   
			 UserAccountRoleMapping sarahUser_AdminRole = null;
			 UserAccountRoleMapping.withSession
			 {
				 sarahUser_AdminRole = UserAccountRoleMapping.findByUserAndRole( userSarah, adminRole );
				 if( !sarahUser_AdminRole )
				 {
					 sarahUser_AdminRole = new UserAccountRoleMapping( userSarah, adminRole );
					 if( ! sarahUser_AdminRole.save( flush: true ) )
					 {
						 sarahUser_AdminRole.errors.allErrors.each { println it };
					 	 throw new RuntimeException( "Failed to create sarahUser_AdminRole" );
					 }
					 else
					 {
						 println "sarahUser_AdminRole created!";
					 }
				 }
				 else
				 {
					 println "sarahUser_AdminRole already exists!";
				 }
			 }
		 }
		 
		 
		 for( int i = 0; i < 20; i++ )
		 {
			 if( userService.findUserByUserId( "testuser${i}" ) == null )
			 {
				 println "Fresh Database, creating TESTUSER ${i} user";
				 User testUser = new User(
								 userId: "testuser${i}",
							   firstName: "Test",
							   lastName: "User${i}",
							   email: "testuser${i}@example.com",
							   bio:"stuff",
							   displayName: "Test User${i}" );
				 
                  String hashedPassword = encoder.encode( "secret" );
				  testUser.password = hashedPassword; 
				  testUser.uuid = "test_user_${i}";
				
				  println "about to create user: ${testUser.toString()}";
				  testUser = userService.updateUser( testUser );
				  		   
				  UserAccountRoleMapping testUser_UserRole = null;
				  UserAccountRoleMapping.withSession
				  {
					  testUser_UserRole = UserAccountRoleMapping.findByUserAndRole( testUser, userRole );
					  if( !testUser_UserRole )
					  {
						  testUser_UserRole = new UserAccountRoleMapping( testUser, userRole );
						  if( ! testUser_UserRole.save( flush: true ) )
						  {
							  testUser_UserRole.errors.allErrors.each { println it };
                              throw new RuntimeException( "Failed to create testUser_UserRole" );
						  }
						  else
						  {
							  println "testUser_UserRole created!";
						  }
					  }
					  else
					  {
						  println "testUser_UserRole already exists!";
					  }
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
		 
		 AccountRole userRole = userService.findAccountRoleByName( "user" );
		 
		 if( userRole == null )
		 {
			 println "did not locate user role!";
		 }
		 else
		 {
			 println "found user role";
		 }
		 
		 AccountRole adminRole = userService.findAccountRoleByName( "admin" );
		 if( adminRole == null )
		 {
			 println "did not locate admin role!";
		 }
		 else
		 {
			 println "found admin role";
		 }
		 
         if( !User.findByUserId( "anonymous" ))
         {
             println( "Fresh Database, creating ANONYMOUS user" );
             def user = new User();
			 user.userId = "anonymous";
			 user.password = "secret";
			 user.fullName = "Anonymous User";
			 user.email = "anonymous@yourhost.com";
			 user.bio = "";
             
			 user.addToRoles( userRole );
			 
			 
             if( !user.save(flush:true) )
             {
                 println( "Saving ANONYMOUS user failed!");
             	 user.errors.allErrors.each { println it; };
			 }
             else
			 {
				 println "Successfully created ANONYMOUS user";
			 }
         }
         else
         {
             println( "Existing ANONYMOUS user, skipping..." );
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
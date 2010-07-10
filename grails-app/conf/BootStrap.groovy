import grails.util.Environment;
import org.fogbeam.neddick.Channel 
import org.fogbeam.neddick.User 

class BootStrap {

	 def entryCacheService;
	
     def init = { servletContext ->
     
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
     
     
	     // build Entry cache for default "non user"
	     entryCacheService.buildCache();
	     
	     
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
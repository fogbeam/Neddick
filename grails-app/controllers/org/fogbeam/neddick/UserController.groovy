package org.fogbeam.neddick

import grails.plugin.springsecurity.annotation.Secured

class UserController 
{
    def userService;
    def entryService;
    def tagService;
	
	@Secured(["ROLE_USER","ROLE_ADMIN"])
    def registerUser( UserRegistrationCommand urc )
	{
    	if( urc.hasErrors() )
    	{
    		flash.user = urc;
    		redirect( action:"register2" );
    	}
    	else
    	{
    		def user = new User( urc.properties );
    		// user.profile = new Profile( urc.properties );
    		if( user.save() )
    		{
    			flash.message = "Welcome Aboard, ${urc.fullName ?: urc.userId}";
    			redirect(controller:'home', action: 'index')
    		}
    		else
    		{
    			// maybe not unique userId?
    			flash.user = urc;
    			redirect( action:"register2" );
    		}
    	}
    }
	
	@Secured(["ROLE_USER","ROLE_ADMIN"])
	def list()
	{
		List<User> allUsers = User.findAll();
		
		[allUsers: allUsers]	
	}
	
	@Secured(["ROLE_USER","ROLE_ADMIN"])
    def viewDetails() 
	{
    	def targetUserName = params.targetUserName
    	log.debug( "Viewing details for user: ${targetUserName}" );

    	User user = userService.findUserByUserId( targetUserName );
    	
    	// get all Entries for the requested user...
    	def allEntries = entryService.getAllEntries( user );
    	[targetUserName:targetUserName, allEntries:allEntries];
    
    }
    
	@Secured(["ROLE_USER","ROLE_ADMIN"])
    def viewBookmarks() 
	{
        def targetUserName = params.targetUserName
        log.debug( "Viewing bookmarks for user: ${targetUserName}" );    
        
        User user = userService.findUserByUserId( targetUserName );
    	def allEntries = entryService.getSavedEntriesForUser( user );    
        
    	[targetUserName:targetUserName, allEntries:allEntries];
    	
    }
    
	@Secured(["ROLE_USER","ROLE_ADMIN"])
    def viewComments()
	{
        def targetUserName = params.targetUserName
        log.debug( "Viewing comments for user: ${targetUserName}" );    
        
    	User user = userService.findUserByUserId( targetUserName );
    	
    	def allComments = entryService.getCommentsForUser( user );
    	
    	[targetUserName:targetUserName, allComments:allComments];	
    }       
    
	@Secured(["ROLE_USER","ROLE_ADMIN"])
    def viewTags()
	{
        def targetUserName = params.targetUserName
        log.debug( "Viewing tags for user: ${targetUserName}" );    
        
    	User user = userService.findUserByUserId( targetUserName );    
    	
    	List<Tag> allTags = new ArrayList<Tag>();
    	
    	allTags.addAll( tagService.getTagListForUser( user ) );
    	allTags.sort();
    	
    	[targetUserName:targetUserName, allTags: allTags];        	
    }
    
	@Secured(["ROLE_USER","ROLE_ADMIN"])
    def viewFriendsActivity() 
	{
        def targetUserName = params.targetUserName
        log.debug( "Viewing friend activity for user: ${targetUserName}" );    
        
    	User user = userService.findUserByUserId( targetUserName );    
    	[targetUserName:targetUserName]
        	
    }     
    
	@Secured(["ROLE_USER","ROLE_ADMIN"])
    def relate() 
	{    		
		// find the logged in user...
		def loggedInUser = userService.getLoggedInUser();
	
		// find the "target" user
		def targetUserName = params.targetUserName
		User targetUser = userService.findUserByUserId( targetUserName );
	
		Integer currentBoost = 0;
		for( UserToUserLink childLink in loggedInUser.childUserLinks )
		{
			if( childLink.target.userId.equals( targetUser.userId ))
			{
				currentBoost = childLink.boost;
				break;
			}
		}
		
		[targetUser:targetUser, currentBoost: currentBoost ];    	
    }

	@Secured(["ROLE_USER","ROLE_ADMIN"])
    def applyBoost() 
	{
		def targetUserId = params.targetUserId;
		
		
		// find the logged in user...
		User loggedInUser = userService.getLoggedInUser();
		
		// create user_user relationship...
		log.debug( "User: ${loggedInUser.userId}, applyBoost: ${params.boostScore} for target user: ${targetUserId}" );
	
		// find the "target" user
		def targetUserName = params.targetUserName
		User targetUser = userService.findUserByUserId( targetUserId );
	
		// create link...
		UserToUserLink.link( loggedInUser, targetUser, Integer.parseInt(params.boostScore));
		
		redirect( action:"index", controller:"home" );    	
    }

	@Secured(["ROLE_USER","ROLE_ADMIN"])
    def boostSettings() 
	{
		// find the logged in user...
		def loggedInUser = userService.getLoggedInUser();    		
	
		[user:loggedInUser];
    }
}

class UserRegistrationCommand
{
    String userId;
    String password;
    String passwordRepeat;
    
    byte[] photo;
    String fullName;
    String bio;
    String homepage;
    String email;
    String timezone;
    String country;
    String jabberAddress;
    
    static constraints = {
        userId( size: 3..20)
        password( size:6..8, blank:false, validator : {password, urc -> return password != urc.userId } );
        passwordRepeat( nullable:false, validator : {password2, urc -> return password2 == urc.password } );
    
        fullName( nullable:true );
        bio( nullable:true, maxSize:1000 );
        homepage( url:true, nullable:true);
        email(email:true, nullable:true);
        photo( nullable:true);
        country( nullable:true);
        timezone( nullable:true);
        jabberAddress( email:true, nullable:true);
        
    }
}





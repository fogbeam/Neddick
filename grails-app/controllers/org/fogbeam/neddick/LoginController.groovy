package org.fogbeam.neddick;

class LoginController {

	def userService;
	def entryCacheService;
	
	
    def index = { }
    
    def login = {
    	
    	def userId = params.username;
    	def password = params.password;
    	
    	def user = userService.findUserByUserIdAndPassword( userId, password );
    	if( user )
    	{
    		session.user = user;
    		
    		entryCacheService.buildCache( user );
    		
    		redirect( controller:'home', action:'index')
    	}
    	else
    	{
    		flash.message = "Login Failed";
    		redirect( action:'index');
    	}
    }
    
    def logout = {
    	entryCacheService.removeCache( session.user );
    	session.user = null;
    	session.invalidate();
    	redirect( uri:'/');
    }
}

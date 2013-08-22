package org.fogbeam.neddick


public class LoginService 
{
	def userService;
	
	public User doUserLogin( final String userId, final String password )
	{
		
		// now find a User that matches this account
		User user = userService.findUserByUserId( userId );
		
		if( password.equals( user.password ))
		{	
			return user;
		}
		else
		{
			return null;
		}
	}	
}

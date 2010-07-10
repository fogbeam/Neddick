package org.fogbeam.neddick

import org.fogbeam.neddick.User;

class UserService {

	public User findUserByUserId( final String userId )
	{
		def user = User.findByUserId( userId );
		return user;
	}
	
	public User findUserByUserIdAndPassword( final String userId, final String password )
	{
		def user = User.findByUserIdAndPassword( userId, password );
		return user;
	}
	
	public void updateUser( final User user )
	{
		if( !user.save() )
		{
			println( "Updating user: ${user.userId} FAILED");
			user.errors.allErrors.each { println it };
		}
	}
	
}

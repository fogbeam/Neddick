package org.fogbeam.neddick

import org.fogbeam.neddick.User;

class UserService {

	public User findUserByUserId( final String userId )
	{
		def user = User.executeQuery( "select user from User as user where user.userId = ?", [userId] );
		return user.get(0);
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
			log.error( "Updating user: ${user.userId} FAILED");
			// user.errors.allErrors.each { p rintln it };
		}
	}
	
}

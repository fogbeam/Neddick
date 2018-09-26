package org.fogbeam.neddick

import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder


class UserService 
{
	public User getLoggedInUser()
	{
		// get the user from the SecurityContext
		SecurityContext securityContext = SecurityContextHolder.getContext();
		Authentication authentication = securityContext.getAuthentication();
		
		if( authentication == null || authentication.principal == null )
		{
			log.warn( "No logged in user found!" );
			return null;
		}
		
		log.trace( "current Authentication: ${authentication}");
		
		User user = this.findUserByUserId( ((User)authentication.principal).userId );
		
		return user;
	}
	
	
	public User findUserByUserId( final String userId )
	{
		List<User> results = User.executeQuery( "select user from User as user where user.userId = ?", [userId] );
		if( results != null && !results.isEmpty() )
		{
			return results.get(0);
		}
		else
		{
			return null;
		}
	}
	
	public User findUserByUserIdAndPassword( final String userId, final String password )
	{
		def user = User.findByUserIdAndPassword( userId, password );
		return user;
	}
	
	public User updateUser( final User user )
	{
		if( !user.save(flush:true) )
		{
			log.error( "Updating user: ${user.userId} FAILED");
			user.errors.allErrors.each { log.error( it.toString() ) };
		}
		
		return user;
	}

	public AccountRole findAccountRoleByAuthority( final String authority )
	{
		log.debug( "searching for AccountRole with authority: ${authority}");
		
		List<AccountRole> roles = AccountRole.executeQuery( "select role from AccountRole as role where role.authority = :authority", [authority:authority]);
		
		AccountRole role = null;
		if( roles.size == 1 )
		{
			role = roles[0];
		}

		log.debug( "returning role ${role}");
		return role;
	}
	
	public AccountRole createAccountRole( AccountRole role )
	{		
		log.debug( "UserService.createAccountRole() - about to create role: ${role.toString()}");
	
		if( !role.save(flush: true))
		{
			role.errors.each { log.error( it.toString() ) };
			throw new RuntimeException( "couldn't create AccountRole: ${role.toString()}" );
		}
		
		log.debug( "returning role: ${role}" );
		return role;
	}
	
	
	public void addChannelToUserFavorites( final User user, final Channel channel )
	{
		log.info( "addChannelToUserFavorites()" );
		
		UserFavoriteChannelLink.link( channel, user );
	}
		
}
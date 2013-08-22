package org.fogbeam.neddick

import org.fogbeam.neddick.User;
import org.fogbeam.neddick.AccountRole;


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

	public AccountRole findAccountRoleByName( final String name )
	{
		println "searching for AccountRole named ${name}";
		
		List<AccountRole> roles = AccountRole.executeQuery( "select role from AccountRole as role where role.name = :name", [name:name]);
		
		AccountRole role = null;
		if( roles.size == 1 )
		{
			role = roles[0];
		}

		println "returning role ${role}";
		return role;
	}
	
	public AccountRole createAccountRole( AccountRole role )
	{
		
		println "UserService.createAccountRole() - about to create role: ${role.toString()}";
	
		if( !role.save(flush: true))
		{
			role.errors.each { println it };
			throw new RuntimeException( "couldn't create AccountRole: ${role.toString()}" );
		}
		
		println "returning role: ${role}";
		return role;
	}
	
		
}

package org.fogbeam.neddick

class UserAccountRoleMapping 
{
	User user;
	AccountRole role;

	public UserAccountRoleMapping( User user, AccountRole role )
	{
		this.user = user;
		this.role = role;
	}
}
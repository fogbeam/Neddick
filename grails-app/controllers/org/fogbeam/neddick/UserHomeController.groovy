package org.fogbeam.neddick

import grails.plugin.springsecurity.annotation.Secured

class UserHomeController 
{

	@Secured(["ROLE_USER","ROLE_ADMIN"])
	def index()
	{
		[:];
	}
}

package org.fogbeam.neddick

import grails.plugin.springsecurity.annotation.Secured

class QueueController 
{

	@Secured(["ROLE_ADMIN"])
	def index()
	{
		log.debug( "sending JMS Message!" );
			
		// send a JMS message to our testQueue
		sendJMSMessage("searchQueue", "This is a TEST message!!!.")	
	}
}

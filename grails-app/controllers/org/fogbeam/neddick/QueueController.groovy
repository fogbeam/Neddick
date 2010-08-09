package org.fogbeam.neddick

class QueueController {

	def index = {
		
			println "sending JMS Message!";
			
		// send a JMS message to our testQueue
		sendJMSMessage("searchQueue", "This is a TEST message!!!.")
		
	}
}

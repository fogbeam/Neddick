package org.fogbeam.neddick

import javax.jms.Message as JMSMessage;

class CacheQueueInputService 
{
	def entryCacheService;
	
	static expose = ['jms']
	static destination = "cacheQueue"

	def onMessage(JMSMessage msg)
	{
		
		log.debug( "CacheQueueInputService: GOT MESSAGE: ${msg}" );
	
		return null;	
			
	}
	
}

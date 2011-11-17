package org.fogbeam.neddick

class CacheQueueInputService 
{
	def entryCacheService;
	
	static expose = ['jms']
	static destination = "cacheQueue"

	def onMessage(msg)
	{
		
		println "CacheQueueInputService: GOT MESSAGE: ${msg}";
	
		return null;	
			
	}
	
}

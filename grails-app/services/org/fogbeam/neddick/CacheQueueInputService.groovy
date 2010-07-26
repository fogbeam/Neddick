package org.fogbeam.neddick

class CacheQueueInputService 
{
	def entryCacheService;
	
	static expose = ['jms']
	static destination = "cacheQueue"

	def onMessage(msg)
	{
		
		println "GOT MESSAGE: ${msg}";
	
		if( msg instanceof java.lang.String )
		{
			println "Yep, it's a string!"

			if( ((String)msg).equalsIgnoreCase("REBUILD_CACHE" ))
			{
				entryCacheService.rebuildAll();
			} 
			
		}
		
	}
	
}

package org.fogbeam.neddick.jms.listeners

class FilterListenerService
{
	
	def siteConfigService;
	def entryService;
	def filterService;
	
	static expose = ['jms']
	static destination = "neddickFilterQueue"
	
	
	def onMessage( msg )
	{
		
		// log.debug "FilterListenerService received message: ${msg}";
	
		String msgType = msg.msgType;
		
		switch( msgType )
		{
			case "TAG_INDEXED":
				
				String tagName = msg.tagName;
				String entryUuid = msg.entry_uuid;
				log.debug "firing TagFilterCriteria";
				filterService.fireTagFilterCriteria( tagName, entryUuid );
				break;
			
			case "NEW_ENTRY_INDEXED":
				String entryUuid = msg.entry_uuid;
				filterService.fireContentFilterCriteria( entryUuid );
				break;
					
			case "ENTRY_SCORE_CHANGED":
			
				String entryUuid = msg.entry_uuid;
				String newScore = msg.newScore;
				filterService.fireThresholdFilterCriteria( entryUuid, newScore );
			
				break;
		
			case "NEW_FILTER_CREATED":
			
				filterService.processExistingContentForFilter( msg.filterUuid );
			
				break;	
								
			default:
				log.debug "Remote sent bad msgType";
				break;
		}
		
		
	}
	
	
}

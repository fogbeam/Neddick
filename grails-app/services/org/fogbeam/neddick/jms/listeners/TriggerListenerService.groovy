package org.fogbeam.neddick.jms.listeners

class TriggerListenerService
{
	
	def siteConfigService;
	def entryService;
	def triggerService;
	
	static expose = ['jms']
	static destination = "neddickTriggerQueue"

	
	def onMessage( msg )
	{
		
		// something happened that might invoke one or more triggers
		// fire the respective method on triggerService
				
		String msgType = msg.msgType;
		
		switch( msgType )
		{
			case "TAG_INDEXED":
				
				String tagName = msg.tagName;
				String entryUuid = msg.entry_uuid;
				triggerService.fireTagTriggerCriteria( tagName, entryUuid );
				break;
			
			case "NEW_ENTRY_INDEXED":
				String entryUuid = msg.entry_uuid;
				triggerService.fireContentTriggerCriteria( entryUuid );
				break;
					
			case "ENTRY_SCORE_CHANGED":
			
				String entryUuid = msg.entry_uuid;
				String newScore = msg.newScore;				
				triggerService.fireThresholdTriggerCriteria( entryUuid, newScore );
			
				break;
						
			default:
				log.debug "Remote sent bad msgType";
				break;
		}
		
	}
		
}

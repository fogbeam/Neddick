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
		
		// [tagName:chrome, entry_uuid:7020a857-6728-465a-b203-6148d3277358, msgType:TAG_INDEXED]
		// TODO: need channel(s) for channel triggers...
		
		println msg;
		
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
					
			default:
				println "Remote sent bad msgType";
				break;
		}
		
	}
		
}

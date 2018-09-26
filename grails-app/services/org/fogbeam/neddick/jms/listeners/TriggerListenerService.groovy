package org.fogbeam.neddick.jms.listeners

import javax.jms.MapMessage

class TriggerListenerService
{
	
	def siteConfigService;
	def entryService;
	def triggerService;
	
	static expose = ['jms']
	static destination = "neddickTriggerQueue"

	
	def onMessage( MapMessage msg )
	{
		// something happened that might invoke one or more triggers
		// fire the respective method on triggerService
		
		String msgType = msg.getString( "msgType" );
		
		switch( msgType )
		{
			case "TAG_INDEXED":
				
				String tagName = msg.getString( "tagName" );
				String entryUuid = msg.getString( "entry_uuid" );
				triggerService.fireTagTriggerCriteria( tagName, entryUuid );
				break;
			
			case "NEW_ENTRY_INDEXED":
				String entryUuid = msg.getString( "entry_uuid" );
				triggerService.fireContentTriggerCriteria( entryUuid );
				break;
					
			case "ENTRY_SCORE_CHANGED":
			
				String entryUuid = msg.getString( "entry_uuid" );
				String newScore = msg.getString( "newScore" );				
				triggerService.fireThresholdTriggerCriteria( entryUuid, newScore );
				break;
			default:
				log.error( "Remote sent bad msgType: ${msgType}" );
				break;
		}
	}
}

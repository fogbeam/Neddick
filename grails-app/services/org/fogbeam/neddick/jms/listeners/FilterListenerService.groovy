package org.fogbeam.neddick.jms.listeners

import javax.jms.MapMessage

class FilterListenerService
{
	
	def siteConfigService;
	def entryService;
	def filterService;
	
	static expose = ['jms']
	static destination = "neddickFilterQueue"
	
	
	def onMessage( MapMessage msg )
	{
		
		// log.debug "FilterListenerService received message: ${msg}";
	
		String msgType = msg.getString( "msgType" );
		
		switch( msgType )
		{
			case "TAG_INDEXED":
				
				String tagName = msg.getString( "tagName" );
				String entryUuid = msg.getString( "entry_uuid" );
				log.debug "firing TagFilterCriteria";
				filterService.fireTagFilterCriteria( tagName, entryUuid );
				break;
			
			case "NEW_ENTRY_INDEXED":
				String entryUuid = msg.getString( "entry_uuid" );
				filterService.fireContentFilterCriteria( entryUuid );
				break;
					
			case "ENTRY_SCORE_CHANGED":
			
				String entryUuid = msg.getString( "entry_uuid" );
				String newScore = msg.getString( "newScore" );
				filterService.fireThresholdFilterCriteria( entryUuid, newScore );
				break;
		
			case "NEW_FILTER_CREATED":
				String filterUuid = msg.getString( "filterUuid" );
				filterService.processExistingContentForFilter( filterUuid );
				break;	
								
			default:
				log.error( "Remote sent bad msgType: ${msgType}" );
				break;
		}
	}
}

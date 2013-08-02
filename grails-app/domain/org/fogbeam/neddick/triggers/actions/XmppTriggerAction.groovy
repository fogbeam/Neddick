package org.fogbeam.neddick.triggers.actions

import org.fogbeam.neddick.Entry


class XmppTriggerAction extends BaseTriggerAction
{
	
	def xmppNotificationService;
	
	String destination;
	
	public void doAction( final String entryUuid )
	{
		println "performing xmpp action for uuid: ${entryUuid}";
		
		def entryToSend = Entry.findByUuid( entryUuid );
		
		
		// share to email address(es)
		String[] addresses = destination.trim().split("\\s+");
		
		def messageText = " \n ${entryToSend.title} \n ${entryToSend.url}";
		def messageSubject = "Neddick entry shared by trigger {" + this.trigger.name + "} (" + this.trigger.owner.fullName + ")";

		
		for( String address : addresses )
		{
			try
			{
				xmppNotificationService.sendChat( address, "\n" + messageSubject + "\n" + messageText );
			}
			catch( Exception e )
			{
				// an exception processing one address shouldn't cause the others to fail
				e.printStackTrace();
				continue;
			}
		}
		
		
		
		
	}
}

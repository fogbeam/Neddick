package org.fogbeam.neddick.triggers.actions

import org.fogbeam.neddick.Entry
import org.fogbeam.neddick.XmppNotificationService
import org.springframework.beans.factory.annotation.Autowired


public class XmppTriggerAction extends BaseTriggerAction
{
	@Autowired
	XmppNotificationService xmppNotificationService;
	
	static transients = ['xmppNotificationService'];
	
	String destination;
	
	public void doAction( final String entryUuid )
	{
		log.info( "performing xmpp action for uuid: ${entryUuid}" );
		
		def entryToSend = Entry.findByUuid( entryUuid );
		
		log.info( "Found entryToSend: ${entryToSend}" );
		
		// share to email address(es)
		String[] addresses = destination.trim().split("\\s+");
		
		def messageText = " \n ${entryToSend.title} \n ${entryToSend.url}";
		
		log.info( "messageText: ${messageText}" );
		
		def messageSubject = "Neddick entry shared by trigger {" + this.trigger.name + "} (" + this.trigger.owner.fullName + ")";
		log.info( "messageSubject: ${messageSubject}" );
		
		
		for( String address : addresses )
		{
			try
			{
				log.info( "sending XMPP message to address: ${address}");
				xmppNotificationService.sendChat( address, "\n" + messageSubject + "\n" + messageText );
			}
			catch( Exception e )
			{
				log.error( "Exception sending XMPP message: ", e );
				continue;
			}
		}
	}
	
	
	public String getShortName()
	{
		return "XmppAction";
	}
	
	public String getValue()
	{
		return destination;
	}
	
}

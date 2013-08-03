package org.fogbeam.neddick.triggers.actions

import org.fogbeam.neddick.Entry
import org.springframework.mail.MailSender
import org.springframework.mail.SimpleMailMessage

class EmailTriggerAction extends BaseTriggerAction
{
	
	MailSender mailSender;
	SimpleMailMessage mailMessage;
	
	String destination;
	
	static transients = ['mailSender', 'mailMessage'];
	
	public void doAction( final String entryUuid )
	{
		println "performing email action for uuid: ${entryUuid}";
		
		def entryToSend = Entry.findByUuid( entryUuid );
		
		// send this entry to this address
		SimpleMailMessage message = new SimpleMailMessage(mailMessage);
		
		// share to email address(es)
		String[] addresses = destination.trim().split("\\s+");
		
		for( String address: addresses )
		{
			try
			{
				String[] to = [address];
				message.to = to;
				// log.debug( "\n\n" + message.to + "\n\n" );
				
				message.text = " \n ${entryToSend.title} \n ${entryToSend.url}";
				message.subject = "Neddick entry shared by trigger {" + this.trigger.name + "} (" + this.trigger.owner.fullName + ")";
				mailSender.send( message );
			}
			catch( Exception e )
			{
				// an exception processing one address shouldn't cause the others to fail
				e.printStackTrace();
				continue;
			}
		}
	}
	
	public String getShortName()
	{
		return "EmailAction";	
	}
	
	public String getValue()
	{
		return destination;
	}
	
}

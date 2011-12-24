package org.fogbeam.neddick

import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;


class ShareController 
{
	MailSender mailSender;
	SimpleMailMessage mailMessage;	
	def xmppNotificationService;
	
	def index = {
		
		log.debug( "user: ${session.user?.userId} sharing entry with id: ${params.entryId}" );
	
		[entryId: params.entryId];
	}

	def shareEntry = {
	
		def entryId = params.entryId;
		def entryToSend = Entry.findById( Integer.parseInt ( entryId ) );
		
		String shareTo = params.share_to;
		String[] addresses = shareTo.trim().split("\\s+");

		def messageText = "${entryToSend.title} \n ${entryToSend.url}\n";
		def messageSubject = "Shared Entry from Project Shelly, shared by ${session.user?.userId}";
		
		for( String address in addresses ) 
		{
			log.debug( "Sharing to address: ${address}");
			
			if( address.startsWith( "xmpp:"))
			{
				String[] parts = address.split( "xmpp:");
				xmppNotificationService.sendChat( parts[1], messageSubject + "\n" + messageText );
			}
			else
			{
			
				// send this entry to this address
				SimpleMailMessage message = new SimpleMailMessage(mailMessage);
				String[] to = [address];
				message.to = to;
				log.debug( "\n\n" + message.to + "\n\n" );
			
				message.text = messageText;
				message.subject = messageSubject; 
				mailSender.send( message );
			}
		}
	}
	
}

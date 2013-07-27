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

	
	def shareItem = {
		
		
		println "in ShareItem:";
		println "params: ${params}";
		
/* 
  
  params: [		
				shareItemUuid:66244d92-c223-4956-ac8b-6a498e539bad, 
				shareEmailCheck:shareEmail, 
				shareItemComment:I think you might find this useful!, 
				shareTargetXmpp:, 
				shareTargetEmail:motley.crue.fan@gmail.com, 
				shareTargetQuoddy:, 
				action:shareItem, 
				controller:share
		   ]

*/		
		
		def entryUuid = params.shareItemUuid;
		def entryToSend = Entry.findByUuid( entryUuid );

		
		def messageSubject = "${session.user?.userId} has shared a Neddick entry with you";
		def messageText = "${params.shareItemComment} \n ${entryToSend.title} \n ${params.permaLink}\n";
				
		
		if( params.shareEmailCheck )
		{

			// send this entry to this address
			SimpleMailMessage message = new SimpleMailMessage(mailMessage);
			
			// share to email address(es)
			String emailTargets = params.shareTargetEmail;
			String[] addresses = emailTargets.trim().split("\\s+");
			
			for( String address: addresses )
			{
				try
				{
					String[] to = [address];
					message.to = to;
					// log.debug( "\n\n" + message.to + "\n\n" );
					
					message.text = messageText;
					message.subject = messageSubject;
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
		
		if( params.shareXmppCheck )
		{
			// share to xmpp address(es)

			
			String xmppTargets = params.shareTargetXmpp;
			String[] addresses = xmppTargets.trim().split("\\s+");
			
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
		
		
		if( params.shareQuoddyCheck )
		{
			// share to Quoddy destinations
			// TODO: 
		}
		
		
		
		
		
		render( "OK" );
	}
}
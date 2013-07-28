package org.fogbeam.neddick

import java.text.SimpleDateFormat

import org.fogbeam.protocol.activitystreams.ActivityStreamEntry
import org.fogbeam.protocol.activitystreams.Actor
import org.fogbeam.protocol.activitystreams.Image
import org.fogbeam.protocol.activitystreams.Target
import org.springframework.http.ResponseEntity
import org.springframework.mail.MailSender
import org.springframework.mail.SimpleMailMessage


class ShareController 
{
	MailSender mailSender;
	SimpleMailMessage mailMessage;	
	def xmppNotificationService;
	def restTemplate;
	
	
	def index = {
		
		log.debug( "user: ${session.user?.userId} sharing entry with id: ${params.entryId}" );
	
		[entryId: params.entryId];
	}

	
	def shareItem = {
		
		
		println "in ShareItem:";
		println "params: ${params}";	
		
		def entryUuid = params.shareItemUuid;
		def entryToSend = Entry.findByUuid( entryUuid );

		
		def messageSubject = "${session.user?.userId} has shared a Neddick entry with you";
						
		
		if( params.shareEmailCheck )
		{

			def messageText = "${params.shareItemComment} \n ${entryToSend.title} \n ${params.permaLink}\n";
			
			
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

			def messageText = "${params.shareItemComment} \n ${entryToSend.title} \n ${params.permaLink}\n";
			
			
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

			def messageText = params.shareItemComment;
			
			
			String quoddyTargets = params.shareTargetQuoddy;
			String[] addresses = quoddyTargets.trim().split("\\s+");
			
			for( String address: addresses )
			{
						
				SimpleDateFormat sdf = new SimpleDateFormat( "yyyy-MM-dd'T'HH:mm:ssZ" );
				
				ActivityStreamEntry newEntry = new ActivityStreamEntry();
				newEntry.setTitle(  entryToSend.title );
				newEntry.setContent( "User ${session.user.userId} has shared a NeddickLink" );
				newEntry.setPublished( sdf.format( new Date() ) );
				
				newEntry.setUrl( params.permaLink );
				newEntry.setVerb( "share_neddick_link" );
				
				Actor actor = new Actor();
				actor.setId( session.user.userId );
				actor.setObjectType( "UserByUserId" );
				actor.setDisplayName( session.user.fullName );
				actor.setUrl( "" );
				Image actorImage = new Image();
				actorImage.setHeight( "" );
				actorImage.setUrl( "" );
				actorImage.setWidth( "" );
				actor.setImage( actorImage );
				
				newEntry.setActor( actor );
				
				org.fogbeam.protocol.activitystreams.Object object = new org.fogbeam.protocol.activitystreams.Object();
				object.setObjectType( "NeddickLink" );
				object.setUrl( params.permaLink );
				object.setId( entryToSend.uuid );
				object.setDisplayName( entryToSend.title );
				object.setSummary( messageSubject );
				object.setContent( messageText );
				newEntry.setObject(  object );
				
				Target target = new Target();
				target.setId( address );
				target.setObjectType( "UserByUserId" );
				target.setDisplayName( "" );
				target.setUrl( "" );
				
				newEntry.setTarget( target );
				
				
				ResponseEntity<String> response =
					restTemplate.postForEntity(
							"http://localhost:8080/quoddy2/api/activitystreamentry",
							newEntry, String.class );
				
				String responseText = response.getBody();
				
				System.out.println( "done with response: " + responseText );
			
			
			} // end quoddy address processing loop
			
		}
		
		
		
		
		
		render( "OK" );
	}
}
package org.fogbeam.neddick

import java.text.SimpleDateFormat

import org.fogbeam.protocol.activitystreams.ActivityStreamEntry
import org.fogbeam.protocol.activitystreams.Actor
import org.fogbeam.protocol.activitystreams.Image
import org.fogbeam.protocol.activitystreams.Target
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.mail.SimpleMailMessage

import grails.plugin.springsecurity.annotation.Secured


public class ShareController 
{
	def mailSender;
	def mailMessage;	
	def xmppNotificationService;
	def restTemplate;
	def userService;
	
	@Autowired
	OAuthService oAuthService;
	
	@Secured(["ROLE_USER","ROLE_ADMIN"])
	def index()
	{
		User currentUser = userService.getLoggedInUser();
		log.debug( "user: ${currentUser.userId} sharing entry with id: ${params.entryId}" );
	
		[entryId: params.entryId];
	}

	@Secured(["ROLE_USER","ROLE_ADMIN"])
	def shareItem() 
	{
		log.debug "in ShareItem:";
		log.debug "params: ${params}";	
		
		def entryUuid = params.shareItemUuid;
		def entryToSend = Entry.findByUuid( entryUuid );
		
		User currentUser = userService.getLoggedInUser();
		def messageSubject = "${currentUser.userId} has shared a Neddick entry with you";
						
		if( params.shareEmailCheck )
		{
			log.info( "Share via Email" );
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
			log.info( "Share via XMPP" );
			
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
			log.info( "Share to Quoddy" );
			
			def messageText = params.shareItemComment;
			
			String quoddyTargets = params.shareTargetQuoddy;
			String[] addresses = quoddyTargets.trim().split("\\s+");
			
			for( String address: addresses )
			{
						
				SimpleDateFormat sdf = new SimpleDateFormat( "yyyy-MM-dd'T'HH:mm:ssZ" );
				
				ActivityStreamEntry newEntry = new ActivityStreamEntry();
				newEntry.setTitle(  entryToSend.title );
				newEntry.setContent( "User ${currentUser.userId} has shared a NeddickLink" );
				newEntry.setPublished( sdf.format( new Date() ) );
				
				newEntry.setUrl( params.permaLink );
				newEntry.setVerb( "share_neddick_link" );
				
				Actor actor = new Actor();
				actor.setId( currentUser.userId );
				actor.setObjectType( "UserByUserId" );
				actor.setDisplayName( currentUser.fullName );
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
				
				
				String quoddyActivityStreamsUrl = grailsApplication.config.urls.quoddy.activitystreams.endpoint;
				log.info "using quoddyActivityStreamsUrl: ${quoddyActivityStreamsUrl}";
				
				HttpHeaders headers = new HttpHeaders();
				String oAuthToken = oAuthService.getQuoddyOAuthToken();
				
				log.debug( "OAuthToken: ${oAuthToken}" );
				
				headers.add( "Authorization", "Bearer " + oAuthToken );
				headers.setContentType(MediaType.APPLICATION_JSON);
				List<MediaType> acceptTypes = new ArrayList<MediaType>();
				acceptTypes.add( MediaType.APPLICATION_JSON);
				// acceptTypes.add( MediaType.APPLICATION_XML );
				headers.setAccept(acceptTypes);
				
				HttpEntity<ActivityStreamEntry> entity = new HttpEntity<ActivityStreamEntry>( newEntry, headers);
				
				HttpEntity<String> responseEntity = restTemplate.exchange(quoddyActivityStreamsUrl, HttpMethod.POST, entity, String.class );
				
				String responseText = responseEntity.getBody();
				
				log.info( "done with response: " + responseText );
			
			
			} // end quoddy address processing loop
			
		}
		
		render( "OK" );
	}
}
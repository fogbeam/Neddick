package org.fogbeam.neddick.triggers.actions

import java.text.SimpleDateFormat

import org.fogbeam.neddick.Entry
import org.fogbeam.protocol.activitystreams.ActivityStreamEntry
import org.fogbeam.protocol.activitystreams.Actor
import org.fogbeam.protocol.activitystreams.Image
import org.fogbeam.protocol.activitystreams.Target
import org.springframework.http.ResponseEntity

class QuoddyShareTriggerAction extends BaseTriggerAction
{
	String destination;
	
	public void doAction( final String entryUuid )
	{
		println "performing quoddy_share action for uuid: ${entryUuid}";
		

		def entryToSend = Entry.findByUuid( entryUuid );
		
		
		// share to email address(es)
		String[] addresses = destination.trim().split("\\s+");
		
		def messageText = " \n ${entryToSend.title} \n ${entryToSend.url}";
		def messageSubject = "Neddick entry shared by trigger {" + this.trigger.name + "} (" + this.trigger.owner.fullName + ")";

		
		for( String address: addresses )
		{
					
			SimpleDateFormat sdf = new SimpleDateFormat( "yyyy-MM-dd'T'HH:mm:ssZ" );
			
			ActivityStreamEntry newEntry = new ActivityStreamEntry();
			newEntry.setTitle(  entryToSend.title );
			newEntry.setContent( "User TBD has shared a NeddickLink" );
			newEntry.setPublished( sdf.format( new Date() ) );
			
			newEntry.setUrl( "TBD" );
			newEntry.setVerb( "share_neddick_link" );
			
			Actor actor = new Actor();
			actor.setId( this.trigger.owner.userId ); 
			actor.setObjectType( "UserByUserId" );
			actor.setDisplayName( this.trigger.owner.fullName );
			actor.setUrl( "" );
			Image actorImage = new Image();
			actorImage.setHeight( "" );
			actorImage.setUrl( "" );
			actorImage.setWidth( "" );
			actor.setImage( actorImage );
			
			newEntry.setActor( actor );
			
			org.fogbeam.protocol.activitystreams.Object object = new org.fogbeam.protocol.activitystreams.Object();
			object.setObjectType( "NeddickLink" );
			object.setUrl( "TBD" );
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
}

package org.fogbeam.neddick.jaxrs


import static org.grails.plugins.jaxrs.response.Responses.*;

import javax.ws.rs.Consumes
import javax.ws.rs.POST
import javax.ws.rs.Path
import javax.ws.rs.Produces

import org.fogbeam.neddick.Channel
import org.fogbeam.neddick.Entry
import org.fogbeam.neddick.User
import org.fogbeam.neddick.WebpageEntry

@Path('/api/webpageentry')
@Consumes(['application/xml','application/json'])
@Produces(['application/xml','application/json'])
class WebpageEntryResource 
{
	def entryService;
	def entryCacheService;
	def userService;
	def channelService;
	def recommenderService;
	def siteConfigService;
	
	@POST
	WebpageEntry insert( WebpageEntry entry )
	{
		// see what channel we're submitting to
		String channelName = params.channelName;
		log.debug( "channelName submitted as: ${channelName}" );
		if( channelName == null || channelName.isEmpty())
		{
			channelName = grailsApplication.config.channel.defaultChannel;
			log.debug( "defaulting channelName to: ${channelName}" );
		}
		
		Channel theChannel = channelService.findByName( channelName );
	
		log.debug( "url submitted as: ${params.url}" );
		def url = params.url;
		
		
		// check if this channel already has an Entry for this same link
		List<Entry> entries = entryService.findByUrlAndChannel( url, theChannel );
				
		if( entries.size() > 0  )
		{
			flash.message = "An Entry for this link already exists";
			redirect( action:'create'); // TODO: save channel so we can pre-populate that form field
			return;
		}
		else
		{
		
			// does this link exist elsewhere in the system (eg, linked to another channel)?
			List<Entry> e2 = entryService.findByUrl( url );
			if( e2 != null && e2.size() > 0 )
			{
				// we already have this Entry, so instead of creating a new Entry object, we just
				// need to link this one to this Channel.
				Entry existingEntry = e2.get(0);
				existingEntry.addToChannels( theChannel );
				existingEntry.save();
			}
			else
			{
				// TODO: get the user from params and authenticate using OAuth
				
				if( session.user )
				{
					def user = User.findByUserId(session.user.userId);
					entry.submitter = user;
				}
				else
				{
					def anonymous = User.findByUserId( "anonymous" );
					entry.submitter = anonymous;
				}
				
				// TODO: deal with transactionality
				if( entryService.saveEntry( entry ) )
				{
				
					log.debug( "Saved Entry: ${entry.url}");
					entry.addToChannels( theChannel );
					
					// send JMS message saying "new entry submitted"
					def newEntryMessage = [msgType:"NEW_ENTRY", id:entry.id, uuid:entry.uuid, url:entry.url, title:entry.title ];
			
					// send a JMS message to our entryQueue
					sendJMSMessage("entryQueue", newEntryMessage );
					
					// send a JMS message to our searchQueue
					sendJMSMessage("searchQueue", newEntryMessage );
				}
				else
				{
					log.error( "Could not save Entry: ${entry.url}" );
				}
			}
				
		}
		
		return entry;
	}
}

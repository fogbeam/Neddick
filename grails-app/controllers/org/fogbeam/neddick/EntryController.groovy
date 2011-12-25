package org.fogbeam.neddick

import org.codehaus.groovy.grails.commons.ConfigurationHolder 

class EntryController {

	def entryService;
	def entryCacheService;
	def userService;
	def channelService;
	def recommenderService;
	def siteConfigService;
	
	def scaffold = true;
	
    def create = {}
    
    def submit = {
    	
    		
    	// see what channel we're submitting to
    	String channelName = params.channelName;
    	log.debug( "channelName submitted as: ${channelName}" );
    	if( channelName == null || channelName.isEmpty()) 
    	{  		
    		channelName = ConfigurationHolder.config.channel.defaultChannel;
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
			
		    	def entry = new Entry(params);
		    	
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
    	
    	redirect(controller:'home', action: 'index');
    }

    def hideEntry = {
            
		if( session.user )
        {
            // lookup the Entry by id
            // add it to the User's hidden list
            log.debug( "entryId: ${params.entryId}" );
            Entry entry = Entry.findById(params.entryId);
            log.debug( "entry: ${entry}" );
            def user = User.findByUserId( session.user.userId );
            log.debug( "user: ${user}" );
            user.addToHiddenEntries( entry );
            userService.updateUser( user );
            
			// note: have to pass the User object from the session, since the cache
			// is keyed on the actual objects, which depend on object identity, not
			// db id equality.
			// DEADCODE: entryCacheService.removeEntry( session.user, entry );
			
            log.debug( "hid Entry for user ${session.user.userId}");
        }
        else
        {
            // do nothing, hidng an Entry is meaningless if you're not logged in.
            log.info( "doing nothing, not logged in!" );
        }
    		
    }
    
    def saveEntry = {
    		
		if( session.user )
		{
			// lookup the Entry by id
			// add it to the User's saved list
			log.debug( "entryId: ${params.entryId}" );
			Entry entry = Entry.findById(params.entryId);
			log.debug( "entry: ${entry}" );
			def user = User.findByUserId( session.user.userId );
			log.debug( "user: ${user}" );
			user.addToSavedEntries( entry );
			
			userService.updateUser( user );
			
			log.debug( "saved Entry for user ${session.user.userId}");
		}
		else
		{
			// do nothing, saving an Entry is meaningless if you're not logged in.
			log.info( "doing nothing, not logged in!" );
		}
    }

    def viewEntry = {
    		
    	def uuid = params.uuid;
    
    	Entry entry = entryService.findByUuid( uuid );
    	
    	List<Entry> recommendedEntries = recommenderService.getRecommendedEntries( entry );
    	
    	[theEntry: entry, recommendedEntries : recommendedEntries ];
    }
    
    
    def createQuestion = {
    	
    }
   
    def submitQuestion = {
    		
	   	def entry = new Question(params);
    	
    	// see what channel we're submitting to
    	String channelName = params.channelName;
    	log.debug( "channelName submitted as: ${channelName}" );
    	if( channelName == null || channelName.isEmpty()) 
    	{  		
    		channelName = ConfigurationHolder.config.channel.defaultChannel;
    		log.debug( "defaulting channelName to: ${channelName}" );
    	}
    	
    	Channel theChannel = channelService.findByName( channelName );    
    	
    	entry.addToChannels( theChannel );
    	
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
    	
		String urlBase = siteConfigService.getSiteConfigEntry( "baseUrl" );
		String url = urlBase + "/entry/e/" + entry.uuid;
    	entry.url = url;
    	
    	if( entryService.saveEntry( entry ) )
		{
    	
			// send JMS message saying "new entry submitted"
			def newEntryMessage = [msgType:"NEW_QUESTION", id:entry.id, uuid:entry.uuid, url:entry.url, title:entry.title ];
		
			// send a JMS message to our entryQueue
			sendJMSMessage("entryQueue", newEntryMessage );
			
			// send a JMS message to our searchQueue
			sendJMSMessage("searchQueue", newEntryMessage );
		}
		
    	redirect(controller:'home', action: 'index');    	
	}
    
}
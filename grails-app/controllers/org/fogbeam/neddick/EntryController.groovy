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
    	println "channelName submitted as: ${channelName}"
    	if( channelName == null || channelName.isEmpty()) 
    	{  		
    		channelName = ConfigurationHolder.config.channel.defaultChannel;
    		println "defaulting channelName to: ${channelName}"
    	}
    	
    	Channel theChannel = channelService.findByName( channelName );     		
    
		println "url submitted as: ${params.url}";
		def url = params.url;
    	
		// check if this channel already has an Entry for this same link
    	List<Entry> entries = Entry.executeQuery( "select entry from Entry as entry where entry.url = ? and entry.channel = ?", [url, theChannel] );
    	
    	if( entries.size() > 0  )
    	{
    		flash.message = "An Entry for this link already exists";
    		redirect( action:'create'); // TODO: save channel so we can pre-populate that form field
    		return;
    	}
    	else 
    	{
    	
	    	def entry = new Entry(params);
	    	
	   
	    	
	    	entry.setChannel( theChannel );
	    	
	    	if( session.user )
	    	{
	    		entry.submitter = session.user;
	    	}
	    	else
	    	{
	    		def anonymous = User.findByUserId( "anonymous" );
	    		entry.submitter = anonymous;
	    	}
	    	
	    	// TODO: deal with transactionality
	    	entryService.saveEntry( entry );
			
			// TODO: make sure the right cache(s) get updated here...
			
			entryCacheService.addEntry(entry);
			
	    	// send JMS message saying "new entry submitted"
	    	def newEntryMessage = [msgType:"NEW_ENTRY", id:entry.id, uuid:entry.uuid, url:entry.url, title:entry.title ];
	    
	    	// send a JMS message to our testQueue
			sendJMSMessage("testQueue", newEntryMessage );
    	}
    	
    	redirect(controller:'home', action: 'index');
    }

    def hideEntry = {
            
		if( session.user )
        {
            // lookup the Entry by id
            // add it to the User's hidden list
            println "entryId: ${params.entryId}";
            Entry entry = Entry.findById(params.entryId);
            println "entry: ${entry}";
            def user = User.findByUserId( session.user.userId );
            println "user: ${user}";
            user.addToHiddenEntries( entry );
            userService.updateUser( user );
            
			// note: have to pass the User object from the session, since the cache
			// is keyed on the actual objects, which depend on object identity, not
			// db id equality.
			entryCacheService.removeEntry( session.user, entry );
			
            println( "hid Entry for user ${session.user.userId}");
        }
        else
        {
            // do nothing, hidng an Entry is meaningless if you're not logged in.
            println( "doing nothing, not logged in!" );
        }
    		
    }
    
    def saveEntry = {
    		
		if( session.user )
		{
			// lookup the Entry by id
			// add it to the User's saved list
			println "entryId: ${params.entryId}";
			Entry entry = Entry.findById(params.entryId);
			println "entry: ${entry}";
			def user = User.findByUserId( session.user.userId );
			println "user: ${user}";
			user.addToSavedEntries( entry );
			
			userService.updateUser( user );
			
			println( "saved Entry for user ${session.user.userId}");
		}
		else
		{
			// do nothing, saving an Entry is meaningless if you're not logged in.
			println( "doing nothing, not logged in!" );
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
    	println "channelName submitted as: ${channelName}"
    	if( channelName == null || channelName.isEmpty()) 
    	{  		
    		channelName = ConfigurationHolder.config.channel.defaultChannel;
    		println "defaulting channelName to: ${channelName}"
    	}
    	
    	Channel theChannel = channelService.findByName( channelName );    
    	
    	entry.setChannel( theChannel );
    	
    	if( session.user )
    	{
    		entry.submitter = session.user;
    	}
    	else
    	{
    		def anonymous = User.findByUserId( "anonymous" );
    		entry.submitter = anonymous;
    	}
    	
		String urlBase = siteConfigService.getSiteConfigEntry( "baseUrl" );
		String url = urlBase + "/entry/e/" + entry.uuid;
    	entry.url = url;
    	
    	entryService.saveEntry( entry );
    	
    	// send JMS message saying "new entry submitted"
    	def newEntryMessage = [msgType:"NEW_QUESTION", id:entry.id, uuid:entry.uuid, url:entry.url, title:entry.title ];
    
    	// send a JMS message to our testQueue
		sendJMSMessage("testQueue", newEntryMessage );
    	
    	redirect(controller:'home', action: 'index');    	
    }
    
}

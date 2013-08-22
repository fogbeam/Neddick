package org.fogbeam.neddick


class ChannelController {

	def scaffold = true;
	def channelService;
	def entryService;
	def siteConfigService;
	def sessionFactory;
	def userService;
	
	int itemsPerPage = -1;
	
	def list = {
			
    	if( itemsPerPage == -1 )
    	{
    		itemsPerPage = Integer.parseInt( siteConfigService.getSiteConfigEntry( "itemsPerPage" ));
    	}
    	
    	String requestedPageNumber = params.pageNumber;
    	int pageNumber = 1;
    	int availablePages = -1;
    	if( requestedPageNumber != null )
    	{
    		try
    		{
    			pageNumber = Integer.parseInt( requestedPageNumber );
    		}
    		catch( NumberFormatException nfe )
    		{
    			flash.message = "Invalid Pagenumber requested";
    			pageNumber = 1;
    		}
    	}
    	
    	log.debug( "requested pageNumber: ${pageNumber}" );			
			
		List<Channel> allChannels = new ArrayList<Channel>();
		allChannels.addAll( channelService.getAllChannels());
	
    	int dataSize = allChannels.size();
    	log.debug( "dataSize: ${dataSize}" );
    	int pages = dataSize / itemsPerPage;
		log.debug( "dataSize / itemsPerPage = ${pages}" );
    	pages = Math.max( pages, 1 );
		
		log.debug( "pages: ${pages}" );
		
		if( dataSize > (pages*itemsPerPage) )
		{
			log.debug( "WTF:  ${dataSize % (pages*itemsPerPage)}" );
			pages += 1;
		}
		
		availablePages = pages;
    	log.debug( "availablePages: ${availablePages}" );
    	
    	if( pageNumber < 1 )
    	{
    		flash.message = "Invalid Pagenumber ${requestedPageNumber} requested";
    		pageNumber = 1;
    	}
    	if( pageNumber > availablePages )
    	{
    		flash.message = "Invalid Pagenumber ${requestedPageNumber} requested";
    		pageNumber = availablePages;
    	}
    	
    	
    	// get the requested page of entry UUIDs
    	int beginIndex = ( pageNumber * itemsPerPage ) - itemsPerPage;
    	int endIndex = Math.min( dataSize -1, ((pageNumber * itemsPerPage ) - 1));
    	
		if( pageNumber == pages )
		{
			endIndex = Math.min( dataSize -1, endIndex);
		}        	
    	
		
    	List<Tag> subList = null;
		
		if( dataSize > 0 )
		{
			subList = allChannels[ beginIndex .. endIndex ];		
		}
		else
		{
			subList = new ArrayList<Channel>();	
		}
		
		[allChannels: subList, currentPageNumber: pageNumber, availablePages: availablePages ];	
	}
	
	def updateFromDatasource = {
				
		// lookup the specified channel
		String channelName = params.channelName;
		if( channelName )
		{
			Channel theChannel = channelService.findByName( channelName );	
			channelService.updateFromDatasource( theChannel );
		}
		
		render( "DONE" );
	}
	
	def create = {
		List<RssFeed> availableFeeds = RssFeed.list();
		
		[availableFeeds:availableFeeds];
	}
	
	def save = {
		
		println "channel save params: ${params}";
		
		log.debug( "Channel.save()" );
		Channel channel = new Channel();
		channel.name = params.channelName;
		channel.description = params.channelDescription;
		List<RssFeed> feeds = new ArrayList<RssFeed>();
		String[] feedsToAdd = params.feeds;
		for( String feedToAdd : feedsToAdd ) 
		{
			log.debug( "adding feed: ${feedToAdd}" );
			RssFeed theFeed = RssFeed.findById( feedToAdd );
			feeds.add( theFeed );	
		}
		
		channel.feeds = feeds;
		
		if( params.privateChannel != null && params.privateChannel.equals( "on" ))
		{
			channel.privateChannel = true;
		}
		else
		{
			channel.privateChannel = false;
		}
		
		channel.owner = session.user;
		
		if( channel.save() )
		{
			
		}
		else
		{
			flash.message = "Failed to save Channel!";	
		}
		
		redirect(controller:"channel", action:"list");
	}
	
	def edit = {

		// lookup our user
		User user = userService.findUserByUserId( session.user.userId );
		
		// lookup the specified channel
		String channelName = params.id;

		println "got channelName as: ${channelName}";
		
		Channel theChannel = null;
		if( channelName )
		{
			theChannel = channelService.findByName( channelName );
		
			if( theChannel )
			{
				println "located channel: ${theChannel.id}";
			
				
				// check if it's a private channel, and - if it is - if the user
				// is the channel owner
				if( theChannel.privateChannel )
				{
					if( theChannel.owner.id != user.id )
					{
						flash.message = "Not authorized to edit this channel";
						return [];
					}
				}		
			}
			else
			{
				println "Could not locate channel: ${channelName}";
			}
			
		}
		else
		{
			flash.message = "No Channel name provided";
		}
		
		
		
		
		List<RssFeed> availableFeeds = RssFeed.executeQuery( "select feed from RssFeed as feed, Channel as channel where channel = ? and feed not in elements(channel.feeds)", [theChannel] );
		println "Found ${availableFeeds.size()} available feeds";
		
				
		[channel: theChannel,availableFeeds:availableFeeds];
		
	}
	
	def update = {
		
		log.debug( "Update Channel Properties: ${params.channelId}" );
				
		Channel theChannel = Channel.findById( params.channelId );
		
		
		// deal with private channel test
		if( theChannel.privateChannel )
		{
			User user = userService.findUserByUserId( session.user.userId );
			if( theChannel.owner.id != user.id )
			{
				flash.message = "Not authorized to edit that Channel";
				redirect(controller:"channel", action:"list");
			}
		}
				
		theChannel.description = params.channelDescription;
		
		if( params.privateChannel != null && params.privateChannel.equals("on"))
		{
			theChannel.privateChannel = true;
		}
		else
		{
			theChannel.privateChannel = false;
		}
		
				
		def feedsToRemove = params.list('feedsToRemove');
		for( String feedToRemove : feedsToRemove )
		{
			
			log.debug( "removing feed: ${feedToRemove}" );
			// RssFeed feed = RssFeed.findById( feedToRemove );
			RssFeed feed = theChannel.feeds.find { it.id == Integer.parseInt(feedToRemove) }
			if( feed )
			{
				log.debug( "calling removeFromFeeds using feed: ${feed}");
				theChannel.removeFromFeeds( feed );
			}
			else
			{
				log.warn( "problem finding feed instance for ${feedToRemove}" );	
			}
		
			log.debug( "about to theChannel.save()" );
			if( !theChannel.save(flush:true, validate:true) )
			{	
				log.error( "Error saving channel" );
				// theChannel.errors.allErrors.each { p rintln it };
			}			
				
		}
	
	
		log.debug( "dealing with feeds to add" );
		def feedsToAdd = params.list( 'feedsToAdd');
		for( String feedToAdd : feedsToAdd ) 
		{	
			log.debug( "adding feed: ${feedToAdd}" );
			RssFeed feed = RssFeed.findById( feedToAdd );
			theChannel.addToFeeds( feed );
		}
	
		if( !theChannel.save() )
		{
			log.error( "Error saving channel" );
			// theChannel.errors.allErrors.each { p rintln it };
		}

		redirect(controller:"channel", action:"list");
	}
	
}
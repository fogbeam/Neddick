package org.fogbeam.neddick


class ChannelController {

	def scaffold = true;
	def channelService;
	def entryService;
	def siteConfigService;
	def sessionFactory;
	
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
		// lookup the specified channel
		String channelName = params.id;
		Channel theChannel = null;
		if( channelName )
		{
			theChannel = channelService.findByName( channelName );
		}

		
		List<RssFeed> availableFeeds = RssFeed.executeQuery( "select feed from RssFeed as feed, Channel as channel where channel = ? and feed not in elements(channel.feeds)", [theChannel] );
				
		[channel: theChannel,availableFeeds:availableFeeds];
		
	}
	
	def update = {
		
		log.debug( "Update Channel Properties: ${params.channelId}" );
				
		Channel theChannel = Channel.findById( params.channelId );
		theChannel.description = params.channelDescription;
				
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
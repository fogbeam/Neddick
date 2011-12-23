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
    	
    	println "requested pageNumber: ${pageNumber}";			
			
		List<Channel> allChannels = new ArrayList<Channel>();
		allChannels.addAll( channelService.getAllChannels());
	
    	int dataSize = allChannels.size();
    	println "dataSize: ${dataSize}";
    	int pages = dataSize / itemsPerPage;
		println "dataSize / itemsPerPage = ${pages}"
    	pages = Math.max( pages, 1 );
		
		println "pages: ${pages}";
		
		if( dataSize > (pages*itemsPerPage) )
		{
			println "WTF:  ${dataSize % (pages*itemsPerPage)}";
			pages += 1;
		}
		
		availablePages = pages;
    	println "availablePages: ${availablePages}";
    	
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

		// done... return.
		System.out.println( "done" );
		render( "DONE" );
	}
	
	def create = {
		List<RssFeed> availableFeeds = RssFeed.list();
		
		[availableFeeds:availableFeeds];
	}
	
	def save = {
		
		println "Channel.save()";
		println params;
		println "";
		
		Channel channel = new Channel();
		channel.name = params.channelName;
		channel.description = params.channelDescription;
		List<RssFeed> feeds = new ArrayList<RssFeed>();
		String[] feedsToAdd = params.feeds;
		for( String feedToAdd : feedsToAdd ) 
		{
			println "adding feed: ${feedToAdd}";
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
		
		println "Update Channel Properties: ${params.channelId}";
		println "feedsToAdd: ${params.feedsToAdd}";
		println "feedsToRemove: ${params.feedsToRemove}";
		println "Params:\n ${params}";
		
		Channel theChannel = Channel.findById( params.channelId );
		theChannel.description = params.channelDescription;
				
		def feedsToRemove = params.list('feedsToRemove');
		for( String feedToRemove : feedsToRemove )
		{
			
			println "removing feed: ${feedToRemove}";
			// RssFeed feed = RssFeed.findById( feedToRemove );
			RssFeed feed = theChannel.feeds.find { it.id == Integer.parseInt(feedToRemove) }
			if( feed )
			{
				println "calling removeFromFeeds using feed: ${feed}";
				theChannel.removeFromFeeds( feed );
			}
			else
			{
				println "problem finding feed instance for ${feedToRemove}";	
			}
		
			println "about to save()";
			if( !theChannel.save(flush:true, validate:true) )
			{
				theChannel.errors.allErrors.each { println it };
			}			
				
		}
	
	
		println "dealing with feeds to add";
		def feedsToAdd = params.list( 'feedsToAdd');
		for( String feedToAdd : feedsToAdd ) 
		{	
			println "adding feed: ${feedToAdd}";
			RssFeed feed = RssFeed.findById( feedToAdd );
			theChannel.addToFeeds( feed );
		}
	
		if( !theChannel.save() )
		{
			theChannel.errors.allErrors.each { println it };
		}
		
		println "done";
		redirect(controller:"channel", action:"list");
	}
	
}
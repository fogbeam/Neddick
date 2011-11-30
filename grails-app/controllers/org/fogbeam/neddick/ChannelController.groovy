package org.fogbeam.neddick


class ChannelController {

	def scaffold = true;
	def channelService;
	def entryService;
	def siteConfigService;
	
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
		// [feeds:16723, channelName:groovy, channelDescription:Groovy Stuff, Save:Save, action:save, controller:channel]
 
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
	
	def edit_temp = {
		
	}
}

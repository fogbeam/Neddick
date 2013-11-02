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
			def itemsPerPageValue = siteConfigService.getSiteConfigEntry( "itemsPerPage" );
    		if( itemsPerPageValue )
			{
				itemsPerPage = Integer.parseInt( itemsPerPageValue );
			}
			else
			{
				itemsPerPage = Integer.parseInt( "25" );
			}
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
		
		// List<RssFeed> availableFeeds = RssFeed.list();
		List<DataSource> availableDatasources = DataSource.list();
		
		User user = userService.findUserByUserId( session.user.userId );
		
		List<Channel> availableChannels = channelService.getEligibleAggregateChannels( user );
		
		
		[ // availableFeeds:availableFeeds,
		  availableDatasources:availableDatasources,
		  availableChannels:availableChannels];
	}
	
	def save = {
		
		println "channel save params: ${params}";
		
		log.debug( "Channel.save()" );
		Channel channel = new Channel();
		channel.name = params.channelName;
		channel.description = params.channelDescription;
		
		/* TODO: change this to manipulate DataSources */
		/* 
		List<RssFeed> feeds = new ArrayList<RssFeed>();
		String[] feedsToAdd = params.feeds;
		for( String feedToAdd : feedsToAdd ) 
		{
			log.debug( "adding feed: ${feedToAdd}" );
			RssFeed theFeed = RssFeed.findById( feedToAdd );
			feeds.add( theFeed );	
		}
		
		channel.feeds = feeds;
		*/
		
		String[] aggregateChannelsToAdd = params.aggregateChannels;
		List<Channel> aggregateChannels = new ArrayList<Channel>();
		// println "aggregateChannels: ${aggregateChannelsToAdd}";
		for( String aggregateChannelId : aggregateChannelsToAdd )
		{
			Channel channelToAdd = channelService.findById( Long.parseLong( aggregateChannelId ));
			if( channelToAdd )
			{
				aggregateChannels.add( channelToAdd );
			}
		}
		
		channel.aggregateChannels = aggregateChannels;
		
		
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
			channel.errors.allErrors.each{ println it };
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
		
		
		
		
		// List<RssFeed> availableFeeds = RssFeed.executeQuery( "select feed from RssFeed as feed, Channel as channel where channel = ? and feed not in elements(channel.feeds)", [theChannel] );
		// println "Found ${availableFeeds.size()} available feeds";
		List<DataSource> availableDatasources = DataSource.executeQuery( "select datasource from DataSource as datasource, Channel as channel where channel = ? and datasource not in elements(channel.dataSources)", [theChannel] );
		println "Found ${availableDatasources.size()} available datasources";
		
		List<Channel> availableChannels = channelService.getEligibleAggregateChannels( user, theChannel );
		
		
		[ 	channel: theChannel,
			// availableFeeds:availableFeeds,
			availableDatasources:availableDatasources,
			availableChannels:availableChannels];
	}
	
	
	def update = {
		
		log.debug( "Update Channel Properties: ${params.channelId}" );
		
		println "params: ${params}";
				
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
		
		// TODO: change all of the following to work with DataSources, not RssFeed instances
				
		def datasourcesToRemove = params.list('datasourcesToRemove');
		for( String datasourceToRemove : datasourcesToRemove )
		{
			
			log.debug( "removing datasource: ${datasourceToRemove}" );
			// RssFeed feed = RssFeed.findById( feedToRemove );
			DataSource datasource = theChannel.dataSource.find { it.id == Integer.parseInt(datasourceToRemove) }
			if( datasource )
			{
				log.debug( "calling removeFromDataSource using datasource: ${datasource}");
				theChannel.removeFromDataSources( datasource );
			}
			else
			{
				log.warn( "problem finding datasource instance for ${datasourceToRemove}" );	
			}
		
			log.debug( "about to theChannel.save()" );
			if( !theChannel.save(flush:true, validate:true) )
			{	
				log.error( "Error saving channel" );
				theChannel.errors.allErrors.each { println it };
			}			
				
		}
	
	
		log.debug( "dealing with datasources to add" );
		def datasourcesToAdd = params.list( 'datasourcesToAdd');
		for( String datasourceToAdd : datasourcesToAdd ) 
		{	
			println( "adding datasource: ${datasourceToAdd}" );
			DataSource datasource = DataSource.findById( Integer.parseInt( datasourceToAdd ) );
			theChannel.addToDataSources( datasource );
		}
	
		if( !theChannel.save() )
		{
			log.error( "Error saving channel" );
			theChannel.errors.allErrors.each { println it };
		}

		
		def aggregateChannelsToRemove = params.list('aggregateChannelsToRemove');
		for( String aggregateChannelToRemove : aggregateChannelsToRemove )
		{
			
			log.debug( "removing channel: ${aggregateChannelToRemove}" );

			Channel channel = theChannel.aggregateChannels.find { it.id == Integer.parseInt(aggregateChannelToRemove) }
			if( channel )
			{
				log.debug( "calling removeFromAggregateChannels using channel: ${channel}");
				theChannel.removeFromAggregateChannels( channel );
			}
			else
			{
				log.warn( "problem finding channel instance for ${aggregateChannelToRemove}" );
			}
		
			log.debug( "about to theChannel.save()" );
			if( !theChannel.save(flush:true, validate:true) )
			{
				log.error( "Error saving channel" );
				// theChannel.errors.allErrors.each { p rintln it };
			}
				
		}
	
	
		log.debug( "dealing with aggregate channels to add" );
		def aggregateChannelsToAdd = params.list('aggregateChannelsToAdd');
		
		for( String aggregateChannelToAdd : aggregateChannelsToAdd )
		{
			log.debug( "adding aggregateChannel: ${aggregateChannelToAdd}" );
			Channel channel = channelService.findById( Long.parseLong( aggregateChannelToAdd ) );
			theChannel.addToAggregateChannels( channel );
		}
	
		if( !theChannel.save() )
		{
			log.error( "Error saving channel" );
			// theChannel.errors.allErrors.each { p rintln it };
		}		
		
		
		redirect(controller:"channel", action:"list");
	}

	/* add channel to User's favorite channels list */
	def addChannelToFavorites =
	{
		def channelId = params.channelId;
		Channel channel = channelService.findById( Long.parseLong(channelId ));
		
		User user = userService.findUserByUserId( session.user.userId );
		
		userService.addChannelToUserFavorites( user, channel );
		
		render( status:200, text:"OK" );
	}
		
}
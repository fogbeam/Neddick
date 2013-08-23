package org.fogbeam.neddick

import org.codehaus.groovy.grails.commons.ConfigurationHolder
import org.fogbeam.neddick.filters.BaseFilter


class HomeController {

	def userService;
	def entryService;
	def entryCacheService;
	def channelService;
	def siteConfigService;	
	def filterService;
	
	int itemsPerPage = -1;
	
    static navigation =
        [
            [group:'tabs', action:'index', title:'New', order:90],
            [action:'hotEntries', title:'Hot', order:94, isVisible:{true}],
            [action:'topEntries', title:'Top', order:96, isVisible: {true}],
            [action:'controversialEntries', title:'Controversial', order:97, isVisible: {true}],
			[action:'savedEntries', title:'Saved', order:98, isVisible: {true}]
        
        ];	
	
	
	def index = {
	
		log.debug( "index" );
		
		if( itemsPerPage == -1 )
		{
			String strItemsPerPage = siteConfigService.getSiteConfigEntry( "itemsPerPage" );
			if( strItemsPerPage != null && !strItemsPerPage.empty ) 
			{
				itemsPerPage = Integer.parseInt( strItemsPerPage );
			}
			else 
			{
				// default if it's not set in the DB
				// TODO: move "default" control into siteConfigEntryService
				itemsPerPage = 25;
			}
		}
		
		log.debug( "itemsPerPage: ${itemsPerPage}");
		
		String requestedPageNumber = params.pageNumber;
		
		log.debug( "requestedPageNumber: ${requestedPageNumber}");
		
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
		
		// lookup the Entries and store them in a variable to be rendered by
		// the GSP page
			
		// see what channel we're looking at
		boolean defaultChannel = false;
		String channelName = params.channelName;
		if( channelName == null )
		{	
			channelName = ConfigurationHolder.config.channel.defaultChannel;
			defaultChannel = true;
		}
		
		Channel theChannel = channelService.findByName( channelName );
		
		User user = null;
		if( session.user )
		{
			user = userService.findUserByUserId( session.user.userId);

		}
		else 
		{
			user = userService.findUserByUserId( "anonymous" );
		}
		
		
		// check if this is a private channel, and - if it is - if the user
		// is the owner
		if( theChannel.privateChannel )
		{
			if( user.id != theChannel.owner.id )
			{
				flash.message = "Not authorized to view Channel: [${theChannel.name}]";
				redirect( controller:'home', action:'index' );
			}
		}
		
		
		// check if there is a Filter for this channel, for this User
		// if there is, only render the entries from the Filter
		BaseFilter filter = filterService.findFilterByUserAndChannel( user, theChannel );
		
		if( filter )
		{

			
			// select count Entries for this channel
			int dataSize = filterService.getCountNonHiddenEntriesForFilter( filter );
			
			int pages = dataSize / itemsPerPage;
			pages = Math.max( pages, 1 );
			
			log.debug( "calculated available pages as: ${pages}");
			
			if( dataSize > (pages*itemsPerPage) )
			{
				pages += 1;
			}
			availablePages = pages;
			
			log.debug( "After allowing for overflow, availablePages now: ${availablePages}");
			
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

			
			log.debug( "calling getAllNonHiddenEntriesForFilter" );
			List<Entry> entries;
			entries = filterService.getAllNonHiddenEntriesForFilter(filter, itemsPerPage, ( pageNumber * itemsPerPage ) - itemsPerPage );
			
			
			def sortedEntries = entries.sort { it.dateCreated }.reverse();
			def model = [allEntries: sortedEntries,
						 channelName: channelName, currentPageNumber: pageNumber, availablePages: availablePages,
						 requestType:"index"];
	
			render(view:"index", model:model);
			
						
		}
		else
		{
			
			// select count Entries for this channel
			int dataSize = 0;
			if( user != null ) {
				
				dataSize = entryService.getCountNonHiddenEntriesForUser( theChannel, user );
			}
			else 
			{
				dataSize = entryService.getCountAllEntries( theChannel );
				
			}
			
			int pages = dataSize / itemsPerPage;
			pages = Math.max( pages, 1 );
			
			log.debug( "calculated available pages as: ${pages}");
			
			if( dataSize > (pages*itemsPerPage) )
			{
				pages += 1;
			}
			availablePages = pages;
			
			log.debug( "After allowing for overflow, availablePages now: ${availablePages}");
			
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
	
			List<Entry> entries = null;
			if( user != null ) 
			{
				log.debug( "calling getAllNonHiddenEntriesForUser" );
				entries = entryService.getAllNonHiddenEntriesForUser(theChannel, user, itemsPerPage, ( pageNumber * itemsPerPage ) - itemsPerPage );
			}
			else 
			{
				log.debug( "calling getAllEntries" );
				entries = entryService.getAllEntries( theChannel, itemsPerPage, ( pageNumber * itemsPerPage ) - itemsPerPage );
			}
			
			def sortedEntries = entries.sort { it.dateCreated }.reverse();
			def model = [allEntries: sortedEntries,
						 channelName: channelName, currentPageNumber: pageNumber, availablePages: availablePages,
						 requestType:"index"];
	
			render(view:"index", model:model);
		}
	}

	
    def index2 = {
    		
    }

	    
    def hotEntries = 
    {
    	log.debug( "hotEntries" );	
    	
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
    	
    	// see what channel we're looking at
    	boolean defaultChannel = false;
    	String channelName = params.channelName;
    	if( channelName == null ) 
    	{
    		/* TODO: if we're on the "default" channel, then get the user's subscribed channels and
    		 * aggregate the Entries associated with those channels
    		 */
    		
    		channelName = ConfigurationHolder.config.channel.defaultChannel;
    		defaultChannel = true;
    	}
    	
    	Channel theChannel = channelService.findByName( channelName );    
		 
		User user = null;
    	if( session.user ) 
        {
			user = userService.findUserByUserId( session.user.userId);
        }
		else
		{
			user = userService.findUserByUserId( "anonymous" );
		}    	
		
		
		// check if this is a private channel, and - if it is - if the user
		// is the owner
		if( theChannel.privateChannel )
		{
			if( user.id != theChannel.owner.id )
			{
				flash.message = "Not authorized to view Channel: [${theChannel.name}]";
				redirect( controller:'home', action:'index' );
			}
		}		
		
		
		// select count Entries for this channel
		int dataSize = 0;
		if( user != null ) {
			
			dataSize = entryService.getCountNonHiddenEntriesForUser( theChannel, user );
		}
		else 
		{
			dataSize = entryService.getCountAllEntries( theChannel );
			
		}

		int pages = dataSize / itemsPerPage;
		pages = Math.max( pages, 1 );
		
		if( dataSize > (pages*itemsPerPage) )
		{
			pages += 1;
		}
		availablePages = pages;
    	
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
    	
		List<Entry> entries = null;
		if( user != null ) 
		{
			entries = entryService.getHotEntriesForUser(theChannel, user, itemsPerPage, ( pageNumber * itemsPerPage ) - itemsPerPage );
		}
		else 
		{
			entries = entryService.getHotEntries( theChannel, itemsPerPage, ( pageNumber * itemsPerPage ) - itemsPerPage );
		}
		
    	
    	def model = [allEntries: entries, 
    	             channelName: ( defaultChannel ? null : channelName ), currentPageNumber: pageNumber, availablePages: availablePages,
    	             requestType:"hotEntries"];
		
    	render(view:"index", model:model);
    }	
	
	
    def newEntries = 
    { 
    	log.debug( "newEntries" );
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
    	
        // lookup the Entries and store them in a variable to be rendered by
        // the GSP page
            
    	// see what channel we're looking at
    	boolean defaultChannel = false;
    	String channelName = params.channelName;
    	if( channelName == null ) 
    	{
    		/* TODO: if we're on the "default" channel, then get the user's subscribed channels and
    		 * aggregate the Entries associated with those channels
    		 */
    		
    		channelName = ConfigurationHolder.config.channel.defaultChannel;
    		defaultChannel = true;
    	}
    	
    	Channel theChannel = channelService.findByName( channelName );
    	
		User user = null;
    	if( session.user ) 
        {
			user = userService.findUserByUserId( session.user.userId);
        }
		else
		{
			user = userService.findUserByUserId( "anonymous" );
		}
		
			
		// check if this is a private channel, and - if it is - if the user
		// is the owner
		if( theChannel.privateChannel )
		{
			if( user.id != theChannel.owner.id )
			{
				flash.message = "Not authorized to view Channel: [${theChannel.name}]";
				redirect( controller:'home', action:'index' )
			}
		}	
		
		
		// select count Entries for this channel
		int dataSize = 0;
		if( user != null ) {
			
			dataSize = entryService.getCountNonHiddenEntriesForUser( theChannel, user );
		}
		else
		{
			dataSize = entryService.getCountAllEntries( theChannel );
			
		}
		
		int pages = dataSize / itemsPerPage;
		pages = Math.max( pages, 1 );
		
		if( dataSize > (pages*itemsPerPage) )
		{
			pages += 1;
		}
		availablePages = pages;
    	
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
    	
		List<Entry> entries = null;
		if( user != null ) 
		{
			entries = entryService.getAllNonHiddenEntriesForUser(theChannel, user, itemsPerPage, ( pageNumber * itemsPerPage ) - itemsPerPage );
		}
		else 
		{
			entries = entryService.getAllEntries( theChannel, itemsPerPage, ( pageNumber * itemsPerPage ) - itemsPerPage );
		}
		    	
        def model = [allEntries: entries, 
                     channelName: ( defaultChannel ? null : channelName ), currentPageNumber: pageNumber, availablePages: availablePages,
                     requestType:"newEntries"];
    	
        render(view:"index", model:model);        
    }	
	
	
	def topEntries = 
    {       
    	log.debug( "topEntries" );
    	
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
    	
        // lookup the Entries and store them in a variable to be rendered by
        // the GSP page
            
    	// see what channel we're looking at
    	boolean defaultChannel = false;
    	String channelName = params.channelName;
    	if( channelName == null ) 
    	{
    		/* TODO: if we're on the "default" channel, then get the user's subscribed channels and
    		 * aggregate the Entries associated with those channels
    		 */
    		
    		channelName = ConfigurationHolder.config.channel.defaultChannel;
    		defaultChannel = true;
    	}
    	
    	Channel theChannel = channelService.findByName( channelName );
    	
		User user = null;
		if( session.user ) 
        {
			user = userService.findUserByUserId( session.user.userId);
        }
		else
		{
			user = userService.findUserByUserId( "anonymous" );
		}
		
				
		// check if this is a private channel, and - if it is - if the user
		// is the owner
		if( theChannel.privateChannel )
		{
			if( user.id != theChannel.owner.id )
			{
				flash.message = "Not authorized to view Channel: [${theChannel.name}]";
				redirect( controller:'home', action:'index' )
			}
		}

		
		// select count Entries for this channel
		int dataSize = 0;
		if( user != null ) {
			
			dataSize = entryService.getCountNonHiddenEntriesForUser( theChannel, user );
		}
		else
		{
			dataSize = entryService.getCountAllEntries( theChannel );
			
		}		
				
    	int pages = dataSize / itemsPerPage;
		pages = Math.max( pages, 1 );
		
		if( dataSize > (pages*itemsPerPage) )
		{
			pages += 1;
		}
		availablePages = pages;
    	
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
		
    	
		List<Entry> entries = null;
		if( user != null ) 
		{
			entries = entryService.getTopEntriesForUser(theChannel, user, itemsPerPage, ( pageNumber * itemsPerPage ) - itemsPerPage );
		}
		else 
		{
			entries = entryService.getTopEntries( theChannel, itemsPerPage, ( pageNumber * itemsPerPage ) - itemsPerPage );
		}
    	
        
    	def model = [allEntries: entries, 
                     channelName: ( defaultChannel ? null : channelName ), currentPageNumber: pageNumber, availablePages: availablePages,
                     requestType:"topEntries"];
        
        render(view:"index", model:model);
  
    }
    	
	def controversialEntries =
	{
		log.debug( "controversialEntries" );
		
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
		
		// lookup the Entries and store them in a variable to be rendered by
		// the GSP page
			
		// see what channel we're looking at
		boolean defaultChannel = false;
		String channelName = params.channelName;
		if( channelName == null )
		{
			/* TODO: if we're on the "default" channel, then get the user's subscribed channels and
			 * aggregate the Entries associated with those channels
			 */
			
			channelName = ConfigurationHolder.config.channel.defaultChannel;
			defaultChannel = true;
		}
		
		Channel theChannel = channelService.findByName( channelName );
		
		User user = null;
		if( session.user )
		{
			user = userService.findUserByUserId( session.user.userId);
		}
		else
		{
			user = userService.findUserByUserId( "anonymous" );
		}

		
		
		// check if this is a private channel, and - if it is - if the user
		// is the owner
		if( theChannel.privateChannel )
		{
			if( user.id != theChannel.owner.id )
			{
				flash.message = "Not authorized to view Channel: [${theChannel.name}]";
				redirect( controller:'home', action:'index' )
			}
		}
		
				
		// select count Entries for this channel
		int dataSize = 0;
		if( user != null ) {
			
			dataSize = entryService.getCountNonHiddenEntriesForUser( theChannel, user );
		}
		else
		{
			dataSize = entryService.getCountAllEntries( theChannel );
			
		}	
		
		
		int pages = dataSize / itemsPerPage;
		pages = Math.max( pages, 1 );
		
		if( dataSize > (pages*itemsPerPage) )
		{
			pages += 1;
		}
		availablePages = pages;
		
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
		
		
		List<Entry> entries = null;
		if( user != null ) 
		{
			entries = entryService.getControversialEntriesForUser(theChannel, user, itemsPerPage, ( pageNumber * itemsPerPage ) - itemsPerPage );
		}
		else 
		{
			entries = entryService.getControversialEntries( theChannel, itemsPerPage, ( pageNumber * itemsPerPage ) - itemsPerPage );
		}

				
		def model = [allEntries: entries,
					 channelName: ( defaultChannel ? null : channelName ), currentPageNumber: pageNumber, availablePages: availablePages,
					 requestType:"controversialEntries"];

			 
		 render(view:"index", model:model);
		
	}
	
	
    def savedEntries = 
    {	
    	log.debug( "savedEntries" );
    	
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
    	
    	
    	def savedEntries = null;
    	if( session.user ) 
    	{
    		User user = userService.findUserByUserId( session.user.userId);
    		savedEntries = entryService.getSavedEntriesForUser( user );
    	}	
    	else
    	{
    		savedEntries = new ArrayList();
    	}
    	
    	int dataSize = savedEntries.size();
    	log.debug( "dataSize: ${dataSize}" );
    	int pages = dataSize / itemsPerPage;
		log.debug( "dataSize / itemsPerPage = ${pages}" );
    	pages = Math.max( pages, 1 );
		
		log.debug( "pages: ${pages}" );
		
		if( dataSize > (pages*itemsPerPage) )
		{
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
			if( dataSize >= 1 )
			{
				endIndex = Math.min( dataSize -1, endIndex);
			}
		}        	
    	
		List<Entry> subList = null;
		if( dataSize > 0 )
		{
			subList = savedEntries[ beginIndex .. endIndex ];
		}
		else
		{
			subList = new ArrayList<Entry>();	
		}
		
    	def model = [allEntries:subList, currentPageNumber: pageNumber, availablePages: availablePages,
                     requestType:"savedEntries"];
    	    	
    	render(view:"index", model:model);
    }
   
    
    def hiddenEntries = 
    {
    	log.debug( "hiddenEntries" );

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
    	
    	
    	def hiddenEntries = null;
    	
    	if( session.user ) 
    	{   
    		User user = userService.findUserByUserId( session.user.userId);
    		hiddenEntries = entryService.getHiddenEntriesForUser( user );
    		
    	}
    	else
    	{
			log.debug( "not logged in" );
    		hiddenEntries = new ArrayList();
    	}
   
    	
    	int dataSize = hiddenEntries.size();
    	log.debug( "dataSize: ${dataSize}" );
    	int pages = dataSize / itemsPerPage;
		log.debug( "dataSize / itemsPerPage = ${pages}" );
    	pages = Math.max( pages, 1 );
		
		log.debug( "pages: ${pages}");
		
		if( dataSize > (pages*itemsPerPage))
		{
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
    	
    	List<Entry> subList = null;
		if( dataSize > 0 )
		{
			subList = hiddenEntries[ beginIndex .. endIndex ];
		}
		else
		{
			subList = new ArrayList<Entry>();	
		}
    	
		def model = [allEntries:hiddenEntries, currentPageNumber: pageNumber, availablePages: availablePages,
                     requestType:"hiddenEntries"];
    	
    	render(view:"index", model:model);
    
    }
     
    /* TODO: limit returned results here */
	/* TODO: require authentication for RSS, so we can enforce private channels here */
    def renderRss = 
    {
    	log.debug( "renderRss" );
    	
    	// see what channel we're looking at
    	String channelName = params.channelName;
    	if( channelName == null ) 
    	{
    		channelName = ConfigurationHolder.config.channel.defaultChannel;
    	}
    	
    	Channel theChannel = channelService.findByName( channelName );    
    	log.debug( "rendering rss for channel: ${channelName}" );
    	
    	/* 
         * filter out the hidden entries for the current user, at least
         * */  
         def entries = null
         
         switch( params.name ) {
         	
         	case "newEntries":
         		log.debug( "rendering Rss for newEntries" );
         		entries = entryService.getNewEntries(theChannel);
         		break;
         	case "hotEntries":
         		log.debug( "rendering Rss for hotEntries" );
         		entries = entryService.getHotEntries(theChannel);
         		break;
         	case "topEntries":
         		log.debug( "rendering Rss for topEntries" );
         		entries = entryService.getTopEntries(theChannel);
         		break;
         	case "controversialEntries":
         		log.debug( "rendering Rss for controversialEntries" );
         		entries = entryService.getControversialEntries(theChannel);
         		break;
         	default:
         		// TODO: make one of the listings the "default" and return it 
         		// in this case???
         		entries = new ArrayList();
         		break;
         }

        
        def sortedEntries = entries.sort { it.dateCreated }.reverse();
        
        def siteBaseUrl = siteConfigService.getSiteConfigEntry( "baseUrl" );
        render(feedType:"rss", feedVersion:"2.0") {
            title = "${channelName}: ${params.name} Feed"
            link = "${siteBaseUrl}/r/${channelName}/${params.name}/"
            description = "Feed for ${channelName}: ${params.name} entries"

            sortedEntries.each() { 
            	article -> entry(article.title) 
            	{ 
            		link = article.url;
            		publishedDate = article.dateCreated;
            		author = article.submitter.fullName;
            		"Content"
            	}
        	} 
        }
    }
}
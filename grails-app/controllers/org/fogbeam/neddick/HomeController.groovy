package org.fogbeam.neddick

import java.util.SortedSet;

import org.codehaus.groovy.grails.commons.ConfigurationHolder


class HomeController {

	def userService;
	def entryService;
	def entryCacheService;
	def channelService;
	def siteConfigService;	
	
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
    	println "index";
    	
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
    	
    	EntryCacheObject entryCache = null;
    	if( session.user ) 
        {
			// lookup the entry cache before pulling in the "live" object for
			// the hibernate session.  The entry cache is keyed on the java object
			// id, so we need to do the lookup using the same object that we stoerd
			// in the session when we built the cache originally.
			entryCache = entryCacheService.getEntryCache( session.user );
			User user = userService.findUserByUserId( session.user.userId);
        	
			 // allEntries = entryService.getAllEntriesForUser( theChannel, user ); 
        	 
        }
    	else 
    	{
    		// use the entryCacheService here...
    		entryCache = entryCacheService.getEntryCache();
    	}
    	
    	List<String> sortedSet = entryCache.getByCreatedDate();
    	List<String> filteredByChannel = new ArrayList<String>();
		sortedSet.each {
			
			println "checking entry: ${it}"
			Entry e = entryCache.getEntry(it); 
			println "found entry ${e}";
			if( e.channel.id == theChannel.id )
			{
				println "adding Entry with uuid: ${it} to filteredByChannel";
				filteredByChannel.add( it );
			}
		}
		
		int dataSize = filteredByChannel.size();
    	int pages = dataSize / itemsPerPage;
		pages = Math.max( pages, 1 );
		
		if( dataSize > (pages*itemsPerPage) )
		{
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
    	

    	List<Entry> entries = new ArrayList<Entry>();
    	if( filteredByChannel.size > 0 )
    	{
			// build the collection of actual Entry objects from the UUID page...
	    	String[] entryUuids = filteredByChannel.toArray();
	    	String[] page = entryUuids[beginIndex..endIndex];
	    	 
	    	// entries = entryService.getAllEntries(theChannel); 
	
	    	for( String uuid :page )
	    	{
	    		println "Getting entry with uuid: ${uuid} from entryCache";
	    		Entry entry = entryCache.getEntry(uuid);
	    		
	    		println "adding Entry ${entry} to entries list to render";
	    		
	    		entries.add( entry );
	    	}
    	}
        
    	// def sortedEntries = entries.sort { it.dateCreated }.reverse();
        def model = [allEntries: entries, 
                     channelName: ( defaultChannel ? null : channelName ), currentPageNumber: pageNumber, availablePages: availablePages,
                     requestType:"index"];

        render(view:"index", model:model);
    		
    }

    def index2 = {
    		
    }
    
    def hotEntries = 
    {
    	println "hotEntries";	
    	
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
		 
    	EntryCacheObject entryCache = null;
    	if( session.user ) 
        {
			// lookup the entry cache before pulling in the "live" object for
			// the hibernate session.  The entry cache is keyed on the java object
			// id, so we need to do the lookup using the same object that we stoerd
			// in the session when we built the cache originally.
			entryCache = entryCacheService.getEntryCache( session.user  );
			User user = userService.findUserByUserId( session.user.userId);
        }
    	else
    	{
    		// use the entryCacheService here...
    		entryCache = entryCacheService.getEntryCache();
    	}
    	
    	List<String> sortedSet = entryCache.getByHotness();
    	List<String> filteredByChannel = new ArrayList<String>();
		sortedSet.each {
			
			println "checking entry: ${it}"
			Entry e = entryCache.getEntry(it); 
			println "found entry ${e}";
			if( e.channel.id == theChannel.id )
			{
				println "adding Entry with uuid: ${it} to filteredByChannel";
				filteredByChannel.add( it );
			}
		}
		
    	int dataSize = filteredByChannel.size();
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
		
    	
    	// get the requested page of entry UUIDs
    	int beginIndex = ( pageNumber * itemsPerPage ) - itemsPerPage;
    	int endIndex = Math.min( dataSize -1, ((pageNumber * itemsPerPage ) - 1));
    	
		if( pageNumber == pages )
		{
			endIndex = Math.min( dataSize -1, endIndex);
		}        	
    	
		
		List<Entry> hotEntries = new ArrayList<Entry>();
    	if( filteredByChannel.size > 0 )
    	{
    		// build the collection of actual Entry objects from the UUID page...
    		String[] entryUuids = filteredByChannel.toArray();
    		String[] page = entryUuids[beginIndex..endIndex];    	
    	
    		for( String uuid :page )
    		{
    			Entry entry = entryCache.getEntry(uuid);
    			hotEntries.add( entry );
    		}
    	}
    	
    	def model = [allEntries: hotEntries, 
    	             channelName: ( defaultChannel ? null : channelName ), currentPageNumber: pageNumber, availablePages: availablePages,
    	             requestType:"hotEntries"];
		
    	render(view:"index", model:model);
    }
    
    def newEntries = 
    { 
    	println "newEntries";
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
    	
    	EntryCacheObject entryCache = null;
    	if( session.user ) 
        {
			// lookup the entry cache before pulling in the "live" object for
			// the hibernate session.  The entry cache is keyed on the java object
			// id, so we need to do the lookup using the same object that we stoerd
			// in the session when we built the cache originally.
			entryCache = entryCacheService.getEntryCache( session.user  );
			User user = userService.findUserByUserId( session.user.userId);
        }
    	else
    	{
    		// use the entryCacheService here...
    		entryCache = entryCacheService.getEntryCache();
    	}
    	
    	List<String> sortedSet = entryCache.getByCreatedDate();
    	List<String> filteredByChannel = new ArrayList<String>();
		sortedSet.each {
			
			println "checking entry: ${it}"
			Entry e = entryCache.getEntry(it); 
			println "found entry ${e}";
			if( e.channel.id == theChannel.id )
			{
				println "adding Entry with uuid: ${it} to filteredByChannel";
				filteredByChannel.add( it );
			}
		}
    	
    	int dataSize = filteredByChannel.size();
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
		
		
    	// get the requested page of entry UUIDs
    	int beginIndex = ( pageNumber * itemsPerPage ) - itemsPerPage;
    	int endIndex = Math.min( dataSize -1, ((pageNumber * itemsPerPage ) - 1));
    	
		if( pageNumber == pages )
		{
			endIndex = Math.min( dataSize -1, endIndex);
		}        	
    	
		List<Entry> entries = new ArrayList<Entry>();
    	if( filteredByChannel.size > 0 )
    	{
    
    		// build the collection of actual Entry objects from the UUID page...
    		String[] entryUuids = filteredByChannel.toArray();
    		String[] page = entryUuids[beginIndex..endIndex]; 
    		
    		for( String uuid :page )
    		{
    			Entry entry = entryCache.getEntry(uuid);
    			entries.add( entry );
    		}
    	}
    	
        def model = [allEntries: entries, 
                     channelName: ( defaultChannel ? null : channelName ), currentPageNumber: pageNumber, availablePages: availablePages,
                     requestType:"newEntries"];
    	
        render(view:"index", model:model);        
    }
    
    def topEntries = 
    {       
    	println "topEntries";
    	
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
    	
    	EntryCacheObject entryCache = null;
    	if( session.user ) 
        {
			// lookup the entry cache before pulling in the "live" object for
			// the hibernate session.  The entry cache is keyed on the java object
			// id, so we need to do the lookup using the same object that we stoerd
			// in the session when we built the cache originally.
			entryCache = entryCacheService.getEntryCache( session.user  );
			User user = userService.findUserByUserId( session.user.userId);
        }
    	else
    	{
    		// use the entryCacheService here...
    		entryCache = entryCacheService.getEntryCache();
    	}
    	
    	List<String> sortedSet = entryCache.getByScore();
    	List<String> filteredByChannel = new ArrayList<String>();
		sortedSet.each {
			
			println "checking entry: ${it}"
			Entry e = entryCache.getEntry(it); 
			println "found entry ${e}";
			if( e.channel.id == theChannel.id )
			{
				println "adding Entry with uuid: ${it} to filteredByChannel";
				filteredByChannel.add( it );
			}
		}
    	
    	int dataSize = filteredByChannel.size();
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
		
    	
    	// get the requested page of entry UUIDs
    	int beginIndex = ( pageNumber * itemsPerPage ) - itemsPerPage;
    	int endIndex = Math.min( dataSize -1, ((pageNumber * itemsPerPage ) - 1));
    	
		if( pageNumber == pages )
		{
			endIndex = Math.min( dataSize -1, endIndex);
		}        	
    	
		List<Entry> topEntries = new ArrayList<Entry>();
    	if( filteredByChannel.size > 0 )
    	{
    		// build the collection of actual Entry objects from the UUID page...
    		String[] entryUuids = filteredByChannel.toArray();
    		String[] page = entryUuids[beginIndex..endIndex];
    	 
    		// entries = entryService.getAllEntries(theChannel); 
    		for( String uuid :page )
    		{
    			Entry entry = entryCache.getEntry(uuid);
    			topEntries.add( entry );
    		}
    	}
        
    	def model = [allEntries: topEntries, 
                     channelName: ( defaultChannel ? null : channelName ), currentPageNumber: pageNumber, availablePages: availablePages,
                     requestType:"topEntries"];
        
        render(view:"index", model:model);
  
    }
    
    def savedEntries = 
    {	
    	println "savedEntries";
    	
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
    	println "dataSize: ${dataSize}";
    	int pages = dataSize / itemsPerPage;
		println "dataSize / itemsPerPage = ${pages}"
    	pages = Math.max( pages, 1 );
		
		println "pages: ${pages}";
		
		if( dataSize > (pages*itemsPerPage) )
		{
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
    	
    	println "model: ${model}";
    	
    	render(view:"index", model:model);
    }

    def controversialEntries = 
    { 
    	println "controversialEntries";
    	
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
    	
    	EntryCacheObject entryCache = null;
    	if( session.user ) 
        {
			// lookup the entry cache before pulling in the "live" object for
			// the hibernate session.  The entry cache is keyed on the java object
			// id, so we need to do the lookup using the same object that we stoerd
			// in the session when we built the cache originally.
			entryCache = entryCacheService.getEntryCache( session.user  );
			User user = userService.findUserByUserId( session.user.userId);
        }
    	else
    	{
    		// use the entryCacheService here...
    		entryCache = entryCacheService.getEntryCache();
    	}
    	
    	List<String> sortedSet = entryCache.getByControversy();
    	List<String> filteredByChannel = new ArrayList<String>();
		sortedSet.each {
			
			println "checking entry: ${it}"
			Entry e = entryCache.getEntry(it); 
			println "found entry ${e}";
			if( e.channel.id == theChannel.id )
			{
				println "adding Entry with uuid: ${it} to filteredByChannel";
				filteredByChannel.add( it );
			}
		}    	
    	
    	int dataSize = filteredByChannel.size();
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
		
    	
    	// get the requested page of entry UUIDs
    	int beginIndex = ( pageNumber * itemsPerPage ) - itemsPerPage;
    	int endIndex = Math.min( dataSize -1, ((pageNumber * itemsPerPage ) - 1));
    	
		if( pageNumber == pages )
		{
			endIndex = Math.min( dataSize -1, endIndex);
		}        	
    	
		List<Entry> controversialEntries = new ArrayList<Entry>();
    	if( filteredByChannel.size > 0 )
    	{
    		// build the collection of actual Entry objects from the UUID page...
    		String[] entryUuids = filteredByChannel.toArray();
    		String[] page = entryUuids[beginIndex..endIndex];
    	 
    		// entries = entryService.getAllEntries(theChannel); 
    		for( String uuid :page )
    		{
    			Entry entry = entryCache.getEntry(uuid);
    			controversialEntries.add( entry );
    		}
    	}
    	
        // def sortedEntries = entries.sort { it.dateCreated }.reverse();
        def model = [allEntries: controversialEntries, 
                     channelName: ( defaultChannel ? null : channelName ), currentPageNumber: pageNumber, availablePages: availablePages,
                     requestType:"controversialEntries"];

             
         render(view:"index", model:model);
		
    }    
    
    def hiddenEntries = 
    {
    	println "hiddenEntries";

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
    	
    	
    	def hiddenEntries = null;
    	
    	if( session.user ) 
    	{   
    		User user = userService.findUserByUserId( session.user.userId);
    		hiddenEntries = entryService.getHiddenEntriesForUser( user );
    		
    	}
    	else
    	{
    		println "not logged in";
    		hiddenEntries = new ArrayList();
    	}
   
    	
    	int dataSize = hiddenEntries.size();
    	println "dataSize: ${dataSize}";
    	int pages = dataSize / itemsPerPage;
		println "dataSize / itemsPerPage = ${pages}"
    	pages = Math.max( pages, 1 );
		
		println "pages: ${pages}";
		
		if( dataSize > (pages*itemsPerPage))
		{
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
    def renderRss = 
    {
    	println "renderRss";
    	
    	// see what channel we're looking at
    	String channelName = params.channelName;
    	if( channelName == null ) 
    	{
    		channelName = ConfigurationHolder.config.channel.defaultChannel;
    	}
    	
    	Channel theChannel = channelService.findByName( channelName );    
    	println "rendering rss for channel: ${channelName}";
    	
    	/* 
         * filter out the hidden entries for the current user, at least
         * */  
         def entries = null
         
         switch( params.name ) {
         	
         	case "newEntries":
         		println "rendering Rss for newEntries";
         		entries = entryService.getNewEntries(theChannel);
         		break;
         	case "hotEntries":
         		println "rendering Rss for hotEntries";
         		entries = entryService.getHotEntries(theChannel);
         		break;
         	case "topEntries":
         		println "rendering Rss for topEntries";
         		entries = entryService.getTopEntries(theChannel);
         		break;
         	case "controversialEntries":
         		println "rendering Rss for controversialEntries";
         		entries = entryService.getControversialEntries(theChannel);
         		break;
         	default:
         		// TOODO: make one of the listings the "default" and return it 
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
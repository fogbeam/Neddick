package org.fogbeam.neddick

import org.apache.jena.riot.Lang
import org.apache.jena.riot.RDFDataMgr
import org.fogbeam.neddick.controller.mixins.SidebarPopulatorMixin
import org.fogbeam.neddick.filters.BaseFilter

import grails.plugin.springsecurity.annotation.Secured

import org.apache.jena.query.Dataset
import org.apache.jena.query.DatasetFactory
import org.apache.jena.query.Query
import org.apache.jena.query.QueryExecution
import org.apache.jena.query.QueryExecutionFactory
import org.apache.jena.query.QueryFactory
import org.apache.jena.query.QuerySolution
import org.apache.jena.query.ResultSet
import org.apache.jena.rdf.model.InfModel
import org.apache.jena.rdf.model.Literal
import org.apache.jena.rdf.model.Model
import org.apache.jena.rdf.model.ModelFactory
import org.apache.jena.rdf.model.Statement
import org.apache.jena.rdf.model.StmtIterator
import org.apache.jena.reasoner.Reasoner
import org.apache.jena.reasoner.ReasonerRegistry
import org.apache.jena.vocabulary.ReasonerVocabulary


@Mixin(SidebarPopulatorMixin)
class HomeController
{

	def userService;
	def entryService;
	def entryCacheService;
	def channelService;
	def siteConfigService;	
	def filterService;
	def tagService;
	def restTemplate;
	
	int itemsPerPage = -1;
		
	@Secured(["ROLE_USER", "ROLE_ADMIN"])
	def index()
	{
		log.debug( "index" );
		log.info( "index() called" );
		
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
			channelName = grailsApplication.config.channel.defaultChannel;
			defaultChannel = true;
		}
		log.info( "Looking up Channel named: ${channelName}" );
		Channel theChannel = channelService.findByName( channelName );
		
		log.debug "theChannel: ${theChannel.name}";
		
		User user = userService.getLoggedInUser();
		
		
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
		
			
		Map sidebarCollections = populateSidebarCollections( this, user );
			
				
		// check if there is a Filter for this channel, for this User
		// if there is, only render the entries from the Filter
		BaseFilter filter = filterService.findFilterByUserAndChannel( user, theChannel );
		
		if( filter )
		{
			log.debug "there is a filter in effect for this channel";
			
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
						 channelName: channelName, currentPageNumber: pageNumber, 
						 availablePages: availablePages,
						 theChannel:theChannel,
						 requestType:"index"];
	
			model.putAll( sidebarCollections );
			render(view:"index", model:model);
						
		}
		else
		{
			log.debug "no filter for this channel";
			
			// select count Entries for this channel
			int dataSize = 0;
			if( user != null ) {
				
				dataSize = entryService.getCountNonHiddenEntriesForUser( theChannel, user );
			}
			else 
			{
				dataSize = entryService.getCountAllEntries( theChannel );
				
			}
	
			log.debug "dataSize: ${dataSize}";
					
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
			
			log.debug "returning ${entries.size()} entries";
			
			List quoddyUserNames = new ArrayList();

			if( grailsApplication.config.interface.foaf.enabled != null && 
                              !grailsApplication.config.interface.foaf.enabled.isEmpty() &&
                              Boolean.parseBoolean( grailsApplication.config.interface.foaf.enabled ) )
			{

			Object quoddyFoafUrl = grailsApplication.config.urls.quoddy.foaf.endpoint;
			
			if( quoddyFoafUrl )
			{
				log.debug "quoddyFoafUrl: ${quoddyFoafUrl}";
				
				String foafResponse = restTemplate.getForObject( quoddyFoafUrl, String.class );
				// log.debug "foafResponse:\n\n${foafResponse}";
				Dataset dataset = DatasetFactory.createMem();
				Model foafModel = dataset.getDefaultModel();
				StringReader foafReader = new StringReader(foafResponse);
				RDFDataMgr.read(foafModel, foafReader, "", Lang.RDFXML );
				
				
				Reasoner reasoner = ReasonerRegistry.getRDFSReasoner();
				reasoner.setParameter(ReasonerVocabulary.PROPsetRDFSLevel,
						ReasonerVocabulary.RDFS_DEFAULT);
				
				InfModel infmodel = ModelFactory.createInfModel(reasoner, foafModel );
				
				/* Do a SPARQL Query over the data in the model */
				String queryString = "SELECT ?accountName " + 
				" WHERE { ?x  <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://xmlns.com/foaf/0.1/Person> . " +  
				"        ?x <http://xmlns.com/foaf/0.1/account> ?account . " + 
				"         ?account <http://xmlns.com/foaf/0.1/accountName> ?accountName . }" ;
		
				/* Now create and execute the query using a Query object */
				Query query = QueryFactory.create(queryString) ;
				QueryExecution qexec = QueryExecutionFactory.create(query, infmodel) ;
				
				ResultSet rs = qexec.execSelect();
				
				while( rs.hasNext())
				{
					QuerySolution soln = rs.next();
					Literal l = soln.getLiteral( "accountName" );
					String accountName = l.getString();
					quoddyUserNames.add( accountName );
				}
			
			}
			}


			def sortedEntries = entries.sort { it.dateCreated }.reverse();
			def model = [allEntries: sortedEntries,
						 channelName: channelName, currentPageNumber: pageNumber, 
						 availablePages: availablePages,
						 theChannel:theChannel,
						 requestType:"index",
						 quoddyUserNames:quoddyUserNames];
	
			model.putAll( sidebarCollections );		 
					 
			render(view:"index", model:model);
		}
	}

	@Secured(["ROLE_USER", "ROLE_ADMIN"])
    def index2()
	{
    		
    }

	@Secured(["ROLE_USER", "ROLE_ADMIN"])
    def hotEntries()
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
    		
    		channelName = grailsApplication.config.channel.defaultChannel;
    		defaultChannel = true;
    	}
    	
    	Channel theChannel = channelService.findByName( channelName );    
		 
		User user = userService.getLoggedInUser();
		
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
		
		Map sidebarCollections = populateSidebarCollections( this, user );
		
		
		// check if there is a Filter for this channel, for this User
		// if there is, only render the entries from the Filter
		BaseFilter filter = filterService.findFilterByUserAndChannel( user, theChannel );
		
		if( filter )
		{
			/* there's a filter in place, which may have some entries associated with it.  We've been asked
			 * for "hot" entries.  So we need to request the entries from the filter, but sorted by hotness.  
			 */
			
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

			
			log.debug( "calling getHotNonHiddenEntriesForFilter" );
			List<Entry> entries;
			entries = filterService.getHotNonHiddenEntriesForFilter(filter, itemsPerPage, ( pageNumber * itemsPerPage ) - itemsPerPage );
			
			
			def model = [allEntries: entries,
						 channelName: channelName, currentPageNumber: pageNumber, availablePages: availablePages,
						 requestType:"index"];
	
			model.putAll( sidebarCollections );		 
					 
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
		
			model.putAll( sidebarCollections );	 
				 
			render(view:"index", model:model);
		}	
    }
	
	@Secured(["ROLE_USER", "ROLE_ADMIN"])
    def newEntries() 
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
    		
    		channelName = grailsApplication.config.channel.defaultChannel;
    		defaultChannel = true;
    	}
    	
    	Channel theChannel = channelService.findByName( channelName );
    	
		User user = userService.getLoggedInUser();
			
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
		
		
		Map sidebarCollections = populateSidebarCollections( this, user );
		
		// check if there is a Filter for this channel, for this User
		// if there is, only render the entries from the Filter
		BaseFilter filter = filterService.findFilterByUserAndChannel( user, theChannel );
		
		if( filter )
		{
			/* there's a filter in place, which may have some entries associated with it.  We've been asked
			 * for "hot" entries.  So we need to request the entries from the filter, but sorted by hotness.
			 */
			
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
			
			
			// def sortedEntries = entries.sort { it.dateCreated }.reverse();
			def model = [allEntries: entries,
						 channelName: channelName, currentPageNumber: pageNumber, availablePages: availablePages,
						 requestType:"index"];
	
			model.putAll( sidebarCollections );		 
					 
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
	    	
			model.putAll( sidebarCollections );		 
					 
	        render(view:"index", model:model);      
    	}  
    }	
	
	@Secured(["ROLE_USER", "ROLE_ADMIN"])
	def topEntries() 
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
    		
    		channelName = grailsApplication.config.channel.defaultChannel;
    		defaultChannel = true;
    	}
    	
    	Channel theChannel = channelService.findByName( channelName );
    	
		User user = userService.getLoggedInUser();	
				
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

		Map sidebarCollections = populateSidebarCollections( this, user );
		
		// check if there is a Filter for this channel, for this User
		// if there is, only render the entries from the Filter
		BaseFilter filter = filterService.findFilterByUserAndChannel( user, theChannel );
		
		if( filter )
		{
			/* there's a filter in place, which may have some entries associated with it.  We've been asked
			 * for "hot" entries.  So we need to request the entries from the filter, but sorted by hotness.
			 */
			
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

			
			log.debug( "calling getTopNonHiddenEntriesForFilter" );
			List<Entry> entries;
			entries = filterService.getTopNonHiddenEntriesForFilter(filter, itemsPerPage, ( pageNumber * itemsPerPage ) - itemsPerPage );
			
			
			// def sortedEntries = entries.sort { it.dateCreated }.reverse();
			def model = [allEntries: entries,
						 channelName: channelName, currentPageNumber: pageNumber, availablePages: availablePages,
						 requestType:"index"];
	
			model.putAll( sidebarCollections );
					 		 
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
	        
			model.putAll( sidebarCollections );		 
					 
	        render(view:"index", model:model);
    	}  
    }
	@Secured(["ROLE_USER", "ROLE_ADMIN"])
	def controversialEntries()
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
			
			channelName = grailsApplication.config.channel.defaultChannel;
			defaultChannel = true;
		}
		
		Channel theChannel = channelService.findByName( channelName );
		
		User user = userService.getLoggedInUser();
		
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
		
		
		Map sidebarCollections = populateSidebarCollections( this, user );
		
		// check if there is a Filter for this channel, for this User
		// if there is, only render the entries from the Filter
		BaseFilter filter = filterService.findFilterByUserAndChannel( user, theChannel );
		
		if( filter )
		{
			/* there's a filter in place, which may have some entries associated with it.  We've been asked
			 * for "hot" entries.  So we need to request the entries from the filter, but sorted by hotness.
			 */
			
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

			
			log.debug( "calling getTopNonHiddenEntriesForFilter" );
			List<Entry> entries;
			entries = filterService.getControversialNonHiddenEntriesForFilter(filter, itemsPerPage, ( pageNumber * itemsPerPage ) - itemsPerPage );
			
			
			def sortedEntries = entries.sort { it.dateCreated }.reverse();
			
			model.putAll( sidebarCollections );
			
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
	
			
			model.putAll( sidebarCollections );		 
					 	 
			render(view:"index", model:model);
		}	
	}
	
	@Secured(["ROLE_USER", "ROLE_ADMIN"])
    def savedEntries()
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
		User user = userService.getLoggedInUser();
    	savedEntries = entryService.getSavedEntriesForUser( user );
    	
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
		
		Map sidebarCollections = populateSidebarCollections( this, user );
		
    	def model = [allEntries:subList, currentPageNumber: pageNumber, availablePages: availablePages,
                     requestType:"savedEntries"];
    	 
		model.putAll( sidebarCollections );		 
				    	
    	render(view:"savedEntries", model:model);
    }
   
	@Secured(["ROLE_USER", "ROLE_ADMIN"])
    def hiddenEntries()
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
    	
    	
		User user = userService.getLoggedInUser();
    	def hiddenEntries = entryService.getHiddenEntriesForUser( user );
 
    	
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
    	
		Map sidebarCollections = populateSidebarCollections( this, user );
		
		def model = [allEntries:hiddenEntries, currentPageNumber: pageNumber, availablePages: availablePages,
                     requestType:"hiddenEntries"];
    	
		model.putAll( sidebarCollections );
				 
    	render(view:"hiddenEntries", model:model);
    
    }
     
    /* TODO: limit returned results here */
	/* TODO: require authentication for RSS, so we can enforce private channels here */

	@Secured(["ROLE_USER", "ROLE_ADMIN"])
	def renderRss()
    {
    	log.debug( "renderRss" );
    	
    	// see what channel we're looking at
    	String channelName = params.channelName;
    	if( channelName == null ) 
    	{
    		channelName = grailsApplication.config.channel.defaultChannel;
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
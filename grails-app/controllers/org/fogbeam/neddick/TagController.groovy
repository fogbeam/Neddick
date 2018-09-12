package org.fogbeam.neddick

import org.fogbeam.neddick.controller.mixins.SidebarPopulatorMixin

import grails.plugin.springsecurity.annotation.Secured


@Mixin(SidebarPopulatorMixin)
class TagController 
{
	def tagService;
	def entryService;
	def userService;
	def siteConfigService;
	def channelService;

	int defaultItemsPerPage = 25;	
	
	@Secured(["ROLE_USER","ROLE_ADMIN"])
	def list()
	{
		String strItemsPerPage = siteConfigService.getSiteConfigEntry( "itemsPerPage" );
		
    	int itemsPerPage = defaultItemsPerPage;
		if( strItemsPerPage != null )
    	{
    		itemsPerPage = Integer.parseInt( strItemsPerPage );
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
			
			
		List<Tag> allTags = new ArrayList<Tag>();	
		allTags.addAll( tagService.getAllTags());
		
    	int dataSize = allTags.size();
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
			endIndex = Math.min( dataSize -1, endIndex);
		}        	
    	
    	List<Tag> subList = null;
		
		if( dataSize > 0 )
		{
			subList = allTags[ beginIndex .. endIndex ];		
		}
		else
		{
			subList = new ArrayList<Tag>();	
		}
		
		
		User user = userService.getLoggedInUser();
		Map sidebarCollections = populateSidebarCollections( this, user );
		
		def model = [allTags:subList, currentPageNumber: pageNumber, availablePages: availablePages ];
		
		model.putAll( sidebarCollections );
		
		return model;
	}
	
	@Secured(["ROLE_USER","ROLE_ADMIN"])
	def addTag() 
	{
		log.debug( "addTag called with tagName: ${params.tagName}, uuid: ${params.entryUuid}" );
		
		User user = userService.getLoggedInUser();
		
		String tagName = params.tagName;
		if( null != tagName )
		{
			tagName = tagName.trim().toLowerCase()
			
			Tag tag = tagService.findTagByName( tagName );
			if( tag == null )
			{
				// nobody's used this tag before, create it
				// TODO: make tagService.createTag call. Note the race condition inherent in this.
				tag = new Tag( name: tagName );
			
				// set creator
				if( !tag.save() )
				{
					log.error( "Creating tag: ${tagName} FAILED");
					// tag.errors.allErrors.each { p rintln it };
				}
				else
				{
					log.debug( "Created tag ${tagName} OK" );	
				}
			}
			else
			{
				log.debug( "Tag: ${tagName} already exists" );	
			}
		
			// lookup our entry by the uuid
			Entry entry = entryService.findById( params.entryId );
		
			// add the tag to the tags collection
			entry.addToTags( tag, user );
		
			// save
			entryService.saveEntry( entry );
		
			String newTagMessage = "ADDTAG|${entry.uuid}|${tagName}";
	    	// send a JMS message to our testQueue
			// sendJMSMessage("searchQueue", newTagMessage );
			jmsService.send( queue: 'searchQueue', newTagMessage, 'standard', null );
			
			render("done");
		}
		else
		{
			// no tag to add...
		}
	}

	@Secured(["ROLE_USER","ROLE_ADMIN"])
	def listEntriesByTag() 
	{
		/* list all the Entry's that have been tagged with a given tag. */
		def taggedEntries = new ArrayList();
		String tagName = params.tagName;
		if( null != tagName )
		{
			tagName = tagName.trim().toLowerCase();
			
			Tag tag = tagService.findTagByName(tagName);
			if( null != tag )
			{
				taggedEntries = tag.entries;
			}
		}
		
		User user = userService.getLoggedInUser();
		Map sidebarCollections = populateSidebarCollections( this, user );
		
		def model = [allEntries:taggedEntries];
		
		model.putAll( sidebarCollections );
		
		render(view:"listEntriesByTag", model:model); 
	}

	@Secured(["ROLE_USER","ROLE_ADMIN"])
	def myTags() 
	{
		log.debug( "myTags called" );	
			
		User user = userService.getLoggedInUser();
		// get a list of the distinct tags that I have used
		def tagList = tagService.getTagListForUser( user );
		
	
		log.debug( "found ${tagList.size()} tags for user ${user.userId}" );
		
		[tagList: tagList];
	}
}
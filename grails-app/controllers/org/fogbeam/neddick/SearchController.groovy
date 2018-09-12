package org.fogbeam.neddick

import org.fogbeam.neddick.controller.mixins.SidebarPopulatorMixin

import grails.plugin.springsecurity.annotation.Secured


@Mixin(SidebarPopulatorMixin)
class SearchController 
{

	def entryService;
	def siteConfigService;
	def searchService;
	def userService;
	def tagService;
	def channelService;
	def jmsService;
	
	@Secured(["ROLE_USER","ROLE_ADMIN"])
	def doSearch() 
	{		
			
		def entries = new ArrayList<Entry>();
		
		String queryString = params.queryString;
		log.debug( "searching Users, queryString: ${queryString}" );
		
		List<Entry> searchResults = searchService.doSearch( queryString );
		
		if( searchResults != null )
		{
			entries.addAll( searchResults );
		}
		
		
		User user = userService.getLoggedInUser();
		Map sidebarCollections = populateSidebarCollections( this, user );
		
		def model = [searchResults:entries];
		
		model.putAll( sidebarCollections );
		
		render( view:'displaySearchResults', model:model);
	}

	@Secured(["ROLE_USER","ROLE_ADMIN"])
	def reindexAll()
	{	
		log.debug( "SearchController: Sending reindexAll message to searchQueue" );
		// sendJMSMessage("searchQueue", "REINDEX_ALL" );
		jmsService.send( queue: 'searchQueue', "REINDEX_ALL", 'standard', null );
		render( "<h1>DONE</h1>" );
	}
}

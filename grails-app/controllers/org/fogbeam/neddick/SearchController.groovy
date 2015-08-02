package org.fogbeam.neddick

import org.fogbeam.neddick.controller.mixins.SidebarPopulatorMixin


@Mixin(SidebarPopulatorMixin)
class SearchController {

	def entryService;
	def siteConfigService;
	def searchService;
	def userService;
	def tagService;
	def channelService;
	
	def doSearch = {
			
		
		def entries = new ArrayList<Entry>();
		
		String queryString = params.queryString;
		log.debug( "searching Users, queryString: ${queryString}" );
		
		List<Entry> searchResults = searchService.doSearch( queryString );
		
		if( searchResults != null )
		{
			entries.addAll( searchResults );
		}
		
		
		User user = userService.findUserByUserId( session.user.userId );
		Map sidebarCollections = populateSidebarCollections( this, user );
		
		def model = [searchResults:entries];
		
		model.putAll( sidebarCollections );
		
		render( view:'displaySearchResults', model:model);
	}

	def reindexAll = {
		
		log.debug( "SearchController: Sending reindexAll message to searchQueue" );
		sendJMSMessage("searchQueue", "REINDEX_ALL" );		
		render( "<h1>DONE</h1>" );
	}
}

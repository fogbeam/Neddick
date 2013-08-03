package org.fogbeam.neddick

import org.apache.lucene.analysis.standard.StandardAnalyzer 
import org.apache.lucene.document.Document 
import org.apache.lucene.queryParser.MultiFieldQueryParser 
import org.apache.lucene.queryParser.QueryParser 
import org.apache.lucene.search.IndexSearcher 
import org.apache.lucene.search.Query 
import org.apache.lucene.search.ScoreDoc 
import org.apache.lucene.search.TopDocs 
import org.apache.lucene.store.Directory 
import org.apache.lucene.store.FSDirectory 
import org.apache.lucene.util.Version 

class SearchController {

	def entryService;
	def siteConfigService;
	def searchService;
	
	
	def doSearch = {
			
		
		def entries = new ArrayList<Entry>();
		
		String queryString = params.queryString;
		log.debug( "searching Users, queryString: ${queryString}" );
		
		List<Entry> searchResults = searchService.doSearch( queryString );
		
		if( searchResults != null )
		{
			entries.addAll( searchResults );
		}
		
		render( view:'displaySearchResults', model:[searchResults:entries]);
	}

	def reindexAll = {
		
		log.debug( "SearchController: Sending reindexAll message to searchQueue" );
		sendJMSMessage("searchQueue", "REINDEX_ALL" );		
		render( "<h1>DONE</h1>" );
	}
}

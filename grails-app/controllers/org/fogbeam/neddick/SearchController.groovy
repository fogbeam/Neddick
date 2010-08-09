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
	
	def doSearch = {
			
		
		String queryString = params.queryString;
		println "searching Users, queryString: ${queryString}";
		
		String indexDirLocation = siteConfigService.getSiteConfigEntry( "indexDirLocation" );
		File indexDir = new File( indexDirLocation );
		Directory fsDir = FSDirectory.open( indexDir );
		
		IndexSearcher searcher = new IndexSearcher( fsDir );
	
		String[] searchFields = ['title', 'url', 'tags', 'content'];
		QueryParser queryParser = new MultiFieldQueryParser(Version.LUCENE_30, searchFields, new StandardAnalyzer(Version.LUCENE_30));
		Query query = queryParser.parse(queryString);
		
		TopDocs hits = searcher.search(query, 40);
		
		def entries = new ArrayList<Entry>();
		ScoreDoc[] docs = hits.scoreDocs;
		for( ScoreDoc doc : docs )
		{
			Document result = searcher.doc( doc.doc );
			
			String docType = result.get( "docType" );
			if( docType.equals( "docType.entry" ))
			{
			
				String id = result.get("id")
				println( id + " " + result.get("title"));
		
				entries.add( entryService.findById(id));
			}
			else if( docType.equals( "docType.comment" ))
			{
				// this document is actually a comment, so we have to lookup the
				// associated Entry using the entry_id field.
				String id = result.get("entry_id");
				entries.add( entryService.findById(id));
			}
			else
			{
				
				println "bad docType: ${docType}"
				continue;
			}
		
		}
		
		println "found some entries: ${entries.size()}";
		
		
		render( view:'displaySearchResults', model:[searchResults:entries]);
	}

	def reindexAll = {
		
		sendJMSMessage("searchQueue", "REINDEX_ALL" );		
		render( "<h1>DONE</h1>" );
	}
}

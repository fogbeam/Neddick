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

class SearchService
{
	def siteConfigService;
	def entryService;
	
	List<Entry> doSearch( final String queryString )
	{

		List<Entry> entries = new ArrayList<Entry>();
		
		String indexDirLocation = siteConfigService.getSiteConfigEntry( "indexDirLocation" );
		
		if( indexDirLocation == null || indexDirLocation.isEmpty())
		{
			String neddickHome = System.getProperty( "neddick.home" );
			indexDirLocation = neddickHome + "/index";
		}
		
		File indexDir = new File( indexDirLocation );
		Directory fsDir = FSDirectory.open( indexDir );
		
		IndexSearcher searcher = new IndexSearcher( fsDir );
	
		
		String[] searchFields = ['entry_uuid', 'title', 'url', 'tags', 'content'];
		QueryParser queryParser = new MultiFieldQueryParser(Version.LUCENE_30, searchFields, new StandardAnalyzer(Version.LUCENE_30));
		Query query = queryParser.parse(queryString);
		
		TopDocs hits = searcher.search(query, 40);
		
		
		ScoreDoc[] docs = hits.scoreDocs;
		for( ScoreDoc doc : docs )
		{
			Document result = searcher.doc( doc.doc );
			
			String docType = result.get( "docType" );
			if( docType.equals( "docType.entry" ))
			{
			
				String id = result.get("id")
				log.debug( id + " " + result.get("title"));
		
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
				
				log.warn( "bad docType: ${docType}" );
				continue;
			}
		
		}
		
		log.debug( "found some entries: ${entries.size()}");
	
		return entries;
		
	}
}

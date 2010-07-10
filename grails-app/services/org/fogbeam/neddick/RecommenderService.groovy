package org.fogbeam.neddick

import org.apache.lucene.analysis.standard.StandardAnalyzer 
import org.apache.lucene.document.Document 
import org.apache.lucene.index.IndexReader 
import org.apache.lucene.queryParser.QueryParser 
import org.apache.lucene.search.IndexSearcher 
import org.apache.lucene.search.Query 
import org.apache.lucene.search.ScoreDoc 
import org.apache.lucene.search.TopDocs 
import org.apache.lucene.search.similar.MoreLikeThis 
import org.apache.lucene.store.Directory 
import org.apache.lucene.store.NIOFSDirectory 
import org.apache.lucene.util.Version 
import org.fogbeam.neddick.Entry;

class RecommenderService 
{

	def entryService;
	def siteConfigService;
	
	public List<Entry> getRecommendedEntries( Entry entry )
	{
		println "getRecommendedEntries called"
		
		String entryUuid = entry.uuid;
	
		List<Entry> recommendedEntries = this.getRecommendedEntries( entryUuid );
	
		return( recommendedEntries );
	}
	
	public List<Entry> getRecommendedEntries( String entryUuid )
	{
		
		String indexDirLocation = siteConfigService.getSiteConfigEntry( "indexDirLocation" );
		Directory indexDir = new NIOFSDirectory( new java.io.File( indexDirLocation ) );
		IndexSearcher searcher = new IndexSearcher( indexDir );
		IndexReader reader = searcher.getIndexReader();
		QueryParser queryParser = new QueryParser(Version.LUCENE_30, "uuid", new StandardAnalyzer(Version.LUCENE_30));
		Query query = queryParser.parse( entryUuid );

		TopDocs hits = searcher.search(query, 1);
		ScoreDoc[] scoreDocs = hits.scoreDocs;
		println "searching for doc with uuid: ${entryUuid}"
		int docNum = -1;
		for( ScoreDoc scoreDoc : scoreDocs )
		{
			docNum = scoreDoc.doc;
		}
		
		if( docNum == -1 )
		{
			println "did not find entry with uuid: ${entryUuid}"
		}
		else
		{
			println "DID find entry for uuid: ${entryUuid}"
		}
		
		ScoreDoc[] mltDocs = null;
		MoreLikeThis mlt = new MoreLikeThis(reader);
		String[] fieldNames = new String[2]
		fieldNames[0] = 'title';
		fieldNames[1] = 'content';
		try
		{
			mlt.setFieldNames(fieldNames);
			Query mltQuery = mlt.like(docNum);
			TopDocs mltHits = searcher.search(mltQuery, 5);
			mltDocs = mltHits.scoreDocs;
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
		
		println "found ${mltDocs?.length} recommended entries"
		
		List<Entry> recommendedEntries = new ArrayList<Entry>();
		for( ScoreDoc mltDoc : mltDocs )
		{
			println "found recommended entry with docId: ${mltDoc.doc}"
			Document recommended = searcher.doc( mltDoc.doc );
			String rUuid = recommended.get( "uuid" );
			println "and uuid: ${rUuid}"
			Entry recommendedEntry = entryService.findByUuid( rUuid );
			
			recommendedEntries.add( recommendedEntry );
		}		
		
	
		return( recommendedEntries );
	}
	
}

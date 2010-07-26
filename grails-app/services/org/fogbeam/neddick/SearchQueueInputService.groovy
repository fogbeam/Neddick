package org.fogbeam.neddick

import org.apache.commons.httpclient.HttpClient 
import org.apache.commons.httpclient.HttpException 
import org.apache.commons.httpclient.HttpMethod 
import org.apache.commons.httpclient.methods.GetMethod 
import org.apache.lucene.analysis.standard.StandardAnalyzer 
import org.apache.lucene.document.Document 
import org.apache.lucene.document.Field 
import org.apache.lucene.index.IndexReader 
import org.apache.lucene.index.IndexWriter 
import org.apache.lucene.index.Term 
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.index.IndexWriter.MaxFieldLength 
import org.apache.lucene.store.Directory 
import org.apache.lucene.store.NIOFSDirectory 
import org.apache.lucene.util.Version 
import org.apache.tika.metadata.Metadata 
import org.apache.tika.parser.Parser
import org.apache.tika.parser.AutoDetectParser
import org.apache.tika.sax.BodyContentHandler 
import org.fogbeam.neddick.Entry;

class SearchQueueInputService
{
	
	def siteConfigService;
	def entryService;
	
    static expose = ['jms']
    static destination = "searchQueue"                 
    
    def onMessage(msg)
    { 
    	
    	/* note: what we would ordinarily do where is turn around and copy this message
    	 * to other queue's, topics, etc., or otherwise route it as needed.  But for
    	 * now we just assume we are the "indexer" job.
    	 */
    	
    	println "GOT MESSAGE: ${msg}"; 
    
    	if( msg instanceof java.lang.String )
    	{
    		println "Yep, it's a string!"
    		
    		
    		if( msg.equals( "REINDEX_ALL" ))
    		{
    			rebuildIndex();
    			return;
    		}
    		else if( msg.startsWith( "ADDTAG" ))
    		{
    			println "proceeding to addTag";
    			// parse out the tagname and the uuid
    			String[] msgParts = msg.split( "\\|" );
    			
    			println msgParts;
    			
    			// add tag
    			String uuid = msgParts[1];
    			println "uuid: ${uuid}";
    			String tagName = msgParts[2];
    			println "tagName: ${tagName}";
    			addTag( uuid, tagName );
    			return;
    		}
    		else
    		{
    			println "BAD STRING";
    			return;
    		}
    	
    	}
    	else
    	{
    	
    		String msgType = msg['msgType'];
    		
    		if( msgType.equals( "NEW_COMMENT" ))
    		{
    			
		    	println "adding document to index"
				String indexDirLocation = siteConfigService.getSiteConfigEntry( "indexDirLocation" );
		    	Directory indexDir = new NIOFSDirectory( new java.io.File( indexDirLocation ) );
				IndexWriter writer = new IndexWriter( indexDir, new StandardAnalyzer(Version.LUCENE_30), false, MaxFieldLength.LIMITED);
				writer.setUseCompoundFile(false);
		    
				Document doc = new Document();    			
    			
				doc.add( new Field( "docType", "docType.comment", Field.Store.YES, Field.Index.NOT_ANALYZED, Field.TermVector.NO ));
				
				doc.add( new Field( "entry_id", Long.toString( msg['entry_id'] ), Field.Store.YES, Field.Index.NOT_ANALYZED ) );
				doc.add( new Field( "entry_uuid", msg['entry_uuid'], Field.Store.YES, Field.Index.NOT_ANALYZED ) );
				
				doc.add( new Field( "id", Long.toString( msg['comment_id'] ), Field.Store.YES, Field.Index.NOT_ANALYZED ) );
				doc.add( new Field( "uuid", msg['comment_uuid'], Field.Store.YES, Field.Index.NOT_ANALYZED ) );
				doc.add( new Field( "content", msg['comment_text'], Field.Store.NO, Field.Index.ANALYZED, Field.TermVector.YES ) );
				writer.addDocument( doc );		
			
				writer.optimize();
				writer.close();
    		
    		}
    		else if( msgType.equals( "NEW_ENTRY" ))
    		{
		    	// add document to index
		    	println "adding document to index"
				String indexDirLocation = siteConfigService.getSiteConfigEntry( "indexDirLocation" );
		    	Directory indexDir = new NIOFSDirectory( new java.io.File( indexDirLocation ) );
				IndexWriter writer = new IndexWriter( indexDir, new StandardAnalyzer(Version.LUCENE_30), false, MaxFieldLength.LIMITED);
				writer.setUseCompoundFile(false);
		    
				Document doc = new Document();
				
				doc.add( new Field( "docType", "docType.entry", Field.Store.YES, Field.Index.NOT_ANALYZED, Field.TermVector.NO ));
				doc.add( new Field( "uuid", msg['uuid'], Field.Store.YES, Field.Index.NOT_ANALYZED ) );
				doc.add( new Field( "id", Long.toString( msg['id'] ), Field.Store.YES, Field.Index.NOT_ANALYZED ) );
				doc.add( new Field( "url", msg['url'], Field.Store.YES, Field.Index.NOT_ANALYZED ) );
				doc.add( new Field( "title", msg['title'], Field.Store.YES, Field.Index.ANALYZED ) );
				doc.add( new Field( "tags", "", Field.Store.YES, Field.Index.ANALYZED ));
				
				/* use HttpClient to load the page, then extract the content and index it.
				 * We'll assume HTTP only links for now... */
				
				
				HttpClient client = new HttpClient();
			 	
				//establish a connection within 10 seconds
				client.getHttpConnectionManager().getParams().setConnectionTimeout(10000); 
			    String url = msg['url'];
			    HttpMethod method = new GetMethod(url);
		
		        String responseBody = null;
		        try{
		            client.executeMethod(method);
		            responseBody = method.getResponseBodyAsString();
		        } catch (HttpException he) {
		            System.err.println("Http error connecting to '" + url + "'");
		            System.err.println(he.getMessage());
		            // System.exit(-4);
		        } catch (IOException ioe){
		            System.err.println("Unable to connect to '" + url + "'");
		            // System.exit(-3);
		        }
		
				// println "responseBody: \n ${responseBody}"
				
				// method.getResponseBodyAsStream()
				
				// extract text with Tika
				 
				// InputStream input = new FileInputStream(new File(resourceLocation));
				InputStream input = method.getResponseBodyAsStream();
				org.xml.sax.ContentHandler textHandler = new BodyContentHandler(-1);
				Metadata metadata = new Metadata();
				// PDFParser parser = new PDFParser();
				Parser parser = new AutoDetectParser();
				parser.parse(input, textHandler, metadata);
				input.close();
				// println("Title: " + metadata.get("title"));
				// println("Author: " + metadata.get("Author"));
				// println("content: " + textHandler.toString());
				
				doc.add( new Field( "content", textHandler.toString(), Field.Store.NO, Field.Index.ANALYZED, Field.TermVector.YES ) );
				writer.addDocument( doc );		
			
				writer.optimize();
				writer.close();
    		}
    		else if( msgType.equals( "NEW_QUESTION" ))
    		{
		    	// add document to index
		    	println "adding document to index"
				String indexDirLocation = siteConfigService.getSiteConfigEntry( "indexDirLocation" );
		    	Directory indexDir = new NIOFSDirectory( new java.io.File( indexDirLocation ) );
				IndexWriter writer = new IndexWriter( indexDir, new StandardAnalyzer(Version.LUCENE_30), false, MaxFieldLength.LIMITED);
				writer.setUseCompoundFile(false);
		    
				Document doc = new Document();
				
				doc.add( new Field( "docType", "docType.entry", Field.Store.YES, Field.Index.NOT_ANALYZED, Field.TermVector.NO ));
				doc.add( new Field( "uuid", msg['uuid'], Field.Store.YES, Field.Index.NOT_ANALYZED ) );
				doc.add( new Field( "id", Long.toString( msg['id'] ), Field.Store.YES, Field.Index.NOT_ANALYZED ) );
				doc.add( new Field( "url", msg['url'], Field.Store.YES, Field.Index.NOT_ANALYZED ) );
				doc.add( new Field( "title", msg['title'], Field.Store.YES, Field.Index.ANALYZED ) );
				doc.add( new Field( "tags", "", Field.Store.YES, Field.Index.ANALYZED ));

				writer.addDocument( doc );		
				
				writer.optimize();
				writer.close();
				
				
    		}
			else 
    		{
    			println "Bad message type: ${msgType}";
    		}
    	}
    }

    private void addTag( final String uuid, final String tagName )
    {
    	println "addTag called with uuid: ${uuid} and tagName: ${tagName}";
    	
		String indexDirLocation = siteConfigService.getSiteConfigEntry( "indexDirLocation" );
    	Directory indexDir = new NIOFSDirectory( new java.io.File( indexDirLocation ) );
    	IndexReader indexReader = IndexReader.open( indexDir, false );
    	
    	Term uuidTerm = new Term( "uuid", uuid );
    	TermDocs termDocs = indexReader.termDocs(uuidTerm);
    	
    	if( termDocs.next() )
    	{
    		int docNum = termDocs.doc();
    		indexReader.deleteDocument( docNum );
    		indexReader.close();
    		
    		IndexWriter writer = new IndexWriter( indexDir, new StandardAnalyzer(Version.LUCENE_30), false, MaxFieldLength.UNLIMITED );
    		
    		Entry entry = entryService.findByUuid( uuid );
    		Document doc = new Document();
    		doc.add( new Field( "docType", "docType.entry", Field.Store.YES, Field.Index.NOT_ANALYZED, Field.TermVector.NO ));
			doc.add( new Field( "uuid", entry.uuid, Field.Store.YES, Field.Index.NOT_ANALYZED ) );
			doc.add( new Field( "id", Long.toString(entry.id), Field.Store.YES, Field.Index.NOT_ANALYZED ) );
			doc.add( new Field( "url", entry.url, Field.Store.YES, Field.Index.NOT_ANALYZED ) );
			doc.add( new Field( "title", entry.title, Field.Store.YES, Field.Index.ANALYZED ) );
	
			String tagString = "";
			entry.tags.each { tagString += it.name + " " };
			doc.add( new Field( "tags", tagString, Field.Store.YES, Field.Index.ANALYZED ) );
			
			/* use HttpClient to load the page, then extract the content and index it.
			 * We'll assume HTTP only links for now... */
		
			HttpClient client = new HttpClient();
		 	println "establishing httpClient object to download content for indexing";
		 	
			//establish a connection within 10 seconds
			client.getHttpConnectionManager().getParams().setConnectionTimeout(10000); 
		    String url = entry.url;
		    HttpMethod method = new GetMethod(url);

	        String responseBody = null;
	        boolean skipContent = false;
	        try{
	            println "executing http request";
	        	client.executeMethod(method);
	            responseBody = method.getResponseBodyAsString();
	        } catch (HttpException he) {
	            System.err.println("Http error connecting to '" + url + "'");
	          //   System.err.println(he.getMessage());
	            skipContent = true;
	        } catch (IOException ioe){
	            System.err.println("Unable to connect to '" + url + "'");
	            skipContent = true;
	        }

			// println "responseBody: \n ${responseBody}"
			
			// extract text with Tika
			if( !skipContent )
			{
				InputStream input = method.getResponseBodyAsStream();
				org.xml.sax.ContentHandler textHandler = new BodyContentHandler(-1);
				Metadata metadata = new Metadata();
				// PDFParser parser = new PDFParser();
				Parser parser = new AutoDetectParser();
				parser.parse(input, textHandler, metadata);
				input.close();
				// println("Title: " + metadata.get("title"));
				// println("Author: " + metadata.get("Author"));
				// println("content: " + textHandler.toString());
				String content = textHandler.toString();
				doc.add( new Field( "content", content, Field.Store.NO, Field.Index.ANALYZED, Field.TermVector.YES ) );			
			}
			
			println "adding document to writer";
			writer.addDocument( doc );		;
    		writer.optimize();
    		writer.close();
    		
    	}
    	else
    	{
    		// no document with that uuid???
    		println( "no document for uuid: ${uuid}" );
    	}
    	
    	
    	
    }
    
    private void rebuildIndex()
    {
    	
    	println "doing rebuildIndex";
    	List<Entry> allEntries = entryService.getAllEntries();
    	
    	
    	
    	// add document to index
		String indexDirLocation = siteConfigService.getSiteConfigEntry( "indexDirLocation" );
    	Directory indexDir = new NIOFSDirectory( new java.io.File( indexDirLocation ) );
		IndexWriter writer = new IndexWriter( indexDir, new StandardAnalyzer(Version.LUCENE_30), true, MaxFieldLength.UNLIMITED);
		writer.setUseCompoundFile(false);
    
		println "about to process all entries";
		
		for( Entry entry : allEntries )
		{
			println "processing entry with id: ${entry.id} and title: ${entry.title} and uuid: ${entry.uuid}";
			

			println "NOT an instanceof Question!";
			
			Document doc = new Document();
		
			doc.add( new Field( "docType", "docType.entry", Field.Store.YES, Field.Index.NOT_ANALYZED, Field.TermVector.NO ));
			doc.add( new Field( "uuid", entry.uuid, Field.Store.YES, Field.Index.NOT_ANALYZED ) );
			doc.add( new Field( "id", Long.toString(entry.id), Field.Store.YES, Field.Index.NOT_ANALYZED ) );
			doc.add( new Field( "url", entry.url, Field.Store.YES, Field.Index.NOT_ANALYZED ) );
			doc.add( new Field( "title", entry.title, Field.Store.YES, Field.Index.ANALYZED ) );
			String tagString = "";
			entry.tags.each { tagString += it.name + " " };
			doc.add( new Field( "tags", tagString, Field.Store.YES, Field.Index.ANALYZED ) );
			
			// comments on the entry
			entry.comments.each {
				
				Document commentDoc = new Document();    			
    			
				commentDoc.add( new Field( "docType", "docType.comment", Field.Store.YES, Field.Index.NOT_ANALYZED, Field.TermVector.NO ));
				
				commentDoc.add( new Field( "entry_id", Long.toString( entry.id ), Field.Store.YES, Field.Index.NOT_ANALYZED ) );
				commentDoc.add( new Field( "entry_uuid", entry.uuid, Field.Store.YES, Field.Index.NOT_ANALYZED ) );
				
				commentDoc.add( new Field( "id", Long.toString( it.id), Field.Store.YES, Field.Index.NOT_ANALYZED ) );
				commentDoc.add( new Field( "uuid", it.uuid, Field.Store.YES, Field.Index.NOT_ANALYZED ) );
				commentDoc.add( new Field( "content", it.text, Field.Store.NO, Field.Index.ANALYZED, Field.TermVector.YES ) );
				writer.addDocument( commentDoc );
			}
			
			
			/* TODO: use HttpClient to load the page, then extract the content and index it.
			 * We'll assume HTTP only links for now... */

			if( !(entry instanceof org.fogbeam.neddick.Question) )
			{
			
				HttpClient client = new HttpClient();
			 	println "establishing httpClient object to download content for indexing";
			 	
				//establish a connection within 10 seconds
				client.getHttpConnectionManager().getParams().setConnectionTimeout(10000); 
			    String url = entry.url;
			    HttpMethod method = new GetMethod(url);
	
		        String responseBody = null;
		        try{
		            println "executing http request";
		        	client.executeMethod(method);
		            responseBody = method.getResponseBodyAsString();
		        } catch (HttpException he) {
		            System.err.println("Http error connecting to '" + url + "'");
		            System.err.println(he.getMessage());
		            continue;
		        } catch (IOException ioe){
		            System.err.println("Unable to connect to '" + url + "'");
		            continue;
		        }
	
				// println "responseBody: \n ${responseBody}"
				
				// extract text with Tika
				InputStream input = method.getResponseBodyAsStream();
				org.xml.sax.ContentHandler textHandler = new BodyContentHandler(-1);
				Metadata metadata = new Metadata();
				// PDFParser parser = new PDFParser();
				Parser parser = new AutoDetectParser();
				parser.parse(input, textHandler, metadata);
				input.close();
				// println("Title: " + metadata.get("title"));
				// println("Author: " + metadata.get("Author"));
				// println("content: " + textHandler.toString());
				String content = textHandler.toString();
				doc.add( new Field( "content", content, Field.Store.NO, Field.Index.ANALYZED, Field.TermVector.YES ) );			
			}
			
			println "adding document to writer";
			writer.addDocument( doc );		

		}
		
		println "optimizing index";
		writer.optimize();
		
		println "closing writer"
		writer.close();	    	
    }
    
}
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
import org.apache.lucene.index.TermDocs
import org.apache.lucene.index.IndexWriter.MaxFieldLength
import org.apache.lucene.store.Directory
import org.apache.lucene.store.NIOFSDirectory
import org.apache.lucene.util.Version
import org.apache.tika.metadata.Metadata
import org.apache.tika.parser.AutoDetectParser
import org.apache.tika.parser.Parser
import org.apache.tika.sax.BodyContentHandler

public class SearchQueueInputService
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
    	
    	log.debug( "GOT MESSAGE: ${msg}" ); 
    
    	if( msg instanceof java.lang.String )
    	{
    		log.info( "Received message: ${msg}" );
    		
    		
    		if( msg.equals( "REINDEX_ALL" ))
    		{
    			rebuildIndex();
    			return;
    		}
    		else if( msg.startsWith( "ADDTAG" ))
    		{
    			log.debug( "proceeding to addTag" );
    			// parse out the tagname and the uuid
    			String[] msgParts = msg.split( "\\|" );
    			
    			log.debug( msgParts );
    			
    			// add tag
    			String uuid = msgParts[1];
    			log.debug( "uuid: ${uuid}" );
    			String tagName = msgParts[2];
    			log.debug( "tagName: ${tagName}" );
    			addTag( uuid, tagName );
    			return;
    		}
    		else
    		{
    			log.debug( "BAD STRING" );
    			return;
    		}
    	}
    	else
    	{
			log.info( "Received message: ${msg}" );
			
    		String msgType = msg['msgType'];
    		
    		if( msgType.equals( "NEW_COMMENT" ))
    		{
    			
		    	log.debug( "adding document to index" );
				String indexDirLocation = siteConfigService.getSiteConfigEntry( "indexDirLocation" );
		    	Directory indexDir = new NIOFSDirectory( new java.io.File( indexDirLocation ) );
				IndexWriter writer = null;
				
				// TODO: fix this so it will eventually give up, to deal with the pathological case
				// where we never do get the required lock.
				int luceneLockRetryCount = 0;
				while( writer == null )
				{
					try
					{
						writer = new IndexWriter( indexDir, new StandardAnalyzer(Version.LUCENE_30), false, MaxFieldLength.UNLIMITED );
					}
					catch( org.apache.lucene.store.LockObtainFailedException lfe )
					{
						luceneLockRetryCount++;
						if( luceneLockRetryCount > 10 )
						{
							log.error( "Failed to obtain lock for Lucene store", e );
							return;
						}
						else 
						{
							Thread.sleep( 1200 );
							continue;
						}
					}
				}
				
				try
				{
					
					writer.setUseCompoundFile(true);
		    
					Document doc = new Document();    			
    			
					doc.add( new Field( "docType", "docType.comment", Field.Store.YES, Field.Index.NOT_ANALYZED, Field.TermVector.NO ));
				
					doc.add( new Field( "entry_id", Long.toString( msg['entry_id'] ), Field.Store.YES, Field.Index.NOT_ANALYZED ) );
					doc.add( new Field( "entry_uuid", msg['entry_uuid'], Field.Store.YES, Field.Index.NOT_ANALYZED ) );
				
					doc.add( new Field( "id", Long.toString( msg['comment_id'] ), Field.Store.YES, Field.Index.NOT_ANALYZED ) );
					doc.add( new Field( "uuid", msg['comment_uuid'], Field.Store.YES, Field.Index.NOT_ANALYZED ) );
					doc.add( new Field( "content", msg['comment_text'], Field.Store.NO, Field.Index.ANALYZED, Field.TermVector.YES ) );
					writer.addDocument( doc );		
			
					writer.optimize();
				}
				finally 
				{
					try 
					{
						writer.close();
					}
					catch( Exception e ) {
						// ignore this for now, but add a log message at least
					}
					
					try 
					{
						indexDir.close();
					}
					catch( Exception e )
					{
						// ignore this for now, but add a log message at least
					}
				}
    		}
    		else if( msgType.equals( "NEW_ENTRY" ))
    		{
		    	// add document to index
		    	log.info( "adding document to index: ${msg['uuid']}" );
				String indexDirLocation = siteConfigService.getSiteConfigEntry( "indexDirLocation" );
		    	log.info ( "got indexDirLocation as: ${indexDirLocation}");
				Directory indexDir = new NIOFSDirectory( new java.io.File( indexDirLocation ) );
				IndexWriter writer = null;
				
				// TODO: fix this so it will eventually give up, to deal with the pathological case
				// where we never do get the required lock.
				int count = 0;
				while( writer == null )
				{
					count++;
					if( count > 3 ) {
						log.debug( "tried to obtain Lucene lock 3 times, giving up..." );
						return;	
					}
					try
					{
						writer = new IndexWriter( indexDir, new StandardAnalyzer(Version.LUCENE_30), false, MaxFieldLength.UNLIMITED );
					}
					catch( org.apache.lucene.store.LockObtainFailedException lfe )
					{
						Thread.sleep( 1200 );
					}
				}
				
				InputStream input = null;
				try
				{
					writer.setUseCompoundFile(true);
		    
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
		            	log.error("Http error connecting to '" + url + "'");
						log.error(he.getMessage());
						return;
					} catch (IOException ioe){
						// ioe.printStackTrace();
		            	log.error("Unable to connect to '" + url + "'");
		            	log.error( ioe );
						return;
					}
				
					// extract text with Tika
					
					input = method.getResponseBodyAsStream();
					org.xml.sax.ContentHandler textHandler = new BodyContentHandler(-1);
					Metadata metadata = new Metadata();
					Parser parser = new AutoDetectParser();
					parser.parse(input, textHandler, metadata);
									
					doc.add( new Field( "content", textHandler.toString(), Field.Store.NO, Field.Index.ANALYZED, Field.TermVector.YES ) );
					writer.addDocument( doc );
					writer.optimize();
				}		
				finally
				{
					try 
					{
						if( input != null )
						{
							input.close();
						}
					}
					catch( Exception e ) 
					{
						// ignore this for now, but add a log message at least
						e.printStackTrace();
					}
				
					try
					{
						if( method != null ) 
						{
							log.debug( "calling method.releaseConnection()" );
							method.releaseConnection();
						}
					}
					catch( Exception e ) 
					{
						// ignore this for now, but add a log message at least
						e.printStackTrace();
					}
						
					try
					{
						if( writer != null ) 
						{
							writer.close();
						}
					}
					catch( Exception e ) 
					{
						// ignore this for now, but add a log message at least
						e.printStackTrace();
					}
					
					try
					{
						if( indexDir != null ) 
						{
							indexDir.close();
						}
					}
					catch( Exception e )
					{
						// ignore this for now, but add a log message at least
						e.printStackTrace();
					}
				}

    		}
    		else if( msgType.equals( "NEW_QUESTION" ))
    		{
		    	// add document to index
		    	log.debug( "adding document to index" );
				String indexDirLocation = siteConfigService.getSiteConfigEntry( "indexDirLocation" );
		    	Directory indexDir = new NIOFSDirectory( new java.io.File( indexDirLocation ) );
				IndexWriter writer = null;
				
				// TODO: fix this so it will eventually give up, to deal with the pathological case
				// where we never do get the required lock.
				while( writer == null )
				{
					try
					{
						writer = new IndexWriter( indexDir, new StandardAnalyzer(Version.LUCENE_30), false, MaxFieldLength.UNLIMITED );
					}
					catch( org.apache.lucene.store.LockObtainFailedException lfe )
					{
						Thread.sleep( 1200 );
					}
				}
				
				try
				{
					writer.setUseCompoundFile(true);
		    
					Document doc = new Document();
				
					doc.add( new Field( "docType", "docType.entry", Field.Store.YES, Field.Index.NOT_ANALYZED, Field.TermVector.NO ));
					doc.add( new Field( "uuid", msg['uuid'], Field.Store.YES, Field.Index.NOT_ANALYZED ) );
					doc.add( new Field( "id", Long.toString( msg['id'] ), Field.Store.YES, Field.Index.NOT_ANALYZED ) );
					doc.add( new Field( "url", msg['url'], Field.Store.YES, Field.Index.NOT_ANALYZED ) );
					doc.add( new Field( "title", msg['title'], Field.Store.YES, Field.Index.ANALYZED ) );
					doc.add( new Field( "tags", "", Field.Store.YES, Field.Index.ANALYZED ));

					writer.addDocument( doc );		
				
					writer.optimize();
				}
				finally
				{
					try
					{
						writer.close();
					}
					catch( Exception e ) {
						// ignore this for now, but add a log message at least
					}
					
					try
					{
						indexDir.close();
					}
					catch( Exception e )
					{
						// ignore this for now, but add a log message at least
					}
				}
				
    		}
			else 
    		{
    			log.debug( "Bad message type: ${msgType}" );
    		}
    	}
    }

    private void addTag( final String uuid, final String tagName )
    {
    	log.debug( "addTag called with uuid: ${uuid} and tagName: ${tagName}" );
    	
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
    		
    		IndexWriter writer = null;
			
			// TODO: fix this so it will eventually give up, to deal with the pathological case
			// where we never do get the required lock.
			while( writer == null )
			{
				try
				{
					writer = new IndexWriter( indexDir, new StandardAnalyzer(Version.LUCENE_30), false, MaxFieldLength.UNLIMITED );
				}
				catch( org.apache.lucene.store.LockObtainFailedException lfe )
				{
					Thread.sleep( 1200 );
				}
			}
			
	   		writer.setUseCompoundFile( true );
			
			try
			{
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
				log.debug("establishing httpClient object to download content for indexing" );
		 	
				//establish a connection within 10 seconds
				client.getHttpConnectionManager().getParams().setConnectionTimeout(10000); 
				String url = entry.url;
				HttpMethod method = new GetMethod(url);

		        String responseBody = null;
		        boolean skipContent = false;
		        try{
		            log.debug( "executing http request" );
		        	client.executeMethod(method);
		        } catch (HttpException he) {
		            log.error("Http error connecting to '" + url + "'");
		            skipContent = true;
		        } catch (IOException ioe){
					ioe.printStackTrace();
		            log.error("Unable to connect to '" + url + "'");
		            skipContent = true;
		        }
	
				// extract text with Tika

				if( !skipContent )
				{
					InputStream input = method.getResponseBodyAsStream();
					org.xml.sax.ContentHandler textHandler = new BodyContentHandler(-1);
					Metadata metadata = new Metadata();
					Parser parser = new AutoDetectParser();
					parser.parse(input, textHandler, metadata);
					String content = textHandler.toString();
					doc.add( new Field( "content", content, Field.Store.NO, Field.Index.ANALYZED, Field.TermVector.YES ) );			
				}
				
				log.debug( "adding document to writer" );
				writer.addDocument( doc );		;
	    		writer.optimize();
			}
			finally
			{
				
				try
				{
					if( input != null )
					{
						input.close();
					}
				}
				catch( Exception e )
				{
					// ignore this for now, but add a log message at least
				}
			
				try
				{
					if( client != null )
					{
						log.debug( "calling connectionManager.shutdown()" );
						client.getConnectionManager().shutdown();
					}
				}
				catch( Exception e )
				{
					// ignore this for now, but add a log message at least
				}
				
				
				try
				{
					writer.close();
				}
				catch( Exception e ) {
					// ignore this for now, but add a log message at least
				}
				
				try
				{
					indexDir.close();
				}
				catch( Exception e )
				{
					// ignore this for now, but add a log message at least
				}
			}
    	}
    	else
    	{
    		// no document with that uuid???
    		log.debug( "no document for uuid: ${uuid}" );
    	}
    }
    
    private void rebuildIndex()
    {
    	
    	log.debug( "doing rebuildIndex" );
    	List<Entry> allEntries = entryService.getAllEntries();
    	
    	// add document to index
		String indexDirLocation = siteConfigService.getSiteConfigEntry( "indexDirLocation" );
    	Directory indexDir = new NIOFSDirectory( new java.io.File( indexDirLocation ) );
		IndexWriter writer = null;
		
		// TODO: fix this so it will eventually give up, to deal with the pathological case
		// where we never do get the required lock.
		while( writer == null )
		{
			try
			{
				writer = new IndexWriter( indexDir, new StandardAnalyzer(Version.LUCENE_30), true, MaxFieldLength.UNLIMITED );
			}
			catch( org.apache.lucene.store.LockObtainFailedException lfe )
			{
				Thread.sleep( 1200 );
			}
		}
		
		try
		{
			writer.setUseCompoundFile(true);
    
			log.debug( "about to process all entries" );
		
			for( Entry entry : allEntries )
			{
				log.debug( "processing entry with id: ${entry.id} and title: ${entry.title} and uuid: ${entry.uuid}" );
				
				log.debug( "NOT an instanceof Question!" );
				
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
				 	log.debug( "establishing httpClient object to download content for indexing" );
				 	
					//establish a connection within 10 seconds
					client.getHttpConnectionManager().getParams().setConnectionTimeout(10000); 
				    String url = entry.url;
				    HttpMethod method = new GetMethod(url);
		
			        String responseBody = null;
			        try 
					{
			            log.debug( "executing http request" );
			        	client.executeMethod(method);
			        } 
					catch (HttpException he) 
					{
			            log.error("Http error connecting to '" + url + "'");
			            log.error(he.getMessage());
			            continue;
			        } 
					catch (IOException ioe)
					{
			            log.error( "Unable to connect to '" + url + "'" );
			            continue;
			        }
		
					// extract text with Tika
					InputStream input = method.getResponseBodyAsStream();
					org.xml.sax.ContentHandler textHandler = new BodyContentHandler(-1);
					Metadata metadata = new Metadata();

					Parser parser = new AutoDetectParser();
					parser.parse(input, textHandler, metadata);
					
					String content = textHandler.toString();
					doc.add( new Field( "content", content, Field.Store.NO, Field.Index.ANALYZED, Field.TermVector.YES ) );			
				}
				
				log.debug( "adding document to writer" );
				writer.addDocument( doc );		
	
			}
		
			log.debug( "optimizing index" );
			writer.optimize();
		}
		finally
		{
			
			try
			{
				if( input != null )
				{
					input.close();
				}
			}
			catch( Exception e )
			{
				// ignore this for now, but add a log message at least
			}
		
			try
			{
				if( client != null )
				{
					log.debug( "calling connectionManager.shutdown()" );
					client.getConnectionManager().shutdown();
				}
			}
			catch( Exception e )
			{
				// ignore this for now, but add a log message at least
			}
			
			try
			{
				if( writer != null )
				{
					writer.close();
				}
			}
			catch( Exception e ) {
				// ignore this for now, but add a log message at least
			}
			
			try
			{
				if( indexDir != null )
				{
					indexDir.close();
				}
			}
			catch( Exception e )
			{
				// ignore this for now, but add a log message at least
			}
		}
			    	
    }
}
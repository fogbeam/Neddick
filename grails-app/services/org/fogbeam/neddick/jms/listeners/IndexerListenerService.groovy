package org.fogbeam.neddick.jms.listeners

import groovyx.net.http.RESTClient
import javax.mail.BodyPart
import javax.mail.Folder
import javax.mail.Message
import javax.mail.MessagingException
import javax.mail.NoSuchProviderException
import javax.mail.Session
import javax.mail.Store
import javax.mail.internet.MimeMessage
import javax.mail.internet.MimeMultipart
import javax.mail.search.MessageIDTerm

import org.apache.commons.httpclient.Header
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
import org.apache.tika.parser.html.BoilerpipeContentHandler
import org.apache.tika.parser.html.HtmlParser
import org.apache.tika.sax.BodyContentHandler
import org.fogbeam.neddick.EMailEntry
import org.fogbeam.neddick.Entry
import org.fogbeam.neddick.IMAPAccount
import org.fogbeam.neddick.WebpageEntry


import de.l3s.boilerpipe.document.TextBlock
import de.l3s.boilerpipe.document.TextDocument





public class IndexerListenerService {

	def siteConfigService;
	def entryService;


	static expose = ['jms']
	static destination = "searchQueue"

	def onMessage(msg) {

		/* note: what we would ordinarily do where is turn around and copy this message
		 * to other queue's, topics, etc., or otherwise route it as needed.  But for
		 * now we just assume we are the "indexer" job.
		 */

		log.debug( "GOT MESSAGE: ${msg}" );

		if( msg instanceof java.lang.String ) {
			log.info( "Received message: ${msg}" );


			if( msg.equals( "REINDEX_ALL" )) {
				rebuildIndex();
				return;
			}
			else if( msg.startsWith( "ADDTAG" )) {
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

				// send JMS message saying "tag added"
				def tagIndexedMessage = [msgType:"TAG_INDEXED", entry_uuid:uuid, tagName: tagName ];

				// send notifications
				sendJMSMessage( "neddickTriggerQueue", tagIndexedMessage );
				sendJMSMessage( "neddickFilterQueue", tagIndexedMessage );

				return;
			}
			else {
				log.debug( "BAD STRING" );
				return;
			}
		}
		else {
			log.info( "Received message: ${msg}" );

			String msgType = msg['msgType'];

			if( msgType.equals( "NEW_COMMENT" )) {

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
				
				Entry entry = entryService.findByUuid( msg['uuid']);
				
				if( entry == null )
				{
					println "WARN: No such entry: ${msg['uuid']}";
					return;
				}
				
				
				// Now we have to distinguish between the different kinds of Entry's we can receive here.
				// Webpage (HTTP) entries, Email (IMAP) entries, etc.  We'll have to extract the content, save it
				// with the associated Entry and index it.  We also have to figure out where in this process to inject
				// the call to Stanbol to do our Semantic Concept Extraction work.
				
				// First make a polymorphic call to extractAndIndexContent( entry );
				extractAndIndexContent( entry, msg );
				
				// and then a call to the Stanbol Enhancer
				doSemanticEnhacement( entry, msg );
				

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

	
	private void doSemanticEnhancement( WebpageEntry entry, def msg )
	{
		// Hit Stanbol to get enrichmentData
		// call Stanbol REST API to get enrichment data
		RESTClient restClient = new RESTClient( "http://localhost:8080" )
	
		println "content submitted: ${content}";
		def restResponse = restClient.post(	path:'enhancer',
										body: params.statusText,
										requestContentType : TEXT );
	
		def restResponseText = restResponse.getData();
	}
	
	private void doSemanticEnhancement( EMailEntry entry, def msg )
	{
		// Hit Stanbol to get enrichmentData
		// call Stanbol REST API to get enrichment data
		RESTClient restClient = new RESTClient( "http://localhost:8080" )
	
		// println "content submitted: ${content}";
		def restResponse = restClient.post(	path:'enhancer',
										body: params.statusText,
										requestContentType : TEXT );
	
		def restResponseText = restResponse.getData();
	}
	
		
	private void extractAndIndexContent( WebpageEntry entry, def msg )
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
		HttpMethod method = null;
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

			// add channels
			String channelUuidString = "";
			entry.channels.each { channelUuidString += it.uuid + " " };
			doc.add( new Field( "channel_uuids", channelUuidString, Field.Store.YES, Field.Index.ANALYZED ));
							
			/* use HttpClient to load the page, then extract the content and index it.
			 * We'll assume HTTP only links for now... */


			HttpClient client = new HttpClient();

			//establish a connection within 10 seconds
			client.getHttpConnectionManager().getParams().setConnectionTimeout(10000);
			String url = msg['url'];
			method = new GetMethod(url);

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
			// detect HTML responses and use the BoilerpipeContentHandler
			// in those cases.  Continue to use BodyContentHandler elsewhere
			Header contentTypeHeader = method.getResponseHeader( "Content-Type" );
			
			String contentType = "html";
			if( contentTypeHeader != null )
			{
				contentType = contentTypeHeader.toString();
			}
			
			println "Got Content-Type as: ${contentType}";
			
			org.xml.sax.ContentHandler textHandler = null;
			
			org.xml.sax.ContentHandler bodyContentHandler = new BodyContentHandler(-1);
			String bodyContent = "";
			
			if( contentType.contains( "html" ) || contentType.contains( "xhtml" ))
			{
				textHandler = new BoilerpipeContentHandler( bodyContentHandler ); 
				textHandler.setIncludeMarkup( true );
				
				input = method.getResponseBodyAsStream();
				
				Metadata metadata = new Metadata();
				HtmlParser parser = new HtmlParser();
				parser.parse(input, textHandler, metadata);
   
				TextDocument tDoc = textHandler.toTextDocument();
				
				bodyContent = "";
				List<TextBlock> blocks = tDoc.getTextBlocks();
				
				for( TextBlock block : blocks )
				{
					
					
					println "**********************************\n ${block.getText()}\n**************************";
					
					if( block.isContent())
					{
					
						bodyContent += "<p>";
						bodyContent += block.getText();
						bodyContent += "</p>";
					}
					
				}
				
				// bodyContent = bodyContentHandler.toString(); // tDoc.getContent(); // bodyContentHandler.toString(); 
				
				if( bodyContent != null && !bodyContent.isEmpty())
				{
				   println "bodyContent: ${bodyContent}";
				}
				else
				{
				   println "No BodyContent!!! WTF???";
				}
			   
				entry.pageContent = bodyContent;
				
				
			}
			else
			{
				textHandler = bodyContentHandler;

				input = method.getResponseBodyAsStream();
				
				Metadata metadata = new Metadata();
				Parser parser = new AutoDetectParser();
				parser.parse(input, textHandler, metadata);
   
				bodyContent = bodyContentHandler.toString();
				if( bodyContent != null && !bodyContent.isEmpty())
				{
				   println "bodyContent: ${bodyContent}";
				}
				else
				{
				   println "No BodyContent!!! WTF???";
				}
			   
				entry.pageContent = bodyContent;
			}
			
			/* Hmmm... we can somehow get a StaleObjectState exception here, possibly because the thread
			 * that originally created this Entry was delayed in committing its transaction for some reason.
			 * We should be able to make up for that by doing a refetch, and then re-persist the updated
			 * object.
			 */
			if( entry.save(flush:true))
			{
				println "saved entry with bodyContent, adding to Lucene index";
				
				doc.add( new Field( "content", bodyContent, Field.Store.NO, Field.Index.ANALYZED, Field.TermVector.YES ) );
				writer.addDocument( doc );
				writer.optimize();
			}
			else
			{
				entry.errors.allErrors.each {println it;}
			}
			
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


		// send JMS message saying "content indexed"
		def contentIndexedMessage = [msgType:"NEW_ENTRY_INDEXED", entry_uuid:msg['uuid'] ];

		// send notifications
		sendJMSMessage( "neddickTriggerQueue", contentIndexedMessage );
		sendJMSMessage( "neddickFilterQueue", contentIndexedMessage );

	}
	
	
	private void extractAndIndexContent( EMailEntry entry, def msg )
	{
		println "INDEXING EMAIL ENTRY HERE!!!!!!!!!!!!!!!!!!";
		
		// we need to look up the datasource used to generate this
		// entry, so we can connect and download the content
		// add document to index
		IMAPAccount imapAccount = entry.theDataSource;
		
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

		try
		{
			// connect and download the message for indexing...
			writer.setUseCompoundFile(true);
		
			Document doc = new Document();

			doc.add( new Field( "docType", "docType.entry", Field.Store.YES, Field.Index.NOT_ANALYZED, Field.TermVector.NO ));
			doc.add( new Field( "uuid", msg['uuid'], Field.Store.YES, Field.Index.NOT_ANALYZED ) );
			doc.add( new Field( "id", Long.toString( msg['id'] ), Field.Store.YES, Field.Index.NOT_ANALYZED ) );
			doc.add( new Field( "messageId", msg['messageId'], Field.Store.YES, Field.Index.NOT_ANALYZED ) );
			doc.add( new Field( "title", msg['title'], Field.Store.YES, Field.Index.ANALYZED ) );
			doc.add( new Field( "tags", "", Field.Store.YES, Field.Index.ANALYZED ));

			// add channels
			String channelUuidString = "";
			entry.channels.each { channelUuidString += it.uuid + " " };
			doc.add( new Field( "channel_uuids", channelUuidString, Field.Store.YES, Field.Index.ANALYZED ));
		
		
		
			Properties props = System.getProperties();
			props.setProperty("mail.store.protocol", "imaps");
			Folder inboxFolder = null;
			Store store = null;
			try
			{
				Session session = Session.getDefaultInstance(props, null);
				store = session.getStore("imaps");
			
				
				// TODO: deal with failure to connect...
				store.connect(imapAccount.server, imapAccount.username, imapAccount.password);
			 
				
				inboxFolder = store.getFolder( imapAccount.folder );
				if( inboxFolder != null )
				{
					inboxFolder.open(Folder.READ_ONLY);
					println "Folder: ${inboxFolder.fullName}";
				}
				else
				{
					println "no inboxFolder!!!";
				}
				
				// search on message id
				MessageIDTerm searchTerm = new MessageIDTerm( entry.messageId );
				Message[] messages = inboxFolder.search( searchTerm );
				
				println "found ${messages.length} matching messages";
				
				if( messages.length == 1 )
				{
				
				
					MimeMessage mimeMessage = messages[0];
					
					String formattedContent = "<p>";
					
					Object bodyContent = mimeMessage.getContent();
					if( bodyContent instanceof MimeMultipart )
					{
						MimeMultipart mimeMulti = bodyContent;
						int partCount = mimeMulti.getCount();
						Map partsKeyedByType = [:];
						
						for( int i = 0; i < partCount; i++ )
						{
							BodyPart part = mimeMulti.getBodyPart( i );
							if( part.getContentType().contains( "plain" ))
							{
								partsKeyedByType.textPart = part;
								break;
							}
							else if( part.getContentType().contains("html"))
							{
								partsKeyedByType.htmlPart = part;
							}
						}
					
					
						// try plain text first
						if( partsKeyedByType.containsKey( "textPart" ))
						{
							bodyContent = partsKeyedByType.textPart.getContent();
						}
						else if( partsKeyedByType.containsKey("htmlPart"))
						{
							// TODO: extract the raw text from the HTML
							bodyContent = "HTML ONLY, Come back to this one";
						}
						else
						{
							// nothing here we can process...
							println "Multipart Message had nothing we could process!";
							bodyContent = "Multipart Message had nothing we could process!";
						}
					}
					
					/* NOTE: What follows is really formatting / presentational logic and
					 * ultimately needs to be moved elsewhere.  Whether or not we even
					 * want to persist a formatted version or not, or whether all formatting
					 * should be done "on the fly" is an open question.
					 */
					
					formattedContent += bodyContent.replace( "[\r\n]+", "</p>");
					if( ! formattedContent.endsWith("</p>") )
					{
						formattedContent += "</p>";
					}
										
					println "####### FORMATTED EMAIL CONTENT\n${formattedContent}\n###########################";
					
					entry.bodyContent = formattedContent;
					
					/* NOTE: This is also susceptible to the StaleObjectState problem, and should
					 * get the same fix as mentioned above:  If we get that exception, reload
					 * the object by ID, then re-persist it.
					 */
					if( entry.save())
					{
						println "saved entry with bodyContent, adding to Lucene index";
						
						doc.add( new Field( "content", bodyContent, Field.Store.NO, Field.Index.ANALYZED, Field.TermVector.YES ) );
						writer.addDocument( doc );
						writer.optimize();
						
					}
					else
					{
						
						println "failed to save EMailEntry with updated bodyContent";
					}
				}
				else
				{
					println "Message not found, or too many matching messages found!";
				}				
			
			}
			catch (NoSuchProviderException e)
			{
				e.printStackTrace();
				// System.exit(1);
			}
			catch (MessagingException e) {
				e.printStackTrace();
				// System.exit(2);
			}
			finally
			{
				if( inboxFolder != null )
				{
					if( inboxFolder.isOpen() )
					{
						inboxFolder.close( false );
					}
				}
				
				if( store != null )
				{
					if( store.isConnected())
					{
						store.close();
					}
				}
				
			}
		
		
		}
		finally
		{
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
		
				
		
		// send JMS message saying "content indexed"
		def contentIndexedMessage = [msgType:"NEW_ENTRY_INDEXED", entry_uuid:msg['uuid'] ];

		// send notifications
		sendJMSMessage( "neddickTriggerQueue", contentIndexedMessage );
		sendJMSMessage( "neddickFilterQueue", contentIndexedMessage );
		
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

		println( "doing rebuildIndex" );
		List<Entry> allEntries = entryService.getAllEntries();

		// add document to index
		String indexDirLocation = siteConfigService.getSiteConfigEntry( "indexDirLocation" );
		println "using indexDirLocation: ${indexDirLocation}";
		
		// check if there's an initialized index yet.  If not, initialize empty index
		File indexFile = new java.io.File( indexDirLocation );
		String[] indexFileChildren = indexFile.list();
		boolean indexIsInitialized = (indexFileChildren != null && indexFileChildren.length > 0 );
		if( ! indexIsInitialized )
		{
			println( "Index not previously initialized, creating empty index" );
			/* initialize empty index */
			Directory indexDir = new NIOFSDirectory( indexFile );
			IndexWriter writer = new IndexWriter( indexDir, new StandardAnalyzer(Version.LUCENE_30), true, MaxFieldLength.UNLIMITED);
			Document doc = new Document();
			writer.addDocument(doc);
			writer.close();
		}
		else
		{   
		   println( "Index already initialized..." );
		   println "indexFileChildren.length = ${indexFileChildren.length}";
		   
		   indexFileChildren.each { println it; }
		   println "********";
		}
		
		
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
				doc.add( new Field( "subject", entry.subject, Field.Store.YES, Field.Index.NOT_ANALYZED ) );
				doc.add( new Field( "title", entry.title, Field.Store.YES, Field.Index.ANALYZED ) );

				String tagString = "";
				entry.tags.each { tagString += it.name + " " };
				doc.add( new Field( "tags", tagString, Field.Store.YES, Field.Index.ANALYZED ) );

				String channelUuidString = "";
				entry.channels.each { channelUuidString += it.uuid + " " };
				doc.add( new Field( "channel_uuids", channelUuidString, Field.Store.YES, Field.Index.ANALYZED ));


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
					try
					{
						parser.parse(input, textHandler, metadata);
					}
					catch( Exception e )
					{
						log.error( "Unable to parse content", e );
						println "Unable to parse content: continuing...";
						e.printStackTrace();
						continue;	
					}
					
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
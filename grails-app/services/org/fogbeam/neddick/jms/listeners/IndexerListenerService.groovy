package org.fogbeam.neddick.jms.listeners

import static groovyx.net.http.ContentType.TEXT

import javax.jms.Message as JMSMessage;
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

import org.apache.commons.io.IOUtils
import org.apache.http.Header
import org.apache.http.HttpException
import org.apache.http.HttpResponse
import org.apache.http.client.config.RequestConfig
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClients
import org.apache.http.util.EntityUtils
import org.apache.jena.query.Dataset
import org.apache.jena.query.ReadWrite
import org.apache.jena.rdf.model.Model
import org.apache.jena.rdf.model.ModelFactory
import org.apache.jena.rdf.model.ResIterator
import org.apache.jena.rdf.model.Resource
import org.apache.jena.riot.RDFDataMgr
import org.apache.jena.tdb.TDBFactory
import org.apache.jena.vocabulary.DCTerms
import org.apache.jena.vocabulary.OWL
import org.apache.jena.vocabulary.RDF
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
import org.fogbeam.neddick.TwitterEntry
import org.fogbeam.neddick.WebpageEntry
import org.hibernate.StaleObjectStateException
import org.springframework.orm.hibernate3.HibernateOptimisticLockingFailureException

import de.l3s.boilerpipe.document.TextBlock
import de.l3s.boilerpipe.document.TextDocument
import groovyx.net.http.RESTClient

public class IndexerListenerService 
{

	def siteConfigService;
	def entryService;
	def grailsApplication;
	def jmsService;
	
	static expose = ['jms']
	static destination = "searchQueue"

	def onMessage( JMSMessage msg ) 
	{
		/* note: what we would ordinarily do where is turn around and copy this message
		 * to other queue's, topics, etc., or otherwise route it as needed.  But for
		 * now we just assume we are the "indexer" job.
		 */

		log.info( "GOT MESSAGE: ${msg}" );

		if( msg instanceof javax.jms.TextMessage ) 
		{
			log.info( "Received message: ${msg}" );

			String msgBody = msg.getText();
			
			if( msgBody.equals( "REINDEX_ALL" )) {
				rebuildIndex();
				return;
			}
			else if( msgBody.startsWith( "ADDTAG" )) {
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
				// sendJMSMessage( "neddickTriggerQueue", tagIndexedMessage );
				jmsService.send( queue: 'neddickTriggerQueue', tagIndexedMessage, 'standard', null );
				
				// sendJMSMessage( "neddickFilterQueue", tagIndexedMessage );
				jmsService.send( queue: 'neddickFilterQueue', tagIndexedMessage, 'standard', null );
				
				
				return;
			}
			else 
			{
				log.debug( "BAD STRING: ${msgBody}" );
				return;
			}
		}
		else if( msg instanceof javax.jms.MapMessage )
		{
			log.info( "Received message: ${msg}" );
			
			String msgType = msg.getString( 'msgType' );

			if( msgType.equals( "NEW_COMMENT" )) 
			{
				log.debug( "adding document to index" );
				String indexDirLocation = siteConfigService.getSiteConfigEntry( "indexDirLocation" );
				
				if( indexDirLocation == null || indexDirLocation.isEmpty())
				{
					String neddickHome = System.getProperty( "neddick.home" );
					indexDirLocation = neddickHome + "/index";
				}
				
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

					doc.add( new Field( "entry_id", Long.toString( msg.getLong('entry_id' ) ), Field.Store.YES, Field.Index.NOT_ANALYZED ) );
					doc.add( new Field( "entry_uuid", msg.getString( 'entry_uuid'), Field.Store.YES, Field.Index.NOT_ANALYZED ) );

					doc.add( new Field( "id", Long.toString( msg.getLong('comment_id') ), Field.Store.YES, Field.Index.NOT_ANALYZED ) );
					doc.add( new Field( "uuid", msg.getString( 'comment_uuid'), Field.Store.YES, Field.Index.NOT_ANALYZED ) );
					doc.add( new Field( "content", msg.getString( 'comment_text'), Field.Store.NO, Field.Index.ANALYZED, Field.TermVector.YES ) );
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
				
				log.info( "NEW_ENTRY for uuid: ${msg.getString( 'uuid' ) }" );
				
				Entry entry = entryService.findByUuid( msg.getString( 'uuid' ) );
				
				if( entry == null )
				{
					log.debug "WARN: No such entry: ${msg.getString('uuid') }";
					return;
				}	
				
				// Now we have to distinguish between the different kinds of Entry's we can receive here.
				// Webpage (HTTP) entries, Email (IMAP) entries, etc.  We'll have to extract the content, save it
				// with the associated Entry and index it.  We also have to figure out where in this process to inject
				// the call to Stanbol to do our Semantic Concept Extraction work.
				
				// First make a polymorphic call to extractAndIndexContent( entry );
				extractAndIndexContent( entry, msg );
				
				entry = Entry.findByUuid( entry.uuid );
				entry.refresh();
				
				// and then a call to the Stanbol Enhancer
				doSemanticEnhancement( entry, msg );
				
			}
			else if( msgType.equals( "NEW_QUESTION" ))
			{
				// add document to index
				log.debug( "adding document to index" );
				String indexDirLocation = siteConfigService.getSiteConfigEntry( "indexDirLocation" );
				
				if( indexDirLocation == null || indexDirLocation.isEmpty())
				{
					String neddickHome = System.getProperty( "neddick.home" );
					indexDirLocation = neddickHome + "/index";
				}
				
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
					doc.add( new Field( "uuid", msg.getString( 'uuid' ), Field.Store.YES, Field.Index.NOT_ANALYZED ) );
					doc.add( new Field( "id", Long.toString( msg.getLong('id') ), Field.Store.YES, Field.Index.NOT_ANALYZED ) );
					doc.add( new Field( "url", msg.getString( 'url' ), Field.Store.YES, Field.Index.NOT_ANALYZED ) );
					doc.add( new Field( "title", msg.getString( 'title' ), Field.Store.YES, Field.Index.ANALYZED ) );
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
		else
		{
			log.error( "Unknown message type received!!" );
		}
	}


	static String convertStreamToString(java.io.InputStream is) {
		java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
		return s.hasNext() ? s.next() : "";
	}
	
	private void doSemanticEnhancement( TwitterEntry entry, def msg )
	{
		// call Stanbol REST API to get enrichment data
		String stanbolServerUrl = grailsApplication.config.urls.stanbol.endpoint;
		log.debug "using stanbolServerUrl: ${stanbolServerUrl}";
		RESTClient restClient = new RESTClient( stanbolServerUrl )
	
		// log.debug "content submitted: ${content}";
		def restResponse = restClient.post(	path:'enhancer',
										body: entry.tweetContent,
										requestContentType : TEXT );
	
		def restResponseText = restResponse.getData();
		// log.debug "restResponseText.class.name = ${restResponseText.class.name}";
		
		entry.refresh();
		entry.enhancementJSON = restResponseText;
		
		log.debug "restResponseText: \n${restResponseText}\n\n";
		
		if( restResponseText != null && !restResponseText.isEmpty())
		{
		
			// create an empty Model
			Model tempModel = ModelFactory.createDefaultModel();
			
			StringReader reader = new StringReader( restResponseText.toString() );
		
			RDFDataMgr.read(tempModel, reader, "http://www.example.com", JenaJSONLD.JSONLD);
			
	
			// Make a TDB-backed dataset
			String neddickHome = System.getProperty( "neddick.home" );
			String directory = "${neddickHome}/jenastore/triples" ;
			log.debug "Opening TDB triplestore at: ${directory}";
			Dataset dataset = TDBFactory.createDataset(directory) ;
			
			dataset.begin(ReadWrite.WRITE);
			// Get model inside the transaction
			Model model = dataset.getDefaultModel() ;
			
			// find all the "entity" entries in our graph and then associate each
			// one with our "DocumentID"
			ResIterator iter = tempModel.listSubjectsWithProperty( RDF.type, OWL.Thing );
			
			while( iter.hasNext() )
			{
	
				Resource anEntity = iter.nextResource();
			
				log.debug "adding resource \"neddick:${entry.uuid}\" dc:references entity: ${anEntity.toString()}";
				
				Resource newResource = model.createResource( "neddick:${entry.uuid}" );
				newResource.addProperty( DCTerms.references, anEntity);
	
			}
	
			// now add all the triples from the Stanbol response to our canonical Model
			model.add( tempModel );
					
			dataset.commit();
			
			dataset.end();
		
		}
		else
		{
			log.debug "Can't process JSON -> TDB operation!";
		}
		
		
		try
		{
			if( !entry.save(flush:true))
			{
				entry.errors.allErrors.each{ log.debug it;}
			}
		}
		catch( HibernateOptimisticLockingFailureException | StaleObjectStateException sose )
		{
			log.debug "!!!!!!!!\nCaught HibernateOptimisticLockingFailureException, reloading object and trying again\n!!!!!!!!!";
			
			Thread.sleep(15000 );
			
			entry = Entry.get( entry.id );
			entry.refresh();
			entry.enhancementJSON = restResponseText;
			
			
			try
			{
				if( !entry.save(flush:true))
				{
					entry.errors.allErrors.each{ log.debug it;}
				}
			}
			catch( HibernateOptimisticLockingFailureException | StaleObjectStateException sose2 )
			{
				log.debug "!!\nFailed Second Attempt To Persist Stale Object Instance!\n!!";
				
				Thread.sleep( 60000 );
				
				entry = Entry.get( entry.id );
				entry.refresh();
				entry.enhancementJSON = restResponseText;
				
				try
				{
					if( !entry.save(flush:true))
					{
						entry.errors.allErrors.each{ log.debug it;}
					}
				}
				catch( HibernateOptimisticLockingFailureException | StaleObjectStateException sose3 )
				{
					throw new RuntimeException( "Failed Third Attempt To Persist Stale Object Instance!" );
				}
			}
		}
	}
	
		
	private void doSemanticEnhancement( WebpageEntry entry, def msg )
	{
				
		if( grailsApplication.config.semantic.enhancement.enabled == null || 
            grailsApplication.config.semantic.enhancement.enabled.isEmpty() ||
            Boolean.parseBoolean( grailsApplication.config.semantic.enhancement.enabled ) == false )
		{
			// semantic enhancement is not enabled, just return
			return;
		}

				
		// Hit Stanbol to get enrichmentData
		// call Stanbol REST API to get enrichment data
		String stanbolServerUrl = grailsApplication.config.urls.stanbol.endpoint;

		
		log.info( "using stanbolServerUrl: ${stanbolServerUrl}");
		RESTClient restClient = new RESTClient( stanbolServerUrl )
	
		// log.debug "content submitted: ${content}";
		def restResponse = restClient.post(	path:'enhancer',
										body: entry.pageContent,
										requestContentType : TEXT );
	
		def restResponseText = restResponse.getData();
		// log.debug "restResponseText.class.name = ${restResponseText.class.name}";
		
			
		entry.refresh();
		entry.enhancementJSON = restResponseText;
		
		log.debug "restResponseText: \n${restResponseText}\n\n";
		
		if( restResponseText != null && !restResponseText.isEmpty())
		{
		
			// create an empty Model
			Model tempModel = ModelFactory.createDefaultModel();
			
			StringReader reader = new StringReader( restResponseText.toString() );
			
			RDFDataMgr.read(tempModel, reader, "http://www.example.com", JenaJSONLD.JSONLD);
			
	
			// Make a TDB-backed dataset
			String neddickHome = System.getProperty( "neddick.home" );
			String directory = "${neddickHome}/jenastore/triples" ;
			log.debug "Opening TDB triplestore at: ${directory}";
			Dataset dataset = TDBFactory.createDataset(directory) ;
			
			dataset.begin(ReadWrite.WRITE);
			// Get model inside the transaction
			Model model = dataset.getDefaultModel() ;
			
			// find all the "entity" entries in our graph and then associate each
			// one with our "DocumentID"
			ResIterator iter = tempModel.listSubjectsWithProperty( RDF.type, OWL.Thing );
			
			while( iter.hasNext() )
			{
	
				Resource anEntity = iter.nextResource();
			
				log.debug "adding resource \"neddick:${entry.uuid}\" dc:references entity: ${anEntity.toString()}";
				
				Resource newResource = model.createResource( "neddick:${entry.uuid}" );
				newResource.addProperty( DCTerms.references, anEntity);
	
			}
	
			// now add all the triples from the Stanbol response to our canonical Model
			model.add( tempModel );
					
			dataset.commit();
			
			dataset.end();
		
		}
		else
		{
			log.debug "Can't process JSON -> TDB operation!";
		}
		
		
		try
		{
			if( !entry.save(flush:true))
			{
				entry.errors.allErrors.each{ log.debug it;}
			}
		}
		catch( HibernateOptimisticLockingFailureException | StaleObjectStateException sose )
		{
			log.debug "!!!!!!!!\nCaught HibernateOptimisticLockingFailureException, reloading object and trying again\n!!!!!!!!!";
			
			Thread.sleep( 15000 );
			
			entry = Entry.get( entry.id );
			entry.refresh();
			entry.enhancementJSON = restResponseText;
			
			try
			{
				if( !entry.save(flush:true))
				{
					entry.errors.allErrors.each{ log.debug it;}
				}
			}
			catch( HibernateOptimisticLockingFailureException | StaleObjectStateException sose2 )
			{
				log.debug "!!\nFailed Second Attempt To Persist Stale Object Instance!\n!!";
						
				Thread.sleep( 60000 );
				
				entry = Entry.get( entry.id );
				entry.refresh();
				entry.enhancementJSON = restResponseText;
				
				try
				{
					if( !entry.save(flush:true))
					{
						entry.errors.allErrors.each{ log.debug it;}
					}
				}
				catch( HibernateOptimisticLockingFailureException | StaleObjectStateException sose3 )
				{
					throw new RuntimeException( "Failed Third Attempt To Persist Stale Object Instance!" );
				}
			}
		}
		
	}
	
	private void doSemanticEnhancement( EMailEntry entry, def msg )
	{
		// Hit Stanbol to get enrichmentData
		// call Stanbol REST API to get enrichment data
		String stanbolServerUrl = grailsApplication.config.urls.stanbol.endpoint;
		log.debug "using stanbolServerUrl: ${stanbolServerUrl}";
		RESTClient restClient = new RESTClient( stanbolServerUrl )
	
		// log.debug "content submitted: ${content}";
		def restResponse = restClient.post(	path:'enhancer',
										body: entry.bodyContent,
										requestContentType : TEXT );
	
		def restResponseText = restResponse.getData();
		
		entry.refresh();
		entry.enhancementJSON = restResponseText;
		
		log.debug "restResponseText: \n${restResponseText}\n\n";
		
		if( restResponseText != null && !restResponseText.isEmpty())
		{
		
			// create an empty Model
			Model tempModel = ModelFactory.createDefaultModel();
			
			StringReader reader = new StringReader( restResponseText.toString() );
			
			RDFDataMgr.read(tempModel, reader, "http://www.example.com", JenaJSONLD.JSONLD);
			
	
			// Make a TDB-backed dataset
			String neddickHome = System.getProperty( "neddick.home" );
			String directory = "${neddickHome}/jenastore/triples" ;
			log.debug "Opening TDB triplestore at: ${directory}";
			Dataset dataset = TDBFactory.createDataset(directory) ;
			
			dataset.begin(ReadWrite.WRITE);
			// Get model inside the transaction
			Model model = dataset.getDefaultModel() ;
			
			// find all the "entity" entries in our graph and then associate each
			// one with our "DocumentID"
			ResIterator iter = tempModel.listSubjectsWithProperty( RDF.type, OWL.Thing );
			
			while( iter.hasNext() )
			{
	
				Resource anEntity = iter.nextResource();
			
				log.debug "adding resource \"neddick:${entry.uuid}\" dc:references entity: ${anEntity.toString()}";
				
				Resource newResource = model.createResource( "neddick:${entry.uuid}" );
				newResource.addProperty( DCTerms.references, anEntity);
	
			}
	
			// now add all the triples from the Stanbol response to our canonical Model
			model.add( tempModel );
					
			dataset.commit();
			
			dataset.end();
		
		}
		else
		{
			log.debug "Can't process JSON -> TDB operation!";
		}
		
				
		try
		{
			if( !entry.save(flush:true))
			{
				entry.errors.allErrors.each{ log.debug it;}
			}
		}
		catch( HibernateOptimisticLockingFailureException | StaleObjectStateException sose )
		{
			
			log.debug "!!!!!!!!\nCaught HibernateOptimisticLockingFailureException, reloading object and trying again\n!!!!!!!!!";

			Thread.sleep( 15000 );
			
			entry = Entry.get( entry.id );
			entry.refresh();
			entry.enhancementJSON = restResponseText;
			
			try
			{
				if( !entry.save(flush:true))
				{
					entry.errors.allErrors.each{ log.debug it;}
				}
			}
			catch( HibernateOptimisticLockingFailureException | StaleObjectStateException sose2 )
			{
				log.debug "!!\nFailed Second Attempt To Persist Stale Object Instance!\n!!";				
				
				Thread.sleep( 60000 );
				
				entry = Entry.findById( entry.id );
				entry.refresh();
				entry.enhancementJSON = restResponseText;
				
				try
				{
					if( !entry.save(flush:true))
					{
						entry.errors.allErrors.each{ log.debug it;}
					}
				}
				catch( HibernateOptimisticLockingFailureException | StaleObjectStateException sose3 )
				{
					throw new RuntimeException( "Failed Third Attempt To Persist Stale Object Instance!" );
				}
			}
		}
	}
	
	private void extractAndIndexContent( TwitterEntry entry, def msg )
	{
		// log.debug "extractAndIndexContent for TwitterEntry!";
		
		
		// add document to index
		log.info( "adding document to index: ${msg.getString( 'uuid' ) }" );
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
			writer.setUseCompoundFile(true);

			Document doc = new Document();

			doc.add( new Field( "docType", "docType.entry", Field.Store.YES, Field.Index.NOT_ANALYZED, Field.TermVector.NO ));
			doc.add( new Field( "uuid", msg.getString( 'uuid' ), Field.Store.YES, Field.Index.NOT_ANALYZED ) );
			doc.add( new Field( "id", Long.toString( msg.getLong('id' ) ), Field.Store.YES, Field.Index.NOT_ANALYZED ) );
			doc.add( new Field( "title", msg.getString( 'title' ), Field.Store.YES, Field.Index.ANALYZED ) );
			doc.add( new Field( "tags", "", Field.Store.YES, Field.Index.ANALYZED ));

			// add channels
			String channelUuidString = "";
			entry.channels.each { channelUuidString += it.uuid + " " };
			doc.add( new Field( "channel_uuids", channelUuidString, Field.Store.YES, Field.Index.ANALYZED ));
		
			doc.add( new Field( "content", entry.tweetContent, Field.Store.YES, Field.Index.ANALYZED ) );
			
			writer.addDocument( doc );	
			
			writer.optimize();
			
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
		def contentIndexedMessage = [msgType:"NEW_ENTRY_INDEXED", entry_uuid:msg.getString( 'uuid' ) ];

		// send notifications
		// sendJMSMessage( "neddickTriggerQueue", contentIndexedMessage );
		jmsService.send( queue: 'neddickTriggerQueue', contentIndexedMessage, 'standard', null );
		
		
		// sendJMSMessage( "neddickFilterQueue", contentIndexedMessage );
		jmsService.send( queue: 'neddickFilterQueue', contentIndexedMessage, 'standard', null );
		
		
	}
		
	private void extractAndIndexContent( WebpageEntry entry, def msg )
	{
		// add document to index
		log.info( "WebpageEntry: adding document to index: ${msg.getString('uuid') }" );
		
		String indexDirLocation = siteConfigService.getSiteConfigEntry( "indexDirLocation" );
		
		if( indexDirLocation == null || indexDirLocation.isEmpty())
		{
			String neddickHome = System.getProperty( "neddick.home" );
			indexDirLocation = neddickHome + "/index";
		}
		
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
		HttpGet method = null;
		try
		{
			writer.setUseCompoundFile(true);

			Document doc = new Document();

			doc.add( new Field( "docType", "docType.entry", Field.Store.YES, Field.Index.NOT_ANALYZED, Field.TermVector.NO ));
			doc.add( new Field( "uuid", msg.getString( 'uuid' ), Field.Store.YES, Field.Index.NOT_ANALYZED ) );
			doc.add( new Field( "id", Long.toString( msg.getLong('id' ) ), Field.Store.YES, Field.Index.NOT_ANALYZED ) );
			doc.add( new Field( "url", msg.getString( 'url'), Field.Store.YES, Field.Index.NOT_ANALYZED ) );
			doc.add( new Field( "title", msg.getString( 'title' ), Field.Store.YES, Field.Index.ANALYZED ) );
			doc.add( new Field( "tags", "", Field.Store.YES, Field.Index.ANALYZED ));

			// add channels
			String channelUuidString = "";
			entry.channels.each { channelUuidString += it.uuid + " " };
			doc.add( new Field( "channel_uuids", channelUuidString, Field.Store.YES, Field.Index.ANALYZED ));
							
			/* use HttpClient to load the page, then extract the content and index it.
			 * We'll assume HTTP only links for now... */

			RequestConfig defaultRequestConfig = RequestConfig.custom()
				.setConnectTimeout(10000)
				.setSocketTimeout(10000)
				.setConnectionRequestTimeout(20000)
				.build();

			CloseableHttpClient httpClient = HttpClients.custom()
				.setDefaultRequestConfig(defaultRequestConfig)
				.build();
						
			String url = msg.getString( 'url' );
			
			log.info( "making HTTP GET request for URL: ${url}" );
			
			method = new HttpGet(url);

			String responseBody = null;
			HttpResponse response = null;
			try
			{
				response = httpClient.execute(method);
				responseBody = EntityUtils.toString( response.entity );
			} 
			catch (HttpException he) 
			{
				log.error("Http error connecting to '" + url + "'");
				log.error(he.getMessage());
				return;
			} 
			catch (IOException ioe)
			{
				// ioe.printStackTrace();
				log.error("Unable to connect to '" + url + "'");
				log.error( ioe );
				return;
			}

			// extract text with Tika
			// detect HTML responses and use the BoilerpipeContentHandler
			// in those cases.  Continue to use BodyContentHandler elsewhere
			Header[] headers = response.getAllHeaders();
			Header contentTypeHeader = null;
			for( Header h : headers )
			{
				if( h.name.equals( "Content-Type" ))
				{
					contentTypeHeader = h;
				}
			}
			
			String contentType = "html";
			if( contentTypeHeader != null )
			{
				contentType = contentTypeHeader.toString();
			}
			
			log.debug "Got Content-Type as: ${contentType}";
			
			org.xml.sax.ContentHandler textHandler = null;
			
			org.xml.sax.ContentHandler bodyContentHandler = new BodyContentHandler(-1);
			String bodyContent = "";
			
			if( contentType.contains( "html" ) || contentType.contains( "xhtml" ))
			{
				textHandler = new BoilerpipeContentHandler( bodyContentHandler );
				textHandler.setIncludeMarkup( true );
				
				input = IOUtils.toInputStream( responseBody, "UTF-8" );
				
				Metadata metadata = new Metadata();
				HtmlParser parser = new HtmlParser();
				parser.parse(input, textHandler, metadata);
   
				TextDocument tDoc = textHandler.toTextDocument();
				
				bodyContent = "";
				List<TextBlock> blocks = tDoc.getTextBlocks();
				
				for( TextBlock block : blocks )
				{
					log.debug "**********************************\n ${block.getText()}\n**************************";
					
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
				   log.debug "bodyContent: ${bodyContent}";
				}
				else
				{
				   log.debug "No BodyContent!!! WTF???";
				}
			   
				entry.pageContent = bodyContent;
				
				
			}
			else
			{
				textHandler = bodyContentHandler;

				input = response.entity.content;
				
				Metadata metadata = new Metadata();
				Parser parser = new AutoDetectParser();
				parser.parse(input, textHandler, metadata);
   
				bodyContent = bodyContentHandler.toString();
				if( bodyContent != null && !bodyContent.isEmpty())
				{
				   log.debug "bodyContent: ${bodyContent}";
				}
				else
				{
				   log.debug "No BodyContent!!! WTF???";
				}
			   
				entry.pageContent = bodyContent;
			}
			
			/* Hmmm... we can somehow get a StaleObjectState exception here, possibly because the thread
			 * that originally created this Entry was delayed in committing its transaction for some reason.
			 * We should be able to make up for that by doing a refetch, and then re-persist the updated
			 * object.
			 */
			try
			{
			
				if( entry.save(flush:true))
				{
					log.debug "saved entry with bodyContent, adding to Lucene index";
					
					doc.add( new Field( "content", bodyContent, Field.Store.NO, Field.Index.ANALYZED, Field.TermVector.YES ) );
					writer.addDocument( doc );
					writer.optimize();
				}
				else
				{
					entry.errors.allErrors.each {log.debug it;}
				}
			}
			catch( HibernateOptimisticLockingFailureException | StaleObjectStateException sose )
			{
				log.debug "!!!!!!!!\nCaught HibernateOptimisticLockingFailureException, reloading object and trying again\n!!!!!!!!!";
				
				
				Thread.sleep( 30000 );
				
				// requery the object from the db
				
				entry = Entry.get( entry.id );
				if( !entry )
				{
					throw new RuntimeException( "Failed to locate Entry for uuid: ${uuid}");
				}
				
				entry.refresh();
				
				entry.pageContent = bodyContent;
				
				try
				{
					if( entry.save(flush:true))
					{
						log.debug "saved entry with bodyContent, adding to Lucene index";
					
						doc.add( new Field( "content", bodyContent, Field.Store.NO, Field.Index.ANALYZED, Field.TermVector.YES ) );
						writer.addDocument( doc );
						writer.optimize();
					}
					else
					{
						entry.errors.allErrors.each {log.debug it;}
					}
				}
				catch( HibernateOptimisticLockingFailureException | StaleObjectStateException sose2)
				{
					throw new RuntimeException( "Failed Second Attempt To Persist Stale Object Instance!");
				}
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
		def contentIndexedMessage = [msgType:"NEW_ENTRY_INDEXED", entry_uuid:msg.getString( 'uuid' ) ];

		// send notifications
		// sendJMSMessage( "neddickTriggerQueue", contentIndexedMessage );
		jmsService.send( queue: 'neddickTriggerQueue', contentIndexedMessage, 'standard', null );
		
		// sendJMSMessage( "neddickFilterQueue", contentIndexedMessage );
		jmsService.send( queue: 'neddickFilterQueue', contentIndexedMessage, 'standard', null );
		
	}
	
	
	private void extractAndIndexContent( EMailEntry entry, def msg )
	{
		log.debug "INDEXING EMAIL ENTRY HERE!!!!!!!!!!!!!!!!!!";
		
		// we need to look up the datasource used to generate this
		// entry, so we can connect and download the content
		// add document to index
		IMAPAccount imapAccount = entry.theDataSource;
		
		log.info( "adding document to index: ${ msg.getString( 'uuid' ) }" );
		String indexDirLocation = siteConfigService.getSiteConfigEntry( "indexDirLocation" );
		
		if( indexDirLocation == null || indexDirLocation.isEmpty())
		{
			String neddickHome = System.getProperty( "neddick.home" );
			indexDirLocation = neddickHome + "/index";
		}
		
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
			doc.add( new Field( "uuid", msg.getString( 'uuid'), Field.Store.YES, Field.Index.NOT_ANALYZED ) );
			doc.add( new Field( "id", Long.toString( msg.getLong('id' ) ), Field.Store.YES, Field.Index.NOT_ANALYZED ) );
			doc.add( new Field( "messageId", msg.getString( 'messageId' ), Field.Store.YES, Field.Index.NOT_ANALYZED ) );
			doc.add( new Field( "title", msg.getString( 'title' ), Field.Store.YES, Field.Index.ANALYZED ) );
			if( msg.getString( 'subject' ) != null )
			{
				doc.add( new Field( "subject", msg.getString( 'subject' ), Field.Store.YES, Field.Index.ANALYZED ) );
			}
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
					log.debug "Folder: ${inboxFolder.fullName}";
				}
				else
				{
					log.debug "no inboxFolder!!!";
				}
				
				// search on message id
				MessageIDTerm searchTerm = new MessageIDTerm( entry.messageId );
				Message[] messages = inboxFolder.search( searchTerm );
				
				log.debug "found ${messages.length} matching messages";
				
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
							else
							{
								log.debug "part had type: ${part.getContentType()}";
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
							log.debug "Multipart Message had nothing we could process!";
							bodyContent = "Multipart Message had nothing we could process!";
						}
					}
					else
					{
						// should probably be String.  If it's anything else, we don't know how to handle that yet.
						if( ! ( bodyContent instanceof String ) )
						{
							log.debug "message content was of class: ${bodyContent.class}";	
						}
					}
					
					
					// log.debug "Email bodyContent: \n ${bodyContent}";
					
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
										
					log.debug "####### FORMATTED EMAIL CONTENT\n${formattedContent}\n###########################";
					
					entry.bodyContent = formattedContent;
					
					/* NOTE: This is also susceptible to the StaleObjectState problem, and should
					 * get the same fix as mentioned above:  If we get that exception, reload
					 * the object by ID, then re-persist it.
					 */
					try
					{
					
						if( entry.save(flush:true))
						{
							log.debug "saved entry with bodyContent, adding to Lucene index";
							
							doc.add( new Field( "content", bodyContent, Field.Store.NO, Field.Index.ANALYZED, Field.TermVector.YES ) );
							writer.addDocument( doc );
							writer.optimize();
							
						}
						else
						{
							
							log.debug "failed to save EMailEntry with updated bodyContent";
							entry.errors.allErrors.each {log.debug it}
						}
						
					}
					catch( HibernateOptimisticLockingFailureException | StaleObjectStateException sose )
					{
						log.debug "!!!!!!!!\nCaught HibernateOptimisticLockingFailureException, reloading object and trying again\n!!!!!!!!!";
						
						Thread.sleep( 10000 );
						
						// requery the object from the db
						entry = Entry.get( entry.id );
		
						if( !entry )
						{
							throw new RuntimeException( "Failed to locate Entry for uuid: ${uuid}");
						}
						
						entry.refresh();
						entry.bodyContent = formattedContent;
						
						
						try
						{
							if( entry.save(flush:true))
							{
								log.debug "saved entry with bodyContent, adding to Lucene index";
							
								doc.add( new Field( "content", bodyContent, Field.Store.NO, Field.Index.ANALYZED, Field.TermVector.YES ) );
								writer.addDocument( doc );
								writer.optimize();
							}
							else
							{
								entry.errors.allErrors.each {log.debug it;}
							}
						}
						catch( HibernateOptimisticLockingFailureException | StaleObjectStateException sose2)
						{
							throw new RuntimeException( "Failed Second Attempt To Persist Stale Object Instance!");
						}
						
					}
							
				}
				else
				{
					log.debug "Message not found, or too many matching messages found!";
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
		def contentIndexedMessage = [msgType:"NEW_ENTRY_INDEXED", entry_uuid:msg.getString( 'uuid' ) ];

		// send notifications
		// sendJMSMessage( "neddickTriggerQueue", contentIndexedMessage );
		jmsService.send( queue: 'neddickTriggerQueue', contentIndexedMessage, 'standard', null );
		
		// sendJMSMessage( "neddickFilterQueue", contentIndexedMessage );
		jmsService.send( queue: 'neddickFilterQueue', contentIndexedMessage, 'standard', null );
	
		
	}

	
	private void addTag( final String uuid, final String tagName )
	{
		log.debug( "addTag called with uuid: ${uuid} and tagName: ${tagName}" );

		String indexDirLocation = siteConfigService.getSiteConfigEntry( "indexDirLocation" );
		
		if( indexDirLocation == null || indexDirLocation.isEmpty())
		{
			String neddickHome = System.getProperty( "neddick.home" );
			indexDirLocation = neddickHome + "/index";
		}
		
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
				
				RequestConfig defaultRequestConfig = RequestConfig.custom()
					.setConnectTimeout(10000)
					.setSocketTimeout(10000)
					.setConnectionRequestTimeout(20000)
					.build();

				CloseableHttpClient httpClient = HttpClients.custom()
					.setDefaultRequestConfig(defaultRequestConfig)
					.build();
					
				log.debug("establishing httpClient object to download content for indexing" );
				
				String url = entry.url;
				
				HttpGet method = new HttpGet(url);

				String responseBody = null;
				HttpResponse response = null;
				boolean skipContent = false;
				try
				{
					log.debug( "executing http request" );
					response = httpClient.execute(method);
					responseBody = EntityUtils.toString( response.entity );

				} 
				catch (HttpException he) 
				{
					log.error("Http error connecting to '" + url + "'");
					skipContent = true;
				} 
				catch (IOException ioe)
				{
					ioe.printStackTrace();
					log.error("Unable to connect to '" + url + "'");
					skipContent = true;
				}

				// extract text with Tika

				if( !skipContent )
				{
					InputStream input = response.entity.content;
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
		
		if( indexDirLocation == null || indexDirLocation.isEmpty())
		{
			String neddickHome = System.getProperty( "neddick.home" );
			indexDirLocation = neddickHome + "/index";
		}
		
		log.debug "using indexDirLocation: ${indexDirLocation}";
		
		// check if there's an initialized index yet.  If not, initialize empty index
		File indexFile = new java.io.File( indexDirLocation );
		String[] indexFileChildren = indexFile.list();
		boolean indexIsInitialized = (indexFileChildren != null && indexFileChildren.length > 0 );
		if( ! indexIsInitialized )
		{
			log.debug( "Index not previously initialized, creating empty index" );
			/* initialize empty index */
			Directory indexDir = new NIOFSDirectory( indexFile );
			IndexWriter writer = new IndexWriter( indexDir, new StandardAnalyzer(Version.LUCENE_30), true, MaxFieldLength.UNLIMITED);
			Document doc = new Document();
			writer.addDocument(doc);
			writer.close();
		}
		else
		{   
		   log.debug( "Index already initialized..." );
		   log.debug "indexFileChildren.length = ${indexFileChildren.length}";
		   
		   indexFileChildren.each { log.debug it; }
		   log.debug "********";
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

				extractAndIndexContent( entry, writer );

			}

			log.debug( "optimizing index" );
			writer.optimize();
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
	
	
	private void extractAndIndexContent( TwitterEntry entry, Writer writer )
	{
		log.debug "extractAndIndexContent for TwitterEntry!";
		
		try
		{
			Document doc = new Document();

			doc.add( new Field( "docType", "docType.entry", Field.Store.YES, Field.Index.NOT_ANALYZED, Field.TermVector.NO ));
			doc.add( new Field( "uuid", msg.getString( 'uuid' ), Field.Store.YES, Field.Index.NOT_ANALYZED ) );
			doc.add( new Field( "id", Long.toString( msg.getLong('id' ) ), Field.Store.YES, Field.Index.NOT_ANALYZED ) );
			doc.add( new Field( "title", msg.getString( 'title' ), Field.Store.YES, Field.Index.ANALYZED ) );
			doc.add( new Field( "tags", "", Field.Store.YES, Field.Index.ANALYZED ));

			// add channels
			String channelUuidString = "";
			entry.channels.each { channelUuidString += it.uuid + " " };
			doc.add( new Field( "channel_uuids", channelUuidString, Field.Store.YES, Field.Index.ANALYZED ));
		
			doc.add( new Field( "content", entry.tweetContent, Field.Store.YES, Field.Index.ANALYZED ) );
			
			writer.addDocument( doc );
		
		}
		finally
		{
		}
	}
		
	private void extractAndIndexContent( WebpageEntry entry, Writer writer )
	{
		// add document to index
		log.info( "adding document to index: ${entry.uuid}" );
	
		
		InputStream input = null;
		HttpGet method = null;
		try
		{
			Document doc = new Document();

			doc.add( new Field( "docType", "docType.entry", Field.Store.YES, Field.Index.NOT_ANALYZED, Field.TermVector.NO ));
			doc.add( new Field( "uuid", entry.uuid, Field.Store.YES, Field.Index.NOT_ANALYZED ) );
			doc.add( new Field( "id", Long.toString( entry.id ), Field.Store.YES, Field.Index.NOT_ANALYZED ) );
			doc.add( new Field( "url", entry.url, Field.Store.YES, Field.Index.NOT_ANALYZED ) );
			doc.add( new Field( "title", entry.title, Field.Store.YES, Field.Index.ANALYZED ) );
			doc.add( new Field( "tags", "", Field.Store.YES, Field.Index.ANALYZED ));

			// add channels
			String channelUuidString = "";
			entry.channels.each { channelUuidString += it.uuid + " " };
			doc.add( new Field( "channel_uuids", channelUuidString, Field.Store.YES, Field.Index.ANALYZED ));
							
			/* use HttpClient to load the page, then extract the content and index it.
			 * We'll assume HTTP only links for now... */

			RequestConfig defaultRequestConfig = RequestConfig.custom()
				.setConnectTimeout(10000)
				.setSocketTimeout(10000)
				.setConnectionRequestTimeout(20000)
				.build();

			CloseableHttpClient httpClient = HttpClients.custom()
				.setDefaultRequestConfig(defaultRequestConfig)
				.build();
			
			String url = entry.url;
			
			log.info("for entry ${entry.uuid} trying to load url: ${url}");
			
			method = new HttpGet(url);

			String responseBody = null;
			HttpResponse response = null;
			try
			{
				response = httpClient.execute(method);
				responseBody = EntityUtils.toString(response.entity);
			}
			catch (HttpException he) 
			{
				log.error("Http error connecting to '" + url + "'");
				log.error(he.getMessage());
				return;
			} 
			catch (IOException ioe)
			{
				// ioe.printStackTrace();
				log.error("Unable to connect to '" + url + "'");
				log.error( ioe );
				return;
			}

			// extract text with Tika
			// detect HTML responses and use the BoilerpipeContentHandler
			// in those cases.  Continue to use BodyContentHandler elsewhere
			Header[] headers = response.getAllHeaders();
			Header contentTypeHeader = null;
			for( Header h : headers )
			{
				if( h.name.equals( "Content-Type" ))
				{
					contentTypeHeader = h;
				}
			}
			
			String contentType = "html";
			if( contentTypeHeader != null )
			{
				contentType = contentTypeHeader.toString();
			}
			
			log.info("Got Content-Type as: ${contentType}");
			
			org.xml.sax.ContentHandler textHandler = null;
			
			org.xml.sax.ContentHandler bodyContentHandler = new BodyContentHandler(-1);
			String bodyContent = "";
			
			if( contentType.contains( "html" ) || contentType.contains( "xhtml" ))
			{
				textHandler = new BoilerpipeContentHandler( bodyContentHandler );
				textHandler.setIncludeMarkup( true );
				
				input = response.entity.content;
				
				Metadata metadata = new Metadata();
				HtmlParser parser = new HtmlParser();
				parser.parse(input, textHandler, metadata);
   
				TextDocument tDoc = textHandler.toTextDocument();
				
				bodyContent = "";
				List<TextBlock> blocks = tDoc.getTextBlocks();
				
				for( TextBlock block : blocks )
				{
					
					
					log.debug "**********************************\n ${block.getText()}\n**************************";
					
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
				   log.debug "bodyContent: ${bodyContent}";
				}
				else
				{
				   log.debug "No BodyContent!!! WTF???";
				}
			   
				entry.pageContent = bodyContent;
				
				
			}
			else
			{
				textHandler = bodyContentHandler;

				input = response.entity.content;
				
				Metadata metadata = new Metadata();
				Parser parser = new AutoDetectParser();
				parser.parse(input, textHandler, metadata);
   
				bodyContent = bodyContentHandler.toString();
				if( bodyContent != null && !bodyContent.isEmpty())
				{
				   log.debug "bodyContent: ${bodyContent}";
				}
				else
				{
				   log.debug "No BodyContent!!! WTF???";
				}
			   
				entry.pageContent = bodyContent;
			}
			
			/* Hmmm... we can somehow get a StaleObjectState exception here, possibly because the thread
			 * that originally created this Entry was delayed in committing its transaction for some reason.
			 * We should be able to make up for that by doing a refetch, and then re-persist the updated
			 * object.
			 */
			try
			{
			
				if( entry.save(flush:true))
				{
					log.info( "saved entry (uuid: ${entry.uuid} ) with bodyContent, adding to Lucene index");
					
					doc.add( new Field( "content", bodyContent, Field.Store.NO, Field.Index.ANALYZED, Field.TermVector.YES ) );
					writer.addDocument( doc );
				}
				else
				{
					entry.errors.allErrors.each {log.error( it ); }
				}
			}
			catch( HibernateOptimisticLockingFailureException | StaleObjectStateException sose )
			{

				log.warn( "!!!!!!!!\nCaught HibernateOptimisticLockingFailureException, reloading object and trying again\n!!!!!!!!!");
				log.debug "!!!!!!!!\nCaught HibernateOptimisticLockingFailureException, reloading object and trying again\n!!!!!!!!!";
				
				
				Thread.sleep( 2000 );
				
				// requery the object from the db
				
				entry = Entry.get( entry.id );
				if( !entry )
				{
					throw new RuntimeException( "Failed to locate Entry for uuid: ${uuid}");
				}
				
				entry.pageContent = bodyContent;
				
				try
				{
					if( entry.save(flush:true))
					{
						log.info( "saved entry with bodyContent, adding to Lucene index" );
					
						doc.add( new Field( "content", bodyContent, Field.Store.NO, Field.Index.ANALYZED, Field.TermVector.YES ) );
						writer.addDocument( doc );
					}
					else
					{
						entry.errors.allErrors.each {log.debug it;}
					}
				}
				catch( HibernateOptimisticLockingFailureException | StaleObjectStateException sose2)
				{
					throw new RuntimeException( "Failed Second Attempt To Persist Stale Object Instance!");
				}
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

		}
	}
	
	
	private void extractAndIndexContent( EMailEntry entry, Writer writer )
	{
		log.debug "INDEXING EMAIL ENTRY HERE!!!!!!!!!!!!!!!!!!";
		
		// we need to look up the datasource used to generate this
		// entry, so we can connect and download the content
		// add document to index
		IMAPAccount imapAccount = entry.theDataSource;
		
		log.info( "adding document to index: ${entry.uuid}" );

		try
		{
			// connect and download the message for indexing...
			Document doc = new Document();

			doc.add( new Field( "docType", "docType.entry", Field.Store.YES, Field.Index.NOT_ANALYZED, Field.TermVector.NO ));
			doc.add( new Field( "uuid", entry.uuid, Field.Store.YES, Field.Index.NOT_ANALYZED ) );
			doc.add( new Field( "id", Long.toString( entry.id ), Field.Store.YES, Field.Index.NOT_ANALYZED ) );
			doc.add( new Field( "messageId", entry.messageId, Field.Store.YES, Field.Index.NOT_ANALYZED ) );
			doc.add( new Field( "title", entry.subject, Field.Store.YES, Field.Index.ANALYZED ) );
			doc.add( new Field( "subject", entry.subject, Field.Store.YES, Field.Index.ANALYZED ) );
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
					log.debug "Folder: ${inboxFolder.fullName}";
				}
				else
				{
					log.debug "no inboxFolder!!!";
				}
				
				// search on message id
				MessageIDTerm searchTerm = new MessageIDTerm( entry.messageId );
				Message[] messages = inboxFolder.search( searchTerm );
				
				log.debug "found ${messages.length} matching messages";
				
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
							else
							{
								log.debug "part had type: ${part.getContentType()}";
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
							log.debug "Multipart Message had nothing we could process!";
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
										
					log.debug "####### FORMATTED EMAIL CONTENT\n${formattedContent}\n###########################";
					
					entry.bodyContent = formattedContent;
					
					/* NOTE: This is also susceptible to the StaleObjectState problem, and should
					 * get the same fix as mentioned above:  If we get that exception, reload
					 * the object by ID, then re-persist it.
					 */
					try
					{
					
						if( entry.save())
						{
							log.debug "saved entry with bodyContent, adding to Lucene index";
							
							doc.add( new Field( "content", bodyContent, Field.Store.NO, Field.Index.ANALYZED, Field.TermVector.YES ) );
							writer.addDocument( doc );
							
						}
						else
						{
							
							entry.errors.allErrors.each{ log.debug it }
							log.debug "failed to save EMailEntry with updated bodyContent";
						}
						
					}
					catch( HibernateOptimisticLockingFailureException | StaleObjectStateException sose )
					{
						log.debug "!!!!!!!!\nCaught HibernateOptimisticLockingFailureException, reloading object and trying again\n!!!!!!!!!";
						
						Thread.sleep( 2000 );
						
						// requery the object from the db
						entry = Entry.get( entry.id );
						if( !entry )
						{
							throw new RuntimeException( "Failed to locate Entry for uuid: ${uuid}");
						}
						
						entry.bodyContent = formattedContent;
						
						
						try
						{
							if( entry.save(flush:true))
							{
								log.debug "saved entry with bodyContent, adding to Lucene index";
							
								doc.add( new Field( "content", bodyContent, Field.Store.NO, Field.Index.ANALYZED, Field.TermVector.YES ) );
								writer.addDocument( doc );
							}
							else
							{
								entry.errors.allErrors.each {log.debug it;}
							}
						}
						catch( HibernateOptimisticLockingFailureException | StaleObjectStateException sose2)
						{
							throw new RuntimeException( "Failed Second Attempt To Persist Stale Object Instance!");
						}
						
					}
							
				}
				else
				{
					log.debug "Message not found, or too many matching messages found!";
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
			
		}
		
	}
}
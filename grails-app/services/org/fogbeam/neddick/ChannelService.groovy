package org.fogbeam.neddick


import javax.mail.Address
import javax.mail.Folder
import javax.mail.Message
import javax.mail.MessagingException
import javax.mail.NoSuchProviderException
import javax.mail.Session
import javax.mail.Store
import javax.mail.internet.MimeMessage
import javax.mail.search.ComparisonTerm
import javax.mail.search.ReceivedDateTerm

import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

import com.sun.syndication.feed.synd.SyndEntry
import com.sun.syndication.feed.synd.SyndFeed
import com.sun.syndication.io.SyndFeedInput
import com.sun.syndication.io.XmlReader


class ChannelService {

	def entryService;
	static transactional = false;
	
	@Transactional(propagation=Propagation.REQUIRED)
	public Channel findByName( final String channelName )
	{
		Channel channel = Channel.findByName( channelName );
		return channel;
	}

	@Transactional(propagation=Propagation.REQUIRED)
	public Channel findById( final Long id )
	{
		Channel channel = Channel.findById( id );
		return channel;
	}
	
	@Transactional(propagation=Propagation.REQUIRED)
	public List<Channel> getAllChannels( int channelCount = -1, int offset = 0 )
	{
		List<Channel> allChannels = new ArrayList<Channel>();
		
		if( channelCount == -1 )
		{
			allChannels.addAll( Channel.findAll() );
		}
		else
		{
			// add max
			allChannels.addAll( Channel.executeQuery( "select channel from Channel as channel", [max:channelCount, offset:offset] ) );	
		}
		
		allChannels.sort();
		
		return allChannels;
	}

	@Transactional(propagation=Propagation.REQUIRED)
	public List<Channel> getEligibleAggregateChannels( final User user )
	{
		List<Channel> eligibleChannels = new ArrayList<Channel>();
	
		List<Channel> queryResults = 
			Channel.
				executeQuery( 	"select channel from Channel as channel where " + 
								"channel.name <> :defaultName and " + 
								"(channel.privateChannel = false OR " + 
								"(channel.privateChannel = true and channel.owner = :owner ))", [defaultName:"default", owner:user] );
				
		if( queryResults != null )
		{
			eligibleChannels.addAll( queryResults );
		}
		
		
		return eligibleChannels;
	}

	@Transactional(propagation=Propagation.REQUIRED)
	public List<Channel> getEligibleAggregateChannels( final User user, final Channel theChannel )
	{
		List<Channel> eligibleChannels = new ArrayList<Channel>();
	
		/* if aggregateChannels is empty in the query below, the HQL blows up due to
		 * the empty in () expression.  So we have to do it slightly differently in this case
		 */
		List<Channel> queryResults = null;
		
		Set<Channel> selectedChannels = theChannel.aggregateChannels;
		if( selectedChannels == null || selectedChannels.isEmpty())
		{

			queryResults = Channel.
			executeQuery( 	"select channel from Channel as channel where " +
							"channel.name <> :defaultName and " +
							"channel <> :theChannel and " +
							// "channel not in :selectedChannels and " +
							"(channel.privateChannel = false OR " +
							"(channel.privateChannel = true and channel.owner = :owner ))",
								[defaultName:"default", owner:user, theChannel:theChannel] );
							

		}
		else
		{
		
			queryResults = Channel.
				executeQuery( 	"select channel from Channel as channel where " +
								"channel.name <> :defaultName and " +
								"channel <> :theChannel and " +
								"channel not in :selectedChannels and " + 
								"(channel.privateChannel = false OR " +
								"(channel.privateChannel = true and channel.owner = :owner ))", 
									[defaultName:"default", owner:user, theChannel:theChannel, selectedChannels:selectedChannels] );
								
		}
										
		if( queryResults != null )
		{
			eligibleChannels.addAll( queryResults );
		}
		
		
		return eligibleChannels;
	}
	
	
	// Explicitly NOT transactional, but the nested call to entryService.save() should be
	@Transactional(propagation=Propagation.REQUIRED)
	public void updateFromDatasource( Channel channel )
	{
	
		println( "Updating from DataSource for channel: ${channel.name}" );	
		User anonymous = User.findByUserId( "anonymous" );
		
		// if the specified channel has an RssFeed associated with it...
		List<DataSource> dataSources = 
			DataSource.executeQuery( "select cdsl.channelDataSource from ChannelDataSourceLink as cdsl where cdsl.channel = ? ", [channel] );
		
		if( dataSources != null && dataSources.size() > 0 )
		{
			println( "There are DataSources!" );
			
			for( DataSource dataSource in dataSources )
			{
				try
				{
					updateFromDataSource( dataSource, channel, anonymous );
				}
				catch( Exception e )
				{
					// TODO: clean up the error handling here.
					e.printStackTrace();
					continue;
				}	
			}
		}
	}

	
	private void updateFromDataSource( RssFeed rssFeed, Channel channel, User anonymous )
	{
		// lookup the feed, and get the FeedUrl
		String url = rssFeed.feedUrl;
		println( "Loading from url: ${url}, description: ${rssFeed.description}" );
		
		// load the feed, and create an Entry for each link in the RssFeed
		/*
		URL dummyUrl = new URL(url);
		InputStream dummyInStream = dummyUrl.getContent();
		BufferedReader dummyReader = new BufferedReader(new InputStreamReader(dummyInStream));
		String dummyResult;
		String dummyLine = dummyReader.readLine();
		dummyResult = dummyLine;
		while((dummyLine=dummyReader.readLine())!=null){
			dummyResult+=dummyLine;
		}
		
		println ("url content: " + dummyResult );
		*/
		
		URL feedUrl = new URL(url);
		SyndFeedInput input = new SyndFeedInput();
		SyndFeed feed = null;
		XmlReader reader = null;
		
		HttpURLConnection conn = (HttpURLConnection)feedUrl.openConnection();
		conn.setRequestProperty( "User-Agent", "Mozilla/5.0 (X11; Linux i686; rv:25.0) Gecko/20100101 Firefox/25.0" );
		try
		{
			reader = new XmlReader(conn)
			feed = input.build(reader);
			log.debug( "Feed: ${feed.getTitle()}" );
			
			List<SyndEntry> entries = feed.getEntries();
			
			log.debug( "processing ${entries.size()} entries!" );
			int good = 0;
			int bad = 0;
			
			for( SyndEntry entry in entries )
			{
				// TODO: wrap this in a try/catch so one bad
				// link doesn't fail the entire feed.
				
				
				String linkUrl = entry.getLink();
				String linkTitle = entry.getTitle();
				
				List<Entry> testForExisting = entryService.findByUrlAndChannel( linkUrl, channel );
				if( testForExisting != null && testForExisting.size() > 0 )
				{
					log.debug( "An Entry for this link already exists. Skipping" );
					continue;
				}
				else
				{
					
					// does this link exist elsewhere in the system (eg, linked to another channel)?
					List<Entry> e2 = entryService.findByUrl( linkUrl );
					if( e2 != null && e2.size() > 0 )
					{
						// we already have this Entry, so instead of creating a new Entry object, we just
						// need to link this one to this Channel.
						Entry existingEntry = e2.get(0);
						existingEntry.addToChannels( channel );
						existingEntry.save();
					}
					else
					{
					
						log.debug( "creating and adding entry for link: ${linkUrl} with title: ${linkTitle}" );
			
						Entry newEntry = new WebpageEntry( url: linkUrl, title: linkTitle, submitter: anonymous, theDataSource:rssFeed );
						
						boolean success = entryService.saveEntry( newEntry );
					
						if( success )
						{
							newEntry.addToChannels( channel );
							
							good++;
							log.debug( "saved new Entry with id: ${newEntry.id}" );
							// send JMS message saying "new entry submitted"
							def newEntryMessage = [msgType:"NEW_ENTRY", id:newEntry.id, uuid:newEntry.uuid, url:newEntry.url, title:newEntry.title ];
			
							log.debug( "sending new entry message to JMS entryQueue");
							// send a JMS message to our entryQueue
							sendJMSMessage("entryQueue", newEntryMessage );
					
							log.debug( "sending new entry message to JMS searchQueue" );
							// send a JMS message to our searchQueue
							sendJMSMessage("searchQueue", newEntryMessage );
						
						}
						else
						{
							bad++;
							// failed to save newEntry
							println( "Failed to save newEntry!" );
						}
					}
				}
			}
			
			log.debug( "Good entries: ${good}, bad entries:${bad}" );
			
		}
		catch( Exception e )
		{
			println "Caught Exception in Feed Processing Loop!";
			e.printStackTrace();
			
		}
		finally
		{
			if( reader != null )
			{
				reader.close();
			}
		}
	}

	private void updateFromDataSource( IMAPAccount imapAccount, Channel channel, User anonymous )
	{
		println "Updating Channel from IMAPAccount";
		
		
		ChannelDataSourceLink link = ChannelDataSourceLink.getLink( channel, imapAccount );
			
		Properties props = System.getProperties();
		props.setProperty("mail.store.protocol", "imaps");
		Folder inboxFolder = null;
		try
		{
			Session session = Session.getDefaultInstance(props, null);
			Store store = session.getStore("imaps");
		
			
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

			if( inboxFolder.hasNewMessages() )
			{
				println "there are new messages!";
			}
						
			println "${inboxFolder.getMessageCount()} messages in folder!";
						
			
			// search for messages received since the dateLastPolled
			Date dateLastPolled = link.dateLastPolled;
			println "dateLastPolled: ${dateLastPolled}";
			ReceivedDateTerm searchTerm = new ReceivedDateTerm( ComparisonTerm.GT, dateLastPolled );
			Message[] messages = inboxFolder.search( searchTerm );
			
			println "found ${messages.length} matching messages";
			
			// for each Message
			int good = 0;
			int bad = 0;
			messages.each 
			{			
				// check if we have an entry with this message-id already
			
				// if not, construct an entry from this Message
			
				MimeMessage msg = it;
				println "from: ${msg.getFrom()[0].toString()}";
				println( "messageId: ${msg.getMessageID()}");
				
				
				List<Entry> testForExisting = entryService.findByUrlAndChannel( msg.getMessageID(), channel );
				if( testForExisting != null && testForExisting.size() > 0 )
				{
					println( "An Entry for this message already exists. Skipping" );
				}
				else
				{
					
					// does this link exist elsewhere in the system (eg, linked to another channel)?
					List<Entry> e2 = entryService.findByUrl( msg.getMessageID() );
					if( e2 != null && e2.size() > 0 )
					{
						// we already have this Entry, so instead of creating a new Entry object, we just
						// need to link this one to this Channel.
						Entry existingEntry = e2.get(0);
						existingEntry.addToChannels( channel );
						existingEntry.save();
					}
					else
					{
					
						String subject = msg.getSubject();
						Entry newEntry = new EMailEntry( messageId: msg.getMessageID(), title: subject, subject: subject, submitter: anonymous, theDataSource:imapAccount );
						
						Address[] fromAddresses = msg.getFrom();
						fromAddresses.each { newEntry.addToFromAddress( it.getAddress())};
						
						Address[] toAddresses = msg.getRecipients(Message.RecipientType.TO);
						toAddresses.each { newEntry.addToToAddress( it.getAddress() ) };
						
						Date receivedDate = msg.getReceivedDate();
						
						boolean success = entryService.saveEntry( newEntry );
						
						if( success )
						{
							newEntry.addToChannels( channel );
							good++;
							
							println( "saved new Entry with id: ${newEntry.id}" );
							// send JMS message saying "new entry submitted"
							def newEntryMessage = [msgType:"NEW_ENTRY", id:newEntry.id, uuid:newEntry.uuid, messageId:newEntry.messageId, title:newEntry.title ];
			
							println( "sending new entry message to JMS entryQueue");
							// send a JMS message to our entryQueue
							sendJMSMessage("entryQueue", newEntryMessage );
					
							log.debug( "sending new entry message to JMS searchQueue" );
							// send a JMS message to our searchQueue
							sendJMSMessage("searchQueue", newEntryMessage );	
							
						}
						else
						{
							println "failed to save Entry!";
							bad++;
						}
						
					}
				}
				
			}

			println "done";
			
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
			if( inboxFolder.isOpen() )
			{
				inboxFolder.close( false );
			}
		}
		
		// update the link between this datasource and this channel to reflect when it
		// was last polled for entries.
		link.dateLastPolled = new Date();
		link.save();
		
	}		
	
	
	@Transactional(propagation=Propagation.REQUIRED)
	public List<Channel> findChannelsWithDatasource()
	{
		List<Channel> channels = new ArrayList<Channel>();
		
		List subList = Channel.executeQuery( "select channel from Channel as channel where channel.dataSourceLinks is not empty" );
		
		channels.addAll( subList );
		
		return channels;	
	}
}
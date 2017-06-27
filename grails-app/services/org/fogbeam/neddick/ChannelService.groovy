package org.fogbeam.neddick


import groovy.json.JsonSlurper

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

import org.scribe.builder.ServiceBuilder
import org.scribe.builder.api.TwitterApi
import org.scribe.model.OAuthRequest
import org.scribe.model.Response
import org.scribe.model.Token
import org.scribe.model.Verb
import org.scribe.oauth.OAuthService
import org.springframework.test.annotation.NotTransactional
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

	// Explicitly NOT transactional, but the nested call to entryService.save() should be
	private void updateFromDataSource( RssFeed rssFeed, Channel channel, User anonymous )
	{
		// lookup the feed, and get the FeedUrl
		String url = rssFeed.feedUrl;
		log.info( "Loading from url: ${url}, description: ${rssFeed.description}" );
		
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
			log.info( "Feed: ${feed.getTitle()}" );
			
			List<SyndEntry> entries = feed.getEntries();
			
			log.debug( "processing ${entries.size()} entries!" );
			int good = 0;
			int bad = 0;
			
			for( SyndEntry entry in entries )
			{
				// TODO: wrap this in a try/catch so one bad
				// link doesn't fail the entire feed.
				
				
				String linkUrl = entry.getLink();
				if( linkUrl != null ) 
				{
					linkUrl = linkUrl.trim();
				}
				
				String linkTitle = entry.getTitle();
				if( linkTitle != null )
				{
					linkTitle = linkTitle.trim();
				}
				
				List<Entry> testForExisting = entryService.findByUrlAndChannel( linkUrl, channel );
				if( testForExisting != null && testForExisting.size() > 0 )
				{
					log.info( "An Entry for this link: linkUrl: ${linkUrl} and channel: ${channel}, already exists. Skipping" );
					continue;
				}
				else
				{
					log.info( "Initial test for duplication found no results for linkUrl: ${linkUrl} and channel: ${channel}");
					
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
					
						log.info( "creating and adding entry for link: ${linkUrl} with title: ${linkTitle}" );
			
						Entry newEntry = new WebpageEntry( url: linkUrl, title: linkTitle, submitter: anonymous, theDataSource:rssFeed );
						
						boolean success = entryService.saveEntry( newEntry, channel );
					
						if( success )
						{
							// TODO: could *this* be what's causing our StaleObjectState problem?
							// newEntry.addToChannels( channel );
							
							good++;
							log.debug( "saved new Entry with id: ${newEntry.id}" );
							// send JMS message saying "new entry submitted"
							def newEntryMessage = [msgType:"NEW_ENTRY", id:newEntry.id, uuid:newEntry.uuid, url:newEntry.url, title:newEntry.title ];
			
							log.debug( "sending new entry message to JMS entryQueue");
							// send a JMS message to our entryQueue
							// sendJMSMessage("entryQueue", newEntryMessage );
					
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
	
	// Explicitly NOT transactional, but the nested call to entryService.save() should be
	private void updateFromDataSource( TwitterAccount twitterAccount, Channel channel, User anonymous)
	{
		
		println "Updating Channel from TwitterAccount";
						
		OAuthService service = new ServiceBuilder()
		.provider(TwitterApi.class)
		// .apiKey("bwUbU865CNQtt2Xdb62FpQ")
		// .apiSecret("opkW7kQEqJP1YMHE0xYXhxXOD5XOfkVeaw2hTQPY")
		.apiKey( "orGS7crqDqjS76B5RS2w" )
		.apiSecret( "GdtSdh6YzrlqusCOJaFUDvelJtZHzUTELi0pn9DHqA" )
		.build();

		// for proper timeline managment using since_id and max_id, blah..
		ChannelDataSourceLink link = ChannelDataSourceLink.getLink( channel, twitterAccount );
		String sinceId = link.sinceId;
		println "initial sinceId: ${sinceId}";
		
		String maxId = null; // no maxId for first iteration
		
		// call until we don't get anything else?
		boolean keepLooping = true;
		Long newSinceId = Long.parseLong( sinceId );
		int loopCount = 1; 
		while( keepLooping )
		{
			if( loopCount > 3 )
			{ 
				println "looped too many times, forced break";
				break; 
			}
			
			loopCount++;
			
			OAuthRequest oauthRequest = new OAuthRequest(Verb.GET, "https://api.twitter.com/1.1/statuses/home_timeline.json");
			oauthRequest.addQuerystringParameter( "count", "200" );
			if( Long.parseLong( sinceId ) > 0 )
			{
				println "setting sinceId for request to: ${sinceId}";
				oauthRequest.addQuerystringParameter( "since_id", sinceId );
			}
			if( maxId != null )
			{
				println "setting maxId for request to: ${maxId}";
				oauthRequest.addQuerystringParameter( "max_id", maxId );
			}
		
		
			Token accessToken = new Token(twitterAccount.accessToken, twitterAccount.tokenSecret);
			// println "got access token";
			
			service.signRequest(accessToken, oauthRequest);
			
			// println "signed request";
			
			Response oauthResponse = oauthRequest.send();
		
			String responseJSON = oauthResponse.getBody();
			
			// println "JSON response: \n ${responseJSON}";
			
			def slurper = new JsonSlurper();
			def result = slurper.parseText( responseJSON );
		
			Long highTweetId = 0L;
			Long lowTweetId = null;
			
			
			/* after this loop, highTweetId will have the highest number (newest tweet) we saw.  The
			 * highTweetId after every iteration of the outer loop, should become our new value of
			 * sinceId.  
			 * 
			 * lowTweetId will be the lowest valued (oldest) tweet we saw, so it becomes the value of
			 * maxId that is used in subsequent iterations of the outer loop, as we page our way
			 * through the currently available tweets.  
			 */
			if( result.size > 0 )
			{
				println "got results to process, beginning Tweet processing loop";
				
				result.each 
				{ 
				
					Long tweetId = Long.parseLong( it.id_str );
	
					println "current tweet id: ${tweetId}";
	
					// test if we already have this Tweet.  If we do, just return and let the each() iterator carry
					// on to the next record.
					TwitterEntry existingEntry = TwitterEntry.findByTweetId( tweetId );
					if( existingEntry != null )
					{
						// Note: This is groovy and you can't do "continue" in a closure being iterated on by each().  You
						// just return from the closure method call, and the iterator moves on, which has the same basic
						// effect as "continue"ing from a Java style loop
						return;
					}
									
					if( lowTweetId == null )
					{
						lowTweetId = new Long( tweetId );	
					}
					else
					{
						if( tweetId < lowTweetId )
						{
							lowTweetId = new Long( tweetId );
						}	
					}
	
					if( tweetId > highTweetId )
					{
						highTweetId = new Long( tweetId );
					}
									
				
					Entry newEntry = new TwitterEntry( title: it.text, tweetContent: it.text, submitter: anonymous, theDataSource:twitterAccount );
					newEntry.senderScreenName = it.user.screen_name;
					newEntry.senderFullName = it.user.name;
					newEntry.tweetId = tweetId;
				
					boolean success = entryService.saveEntry( newEntry );
			
					if( success )
					{
						newEntry.addToChannels( channel );
					
						// good++;
						log.debug( "saved new Entry with id: ${newEntry.id}" );
						// send JMS message saying "new entry submitted"
						def newEntryMessage = [msgType:"NEW_ENTRY", id:newEntry.id, uuid:newEntry.uuid, title:newEntry.title ];
	
						log.debug( "sending new entry message to JMS entryQueue");
						// send a JMS message to our entryQueue
						sendJMSMessage("entryQueue", newEntryMessage );
			
						log.debug( "sending new entry message to JMS searchQueue" );
						// send a JMS message to our searchQueue
						sendJMSMessage("searchQueue", newEntryMessage );
				
					}
					else
					{
						// bad++;
						// failed to save newEntry
						println( "Failed to save newEntry!" );
					}

				};
		
				println "finished Tweet processing loop";
			
				if( lowTweetId != null )
				{
					maxId = Long.toString( lowTweetId -1  ); // minus one because this is done
													     // on an inclusive basis and we don't want the duplicated entry
					println "lowTweetId was: ${maxId}";
				}
				else
				{
					maxId = null;
				}
				
				if( highTweetId > newSinceId )
				{
					println "highTweetId = ${highTweetId}, replacing old value of newSinceId: ${newSinceId}";
					newSinceId = new Long( highTweetId );
				}
			}
			else
			{
				println "got no tweets, nothing to do";
				// we didn't get any results, so we've paged all the way down through
				// everything new since the sinceId.  Stop this loop now.
				keepLooping = false;
			}
		
		} 
		
		// update sinceId if we got a new one 
		if( newSinceId > Long.parseLong( sinceId ) )
		{
			println "Persisting new value for link.since_id: ${newSinceId}";
			link.sinceId = Long.toString( newSinceId );
			link.save();
		}
		
	}
	
	// Explicitly NOT transactional, but the nested call to entryService.save() should be
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

			if( !inboxFolder.hasNewMessages() )
			{
				println "Folder has no new messages!";
				return;
			}
						
			println "${inboxFolder.getMessageCount()} messages in folder!";
						
			
			// search for messages received since the dateLastPolled
			Date dateLastPolled = link.dateLastPolled;
			println "dateLastPolled: ${dateLastPolled}";
			log.info( "dateLastPolled: ${dateLastPolled}" );
			
			ReceivedDateTerm searchTerm = new ReceivedDateTerm( ComparisonTerm.GT, dateLastPolled );
			Message[] messages = inboxFolder.search( searchTerm );
			
			println "found ${messages.length} matching messages";
			log.info("found ${messages.length} matching messages");
			
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
					println( "An Entry for this message [${msg.getMessageID()}] already exists. Skipping" );
					log.info( "An Entry for this message [${msg.getMessageID()}] already exists. Skipping" );
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
			
			// update the link between this datasource and this channel to reflect when it
			// was last polled for entries.
			link.dateLastPolled = new Date();
			if( !link.save())
			{
				link.errors.allErrors.each  { println it; }
			}
			
		}
		

		
	}		
	
	
	@Transactional(readOnly=true)
	public List<Channel> findChannelsWithDatasource()
	{
		List<Channel> channels = new ArrayList<Channel>();
		
		List subList = Channel.executeQuery( "select channel from Channel as channel where channel.dataSourceLinks is not empty" );
		
		channels.addAll( subList );
		
		return channels;	
	}
}
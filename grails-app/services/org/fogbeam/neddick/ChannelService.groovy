package org.fogbeam.neddick

import com.sun.syndication.feed.synd.SyndEntry 
import com.sun.syndication.feed.synd.SyndFeed 
import com.sun.syndication.io.SyndFeedInput 
import com.sun.syndication.io.XmlReader 
import org.fogbeam.neddick.Channel;

class ChannelService {

	def entryService;
	
	public Channel findByName( final String channelName )
	{
		Channel channel = Channel.findByName( channelName );
		return channel;
	}

	public List<Channel> getAllChannels()
	{
		List<Channel> allChannels = new ArrayList<Channel>();
		allChannels.addAll( Channel.findAll() );
		
		allChannels.sort();
		
		return allChannels;
	}

	public void updateFromDatasource( Channel channel )
	{
	
		println "Updating from DataSource for channel: ${channel.name}";	
		User anonymous = User.findByUserId( "anonymous" );
		
		// if the specified channel has an RssFeed associated with it...
		Set<RssFeed> feeds = channel.feeds;
		
		if( feeds != null && feeds.size() > 0 )
		{
			println "There are feeds!";
			
			for( RssFeed rssFeed in feeds )
			{
			
				// lookup the feed, and get the FeedUrl
				String url = rssFeed.feedUrl;
				println "Loading from url: ${url}";
				
				// load the feed, and create an Entry for each link in the RssFeed
				URL feedUrl = new URL(url);
				SyndFeedInput input = new SyndFeedInput();
				SyndFeed feed = null;
				XmlReader reader = null;
				try
				{
					reader = new XmlReader(feedUrl)
					feed = input.build(reader);
					println "Feed: ${feed.getTitle()}"
					
					List<SyndEntry> entries = feed.getEntries();
					
					println "processing ${entries.size()} entries!";
					int good = 0;
					int bad = 0;
					
					for( SyndEntry entry in entries )
					{
						String linkUrl = entry.getLink();
						String linkTitle = entry.getTitle();
						
						List<Entry> testForExisting = entryService.findByUrlAndChannel( linkUrl, channel );
						if( testForExisting != null && testForExisting.size() > 0 )
						{
							println "An Entry for this link already exists. Skipping";
							continue;
						}
						
						println "creating and adding entry for link: ${linkUrl} with title: ${linkTitle}"
					
						Entry newEntry = new Entry( url: linkUrl, title: linkTitle, channel: channel, submitter: anonymous );
						
						entryService.saveEntry( newEntry );
						if( newEntry )
						{
							good++;
							println "saved new Entry with id: ${newEntry.id}";
							// send JMS message saying "new entry submitted"
							def newEntryMessage = [msgType:"NEW_ENTRY", id:newEntry.id, uuid:newEntry.uuid, url:newEntry.url, title:newEntry.title ];
					
							// send a JMS message to our testQueue
							sendJMSMessage("entryQueue", newEntryMessage );
						}
						else
						{
							bad++;
							// failed to save newEntry
							println "Failed to save newEntry!"
						}
					
					}
					
					println "Good entries: ${good}, bad entries:${bad}";
					
				}
				catch( Exception e )
				{
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
		}
	}

	public List<Channel> findChannelsWithDatasource()
	{
		List<Channel> channels = new ArrayList<Channel>();
		
		List subList = Channel.executeQuery( "select channel from Channel as channel where channel.feeds is not empty" );
		
		channels.addAll( subList );
		
		return channels;	
	}
		
}

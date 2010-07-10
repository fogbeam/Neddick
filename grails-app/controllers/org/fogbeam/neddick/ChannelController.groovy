package org.fogbeam.neddick

import com.sun.syndication.feed.synd.SyndEntry 
import com.sun.syndication.feed.synd.SyndFeed 
import com.sun.syndication.io.SyndFeedInput 
import com.sun.syndication.io.XmlReader 

class ChannelController {

	def scaffold = true;
	def channelService;
	def entryService;
	def siteConfigService;
	
	int itemsPerPage = -1;
	
	def list = {
			
    	if( itemsPerPage == -1 )
    	{
    		itemsPerPage = Integer.parseInt( siteConfigService.getSiteConfigEntry( "itemsPerPage" ));
    	}
    	
    	String requestedPageNumber = params.pageNumber;
    	int pageNumber = 1;
    	int availablePages = -1;
    	if( requestedPageNumber != null )
    	{
    		try
    		{
    			pageNumber = Integer.parseInt( requestedPageNumber );
    		}
    		catch( NumberFormatException nfe )
    		{
    			flash.message = "Invalid Pagenumber requested";
    			pageNumber = 1;
    		}
    	}
    	
    	println "requested pageNumber: ${pageNumber}";			
			
		List<Channel> allChannels = new ArrayList<Channel>();
		allChannels.addAll( channelService.getAllChannels());
	
    	int dataSize = allChannels.size();
    	println "dataSize: ${dataSize}";
    	int pages = dataSize / itemsPerPage;
		println "dataSize / itemsPerPage = ${pages}"
    	pages = Math.max( pages, 1 );
		
		println "pages: ${pages}";
		
		if( dataSize > (pages*itemsPerPage) )
		{
			println "WTF:  ${dataSize % (pages*itemsPerPage)}";
			pages += 1;
		}
		
		availablePages = pages;
    	println "availablePages: ${availablePages}";
    	
    	if( pageNumber < 1 )
    	{
    		flash.message = "Invalid Pagenumber ${requestedPageNumber} requested";
    		pageNumber = 1;
    	}
    	if( pageNumber > availablePages )
    	{
    		flash.message = "Invalid Pagenumber ${requestedPageNumber} requested";
    		pageNumber = availablePages;
    	}
    	
    	
    	// get the requested page of entry UUIDs
    	int beginIndex = ( pageNumber * itemsPerPage ) - itemsPerPage;
    	int endIndex = Math.min( dataSize -1, ((pageNumber * itemsPerPage ) - 1));
    	
		if( pageNumber == pages )
		{
			endIndex = Math.min( dataSize -1, endIndex);
		}        	
    	
		
    	List<Tag> subList = null;
		
		if( dataSize > 0 )
		{
			subList = allChannels[ beginIndex .. endIndex ];		
		}
		else
		{
			subList = new ArrayList<Channel>();	
		}
		
		[allChannels: subList, currentPageNumber: pageNumber, availablePages: availablePages ];	
	}
	
	def updateFromDatasource = {
		
		User anonymous = User.findByUserId( "anonymous" );
			
		// lookup the specified channel
		String channelName = params.channelName;
		if( channelName )
		{
			Channel theChannel = channelService.findByName( channelName );
			
			// if the specified channel has an RssFeed associated with it...
			Set<RssFeed> feeds = theChannel.feeds;
			
			if( feeds != null && feeds.size() > 0 )
			{
				for( RssFeed rssFeed in feeds )
				{
				
					// lookup the feed, and get the FeedUrl
					String url = rssFeed.feedUrl; 
				
					// load the feed, and create an Entry for each link in the RssFeed
					URL feedUrl = new URL(url);
		        	SyndFeedInput input = new SyndFeedInput();
		        	SyndFeed feed = null;
		        	try 
		        	{
		        		feed = input.build(new XmlReader(feedUrl));
		        		println "Feed: ${feed.getTitle()}"
		        	
		        		List<SyndEntry> entries = feed.getEntries(); 
		        		
		        		for( SyndEntry entry in entries )
		        		{	
		        			String linkUrl = entry.getLink();
		        			String linkTitle = entry.getTitle();
		        			
		        			println "creating and adding entry for link: ${linkUrl} with title: ${linkTitle}"
		        		
		        			Entry newEntry = new Entry( url: linkUrl, title: linkTitle, channel: theChannel, submitter: anonymous );
		        	    	
		        			entryService.saveEntry( newEntry );
		        			if( newEntry )
		        			{
		        	    	
		        				// send JMS message saying "new entry submitted"
		        				def newEntryMessage = [msgType:"NEW_ENTRY", id:newEntry.id, uuid:newEntry.uuid, url:newEntry.url, title:newEntry.title ];
		        	    
		        				// send a JMS message to our testQueue
		        				sendJMSMessage("testQueue", newEntryMessage );
		        			}
		        			else
		        			{
		        				// failed to save newEntry
		        				println "Failed to save newEntry!"
		        			}
		        		
		        		}
		        	}
		        	catch( Exception e )
		        	{
		        		e.printStackTrace();
		        	}
				
				}
			}
			
		}

			
		// done... return.
		System.out.println( "done" );
		render( "DONE" );
	}
}

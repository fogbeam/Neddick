package org.fogbeam.neddick

class RssFeedController {

	def scaffold = false;

	def index =
	{
		redirect( controller:"rssFeed", action:"list" );
	}
	
	def list = 
	{
		List<RssFeed> allFeeds = RssFeed.list();
		[allFeeds:allFeeds];	
	}
	
	def edit = 
	{
		RssFeed theFeed = RssFeed.findById( params.id );
		List<Integer> days = 1..31;
		List<Integer> months = 1..12;
		List<Integer> years = 1911..2111;
		Calendar theDate = Calendar.getInstance();
		theDate.setTime( theFeed.dateCreated );
		[theFeed:theFeed, theDate:theDate, days:days, months:months,years:years];
	}
	
	def delete =
	{
		log.debug( "_action_delete: ${params._action_delete}" );
		
		RssFeed theFeed = RssFeed.findById( params.id );
				
		if( params._action_delete )
		{
		
			// find any channels that link to this feed
			List<Channel> linkedChannels = Channel.executeQuery( "select channel from Channel as channel where ? in elements(channel.feeds)", [theFeed] );
			
			// iterate over that list and remove the feed from the feeds list of that channel
			for( Channel channel : linkedChannels ) 
			{
				RssFeed feedToDelete = channel.feeds.find { it.id == theFeed.id };
				channel.removeFromFeeds( feedToDelete );
				channel.save();
			}
			
			// now we can delete the feed
			log.debug( "Deleting feed: ${params.id}" );
			theFeed.delete();
		}
		else
		{
			// TODO: fix this
			log.debug( "Huh, WTF?!????" );
		}
		
		redirect( controller:"rssFeed", action:"list" );
	}
	
	def update = 
	{	
		log.debug( "RssFeed update!" );
		RssFeed theFeed = RssFeed.findById( params.id );

		if( params._action_update )
		{
			log.debug( "Updating feed: ${params.id}" );
		
			theFeed.feedUrl = params.feedUrl;
			theFeed.description = params.feedDescription;
		
			if( !theFeed.save(flush:true, validate:true) )
			{
				log.error( "Error saving Feed" );
				// theFeed.errors.allErrors.each { p rintln it };
			}
		}
		else
		{
			// TODO: fix this
			log.debug( "Huh, WTF?!????" );	
		}
		
		redirect( controller:"rssFeed", action:"list" );
	}
	
	def create = 
	{
		[];
	}
	
	def save = 
	{
		RssFeed newFeed = new RssFeed();
		newFeed.feedUrl = params.feedUrl;
		newFeed.description = params.description;
		RssFeed.withTransaction { status -> 
			
			if( !newFeed.save(flush:true, validate:true) )
			{
				log.error( "Error saving Feed" );
				// newFeed.errors.allErrors.each { p rintln it };
			}
		}
		
		redirect( controller:"rssFeed", action:"list" );
	}
}
package org.fogbeam.neddick.controller.mixins

import java.text.Collator

import org.fogbeam.neddick.Channel
import org.fogbeam.neddick.Tag
import org.fogbeam.neddick.User
import org.fogbeam.neddick.UserFavoriteChannelLink

import com.amjjd.alphanum.AlphanumericComparator
// import org.restlet.engine.util.AlphaNumericComparator


class SidebarPopulatorMixin
{
	Map populateSidebarCollections( def controller, User user )
	{ 
		println "called populateSidebarCollections!";
		
		/**************** MY TAGS ******************/
		// get a list of the distinct tags that I have used
		def	tagList = new ArrayList<Tag>();
		tagList.addAll( controller.tagService.getTagListForUser( user ) );
	
		def chunkedMyTags = new ArrayList<List<Tag>>();
		Collections.sort( tagList, new AlphanumericTagComparator<Tag>( Collator.getInstance(Locale.ENGLISH) ));
		
				
		int iChunkedMyTags = 0;
		def chunkListMyTags = new ArrayList<Tag>();
		chunkedMyTags.add( chunkListMyTags );
		tagList.each {
			
			if( (iChunkedMyTags != 0 ) && ( iChunkedMyTags % 10 == 0 ))
			{
				chunkListMyTags = new ArrayList<Tag>();
				chunkedMyTags.add( chunkListMyTags );
				
			}
			iChunkedMyTags++;
			chunkListMyTags.add( it );
		}
		
		
		/**************** POPULAR TAGS ****************/
		def popularTags = new ArrayList<Channel>();
		// controller.tagService.getPopularTags();
		
		def chunkedPopularTags = new ArrayList<List<Tag>>();
		
		
		/*************** MY FAVORITE CHANNELS *************/
		def allFavoriteChannelLinks = user.userFavoriteChannels;
		def allFavoriteChannels = new ArrayList<Channel>();
		for( UserFavoriteChannelLink link : allFavoriteChannelLinks )
		{
			allFavoriteChannels.add( link.channel );
		}

		Collections.sort( allFavoriteChannels, new AlphanumericChannelComparator<Channel>( Collator.getInstance(Locale.ENGLISH) ));

		def chunkedFavoriteChannels = new ArrayList<List<Channel>>();
		int iChunkedFavoriteChannels = 0;
		def chunkListFavoriteChannels = new ArrayList<Channel>();
		chunkedFavoriteChannels.add( chunkListFavoriteChannels );
		allFavoriteChannels.each {
			
			if( (iChunkedFavoriteChannels != 0 ) && ( iChunkedFavoriteChannels % 10 == 0 ))
			{
				chunkListFavoriteChannels = new ArrayList<Channel>();
				chunkedFavoriteChannels.add( chunkListFavoriteChannels );
				
			}
			iChunkedFavoriteChannels++;
			chunkListFavoriteChannels.add( it );
		}
				
		
		
		/**************** ALL CHANNELS ******************/
		
		def allChannels = new ArrayList<Channel>();		
		
		allChannels.addAll( controller.channelService.getAllChannels());
		
		Collections.sort( allChannels, new AlphanumericChannelComparator<Channel>( Collator.getInstance(Locale.ENGLISH) ));
		
		/* chunk this up and rejigger it so that we return a list of lists.  This
		 * supports out "carousel" approach to presenting this stuff, without requiring
		 * a shit ton of complicated logic in the GSP pages 
		 */
		def chunkedChannels = new ArrayList<List<Channel>>();
		int i = 0;
		def chunkList = new ArrayList<Channel>();
		chunkedChannels.add( chunkList );
		allChannels.each {
			
			if( (i != 0 ) && ( i % 10 == 0 ))
			{
				chunkList = new ArrayList<Channel>();
				chunkedChannels.add( chunkList );
				
			}
			i++;
			chunkList.add( it );
		}
		
		
		return [myTags: tagList, popularTags:popularTags, allChannels:allChannels, 
				chunkedChannels:chunkedChannels,
				chunkedFavoriteChannels:chunkedFavoriteChannels,
				chunkedMyTags:chunkedMyTags,
				chunkedPopularTags:chunkedPopularTags];
	}
	
	
}

class AlphanumericChannelComparator<T> extends AlphanumericComparator
{	
	
	public AlphanumericChannelComparator() {
		super();
	}
	
	public AlphanumericChannelComparator(Comparator collator)
	{
		super(collator);
	}
	
	int compare( T arg0,  T arg1)
	{
		// println "arg0: ${arg0.name}, arg1: ${arg1.name}";
		return super.compare( arg0.name, arg1.name );
	};
}

class AlphanumericTagComparator<T> extends AlphanumericComparator
{
	
	public AlphanumericTagComparator() {
		super();
	}
	
	public AlphanumericTagComparator(Comparator collator)
	{
		super(collator);
	}
	
	int compare( T arg0,  T arg1)
	{
		// println "arg0: ${arg0.name}, arg1: ${arg1.name}";
		return super.compare( arg0.name, arg1.name );
	};
}

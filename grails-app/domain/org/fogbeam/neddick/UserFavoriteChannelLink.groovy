package org.fogbeam.neddick

import java.util.Date;

class UserFavoriteChannelLink
{
	Long id;
	// the two classes we are linking...
	Channel channel;
	User user;
	
	// additional attributes about the link itself...
	Date dateCreated;
	
	static mapping = {
		channel lazy: false;
		user lazy: false;
	}
	

	/* static utility methods for managing the linking of channels and users */
	static UserFavoriteChannelLink link(Channel channel, User user )
   {
	   println "linking...";
		
	   // look for an existing matching link
	   List<UserFavoriteChannelLink> links = UserFavoriteChannelLink.executeQuery( "select link from UserFavoriteChannelLink as link where link.channel = ? and link.user = ?", [channel, user] );
	   
	   def link = null;
	   if( links != null && !links.size == 0 )
	   {
		   link = links[0];
	   }
	   
	   if ( !link )
	   {
		   link = new UserFavoriteChannelLink();
		   channel?.addToUserFavoriteChannels(link);
		   user?.addToUserFavoriteChannels(link);
		   link.save();
	   }
	   
	   return link;
   }

   static void unlink(Channel channel, User user )
   {
	   def link = UserFavoriteChannelLink.executeQuery( "select link from UserFavoriteChannelLink as link where link.channel = ? and link.user = ?", [channel, user] );
	   if ( link )
	   {
		   user?.removeFromUserFavoriteChannels(link);
		   channel?.removeFromUserFavoriteChannels(link);
		   link.delete();
	   }
   }
	
	
}

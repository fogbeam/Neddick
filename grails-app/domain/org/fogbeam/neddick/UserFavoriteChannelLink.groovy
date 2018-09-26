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
	   UserFavoriteChannelLink.log.debug( "linking..." );
	   
	   if( user == null )
	   {
		   UserFavoriteChannelLink.log.error( "user must not be null!" );
		   throw new IllegalArgumentException( "user must not be null!" );
	   }
	   
	   if( channel == null )
	   {
		   UserFavoriteChannelLink.log.error( "channel must not be null!" );
		   throw new IllegalArgumentException( "channel must not be null!" );
	   }
	   
	   // look for an existing matching link
	   List<UserFavoriteChannelLink> links = UserFavoriteChannelLink.executeQuery( "select link from UserFavoriteChannelLink as link where link.channel = ? and link.user = ?", [channel, user] );
	 
	   UserFavoriteChannelLink.log.info( "done querying for existing links.  Found ${links?.size} existing links for channel = ${channel} and user = ${user}" );
	   
	   if( links != null && links.size > 1 )
	   {
		   UserFavoriteChannelLink.log.error( "More than one existing link found!" );
		   throw new IllegalStateException( "More than one existing link found! (${links.size})" );
	   }
	   
	   def link = null;
	   if( links != null && links.size == 1 )
	   {
		   UserFavoriteChannelLink.log.info( "Setting 'link' object to existing UserFavoriteChannelLink with id = ${links[0].id}" );
		   link = links[0];
	   }
	   else
	   {
		   UserFavoriteChannelLink.log.info( "No existing link found! -- ${link}" );
	   }
	   
	   if( link == null )
	   {
		   UserFavoriteChannelLink.log.info( "Existing link not found" );
		   
		   link = new UserFavoriteChannelLink();
		   
		   channel?.addToUserFavoriteChannels(link);
		   user?.addToUserFavoriteChannels(link);
		   
		   UserFavoriteChannelLink.log.info( "About to save new UserFavoriteChannelLink" );
		   
		   if( !link.save(flush:true) )
		   {
			   UserFavoriteChannelLink.log.error( "Failed to save UserFavoriteChannelLink" );
			   link.errors.allErrors.each { UserFavoriteChannelLink.log.error( it.toString() ) }
		   }
		   else
		   {
			   UserFavoriteChannelLink.log.info( "Link saved!" );
		   }
	   }
	   else
	   {
		   UserFavoriteChannelLink.log.info( "Found existing link for channel: ${channel} and user: ${user}" );
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

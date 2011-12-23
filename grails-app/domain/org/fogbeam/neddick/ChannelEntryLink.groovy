package org.fogbeam.neddick

import java.util.Date

class ChannelEntryLink 
{

	// the two classes we are linking...
	Channel channel;
	Entry entry;
	
	// additional attributes about the link itself...
	// User creator;
	Date dateCreated;
	
	
	/* static utility methods for managing the linking of channels and entries */
	static ChannelEntryLink link( Entry entry, Channel channel )
   {
		
	   def alink = ChannelEntryLink.executeQuery( "select cel from ChannelEntryLink as cel where cel.channel = ? and cel.entry = ?", [channel, entry] );
	   if ( !alink )
	   {
		   alink = new ChannelEntryLink();
		   alink.channel = channel;
		   entry?.addToChannelEntryLinks(alink);
		   alink.save();
	   }
	   
	   return alink;
   }

   static void unlink( Entry entry, Channel channel )
   {
	   def alink = ChannelEntryLink.executeQuery( "select cel from ChannelEntryLink as cel where cel.channel = ? and cel.entry = ?", [channel, entry] );
	   if ( alink )
	   {
		   // tag?.removeFromTagEntryLinks(alink);
		   entry?.removeFromChannelEntryLinks(alink);
		   alink.delete();
	   }
   }
	
}

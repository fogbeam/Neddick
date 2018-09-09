package org.fogbeam.neddick

public class ChannelDataSourceLink
{
	// the two classes we are linking...
	Channel channel;
	org.fogbeam.neddick.DataSource channelDataSource;
	
	// additional attributes about the link itself
	Date dateCreated;
	
	Date dateLastPolled;
	
	String sinceId; // mainly used for Twitter, but no use
					// specializing the class just yet...

	static transients = ['channelDataSourceId', 'channelDataSourceDescription'];
	
	
	public int getChannelDataSourceId()
	{
		return channelDataSource.id;
	}
	
	public String getChannelDataSourceDescription()
	{
		return channelDataSource.description;	
	}
	
	/* static utility methods for managing the linking of channels and datasources */
	static ChannelDataSourceLink link( final Channel channel, final org.fogbeam.neddick.DataSource dataSource )
    {
	
	   List<ChannelDataSourceLink> links = ChannelDataSourceLink.executeQuery( "select cdsl from ChannelDataSourceLink as cdsl where cdsl.channel = ? and cdsl.channelDataSource = ?", [channel, dataSource] );
	   
	   def newlink = null;
	   if( links == null || links.size() == 0 )
	   {
		   // log.debug "creating new link";
		   newlink = new ChannelDataSourceLink();
		   // log.debug "setting channel";
		   newlink.channel = channel;
		   // log.debug "setting datasource";
		   newlink.channelDataSource = dataSource;

		   newlink.sinceId = "0";
		   
		   // log.debug "saving...";
		   newlink.dateLastPolled = new Date(0);
		   
		   if( !newlink.save(flush:true) )
		   {
			   ChannelDataSourceLink.log.debug "Failed to create new ChannelDataSourceLink!";
			   newlink.errors.allErrors.each{ ChannelDataSourceLink.log.error( it.toString() ) }
		   }
		   
		   channel.addToDataSourceLinks(newlink);
	   }
	   
	   return newlink;
   }

   static void unlink( final Channel channel, final org.fogbeam.neddick.DataSource dataSource )
   {
	   List<ChannelDataSourceLink> links = ChannelDataSourceLink.executeQuery( "select cdsl from ChannelDataSourceLink as cdsl where cdsl.channel = ? and cdsl.channelDataSource = ?", [channel, dataSource] );
	   if ( links != null && links.size() == 1 )
	   {
		   def alink = links.get(0);
		   channel.removeFromDataSourceLinks(alink);
		   alink.delete();
	   }
   }
	
   static getLink( final Channel channel, final org.fogbeam.neddick.DataSource dataSource )
   {	   
	   List<ChannelDataSourceLink> links = ChannelDataSourceLink.executeQuery( "select cdsl from ChannelDataSourceLink as cdsl where cdsl.channel = ? and cdsl.channelDataSource = ?", [channel, dataSource] );
	   def alink = null;
	   if ( links != null && links.size() == 1 )
	   {
		   alink = links.get(0);
	   }
	   
	   return alink;
   }
		
}

package org.fogbeam.neddick

public class ChannelDataSourceLink
{
	// the two classes we are linking...
	Channel channel;
	org.fogbeam.neddick.DataSource channelDataSource;
	
	// additional attributes about the link itself
	Date dateCreated;
	
	Date dateLastPolled;
	

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
	static ChannelDataSourceLink link( Channel channel, org.fogbeam.neddick.DataSource dataSource )
    {
	
	   List<ChannelDataSourceLink> links = ChannelDataSourceLink.executeQuery( "select cdsl from ChannelDataSourceLink as cdsl where cdsl.channel = ? and cdsl.channelDataSource = ?", [channel, dataSource] );
	   
	   def newlink = null;
	   if( links == null || links.size() == 0 )
	   {
		   // println "creating new link";
		   newlink = new ChannelDataSourceLink();
		   // println "setting channel";
		   newlink.channel = channel;
		   // println "setting datasource";
		   newlink.channelDataSource = dataSource;

		   // println "saving...";
		   newlink.dateLastPolled = new Date(0);
		   
		   if( !newlink.save() )
		   {
			   println "Failed to create new ChannelDataSourceLink!";
			   newlink.errors.allErrors.each{println it;}
		   }
		   
		   channel.addToDataSourceLinks(newlink);
		   
	   }
	   
	   return newlink;
   }

   static void unlink( Channel channel, org.fogbeam.neddick.DataSource dataSource )
   {
	   List<ChannelDataSourceLink> links = ChannelDataSourceLink.executeQuery( "select cdsl from ChannelDataSourceLink as cdsl where cdsl.channel = ? and cdsl.channelDataSource = ?", [channel, dataSource] );
	   if ( links != null && links.size() == 1 )
	   {
		   def alink = links.get(0);
		   channel.removeFromChannelDataSourceLinks(alink);
		   alink.delete();
	   }
   }
	
   static getLink( Channel channel, org.fogbeam.neddick.DataSource dataSource )
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

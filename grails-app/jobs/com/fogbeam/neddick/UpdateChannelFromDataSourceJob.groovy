package com.fogbeam.neddick

import java.text.SimpleDateFormat

import org.fogbeam.neddick.Channel

class UpdateChannelFromDataSourceJob 
{
	def jmsService;
	def channelService;
	
	def group = "MyGroup";
	def volatility = false;
	
	static triggers = {
	}
	
	def execute(context)
	{
		log.debug( "Updating Channels from DataSources" );
		println "Updating Channels from DataSources";
		
		Date now = new Date();
		SimpleDateFormat sdf = SimpleDateFormat.getDateTimeInstance();
		
		if( jmsService != null )
		{
			log.debug( "found a JMS service..." )
			// get a list of channels that have associated rss feeds
			List<Channel> channelsWithDataSources = channelService.findChannelsWithDatasource();
			if( channelsWithDataSources != null && channelsWithDataSources.size() > 0 )
			{
				log.debug( "Found some channels to update" );
			}
			
			// iterate over that list, sending a message for each channel, to update from RSS
			for( Channel channel in channelsWithDataSources )
			{
				String msg = "UPDATE_CHANNEL:${channel.name}";
			
				log.debug( "TRIGGER: sending update channel message: ${sdf.format( now )}" );

				log.debug( "using JMS Service" );
				jmsService.send( queue: "datasourceQueue", msg, "standard", null );			
			}
		}
		else
		{
			log.debug( "no JMS Service!" );
		}
	}
}
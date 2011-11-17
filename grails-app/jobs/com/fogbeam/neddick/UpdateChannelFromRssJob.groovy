package com.fogbeam.neddick

import java.text.SimpleDateFormat 
import org.fogbeam.neddick.Channel 

class UpdateChannelFromRssJob 
{
	def jmsService;
	def channelService;
	
	static triggers = {
		simple name: 'myOtherTrigger', startDelay: 500, repeatInterval: 135000
	  }
	
	def execute()
	{
		println "Updating Channels from RSS";
		Date now = new Date();
		SimpleDateFormat sdf = SimpleDateFormat.getDateTimeInstance();
		
		if( jmsService != null )
		{
			println "found a JMS service..."
			// TODO: get a list of channels that have associated rss feeds
			List<Channel> channelsWithRss = channelService.findChannelsWithDatasource();
			if( channelsWithRss != null && channelsWithRss.size() > 0 )
			{
				println "Found some channels to update";
			}
			
			// iterate over that list, sending a message for each channel, to update from RSS
			for( Channel channel in channelsWithRss )
			{
				String msg = "UPDATE_CHANNEL:${channel.name}";
			
				println "TRIGGER: sending update channel message: ${sdf.format( now )}";

				println "using JMS Service";
				jmsService.send( queue: "datasourceQueue", msg, "standard", null );			
			}
		}
		else
		{
			println "no JMS Service!";
		}
	}
}

package org.fogbeam.neddick.jms.listeners

import javax.jms.TextMessage

import org.fogbeam.neddick.Channel 

class UpdateChannelFromDatasourceListenerService 
{
	def siteConfigService;
	def channelService;
	static transactional = false;
	
	static expose = ['jms']
	static destination = "datasourceQueue"
	
	def onMessage(TextMessage msg)
	{
		try
		{
			String msgBody = msg.getText();
			log.info( "UpdateChannelFromDataSourceListenerService: received message: ${msgBody}" );
			
			String[] parts = msgBody.split(":");
			switch( parts[0] )
			{
				case "UPDATE_CHANNEL":
				
					String channelName = parts[1];
					// lookup the Channel object
					Channel channel = channelService.findByName( channelName );
					if( channel != null )
					{
						log.info( "updating from DataSource for channel: ${channelName}");
						
						// use the channelService to update the channel from the datasource
						channelService.updateFromDatasource( channel );
					}
					else
					{
						log.error( "Could not locate Channel entry for channel: ${channelName}" );
					}
					
					break;
			}
		}
		catch( Exception e )
		{
			e.printStackTrace();	
		}
		
		return null;
	}
}

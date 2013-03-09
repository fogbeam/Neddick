package org.fogbeam.neddick.jms

import org.fogbeam.neddick.Channel 

class DatasourceQueueService 
{
	def siteConfigService;
	def channelService;
	
	static expose = ['jms']
	static destination = "datasourceQueue"
	
	def onMessage(msg)
	{
		try
		{
			// receive message saying to update channel
			if( msg instanceof String )
			{
				
				log.info( "received message: ${msg}" );
				
				String[] parts = ((String)msg).split(":");
				switch( parts[0] )
				{
					case "UPDATE_CHANNEL":
					
						String channelName = parts[1];
						// lookup the Channel object
						Channel channel = channelService.findByName( channelName );
						if( channel != null )
						{
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
		}
		catch( Exception e )
		{
			e.printStackTrace();	
		}
		
		return null;
	}
	
}

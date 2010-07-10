package org.fogbeam.neddick

import org.fogbeam.neddick.Channel;

class ChannelService {

	public Channel findByName( final String channelName )
	{
		Channel channel = Channel.findByName( channelName );
		return channel;
	}

	public List<Channel> getAllChannels()
	{
		List<Channel> allChannels = new ArrayList<Channel>();
		allChannels.addAll( Channel.findAll() );
		
		allChannels.sort();
		
		return allChannels;
	}

}

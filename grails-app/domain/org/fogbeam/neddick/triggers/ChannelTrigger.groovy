package org.fogbeam.neddick.triggers

import org.fogbeam.neddick.Channel

public class ChannelTrigger extends BaseTrigger
{
	
	Channel channel;
	
	static constraints =
	{
		channel( nullable: true ); // change this to false once we have all the trappings
								   // for creating these things in place.  This is just so
								   // we can test partially implemented UI stuff for now.
	}
	
}

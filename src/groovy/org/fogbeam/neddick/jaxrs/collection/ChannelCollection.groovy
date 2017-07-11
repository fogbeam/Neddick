package org.fogbeam.neddick.jaxrs.collection

import javax.xml.bind.annotation.XmlAccessType
import javax.xml.bind.annotation.XmlAccessorType
import javax.xml.bind.annotation.XmlElement
import javax.xml.bind.annotation.XmlElementWrapper
import javax.xml.bind.annotation.XmlRootElement

import org.fogbeam.neddick.Channel


@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
class ChannelCollection 
{	
		
	@XmlElementWrapper(name="channels")
	@XmlElement(name="channel")
	private List<Channel> channels = new ArrayList<Channel>();
	
	public List<Channel> getChannels()
	{
		return this.channels;
	}
	
	public void setChannels( final List<Channel> channels )
	{
		this.channels = channels;
	}
	
	public void addAll( final List<Channel> channels)
	{
		this.channels.addAll( channels );
	}
	
}

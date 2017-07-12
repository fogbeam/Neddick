package org.fogbeam.neddick

import javax.xml.bind.annotation.XmlAccessType
import javax.xml.bind.annotation.XmlAccessorType
import javax.xml.bind.annotation.XmlElement
import javax.xml.bind.annotation.XmlRootElement

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
class Channel implements Comparable {

	@XmlElement
	Long id;
	
	@XmlElement
	String uuid;
	
	@XmlElement
	String name;
	
	@XmlElement
	String description;
	
	@XmlElement
	Date dateCreated;

	@XmlElement
	boolean privateChannel = false;

	User owner;
	
	@XmlElement(name="userId")
	public String getUserId()
	{
		return this.owner.userId;
	}
	
	
	public Channel()
	{
		this.uuid = java.util.UUID.randomUUID().toString();
	}
	
	
	static hasMany = [ dataSourceLinks : ChannelDataSourceLink, aggregateChannels:Channel, userFavoriteChannels:UserFavoriteChannelLink];

	// static mappedBy = [dataSourceLinks: "channel"];
	
	static constraints =
	{
		description( nullable:true, maxSize:2048 );
	}
	
   	@Override
	public int compareTo(Object o) 
   	{
   		Channel otherChannel = (Channel)o;
   		return ( this.name.compareToIgnoreCase( otherChannel.name ) );
	}                   

}

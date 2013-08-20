package org.fogbeam.neddick

class Channel implements Comparable {

	String uuid;
	String name;
	String description;
	Date dateCreated;

	public Channel()
	{
		this.uuid = java.util.UUID.randomUUID().toString();
	}
	
	
	static hasMany = [ feeds : RssFeed];

	static constraints =
	{
		description( nullable:true, maxSize:2048 );
	}
	
   	@Override
	public int compareTo(Object o) 
   	{
		// TODO: switch this to use uuid
   		Channel otherChannel = (Channel)o;
   		return ( this.name.compareToIgnoreCase( otherChannel.name ) );
	}                   

}

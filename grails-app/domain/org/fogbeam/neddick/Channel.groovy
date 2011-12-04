package org.fogbeam.neddick

class Channel implements Comparable {

	String name;
	String description;
	Date dateCreated;

	static hasMany = [ feeds : RssFeed];

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

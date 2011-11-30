package org.fogbeam.neddick

class Channel implements Comparable {

	String name;
	String description;
	Date dateCreated;

	static hasMany = [ feeds : RssFeed];

   	@Override
	public int compareTo(Object o) 
   	{
   		Channel otherChannel = (Channel)o;
   		return ( this.name.compareToIgnoreCase( otherChannel.name ) );
	}                   

}

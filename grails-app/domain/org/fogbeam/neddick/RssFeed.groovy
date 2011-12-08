package org.fogbeam.neddick

class RssFeed 
{
	String feedUrl;
	String description;
	Date dateCreated;
	
	static constraints =
	{
		description( nullable:true, maxSize:2048 );
	}

}

package org.fogbeam.neddick

import java.util.Date;

class DataSource
{
	String description;
	Date dateCreated;

	static constraints =
	{
		description( nullable:true, maxSize:2048 );
	}

	static hasMany = [ dataSourceLinks:ChannelDataSourceLink ];
	
	static mappedBy = [ dataSourceLinks: 'channelDataSource'];
	
	static mapping = {
		tablePerHierarchy false
	}
			
}

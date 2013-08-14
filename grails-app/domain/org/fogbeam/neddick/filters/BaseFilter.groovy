package org.fogbeam.neddick.filters

import org.fogbeam.neddick.Channel
import org.fogbeam.neddick.Entry
import org.fogbeam.neddick.User
import org.fogbeam.neddick.filters.criteria.BaseFilterCriteria

class BaseFilter
{
	static mapping = {
		tablePerHierarchy false
	}

	String name;
	User owner;
	Channel channel;
	
	static hasMany = [ filterCriteria: BaseFilterCriteria, 
						entries: Entry ];
	
}

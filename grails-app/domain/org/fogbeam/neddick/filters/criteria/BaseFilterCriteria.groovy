package org.fogbeam.neddick.filters.criteria

import org.fogbeam.neddick.filters.BaseFilter

class BaseFilterCriteria
{
	static mapping = {
		tablePerHierarchy false
	}
	
	static belongsTo = [filter: BaseFilter];
}

package org.fogbeam.neddick.triggers.criteria

import org.fogbeam.neddick.triggers.BaseTrigger

public class BaseTriggerCriteria
{	
	
	static mapping = {
		tablePerHierarchy false
	}
	
		static belongsTo = [trigger: BaseTrigger];
	
}

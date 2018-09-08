package org.fogbeam.neddick.triggers.actions

import org.fogbeam.neddick.triggers.BaseTrigger

public class BaseTriggerAction
{	
	
	static mapping = {
		tablePerHierarchy false
	}
	
		
	static belongsTo = [trigger: BaseTrigger];
	
	public void doAction( final String entryUuid )
	{
		log.debug "performing action for uuid: ${entryUuid}";
	}
}

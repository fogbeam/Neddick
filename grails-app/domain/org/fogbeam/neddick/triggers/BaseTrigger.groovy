package org.fogbeam.neddick.triggers

import org.fogbeam.neddick.User
import org.fogbeam.neddick.triggers.actions.BaseTriggerAction
import org.fogbeam.neddick.triggers.criteria.BaseTriggerCriteria


public class BaseTrigger
{
	
	static mapping = {
		tablePerHierarchy false
	}

	String name;
	User owner;
	
	
	static hasMany = [ triggerCriteria: BaseTriggerCriteria, triggerActions: BaseTriggerAction ];
	
	
	static constraints =
	{
		
		triggerCriteria( minSize: 1 );
		triggerActions( minSize: 1 )
	}
	
	
	public void fireAllActions( final String entryUuid )
	{
		for( BaseTriggerAction action : triggerActions )
		{
			action.doAction( entryUuid );
		}
	}	
	
}

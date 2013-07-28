package org.fogbeam.neddick.triggers

import groovy.transform.TypeChecked

import org.fogbeam.neddick.triggers.actions.AbstractBaseTriggerAction
import org.fogbeam.neddick.triggers.criteria.AbstractBaseTriggerCriteria


@TypeChecked
public class GlobalTrigger extends AbstractBaseTrigger
{
			
	public GlobalTrigger addTriggerAction( final AbstractBaseTriggerAction action )
	{
		this.triggerActions.add( action );
		return this;
	}

		
	public void fireAllActions()
	{
		for( AbstractBaseTriggerAction triggerAction : triggerActions )
		{
			try
			{
				triggerAction.doAction();
			}
			catch( Exception e )
			{
				e.printStackTrace();
				continue; // don't let a failure in one action stop use from doing the others
			}
		}
	}	
}

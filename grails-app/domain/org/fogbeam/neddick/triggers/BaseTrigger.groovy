package org.fogbeam.neddick.triggers

import grails.util.GrailsNameUtils

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
	
	static transients = ['triggerType'];
	
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
	
	
	public String getTriggerType()
	{
		return GrailsNameUtils.getShortName( this.class );
	}
	
	/* a cheat for now, since we only support one criteria */
	public BaseTriggerCriteria getTheOneCriteria()
	{
		return this.triggerCriteria.toArray()[0];
	}
	
	/* a cheat for now, since we only support one action */
	public BaseTriggerAction getTheOneAction()
	{
		return this.triggerActions.toArray()[0];
	}
	
	 
}

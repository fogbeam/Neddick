package org.fogbeam.neddick.triggers

import groovy.transform.TypeChecked

import org.fogbeam.neddick.triggers.actions.AbstractBaseTriggerAction
import org.fogbeam.neddick.triggers.criteria.AbstractBaseTriggerCriteria

@TypeChecked
public abstract class AbstractBaseTrigger
{
	AbstractBaseTriggerCriteria triggerCriteria;
	List<AbstractBaseTriggerAction> triggerActions = new ArrayList<AbstractBaseTriggerAction>();

}

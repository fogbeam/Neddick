package org.fogbeam.neddick.triggers.criteria

import grails.util.GrailsNameUtils

class NewContentTriggerCriteria extends BaseTriggerCriteria
{
	public String getShortName()
	{
		return GrailsNameUtils.getShortName( this.class );
	}

	public String getValue()
	{
		return "NewContentTriggerCriteria";
	}
}
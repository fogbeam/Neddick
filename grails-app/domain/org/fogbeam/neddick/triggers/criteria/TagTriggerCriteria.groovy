package org.fogbeam.neddick.triggers.criteria

import grails.util.GrailsNameUtils

class TagTriggerCriteria extends BaseTriggerCriteria
{
	String tag;
	
	public String getShortName()
	{
		return GrailsNameUtils.getShortName( this.class );
	}
	
	public String getValue()
	{
		return this.tag;
	}
	
}

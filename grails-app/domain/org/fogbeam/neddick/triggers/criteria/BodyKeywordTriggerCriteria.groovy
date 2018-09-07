package org.fogbeam.neddick.triggers.criteria

import grails.util.GrailsNameUtils

public class BodyKeywordTriggerCriteria extends BaseTriggerCriteria
{
	String bodyKeyword;
	
	public String getShortName()
	{
		return GrailsNameUtils.getShortName( this.class );
	}

	public String getValue()
	{
		return this.bodyKeyword;
	}
		
}

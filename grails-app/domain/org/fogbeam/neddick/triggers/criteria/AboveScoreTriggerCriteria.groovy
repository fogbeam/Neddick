package org.fogbeam.neddick.triggers.criteria

import grails.util.GrailsNameUtils

class AboveScoreTriggerCriteria extends BaseTriggerCriteria
{
	int aboveScoreThreshold;
	String scoreName;

	
	public String getShortName()
	{
		return GrailsNameUtils.getShortName( this.class );
	}
	
	public String getValue()
	{
		return Integer.toString( this.aboveScoreThreshold );
	}
		
}

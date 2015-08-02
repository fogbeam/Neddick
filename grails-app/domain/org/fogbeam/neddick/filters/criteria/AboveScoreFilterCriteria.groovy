package org.fogbeam.neddick.filters.criteria

import grails.util.GrailsNameUtils

class AboveScoreFilterCriteria extends BaseFilterCriteria
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

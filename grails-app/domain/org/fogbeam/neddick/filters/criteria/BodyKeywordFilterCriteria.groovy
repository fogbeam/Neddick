package org.fogbeam.neddick.filters.criteria

import grails.util.GrailsNameUtils

class BodyKeywordFilterCriteria extends BaseFilterCriteria
{
	String bodyKeyword;
	
	public String getShortName()
	{
		return GrailsNameUtils.getShortName( this.class );
	}

	public String getValue()
	{
		return( bodyKeyword );
	}
		
}

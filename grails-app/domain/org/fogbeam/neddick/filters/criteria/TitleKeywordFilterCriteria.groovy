package org.fogbeam.neddick.filters.criteria

import grails.util.GrailsNameUtils

class TitleKeywordFilterCriteria extends BaseFilterCriteria
{
	String titleKeyword;
	
	
	public String getShortName()
	{
		return GrailsNameUtils.getShortName( this.class );
	}

	public String getValue()
	{
		return this.titleKeyword;
	}
		
}

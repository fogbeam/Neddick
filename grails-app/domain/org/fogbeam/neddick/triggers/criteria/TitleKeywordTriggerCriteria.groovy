package org.fogbeam.neddick.triggers.criteria

import grails.util.GrailsNameUtils

class TitleKeywordTriggerCriteria extends BaseTriggerCriteria
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

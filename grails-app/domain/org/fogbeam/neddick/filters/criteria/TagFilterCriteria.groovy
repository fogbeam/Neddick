package org.fogbeam.neddick.filters.criteria

class TagFilterCriteria extends BaseFilterCriteria
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

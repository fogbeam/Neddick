package org.fogbeam.neddick

import org.fogbeam.neddick.filters.BaseFilter

class FilterService
{
	

	public List<BaseFilter> getFiltersForUser( final User user )
	{
		List<BaseFilter> filters = new ArrayList<BaseFilter>();
		
		List<BaseFilter> queryResults = BaseFilter.executeQuery( "select filter from BaseFilter as filter where filter.owner = :owner", [owner:user] );
		if( queryResults != null && queryResults.size() > 0 )
		{
			filters.addAll( queryResults );
		}
		
		return filters;	
	}
	
	
	public BaseFilter saveFilter( final BaseFilter newFilter )
	{
		if( newFilter.save() )
		{
			return newFilter;
		}
		else 
		{
			newFilter.errors.allErrors.each { println it };
			return null;
		}
	}
	
	
}

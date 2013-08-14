package org.fogbeam.neddick

import org.fogbeam.neddick.filters.BaseFilter
import org.fogbeam.neddick.filters.criteria.BodyKeywordFilterCriteria

class FilterController
{
	def filterService;
	def userService;
	def channelService;
	
	
	def index =
	{
		// lookup Filterss for the current user and put them in the domain
		// for rendering
		
		List<BaseFilter> filters = filterService.getFiltersForUser( session.user );
		[filters:filters];
	}
	
	def create =
	{
		
		[];
	}
	
	def save = 
	{
		BaseFilter newFilter = new BaseFilter();
				
		newFilter.name = params.filterName;
		
		Channel filterChannel = channelService.findByName( params.filterChannel );
		newFilter.channel = filterChannel;
		
		User owner = userService.findUserByUserId( session.user.userId );
		newFilter.owner = owner;
		
		BodyKeywordFilterCriteria crit = new BodyKeywordFilterCriteria();
		crit.bodyKeyword = params.get( "criteriaValue-1");
		
		newFilter.addToFilterCriteria( crit );
		
		
		if( ! filterService.saveFilter( newFilter ) )
		{
			flash.message = "ERROR saving Filter!";
		}
		
		redirect( controller:'filter', action:'index' );
	}
	
	def edit = 
	{
		
		[];
	}
	
	
	def update =
	{
		
		redirect( controller:'filter', action:'index' );
	}

}

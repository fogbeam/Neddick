package org.fogbeam.neddick

import org.fogbeam.neddick.filters.BaseFilter
import org.fogbeam.neddick.filters.criteria.AboveScoreFilterCriteria
import org.fogbeam.neddick.filters.criteria.BaseFilterCriteria
import org.fogbeam.neddick.filters.criteria.BodyKeywordFilterCriteria
import org.fogbeam.neddick.filters.criteria.TagFilterCriteria
import org.fogbeam.neddick.filters.criteria.TitleKeywordFilterCriteria

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
		
		
		String criteriaType = params.get("criteriaType.1");
		BaseFilterCriteria criteria = null;
		switch( criteriaType )
		{
			case "BodyKeywordFilterCriteria":
				
				println "creating BodyKeywordFilterCriteria";
				criteria = new BodyKeywordFilterCriteria();
				criteria.bodyKeyword = params.get( "criteriaValue-1");
				
				break;
				
			case "TagFilterCriteria":
			
				println "creating TagFilterCriteria";
				criteria = new TagFilterCriteria();
				String tag = params.get( "criteriaValue-1");
				if( tag != null && !tag.isEmpty())
				{
					tag = tag.trim().toLowerCase();
					criteria.tag = tag;
				}
				else
				{
					throw new RuntimeException( "Empty tag name not allowed in Filter Criteria" );
				}
				break;
				
			case "AboveScoreFilterCriteria":
			
				println "creating AboveScoreFilterCriteria";
				criteria = new AboveScoreFilterCriteria();
				criteria.aboveScoreThreshold = Integer.parseInt( params.get( "criteriaValue-1") );
				criteria.scoreName = "raw";
				break;
				
			case "TitleKeywordFilterCriteria":
			
				println "creating TitleKeywordFilterCriteria";
				criteria = new TitleKeywordFilterCriteria();
				criteria.titleKeyword = params.get( "criteriaValue-1");
				
				break;
				
			default:
				
				println "bad type";
				break;
		}
		
		
		newFilter.addToFilterCriteria( criteria );
		
		
		if( ! filterService.saveFilter( newFilter ) )
		{
			flash.message = "ERROR saving Filter!";
		}
		
		redirect( controller:'filter', action:'index' );
	}
	
	def edit = 
	{
	
		BaseFilter filterToEdit = filterService.findFilterById( Long.parseLong(params.id));	
		
		[filterToEdit:filterToEdit];
	}
	
	
	def update =
	{
		println "update Filter with params: ${params}";
		
		
		BaseFilter filterToEdit = filterService.findFilterById( Long.parseLong( params.id ) );
		
		filterToEdit.name = params.filterName;
		
		
		Channel filterChannel = channelService.findByName( params.filterChannel );
		filterToEdit.channel = filterChannel;
	
		String criteriaType = params.get("criteriaType.1");
		BaseFilterCriteria newCriteria = null;
		switch( criteriaType )
		{
			case "BodyKeywordFilterCriteria":
				
				println "creating BodyKeywordFilterCriteria";
				newCriteria = new BodyKeywordFilterCriteria();
				newCriteria.bodyKeyword = params.get( "criteriaValue-1");
				
				break;
				
			case "TagFilterCriteria":
			
				println "creating TagFilterCriteria";
				newCriteria = new TagFilterCriteria();
				String tag = params.get( "criteriaValue-1");
				if( tag != null && !tag.isEmpty())
				{
					tag = tag.trim().toLowerCase();
					newCriteria.tag = tag;
				}
				else
				{
					throw new RuntimeException( "Empty tag name not allowed in Filter Criteria" );
				}
				break;
				
			case "AboveScoreFilterCriteria":
			
				println "creating AboveScoreFilterCriteria";
				newCriteria = new AboveScoreFilterCriteria();
				newCriteria.aboveScoreThreshold = Integer.parseInt( params.get( "criteriaValue-1") );
				newCriteria.scoreName = "raw";
				break;
				
			case "TitleKeywordFilterCriteria":
			
				println "creating TitleKeywordFilterCriteria";
				newCriteria = new TitleKeywordFilterCriteria();
				newCriteria.titleKeyword = params.get( "criteriaValue-1");
				
				break;
				
			default:
				
				println "bad type";
				break;
		}
		
		
		filterService.updateFilter( filterToEdit, newCriteria );
		
		redirect( controller:'filter', action:'index' );
	}
	
	def delete =
	{
		
		filterService.deleteFilterById( Long.parseLong(params.id));
		
		redirect( controller:'filter', action:'index' );
	}

}

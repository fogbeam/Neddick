package org.fogbeam.neddick

import org.fogbeam.neddick.filters.BaseFilter
import org.fogbeam.neddick.filters.criteria.AboveScoreFilterCriteria
import org.fogbeam.neddick.filters.criteria.BaseFilterCriteria
import org.fogbeam.neddick.filters.criteria.BodyKeywordFilterCriteria
import org.fogbeam.neddick.filters.criteria.TagFilterCriteria
import org.fogbeam.neddick.filters.criteria.TitleKeywordFilterCriteria

import grails.plugin.springsecurity.annotation.Secured

class FilterController
{
	def filterService;
	def userService;
	def channelService;
	
	@Secured(["ROLE_USER","ROLE_ADMIN"])
	def index()
	{
		// lookup Filterss for the current user and put them in the domain
		// for rendering
		User currentUser = userService.getLoggedInUser();
		List<BaseFilter> filters = filterService.getFiltersForUser( currentUser );
		[filters:filters];
	}
	
	@Secured(["ROLE_USER","ROLE_ADMIN"])
	def create()
	{
		
		[:];
	}
	
	@Secured(["ROLE_USER","ROLE_ADMIN"])
	def save()
	{
		BaseFilter newFilter = new BaseFilter();
				
		newFilter.name = params.filterName;
		
		Channel filterChannel = channelService.findByName( params.filterChannel );
		newFilter.channel = filterChannel;
		
		User owner = userService.getLoggedInUser();
		newFilter.owner = owner;
		
		
		String criteriaType = params.get("criteriaType.1");
		BaseFilterCriteria criteria = null;
		switch( criteriaType )
		{
			case "BodyKeywordFilterCriteria":
				
				log.debug "creating BodyKeywordFilterCriteria";
				criteria = new BodyKeywordFilterCriteria();
				criteria.bodyKeyword = params.get( "criteriaValue-1");
				
				break;
				
			case "TagFilterCriteria":
			
				log.debug "creating TagFilterCriteria";
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
			
				log.debug "creating AboveScoreFilterCriteria";
				criteria = new AboveScoreFilterCriteria();
				criteria.aboveScoreThreshold = Integer.parseInt( params.get( "criteriaValue-1") );
				criteria.scoreName = "raw";
				break;
				
			case "TitleKeywordFilterCriteria":
			
				log.debug "creating TitleKeywordFilterCriteria";
				criteria = new TitleKeywordFilterCriteria();
				criteria.titleKeyword = params.get( "criteriaValue-1");
				
				break;
				
			default:
				
				log.debug "bad type";
				break;
		}
		
		
		newFilter.addToFilterCriteria( criteria );
		
		
		if( ! filterService.saveFilter( newFilter ) )
		{
			flash.message = "ERROR saving Filter!";
		}
		
		redirect( controller:'filter', action:'index' );
	}
	
	@Secured(["ROLE_USER","ROLE_ADMIN"])
	def edit() 
	{
		BaseFilter filterToEdit = filterService.findFilterById( Long.parseLong(params.id));	
		
		[filterToEdit:filterToEdit];
	}
	
	
	@Secured(["ROLE_USER","ROLE_ADMIN"])
	def update()
	{
		log.debug "update Filter with params: ${params}";
		
		
		BaseFilter filterToEdit = filterService.findFilterById( Long.parseLong( params.id ) );
		
		filterToEdit.name = params.filterName;
		
		
		Channel filterChannel = channelService.findByName( params.filterChannel );
		filterToEdit.channel = filterChannel;
	
		String criteriaType = params.get("criteriaType.1");
		BaseFilterCriteria newCriteria = null;
		switch( criteriaType )
		{
			case "BodyKeywordFilterCriteria":
				
				log.debug "creating BodyKeywordFilterCriteria";
				newCriteria = new BodyKeywordFilterCriteria();
				newCriteria.bodyKeyword = params.get( "criteriaValue-1");
				
				break;
				
			case "TagFilterCriteria":
			
				log.debug "creating TagFilterCriteria";
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
			
				log.debug "creating AboveScoreFilterCriteria";
				newCriteria = new AboveScoreFilterCriteria();
				newCriteria.aboveScoreThreshold = Integer.parseInt( params.get( "criteriaValue-1") );
				newCriteria.scoreName = "raw";
				break;
				
			case "TitleKeywordFilterCriteria":
			
				log.debug "creating TitleKeywordFilterCriteria";
				newCriteria = new TitleKeywordFilterCriteria();
				newCriteria.titleKeyword = params.get( "criteriaValue-1");
				
				break;
				
			default:
				
				log.debug "bad type";
				break;
		}
		
		filterService.updateFilter( filterToEdit, newCriteria );
		
		redirect( controller:'filter', action:'index' );
	}
	
	@Secured(["ROLE_USER","ROLE_ADMIN"])
	def delete()
	{
		log.info( "delete() called with id: ${params.id}");
		
		filterService.deleteFilterById( Long.parseLong(params.id));
		
		redirect( controller:'filter', action:'index' );
	}
}

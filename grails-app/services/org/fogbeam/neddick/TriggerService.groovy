package org.fogbeam.neddick

import org.fogbeam.neddick.triggers.BaseTrigger
import org.fogbeam.neddick.triggers.ChannelTrigger
import org.fogbeam.neddick.triggers.GlobalTrigger
import org.fogbeam.neddick.triggers.actions.BaseTriggerAction
import org.fogbeam.neddick.triggers.criteria.AboveScoreTriggerCriteria
import org.fogbeam.neddick.triggers.criteria.BaseTriggerCriteria
import org.fogbeam.neddick.triggers.criteria.BodyKeywordTriggerCriteria
import org.fogbeam.neddick.triggers.criteria.NewContentTriggerCriteria
import org.fogbeam.neddick.triggers.criteria.TagTriggerCriteria
import org.fogbeam.neddick.triggers.criteria.TitleKeywordTriggerCriteria

class TriggerService
{
	
	def searchService;
	def entryService;
	
	
	public List<GlobalTrigger> getGlobalTriggersForUser( final User user )
	{
		List<GlobalTrigger> globalTriggers = new ArrayList<GlobalTrigger>();
		
		List<GlobalTrigger> queryResults = 
			GlobalTrigger.executeQuery( "select trigger from GlobalTrigger as trigger where trigger.owner = :owner", [owner:user] );
		
		if( queryResults != null && queryResults.size() > 0 )
		{
			globalTriggers.addAll( queryResults );
		}
		
		
		return globalTriggers;
		
	}
	
	public List<ChannelTrigger> getChannelTriggersForUser( final User user )
	{
		List<ChannelTrigger> channelTriggers = new ArrayList<ChannelTrigger>();

		List<ChannelTrigger> queryResults = ChannelTrigger.executeQuery( "select trigger from ChannelTrigger as trigger where trigger.owner = :owner", [owner:user] );
		
		if( queryResults != null && queryResults.size() > 0 )
		{
			channelTriggers.addAll( queryResults );
		}
		
				
		return channelTriggers;

	}

	public BaseTrigger findTriggerById( final Long id )
	{
		BaseTrigger trigger = BaseTrigger.findById( id );
	
		return trigger;	
	}
	
	public BaseTrigger saveTrigger( final BaseTrigger trigger )
	{
		if( !trigger.save(flush:true))
		{
			trigger.errors.allErrors.each { log.error( it.toString() ); }
			throw new RuntimeException( "Failed to save trigger!");
		}
		
		
		return trigger;
	}

	public BaseTrigger updateTrigger( final BaseTrigger triggerToEdit, 
									  final BaseTriggerCriteria newCriteria, final BaseTriggerAction newTriggerAction )
	{
		
		BaseTriggerCriteria criteriaToRemove = triggerToEdit.theOneCriteria;
		triggerToEdit.removeFromTriggerCriteria( criteriaToRemove );
		criteriaToRemove.delete();
		
		
		BaseTriggerAction actionToRemove = triggerToEdit.theOneAction;
		triggerToEdit.removeFromTriggerActions( actionToRemove );
		actionToRemove.delete();
		
		triggerToEdit.addToTriggerCriteria( newCriteria );
		triggerToEdit.addToTriggerActions( newTriggerAction );
		
		if( !triggerToEdit.save(flush:true))
		{
			triggerToEdit.errors.allErrors.each { log.error( it.toString() ); }
			throw new RuntimeException( "Failed to update trigger!");
		}
		
		
		return triggerToEdit;
	}
	
	
	
	// the score of an entry has changed, process "score" triggers
	public void fireThresholdTriggerCriteria( final String entryUuid, final String strNewScore )
	{
		log.debug "processing score threshold triggers...";
		
		List<AboveScoreTriggerCriteria> triggerCriteria = AboveScoreTriggerCriteria.findAll();
		
		Entry theEntry = entryService.findByUuid( entryUuid );
		
		// Entry theEntry = entryService.findByUuid( entryUuid );
		int newScore = Integer.parseInt( strNewScore);
		
		for( AboveScoreTriggerCriteria criteria : triggerCriteria )
		{
			
			log.debug "found an instance of AboveScoreTriggerCriteria";
			log.debug "newScore: ${newScore}, threshold: ${criteria.aboveScoreThreshold}";
			
			
			/* TODO: this needs to take score personalization into account */
			
			// does this criteria fire?
			if( newScore  > criteria.aboveScoreThreshold )
			{
				// get the associated Trigger
				BaseTrigger trigger = criteria.trigger;
				
				if( trigger instanceof ChannelTrigger )
				{
					Entry tempEntry = entryService.findByUuidAndChannel( entryUuid, trigger.channel );
					if(  tempEntry == null )
					{
						continue;
					}
				}
				
				
				log.debug "found a match, firing Trigger: ${trigger.name}";
				
				// and fire it's actions
				trigger.fireAllActions( entryUuid );
			}
			else
			{
				log.debug "No match, not firing actions...";	
			}

		}			
	}
	
	// a new tag was added, process "tag" triggers
	public void fireTagTriggerCriteria( final String tagName, final String entryUuid )
	{
		log.debug "processing tag triggers..."
		List<TagTriggerCriteria> triggersCriteria = this.findTagTriggerCriteriaByTagName( tagName );
		
		Entry theEntry = entryService.findByUuid( entryUuid );
		
		for( TagTriggerCriteria criteria : triggersCriteria )
		{
			// get the associated Trigger
			BaseTrigger trigger = criteria.trigger;
			if( trigger instanceof ChannelTrigger )
			{
				Entry tempEntry = entryService.findByUuidAndChannel( entryUuid, trigger.channel );
				if(  tempEntry == null )
				{
					continue;
				}
			}
			
			
			// and fire it's actions
			trigger.fireAllActions( entryUuid );
			
		}
		
		
	}
	
	public findTagTriggerCriteriaByTagName( final String tagName )
	{
		log.debug "seeking TagTriggerCriteria with tagName: ${tagName}";
		
		List<TagTriggerCriteria> triggerCriteria = new ArrayList<TagTriggerCriteria>();
		String canonicalTagName = tagName.trim().toLowerCase();
		 
		def queryResults =	TagTriggerCriteria.executeQuery( "select criteria from TagTriggerCriteria as criteria where criteria.tag = :tagName", [tagName:canonicalTagName] );
		
		if( queryResults != null )
		{
			triggerCriteria.addAll( queryResults );
		}	
			
		log.debug "Found ${triggerCriteria.size()} matching TagTriggerCriteria!";
		
		return triggerCriteria;
	}
	
	// a new piece of content was delivered, process "content" (body and title keyword) triggers
	public void fireContentTriggerCriteria( final String entryUuid )
	{
		// log.debug "processing content triggers..."
			
		List<BodyKeywordTriggerCriteria> bodyKeywordTriggerCriteria = BodyKeywordTriggerCriteria.findAll();
		
		Entry theEntry = entryService.findByUuid( entryUuid );
		
		if( bodyKeywordTriggerCriteria != null )
		{
			// log.debug "bodyKeywordTriggerCriteria object is valid";
			
			for( BodyKeywordTriggerCriteria triggerCrit : bodyKeywordTriggerCriteria )
			{

				
				String keyword = triggerCrit.bodyKeyword;
				
				// log.debug "found a triggerCriteria with keyword: ${keyword}";
				
				List<Entry> searchResults = searchService.doSearch( "uuid: ${entryUuid} AND content: ${keyword}" );
				
				log.debug "did search for entry_uuid: ${entryUuid}";
								
				// does this criteria fire?
				if( searchResults != null && !searchResults.isEmpty() )
				{
					
					
					// get the associated Trigger
					BaseTrigger trigger = triggerCrit.trigger;
					if( trigger instanceof ChannelTrigger )
					{
						Entry tempEntry = entryService.findByUuidAndChannel( entryUuid, trigger.channel );
						if(  tempEntry == null )
						{
							continue;
						}
					}
										
					log.debug "found a match, firing Trigger: ${trigger.name}";
					
					// and fire it's actions
					trigger.fireAllActions( entryUuid );
				}
				else
				{
					
				}
				
				
			}
		}
		
		List<TitleKeywordTriggerCriteria> titleKeywordTriggerCriteria = TitleKeywordTriggerCriteria.findAll();
		
		if( titleKeywordTriggerCriteria != null )
		{
			for( TitleKeywordTriggerCriteria triggerCrit : titleKeywordTriggerCriteria )
			{
				log.debug "Found valid TitleKeywordTriggerCriteria with keyword: ${triggerCrit.titleKeyword}";
				
				String keyword = triggerCrit.titleKeyword;
				List<Entry> searchResults = searchService.doSearch( "uuid: ${entryUuid} AND title: ${keyword}" );
				
							
				// does this criteria fire?
				if( searchResults != null && !searchResults.isEmpty() )
				{
					log.debug "found results for entryUuid: ${entryUuid} and title keyword: ${keyword}";
					
					// get the associated Trigger
					BaseTrigger trigger = triggerCrit.trigger;
					if( trigger instanceof ChannelTrigger )
					{
						log.debug "this is a ChannelTrigger for Channel: ${trigger.channel.name}";
						Entry tempEntry = entryService.findByUuidAndChannel( entryUuid, trigger.channel );
						if(  tempEntry == null )
						{
							log.debug "but the channel doesn't match, skipping...";
							continue;
						}
						else
						{
							log.debug "and the channel does match, proceeding...";
						}
					}
					else
					{
						log.debug "this is not a Channel Trigger";
					}
					
		
					log.debug "firing all actions...";			
					// and fire it's actions
					trigger.fireAllActions( entryUuid );
				}
				else
				{
					log.debug "found NO results for entryUuid: ${entryUuid} and title keyword: ${keyword}";
				}
			}
		}
		
		
		// "NewContentTriggerCriteria" which fires the trigger on any
		// new content
		// NOTE: we should possibly disallow creating these as global triggers, otherwise
		// this will fire for *EVERY SINGLE PIECE OF CONTENT* in the entire system.  Not sure
		// that would ever be desirable. 
		List<NewContentTriggerCriteria> newContentTriggerCriteria = NewContentTriggerCriteria.findAll();
		
		if( newContentTriggerCriteria != null )
		{
			
			for( NewContentTriggerCriteria triggerCrit : newContentTriggerCriteria )
			{
				// get the associated Trigger
				BaseTrigger trigger = triggerCrit.trigger;
				if( trigger instanceof ChannelTrigger )
				{
					log.debug "this is a ChannelTrigger for Channel: ${trigger.channel.name}";
					Entry tempEntry = entryService.findByUuidAndChannel( entryUuid, trigger.channel );
					if(  tempEntry == null )
					{
						log.debug "but the channel doesn't match, skipping...";
						continue;
					}
					else
					{
						log.debug "and the channel does match, proceeding...";
					}
				}
				else
				{
					log.debug "this is not a Channel Trigger";
				}
				
	
				log.debug "firing all actions...";
				// and fire it's actions
				trigger.fireAllActions( entryUuid );
			}
		}
		
		
	}

	public void deleteTrigger( final Long id )
	{
		BaseTrigger triggerToDelete = BaseTrigger.findById( id );
		triggerToDelete.delete(flush:true);
	}
				
}
 
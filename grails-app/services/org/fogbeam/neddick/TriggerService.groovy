package org.fogbeam.neddick

import org.fogbeam.neddick.triggers.BaseTrigger
import org.fogbeam.neddick.triggers.ChannelTrigger
import org.fogbeam.neddick.triggers.GlobalTrigger
import org.fogbeam.neddick.triggers.criteria.TagTriggerCriteria

class TriggerService
{
	
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
		if( !trigger.save())
		{
			trigger.errors.allErrors.each { println it; }
			throw new RuntimeException( "Failed to save trigger!");
		}
		
		
		return trigger;
	}

	// the score of an entry has changed, process "score" triggers
	public void fireThresholdTriggerCriteria()
	{
		println "processing score threshold triggers...";
	}
	
	// a new tag was added, process "tag" triggers
	public void fireTagTriggerCriteria( final String tagName, final String entryUuid )
	{
		println "processing tag triggers..."
		List<TagTriggerCriteria> triggersCriteria = this.findTagTriggerCriteriaByTagName( tagName );
		
		for( TagTriggerCriteria criteria : triggersCriteria )
		{
			// get the associated Trigger
			BaseTrigger trigger = criteria.trigger;
			
			// and fire it's actions
			trigger.fireAllActions( entryUuid );
			
		}
		
		
	}
	
	public findTagTriggerCriteriaByTagName( final String tagName )
	{
		println "seeking TagTriggerCriteria with tagName: ${tagName}";
		
		List<TagTriggerCriteria> triggerCriteria = new ArrayList<TagTriggerCriteria>();
		String canonicalTagName = tagName.trim().toLowerCase();
		 
		def queryResults =	TagTriggerCriteria.executeQuery( "select criteria from TagTriggerCriteria as criteria where criteria.tag = :tagName", [tagName:canonicalTagName] );
		
		if( queryResults != null )
		{
			triggerCriteria.addAll( queryResults );
		}	
			
		println "Found ${triggerCriteria.size()} matching TagTriggerCriteria!";
		
		return triggerCriteria;
	}
	
	// a new piece of content was delivered, process "content" (body and title keyword) triggers
	public void fireContentTriggers()
	{
		// note: If we want to just rely on the lucene index to test this stuff, we need to know
		// that we aren't processing this until Lucene indexing is done.  
		
		println "processing content triggers..."
		
	}
			
}
 
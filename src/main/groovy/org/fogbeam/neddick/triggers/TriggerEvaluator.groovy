package org.fogbeam.neddick.triggers

import org.fogbeam.neddick.Entry
import org.fogbeam.neddick.triggers.criteria.AboveScoreTriggerCriteria
import org.fogbeam.neddick.triggers.criteria.BaseTriggerCriteria
import org.fogbeam.neddick.triggers.criteria.AndCriteria
import org.fogbeam.neddick.triggers.criteria.BodyKeywordTriggerCriteria
import org.fogbeam.neddick.triggers.criteria.OrCriteria
import org.fogbeam.neddick.triggers.criteria.TagTriggerCriteria
import org.fogbeam.neddick.triggers.criteria.TitleKeywordTriggerCriteria

public class TriggerEvaluator
{
	
	public boolean evaluateTriggerCriteria( final Entry entry, final BaseTrigger trigger )
	{
		
		BaseTriggerCriteria criteria = trigger.triggerCriteria;
		
		boolean retVal = evaluateCriteria( entry, criteria );
		
		log.debug "returning retVal: ${retVal}";
		return retVal;
	}

	private boolean evaluateCriteria( Entry entry, OrCriteria or )
	{
		log.debug "in OrCriteria";
		
		boolean lhsResult = evaluateCriteria( entry, or.leftHandSide );
		if( lhsResult == true )
		{
			log.debug "OrCriteria returning true";
			return true;
		}
		
		boolean rhsResult = evaluateCriteria( entry, or.rightHandSide );
		if( rhsResult == true )
		{
			log.debug "OrCriteria returning true";
			return true;
		}
		
		log.debug "OrCriteria returning false";
		return false;
	}
	
	private boolean evaluateCriteria( Entry entry, AndCriteria and )
	{
		log.debug "in AndCriteria";
		
		boolean lhsResult = evaluateCriteria( entry, and.leftHandSide );
		if( lhsResult == false )
		{
			log.debug "AndCriteria returning false";
			return false;
		}
		
		boolean rhsResult = evaluateCriteria( entry, and.rightHandSide );
		if( rhsResult == false )
		{
			log.debug "AndCriteria returning false";
			return false;
		}
		
		log.debug "AndCriteria returning true";
		return true;
	}	
	
	private boolean evaluateCriteria( Entry entry, BodyKeywordTriggerCriteria criteria )
	{
		log.debug "BodyKeywordTriggerCriteria";
		
		/*
		if( entry.bodyKeywords.contains( criteria.bodyKeyword ))
		{
			log.debug "BodyKeywordTriggerCriteria return true for keyword: ${criteria.bodyKeyword}";
			return true;
		}
		else
		{
			log.debug "BodyKeywordTriggerCriteria return false for keyword: ${criteria.bodyKeyword}";
			return false;
		}
		*/
		
		return false;
	}
	
	private boolean evaluateCriteria( Entry entry, TagTriggerCriteria criteria )
	{
		log.debug "TagTriggerCriteria";
		return false;
	}

	private boolean evaluateCriteria( Entry entry, TitleKeywordTriggerCriteria criteria )
	{
		log.debug "TitleKeywordTriggerCriteria";
		return false;
	}

	private boolean evaluateCriteria( Entry entry, AboveScoreTriggerCriteria criteria )
	{
		switch( criteria.scoreName )
		{
			case "raw":
				
				log.debug "raw";
				int entryScore = entry.score;
				log.debug "found score as: ${entryScore}";
				
				if( entryScore > criteria.aboveScoreThreshold )
				{
					log.debug "returning true";
					return true;
				}
				else
				{
					log.debug "returning false";
					return false;
				}
			default:
				log.debug "Why are we here?";
				break;
		}
	}

			
}

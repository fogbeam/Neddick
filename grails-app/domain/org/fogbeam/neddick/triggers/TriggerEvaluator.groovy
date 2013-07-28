package org.fogbeam.neddick.triggers

import org.fogbeam.neddick.Entry
import org.fogbeam.neddick.triggers.criteria.AboveScoreTriggerCriteria
import org.fogbeam.neddick.triggers.criteria.AbstractBaseTriggerCriteria
import org.fogbeam.neddick.triggers.criteria.AndCriteria
import org.fogbeam.neddick.triggers.criteria.BodyKeywordTriggerCriteria
import org.fogbeam.neddick.triggers.criteria.OrCriteria
import org.fogbeam.neddick.triggers.criteria.TagTriggerCriteria
import org.fogbeam.neddick.triggers.criteria.TitleKeywordTriggerCriteria

class TriggerEvaluator
{
	
	public boolean evaluateTriggerCriteria( final Entry entry, final AbstractBaseTrigger trigger )
	{
		
		AbstractBaseTriggerCriteria criteria = trigger.triggerCriteria;
		
		boolean retVal = evaluateCriteria( entry, criteria );
		
		println "returning retVal: ${retVal}";
		return retVal;
	}

	private boolean evaluateCriteria( Entry entry, OrCriteria or )
	{
		println "in OrCriteria";
		
		boolean lhsResult = evaluateCriteria( entry, or.lhs );
		if( lhsResult == true )
		{
			println "OrCriteria returning true";
			return true;
		}
		
		boolean rhsResult = evaluateCriteria( entry, or.rhs );
		if( rhsResult == true )
		{
			println "OrCriteria returning true";
			return true;
		}
		
		println "OrCriteria returning false";
		return false;
	}
	
	private boolean evaluateCriteria( Entry entry, AndCriteria and )
	{
		println "in AndCriteria";
		
		boolean lhsResult = evaluateCriteria( entry, and.lhs );
		if( lhsResult == false )
		{
			println "AndCriteria returning false";
			return false;
		}
		
		boolean rhsResult = evaluateCriteria( entry, and.rhs );
		if( rhsResult == false )
		{
			println "AndCriteria returning false";
			return false;
		}
		
		println "AndCriteria returning true";
		return true;
	}	
	
	private boolean evaluateCriteria( Entry entry, BodyKeywordTriggerCriteria criteria )
	{
		println "BodyKeywordTriggerCriteria";
		
		/*
		if( entry.bodyKeywords.contains( criteria.bodyKeyword ))
		{
			println "BodyKeywordTriggerCriteria return true for keyword: ${criteria.bodyKeyword}";
			return true;
		}
		else
		{
			println "BodyKeywordTriggerCriteria return false for keyword: ${criteria.bodyKeyword}";
			return false;
		}
		*/
		
		return false;
	}
	
	private boolean evaluateCriteria( Entry entry, TagTriggerCriteria criteria )
	{
		println "TagTriggerCriteria";
		return false;
	}

	private boolean evaluateCriteria( Entry entry, TitleKeywordTriggerCriteria criteria )
	{
		println "TitleKeywordTriggerCriteria";
		return false;
	}

	private boolean evaluateCriteria( Entry entry, AboveScoreTriggerCriteria criteria )
	{
		switch( criteria.scoreName )
		{
			case "raw":
				
				println "raw";
				int entryScore = entry.score;
				println "found score as: ${entryScore}";
				
				if( entryScore > criteria.aboveScoreThreshold )
				{
					println "returning true";
					return true;
				}
				else
				{
					println "returning false";
					return false;
				}
			default:
				println "Why are we here?";
				break;
		}
	}

			
}

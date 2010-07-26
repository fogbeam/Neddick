package org.fogbeam.neddick

import org.fogbeam.neddick.Entry;
import org.fogbeam.neddick.User;
import org.fogbeam.neddick.Vote;

class VoteService {
	
	// TODO: figure out what this should be for real
	boolean transactional = false

	public Entry submitUpVote( Entry entry, Vote upVote, User submitter )
	{
		
		println "Score before doing anything: ${entry.score}";
		
		upVote.submitter = submitter;

		boolean disregardThisVote = false;
        println "examining existing votes";
        for( vote in entry.votes )
        {
        	println "found ${entry.votes.size()} existing votes";
			
            // don't acknowledge a second or subsequent upvote from the same user
            if( vote.submitter.userId.equals( submitter.userId ) && vote.weight == 1 && vote.enabled == true )
            {
            	println "found an existing upvote, setting disregard flag to TRUE";
            	vote.enabled = false;
            	vote.save();
            	disregardThisVote = true;
            	break;
            }             	
       	
        }
		
        	
    	for( vote in entry.votes )
    	{
            // I'm upvoting, so if there's an existing downvote for me, 
            // disable it
            if( vote.submitter.userId.equals( submitter.userId ) && vote.weight == -1 && vote.enabled == true )
            {
                println "found an existing downVote, disabling it";
                vote.enabled = false;
				vote.save();
				break;
            }
    	}
    	
        if( !disregardThisVote )
        {
			println "NOT disregarding vote, so recalculating score after vote";
        	entry.votes.add( upVote );
        	upVote.entry = entry;
        }
        
		
		// calculate current score
		int score = 0;
		for( vote in entry.votes )
		{
			if( vote.enabled )
			{
				score = score + vote.weight;
			}
		}
		println "calculated score as ${score}";
		entry.score = score;
		
		println "Score just before saving: ${entry.score}";
		if( !entry.save() )
		{
			println( "Failed to save vote!");
			entry.errors.allErrors.each { println it };
		}		
		else
		{
			println "Saved updated Entry with new vote";	
		}
		
		return entry;
		
	}

	
	public Entry submitDownVote( Entry entry, Vote downVote, User submitter )
	{
		downVote.submitter = submitter;
        
		boolean disregardThisVote = false;
        println "examining existing votes";
        for( vote in entry.votes )
		{
			
            // don't acknowledge a second or subsequent downvote from the same user
            if( vote.submitter.userId.equals( submitter.userId ) && vote.weight == -1 && vote.enabled == true )
            {
            	println "found an existing downvote, setting disregard flag to TRUE";
                vote.enabled = false;
                vote.save();
            	disregardThisVote = true;
                break;
            }
            
		}
        
		for( vote in entry.votes )
		{
            // I'm downvoting, so if there's an existing upvote for me, 
            // disable it
            if( vote.submitter.userId.equals( submitter.userId ) && vote.weight == 1 && vote.enabled == true )
            {
            	println "found an existing upvote, disabling it";
                vote.enabled = false;
            }
		}

        if( !disregardThisVote )
        {
        	entry.votes.add( downVote );
        	downVote.entry = entry;			
	        
        	// calculate current score
            int score = 0;
            for( vote in entry.votes )
            {
            	if( vote.enabled )
            	{	
            		score = score + vote.weight;
            	}
            }
            entry.score = score;
        
        }
        
        if( !entry.save() )
        {
            println( "Failed to save vote!");
            entry.errors.allErrors.each { println it };

        }
			
        return entry;
	}
	
}

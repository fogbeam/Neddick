package org.fogbeam.neddick

import org.fogbeam.neddick.Entry;
import org.fogbeam.neddick.User;
import org.fogbeam.neddick.Vote;

class VoteService {
	
	// TODO: figure out what this should be for real
	boolean transactional = false

	public Entry submitUpVote( Entry entry, Vote upVote, User submitter )
	{
		
		upVote.submitter = submitter;

		boolean disregardThisVote = false;
        println "examining existing votes";
        for( vote in entry.votes )
        {
        	
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
                println "found an existing upvote, disabling it";
                vote.enabled = false;
            }
    	}
    	
        if( !disregardThisVote )
        {
    	
        	entry.votes.add( upVote );
        	upVote.entry = entry;
        
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

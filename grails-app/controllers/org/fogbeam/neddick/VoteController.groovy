package org.fogbeam.neddick

class VoteController {

	def scaffold = true;
	
	def voteService;
	
	def submitVoteUp = {
		
		println "submitVoteUp";
			
		// submit a new UP...
		def entryId = params.entryId;
		def entry = Entry.findById( entryId );
		
		if( session.user )
		{
			def user = session.user;
			Vote upVote = new Vote( weight:1, enabled:true);
			entry = voteService.submitUpVote( entry, upVote, user );
			
			
			render( contentType:"application/json" ) {
				
				resp( buttonId:"upVote.${entryId}", entryId:"${entryId}", score:"${entry.score}")
			}
        
		}
		else
		{
			println "Must be logged in to vote!";
		}
	}
	
	def submitVoteDown = {
		
		println "submitVoteDown";	
		
		// submit a new vote DOWN...
        def entryId = params.entryId;
        def entry = Entry.findById( entryId );
        
        if( session.user )
        {
        	def user = session.user;
	        Vote downVote = new Vote( weight:-1, enabled:true);
	        entry = voteService.submitDownVote( entry, downVote, user );
	        
	        render( contentType:"application/json" ) {
	        	
	        	resp( buttonId:"upVote.${entryId}", entryId:"${entryId}", score:"${entry.score}")
	        }

        }
        else
        {
            println "Must be logged in to vote!";	        	
        }
	}
	
}

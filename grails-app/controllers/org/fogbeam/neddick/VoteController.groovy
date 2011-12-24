package org.fogbeam.neddick

class VoteController {

	def scaffold = true;
	
	def voteService;
	
	def submitVoteUp = {
		
		log.debug( "submitVoteUp" );
			
		// submit a new UP...
		def entryId = params.entryId;
		def entry = Entry.findById( entryId );
		
		if( session.user )
		{
			def user = session.user;
			Vote upVote = new Vote( weight:1, enabled:true);
			log.debug( "submitting upVote for user ${session.user} with weight: ${upVote.weight}" );
			entry = voteService.submitUpVote( entry, upVote, user );
			
			log.debug( "submitVoteUp, rendering AJAX response.  entryId: ${entryId}, score: ${entry.score}" );
			render( contentType:"application/json" ) {
				
				resp( buttonId:"upVote.${entryId}", entryId:"${entryId}", score:"${entry.score}")
			}
        
		}
		else
		{
			log.info( "Must be logged in to vote!" );
		}
	}
	
	def submitVoteDown = {
		
		log.debug( "submitVoteDown" );	
		
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
            log.info( "Must be logged in to vote!" );	        	
        }
	}
}

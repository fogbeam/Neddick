package org.fogbeam.neddick

import grails.plugin.springsecurity.annotation.Secured

class VoteController 
{	
	def voteService;

	@Secured(["ROLE_USER","ROLE_ADMIN"])
	def submitVoteUp() 
	{
		log.debug( "submitVoteUp" );
			
		// submit a new UP...
		def entryId = params.entryId;
		def entry = Entry.findById( entryId );
		
		User user = userService.getLoggedInUser();
		Vote upVote = new Vote( weight:1, enabled:true);
		log.debug( "submitting upVote for user ${user} with weight: ${upVote.weight}" );
		entry = voteService.submitUpVote( entry, upVote, user );
		
		log.debug( "submitVoteUp, rendering AJAX response.  entryId: ${entryId}, score: ${entry.score}" );
		render( contentType:"application/json" ) {
			
			resp( buttonId:"upVote.${entryId}", entryId:"${entryId}", score:"${entry.score}")
		}
        

	}
	
	@Secured(["ROLE_USER","ROLE_ADMIN"])
	def submitVoteDown()
	{
		log.debug( "submitVoteDown" );	
		
		// submit a new vote DOWN...
        def entryId = params.entryId;
        def entry = Entry.findById( entryId );
        
    	def user = userService.getLoggedInUser();
        Vote downVote = new Vote( weight:-1, enabled:true);
        entry = voteService.submitDownVote( entry, downVote, user );
        
        render( contentType:"application/json" ) {
        	
        	resp( buttonId:"upVote.${entryId}", entryId:"${entryId}", score:"${entry.score}")
        }
	}
}
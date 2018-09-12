package org.fogbeam.neddick

import grails.plugin.springsecurity.annotation.Secured

class CommentController 
{
	
	def entryService;
	def jmsService;
	
	@Secured(["ROLE_USER","ROLE_ADMIN"])
	def addComment()
	{		
		// lookup the Entry by id
		log.debug( "entryId: ${params.entryId}" );
		Entry entry = Entry.findById(params.entryId);
			
		// add the comment to the Entry
		log.debug( "entry: ${entry}" );
		User user = userService.getLoggedInUser();
		log.debug( "user: ${user}" );
	
		Comment newComment = new Comment();
		newComment.text = params.commentText;
		newComment.creator = user;
		newComment.entry = entry;
		
		if( !newComment.save(flush:true) )
		{
			log.debug "Failed to save Comment!";
			newComment.errors.allErrors.each { log.debug it };
			
			return;
		}
		
		entry.addToComments( newComment );
		
		entryService.saveEntry( entry );
		
    	// send JMS message saying "new entry submitted"
    	def newCommentMessage = [msgType:"NEW_COMMENT", entry_id:entry.id, entry_uuid:entry.uuid, 
    	                       	comment_id:newComment.id, comment_uuid:newComment.uuid, comment_text:newComment.text ];
    
    	// send a JMS message to our testQueue
		// sendJMSMessage("searchQueue", newCommentMessage );			
		jmsService.send( queue: 'searchQueue', newCommentMessage, 'standard', null );
		
		log.debug( "saved Comment for user ${user.userId}, entry ${entry.id}" );
	
		redirect(controller:"entry", action:"viewEntry", params:[uuid:entry.uuid]);	
	}
}

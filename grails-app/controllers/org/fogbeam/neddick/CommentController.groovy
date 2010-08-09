package org.fogbeam.neddick


class CommentController {

	def scaffold = true;
	
	def entryService;
	
	def addComment = {
			
		// lookup the Entry by id
		println "entryId: ${params.entryId}";
		Entry entry = Entry.findById(params.entryId);
			
		// add the comment to the Entry
		if( session.user )
		{
			println "entry: ${entry}";
			def user = User.findByUserId( session.user.userId );
			println "user: ${user}";
		
			Comment newComment = new Comment();
			newComment.text = params.commentText;
			newComment.creator = user;
			newComment.entry = entry;
			newComment.save();
			
			entry.addToComments( newComment );
			
			entryService.saveEntry( entry );
			
	    	// send JMS message saying "new entry submitted"
	    	def newCommentMessage = [msgType:"NEW_COMMENT", entry_id:entry.id, entry_uuid:entry.uuid, 
	    	                       	comment_id:newComment.id, comment_uuid:newComment.uuid, comment_text:newComment.text ];
	    
	    	// send a JMS message to our testQueue
			sendJMSMessage("searchQueue", newCommentMessage );			
			
			println( "saved Comment for user ${user.userId}, entry ${entry.id}" );
		}
		else
		{
			// do nothing, can't comment if you're not logged in.
			println( "doing nothing, not logged in!" );
		}
	
		redirect(controller:"entry", action:"viewEntry", params:[uuid:entry.uuid]);
		
		
	}
}

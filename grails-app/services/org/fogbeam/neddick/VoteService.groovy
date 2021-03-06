package org.fogbeam.neddick

import org.hibernate.SQLQuery

class VoteService 
{
	
	private static final long BEGINNING_OF_TIME = 1230786000000; // 01 01 2009 00:00:00
	
	// TODO: figure out what this should be for real
	boolean transactional = false

	def sessionFactory;
	def triggerService;
	def jmsService;
	
	
	public Entry submitUpVote( Entry entry, Vote upVote, User submitter )
	{
		
		log.debug( "Score before doing anything: ${entry.score}" );
		
		upVote.submitter = submitter;

		boolean disregardThisVote = false;
        log.debug( "examining existing votes" );
        for( vote in entry.votes )
        {
        	log.debug( "found ${entry.votes.size()} existing votes" );
			
            // don't acknowledge a second or subsequent upvote from the same user
            if( vote.submitter.userId.equals( submitter.userId ) && vote.weight == 1 && vote.enabled == true )
            {
            	log.debug( "found an existing upvote, setting disregard flag to TRUE" );
            	vote.enabled = false;
            	if( !vote.save(flush:true) )
            	{
					log.error( "Error saving Vote" );
					vote.errors.allErrors.each { log.error( it.toString() ) }
				}
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
                log.debug( "found an existing downVote, disabling it" );
                vote.enabled = false;
				if( !vote.save(flush:true) )
				{
					log.error( "Error saving Vote " );
					vote.errors.allErrors.each { log.error( it.toString() ) }
				}
				break;
            }
    	}
    	
        if( !disregardThisVote )
        {
			log.debug( "NOT disregarding vote, so recalculating score after vote" );
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

		log.debug( "calculated score as ${score}" );
		entry.score = score;
		
		log.debug( "Score just before saving: ${entry.score}" );
		if( !entry.save(flush:true) )
		{
			log.error( "Failed to save vote!");
			entry.errors.allErrors.each { log.error( it.toString() ) };
		}		
		else
		{
			log.debug( "Saved updated Entry with new vote" );	
		}
		
		// we've recorded a vote.  Now update the score for this entry in
		// the user_entry_score_link table, reflecting any required personalization
		// settings...
		
		// calculate hotness and controversy scores
		Date now = new Date();
		def hotness = calculateHotness( entry, now.getTime() );
		def controversy = calculateControversy( entry, now.getTime() );
		
		log.debug( "we've recorded a vote.  Now update the score for this entry in the user_entry_score_link table" );
		def hibSession = sessionFactory.getCurrentSession();
		SQLQuery query =
			hibSession.createSQLQuery( 
				"update user_entry_score_link set entry_base_score = ${entry.score}, entry_controversy = ${controversy}, entry_hotness = ${hotness} where " 
					+ " entry_id = ${entry.id} and user_id not in (select owner_id from user_to_user_link where target_id = ${upVote.submitter.id})" );
		try
		{
			int updated = query.executeUpdate();
			if( updated < 1 )
			{
				log.error( "No entities updated when updating user_entry_score_link" );
			}
		}
		catch( Exception e )
		{
			log.error( "Error updating user_entry_score_link", e );
		}
		
		// ok, we've updated scores for all users who do NOT have a personalization link with the submitter of this vote
		// now let's get the list of Users who DO have a personalization link, and update their score independently
		List<User> usersWithPersonalization = User.executeQuery( "from User user where user.id in ( select link.owner.id from UserToUserLink as link where link.target = ?)", [submitter] );
		for( User ownerUser : usersWithPersonalization )
		{
			// get the UserToUserLink for this relationship
			List<UserToUserLink> u2uLinks = UserToUserLink.executeQuery( "select link from UserToUserLink as link where link.owner = ? and link.target = ?", [ownerUser, submitter] );
			UserToUserLink link = u2uLinks[0];
			def boost = link.boost;
			List<UserEntryScoreLink> uesLinks = UserEntryScoreLink.executeQuery( "select link from UserEntryScoreLink as link where link.user = ? and link.entry = ?", [ownerUser, entry] );
			UserEntryScoreLink uesLink = uesLinks[0];
			uesLink.entryBaseScore += ( upVote.weight + boost );
			if( !uesLink.save(flush:true) )
			{
				log.error( "Error saving UserEntryScoreLink" );
				uesLink.errors.allErrors.each { println( it.toString() ) }
			}
		}		
		
		
		// send JMS message for triggers, with notification that a score has changed
		log.info( "Sending ScoreChanged JMS Message" );
		def scoreChangedMessage = [msgType:"ENTRY_SCORE_CHANGED", entry_uuid:entry.uuid, newScore:entry.score ];
	
		// send a JMS message to our testQueue
		// sendJMSMessage( "neddickTriggerQueue", scoreChangedMessage );
		log.info( "Sending ScoreChanged JMS Message to neddickTriggerQueue" );
		jmsService.send( queue: 'neddickTriggerQueue', scoreChangedMessage, 'standard', null );
		
		// sendJMSMessage( "neddickFilterQueue", scoreChangedMessage );
		log.info( "Sending ScoreChanged JMS Message to neddickFilterQueue" );
		jmsService.send( queue: 'neddickFilterQueue', scoreChangedMessage, 'standard', null );
		
		return entry;
	}

	
	public Entry submitDownVote( Entry entry, Vote downVote, User submitter )
	{
		downVote.submitter = submitter;
        
		boolean disregardThisVote = false;
        log.debug( "examining existing votes" );
        for( vote in entry.votes )
		{
			
            // don't acknowledge a second or subsequent downvote from the same user
            if( vote.submitter.userId.equals( submitter.userId ) && vote.weight == -1 && vote.enabled == true )
            {
            	log.debug( "found an existing downvote, setting disregard flag to TRUE" );
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
            	log.debug( "found an existing upvote, disabling it" );
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
            log.error( "Failed to save vote!");
            // entry.errors.allErrors.each { p rintln it };

        }
			
		// we've recorded a vote.  Now update the score for this entry in
		// the user_entry_score_link table, reflecting any required personalization
		// settings...
		
		// calculate hotness and controversy scores
		Date now = new Date();
		def hotness = calculateHotness( entry, now.getTime() );
		def controversy = calculateControversy( entry, now.getTime() );
		
		log.debug( "we've recorded a vote.  Now update the score for this entry in the user_entry_score_link table" );
		def hibSession = sessionFactory.getCurrentSession();
		def query =
			hibSession.createSQLQuery(
				"update user_entry_score_link set entry_base_score = ${entry.score}, entry_controversy = ${controversy}, entry_hotness = ${hotness} where "
					+ " entry_id = ${entry.id} and user_id not in (select owner_id from user_to_user_link where target_id = ${downVote.submitter.id})" );
		try
		{
			query.executeUpdate();
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
		
		// ok, we've updated scores for all users who do NOT have a personalization link with the submitter of this vote
		// now let's get the list of Users who DO have a personalization link, and update their score independently
		List<User> usersWithPersonalization = User.executeQuery( "from User user where user.id in ( select link.owner.id from UserToUserLink as link where link.target = ?)", [submitter] );
		for( User ownerUser : usersWithPersonalization )
		{
			// get the UserToUserLink for this relationship
			List<UserToUserLink> u2uLinks = UserToUserLink.executeQuery( "select link from UserToUserLink as link where link.owner = ? and link.target = ?", [ownerUser, submitter] );
			UserToUserLink link = u2uLinks[0];
			def boost = link.boost;
			List<UserEntryScoreLink> uesLinks = UserEntryScoreLink.executeQuery( "select link from UserEntryScoreLink as link where link.user = ? and link.entry = ?", [ownerUser, entry] );
			UserEntryScoreLink uesLink = uesLinks[0];
			int val = (downVote.weight - boost );
			def newScore = uesLink.entryBaseScore + val; 
			uesLink.entryBaseScore = newScore;
			uesLink.save();
		}
		
		
		/* NOTE: for now, we assume triggers only case about the case "a score is voted UP past a certain
		 * threshold, so let's skip sending notifications to the triggerQueue for downvotes.
		 */		
		
        return entry;
	}

	
	public double calculateHotness( final Entry entry, final long now )
	{
		// make "hotness" the same as the raw score initially
		long hotness = entry.score * 10;
		log.debug( "set initial \"hotness\" as ${hotness}" );
		
		long age = ( now - BEGINNING_OF_TIME ) - (entry.dateCreated.time - BEGINNING_OF_TIME );
		age = age / (1000*60);
		
		log.debug( "age in milliseconds: ${age}" );
		
		entry.age = age;
		
		def decayForAge = Math.log( age );
		log.debug( "decayForAge: ${decayForAge}" );
		
		double finalHotness =  hotness - decayForAge;
		log.debug( "final \"hotness\" score: ${finalHotness}" );
		return finalHotness;
	}

	
	public double calculateControversy( final Entry entry, final long now )
	{
		def votes = entry.votes;
		// int score = 0;
		int totalVotes = 0;
		int upVotes = 0;
		int downVotes = 0;
		for( vote in votes )
		{
			if( vote.enabled )
			{
				// score = score + vote.weight;
				totalVotes++;
			
				if( vote.weight > 0 )
				{
					upVotes++;
				}
				else if( vote.weight < 0 )
				{
					downVotes++;
				}
				else
				{
					// a vote with a weight of 0 is nonsensical
				}
			}
		}
				
		long age = ( now - BEGINNING_OF_TIME ) - (entry.dateCreated.time - BEGINNING_OF_TIME );
		age = age / (1000*60);
		
		log.debug( "age in milliseconds: ${age}" );
		
		entry.age = age;
		
		def decayForAge = Math.log( age );
		
		double ratio = 0.0;
		if( upVotes != 0 && downVotes != 0 )
		{
			Math.min( upVotes, downVotes ) / Math.max(upVotes, downVotes)
		}
		
		log.debug( "totalVotes: ${totalVotes}, upVotes: ${upVotes}, downVotes: ${downVotes}, ratio: ${ratio}, decayForAge: ${decayForAge}" );
		
		double finalControversy = ( totalVotes * ratio ) - decayForAge;
		log.debug( "final \"controversy\" score: ${finalControversy}" );
		
		return finalControversy;
	}
}
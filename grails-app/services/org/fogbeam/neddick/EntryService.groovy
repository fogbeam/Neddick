package org.fogbeam.neddick

import org.fogbeam.neddick.Channel;
import org.fogbeam.neddick.Comment;
import org.fogbeam.neddick.Entry;
import org.fogbeam.neddick.User;
import org.fogbeam.neddick.UserToUserLink;

class EntryService {
	
	private static final long BEGINNING_OF_TIME = 1230786000000; // 01 01 2009 00:00:00
	
	public Entry findById( final String id )
	{
		Entry entry = Entry.findById( id );
		return entry;		
	}
	
	public Entry findByUuid( final String uuid )
	{
		Entry entry = Entry.findByUuid( uuid );
		return entry;
	}
	
	public List<Entry> findByUrlAndChannel( final String url, final Channel channel )
	{
		// check if this channel already has an Entry for this same link
		List<Entry> entries = Entry.executeQuery( "select entry from Entry as entry where entry.url = ? and entry.channel = ?", [url, channel] );
	
		return entries;	
	}
	
	public void saveEntry( final Entry entry )
	{
		if( !entry.save() )
		{
			println( "Updating entry: ${entry.id} FAILED");
			entry.errors.allErrors.each { println it };
		}
	}
	
	// give me everything for this channel, as long this user has not asked to hide it.
	public List<Entry> getAllNonHiddenEntriesForUser( final User user ) 
	{
		List<Entry> entries = new ArrayList<Entry>();
		entries.addAll( Entry.executeQuery( "select entry from Entry as entry, User as user where user.userId = ? and entry not in elements(user.hiddenEntries) order by entry.dateCreated desc", [user.userId] ) );
		this.calculateScores( entries, user );
		return entries;
	}
	
	public List<Entry> getAllNonHiddenEntriesForUser( final Channel channel, final User user ) 
	{
		List<Entry> entries = new ArrayList<Entry>();
		entries.addAll( Entry.executeQuery( "select entry from Entry as entry, User as user where user.userId = ? and entry.channel = ? and entry not in elements(user.hiddenEntries)  order by entry.dateCreated desc", [user.userId, channel] ) );
		this.calculateScores( entries, user );
		return entries;
	}

	public List<Entry> getAllEntries()
	{
		List<Entry> entries = new ArrayList<Entry>();
		entries.addAll( Entry.executeQuery( "select entry from Entry as entry order by entry.dateCreated desc") );
		this.calculateScores( entries );
		return entries;		
	}	
	
	public List<Entry> getAllEntries(final Channel channel)
	{
		List<Entry> entries = new ArrayList<Entry>();
		entries.addAll( Entry.executeQuery( "select entry from Entry as entry where entry.channel = ?  order by entry.dateCreated desc", [channel] ) );
		this.calculateScores( entries );
		return entries;		
	}

	public List<Entry> getAllEntries( final User user )
	{
		List<Entry> entries = new ArrayList<Entry>();
		entries.addAll( Entry.executeQuery( "select entry from Entry as entry where entry.submitter = ? order by entry.dateCreated desc", [user] ) );
		this.calculateScores( entries );
		return entries;
	}
	
	public List<Entry> getHotEntriesForUser( final Channel channel, final User user ) 
	{
		List<Entry> entries = new ArrayList<Entry>();
		entries.addAll( Entry.executeQuery( "select entry from Entry as entry, User as user where user.userId = ? and entry.channel = ? and entry not in elements(user.hiddenEntries) order by entry.dateCreated desc", [user.userId, channel] ) );

		this.calculateScores( entries, user );
		this.calculateHotness( entries );
		
		return entries;
	}
	
	public List<Entry> getHotEntries(final Channel channel)
	{
		List<Entry> entries = new ArrayList<Entry>();
		entries.addAll( Entry.executeQuery( "select entry from Entry as entry where entry.channel = ?  order by entry.dateCreated desc", [channel] ) );
		this.calculateScores( entries );
		this.calculateHotness( entries );
		
		return entries;		
	}
	
	public List<Entry> getNewEntriesForUser(final Channel channel, final User user ) 
	{
		List<Entry> entries = new ArrayList<Entry>();
		entries.addAll( Entry.executeQuery( "select entry from Entry as entry, User as user where user.userId = ? and entry.channel = ? and entry not in elements(user.hiddenEntries) order by entry.dateCreated desc", [user.userId, channel] ) );
		this.calculateScores( entries, user );
		
		return entries;
	}
	
	public List<Entry> getNewEntries(final Channel channel)
	{
		List<Entry> entries = new ArrayList<Entry>();
		entries.addAll( Entry.executeQuery( "select entry from Entry as entry where entry.channel = ? order by entry.dateCreated desc", [channel] ) );
		this.calculateScores( entries );
		
		return entries;		
	}

	public List<Entry> getTopEntriesForUser(final Channel channel, final User user ) 
	{
		List<Entry> entries = new ArrayList<Entry>();
		entries.addAll( Entry.executeQuery( "select entry from Entry as entry, User as user where user.userId = ? and entry.channel = ? and entry not in elements(user.hiddenEntries) order by entry.dateCreated desc", [user.userId, channel] ) );
		this.calculateScores( entries, user );
	
		return entries;
	}
	
	public List<Entry> getTopEntries(final Channel channel)
	{
		List<Entry> entries = new ArrayList<Entry>();
		entries.addAll( Entry.executeQuery( "select entry from Entry as entry where entry.channel = ? order by entry.dateCreated desc", [channel] ) );
		this.calculateScores( entries );
		
		return entries;		
	}
	
	
	public List<Entry> getControversialEntriesForUser( final Channel channel, final User user ) 
	{
		List<Entry> entries = new ArrayList<Entry>();
		entries.addAll( Entry.executeQuery( "select entry from Entry as entry, User as user where user.userId = ? and entry.channel = ? and entry not in elements(user.hiddenEntries) order by entry.dateCreated desc", [user.userId, channel] ) );
		this.calculateScores( entries, user );
		this.calculateControversy( entries );
				
		return entries;
	}
	
	public List<Entry> getControversialEntries(final Channel channel)
	{
		List<Entry> entries = new ArrayList<Entry>();
		entries.addAll( Entry.executeQuery( "select entry from Entry as entry where entry.channel = ? order by entry.dateCreated desc", [channel] ) );
		this.calculateScores( entries );
		this.calculateControversy( entries );
		
		return entries;		
	}
	
	public List<Entry> getSavedEntriesForUser( final User user ) 
	{
		println "getSavedEntries";
		List<Entry> entries = new ArrayList<Entry>();
		def theUser = User.findByUserId( user.userId );
		println "found user: ${theUser}";
		def tempEntries = theUser.savedEntries;
		println "found ${tempEntries.size()} savedEntries";
		entries.addAll( tempEntries );
		this.calculateScores( entries, user );
		
		return entries;
	}	
	
	public List<Entry> getHiddenEntriesForUser( final User user ) 
	{
		List<Entry> entries = new ArrayList<Entry>();
		def theUser = User.findByUserId( user.userId );
        def tempEntries = theUser.hiddenEntries;
		entries.addAll( tempEntries );
		this.calculateScores( entries, user );
		
		return entries;
		
	}

	public List<Entry> getCommentsForUser( final User user )
	{
		List<Comment> comments = new ArrayList<Comment>();
		comments.addAll( Comment.executeQuery( "select comment from Comment as comment where comment.creator = ?", [user] ) );
		
		return comments;
	}
	
	private calculateScores( final List<Entry> entries, final User user = null ) 
	{
		entries.each() { 
			this.calculateScore( it, user );
		}
        
    }

	public calculateScore( final Entry entry, final User user = null )
	{
    	// calculate the score for this Entry
		def votes = entry.votes;
    	int score = 0;
    	for( vote in votes )
    	{
    		if( vote.enabled ) 
    		{
    			score = score + vote.weight;
    		
    			User submitter = vote.submitter;
    			
	    		/* adjust score for personalization by user */
	    		if( user )
	    		{
	    			Set<UserToUserLink> childLinks = user.childUserLinks;
	    			if( childLinks )
	    			{
	    				for( UserToUserLink link in childLinks )
	    				{
	    					if( submitter.userId.equals( link.target.userId ))
	    					{
	    						// adjust the current score based on the relationship between the
	    						// current user and the voter.
	    						if( vote.weight > 0 )
	    						{
	    							// if the vote was an "up vote" adding the "boost"
	    							// gives the right effect whether the boost value is positive
	    							// or negative.
	    							score += link.boost;	
	    						}
	    						else 
	    						{
	    							// conversely, if the vote was a down vote, subtracting the
	    							// boost value gives us the right effect.  
	    							score -= link.boost;
	    						}
	    						
	    					}
	    				}
	    			}
	    		}
    		}
    	}
        	
    	// println "Calculated score for entry ${it.id} as ${score}";
    	entry.score = score;		
	}
	
	private calculateHotness( final List<Entry> entries ) 
	{
		
		long now = System.currentTimeMillis();
		for( Entry entry in entries )
		{ 
			entry.hotness = this.calculateHotness( entry, now );
		} 
	}
	
	public double calculateHotness( final Entry entry, final long now )
	{
		// make "hotness" the same as the raw score initially
		long hotness = entry.score * 10;
		println "set initial \"hotness\" as ${hotness}";
		
		long age = ( now - BEGINNING_OF_TIME ) - (entry.dateCreated.time - BEGINNING_OF_TIME );
		age = age / (1000*60);
		
		println "age in milliseconds: ${age}";
		
		entry.age = age;
		
		def decayForAge = Math.log( age );
		println "decayForAge: ${decayForAge}";
		
		double finalHotness =  hotness - decayForAge;
		println "final \"hotness\" score: ${finalHotness}";
		return finalHotness;
	}
	
	private calculateControversy( final List<Entry> entries ) 
	{
	
		long now = System.currentTimeMillis();
        for( Entry entry in entries )
        {
        	entry.controversy = this.calculateControversy( entry, now );
        }
	
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
        
        // println "\n\nCalculated score for entry ${entry.id} as ${score}";
        // entry.score = score;
        
        long age = ( now - BEGINNING_OF_TIME ) - (entry.dateCreated.time - BEGINNING_OF_TIME );
        age = age / (1000*60);
        
        println "age in milliseconds: ${age}";
        
        entry.age = age;
        
        def decayForAge = Math.log( age );
        
        double ratio = 0.0;
        if( upVotes != 0 && downVotes != 0 )
        {
        	Math.min( upVotes, downVotes ) / Math.max(upVotes, downVotes)
        }
        
        println "totalVotes: ${totalVotes}, upVotes: ${upVotes}, downVotes: ${downVotes}, ratio: ${ratio}, decayForAge: ${decayForAge}";
        
        double finalControversy = ( totalVotes * ratio ) - decayForAge;
        println "final \"controversy\" score: ${finalControversy}";
        
        return finalControversy;
	}
	
	
}

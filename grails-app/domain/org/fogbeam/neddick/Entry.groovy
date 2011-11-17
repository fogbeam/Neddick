package org.fogbeam.neddick

import java.util.List;

class Entry
{	
	
	public Entry() 
	{
		this.uuid = java.util.UUID.randomUUID().toString();
	}
	
    static constraints = 
    {
    	url( nullable:true, maxSize:2048 );
    }
	
    static transients = [ "score", "hotness", "controversy", "age", "siteConfigService", "link" ];
    static mapping = {
		channel lazy:false // eagerly fetch the channel
		submitter lazy:false;
	}


    
    String uuid;
    String title;
    String url;
    Date dateCreated;
    int score = 0;
    double hotness = 0.0;
    double controversy = 0.0;
    long age;
    SortedSet comments;
    Channel channel;
    User submitter;
	UserEntryScoreLink link;
    
    static hasMany = [ votes : Vote, savers: User, hiders: User, comments: Comment, tagEntryLinks:TagEntryLink, userEntryScoreLinks:UserEntryScoreLink  ];

    // NOTE: do we really want this?  Should deleting a User remove Entries from the system?
    static belongsTo = [User];

	public List getTags() 	
	{
		return tagEntryLinks.collect{it.tag}
	}    
    
	List addToTags(Tag tag, User user) 
	{ 
		TagEntryLink.link( tag, this, user );
		return tags;
	}

	List removeFromTags(Tag tag, User user ) 
	{ 
		TagEntryLink.unlink(tag, this, user );
		return tags;
	}     	
}

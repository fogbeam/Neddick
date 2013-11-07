package org.fogbeam.neddick

import java.util.List;

class Entry
{	
	
	public Entry() 
	{
		this.uuid = java.util.UUID.randomUUID().toString();
	}
	
	
    static transients = [ "score", "hotness", "controversy", "age", "siteConfigService", "link", "templateName" ];
    
	
	static mapping = {
		submitter lazy:false;
		tablePerHierarchy false
	}	
	
	static constraints =
	{
		enhancementJSON(nullable:true);
	}
    
    String uuid;
    String title;
	String enhancementJSON;
	
	Date dateCreated;
    int score = 0;
    double hotness = 0.0;
    double controversy = 0.0;
    long age;
    SortedSet comments;
    // Channel channel;
    User submitter;
	UserEntryScoreLink link;
    DataSource theDataSource;
	
	
    static hasMany = [ votes : Vote, savers: User, hiders: User, comments: Comment, channelEntryLinks: ChannelEntryLink, tagEntryLinks:TagEntryLink, userEntryScoreLinks:UserEntryScoreLink  ];

		
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
	
	public List getChannels()
	{
		return channelEntryLinks.collect{it.channel};
	}
	
	List addToChannels(Channel channel)
	{
		ChannelEntryLink.link(this, channel );
		return channels;
	}

	List removeFromEntries(Channel channel )
	{
		ChannelEntryLink.unlink(this, entry, user );
		return entries;
	}
	
	public String getTemplateName()
	{
		return "/renderRawEntry";
	}
}

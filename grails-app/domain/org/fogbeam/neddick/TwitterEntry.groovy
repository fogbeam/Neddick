package org.fogbeam.neddick

class TwitterEntry extends Entry
{
	String tweetContent;
	Long tweetId;
	String senderScreenName;
	String senderFullName;
	
	List<String> urls;
	List<String> hashtags;
	List<String> userMentions;
	
	
	
	static constraints =
	{	
		tweetContent(nullable:true, maxSize:255);
		senderScreenName( nullable:true);
		senderFullName( nullable: true);
	}
	
	public String getTemplateName()
	{
		return "/renderTwitterEntry";
	}
	
}

package org.fogbeam.neddick

class EMailEntry extends Entry
{
	
	String messageId;
	String bodyContent;
	String subject;
	
	static hasMany = ['fromAddress':String, 'toAddress':String];
	
	static constraints =
	{
		messageId( nullable:false, maxSize:2048 );
		
		// TODO: deal with content of arbitrary size, which we won't really
		// want to try to store directly and render like this.
		bodyContent(nullable:true, maxSize:300000);
	}
	
	public String getTemplateName()
	{
		return "/renderEmailEntry";
	}
}
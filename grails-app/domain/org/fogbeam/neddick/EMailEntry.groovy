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
		subject( nullable: true );
		bodyContent(nullable:true);
	}
	
	static mapping =
	{
		bodyContent type: 'text';
	}
	
	
	
	public String getTemplateName()
	{
		return "/renderEmailEntry";
	}
}
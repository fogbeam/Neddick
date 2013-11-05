package org.fogbeam.neddick

class WebpageEntry extends Entry
{
	
	String url;
	String pageContent;
	
	static constraints =
	{
		url( nullable:true, maxSize:2048 );
		
		// TODO: deal with content of arbitrary size, which we won't really
		// want to try to store directly and render like this.  A PDF or
		// DOC file or an XLS spreadsheet, for example.  
		pageContent(nullable:true, maxSize:300000);
	}
	
		
	public String getTemplateName()
	{
		return "/renderWebpageEntry";
	}
}

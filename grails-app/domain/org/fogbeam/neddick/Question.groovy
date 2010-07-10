package org.fogbeam.neddick;

import java.util.Date;

public class Question extends Entry {
	
	String questionHeadline;
	String questionDetails;
	String theUrl = "";
	
	public String getTitle()
	{
		return this.questionHeadline;
	}
	
}

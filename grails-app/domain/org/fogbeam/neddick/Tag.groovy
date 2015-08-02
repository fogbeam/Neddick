package org.fogbeam.neddick

import java.util.List;

class Tag implements Comparable
{

    static constraints = {
    
    	name( blank:false);
    }

    String name;
    
    static hasMany = [tagEntryLinks:TagEntryLink];
 
    public List getEntries() 	
	{
    	// TODO: fix this to reflect the fact that a given tag can be associated to a given entry
    	// more than once, where the discriminating factor is the user
    	return tagEntryLinks.collect{it.entry}
	}    
    
	List addToEntries(Entry entry, User user) 
	{ 
		TagEntryLink.link(this, entry, user );
		return entries;
	}

	List removeFromEntries(Entry entry, User user ) 
	{ 
		TagEntryLink.unlink(this, entry, user );
		return entries;
	}     
   
	@Override
	public int compareTo(Object o)
	{
		Tag otherTag = (Tag)o;
		return this.name.compareToIgnoreCase( otherTag.name );
	}
	
}


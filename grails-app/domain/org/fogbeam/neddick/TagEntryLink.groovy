package org.fogbeam.neddick;

/* association class for linking a tag to an entry and keeping attributes, such as the
 * date and who added the tag, etc.
 */
class TagEntryLink 
{
	// the two classes we are linking...
	Tag tag;
	Entry entry;
	
	// additional attributes about the link itself...
	User creator;
	Date dateCreated;

	/* static utility methods for managing the linking of tags and entries */
	 static TagEntryLink link(Tag tag, Entry entry, User user ) 
	{ 
		 
		def alink = TagEntryLink.executeQuery( "select tel from TagEntryLink as tel where tel.tag = ? and tel.entry = ? and tel.creator = ?", [tag, entry, user] );
		if ( !alink )
		{ 
			alink = new TagEntryLink();
			alink.creator = user;
			tag?.addToTagEntryLinks(alink);
			entry?.addToTagEntryLinks(alink);
			alink.save();
		} 
		
		return alink;
	}

	static void unlink(Tag tag, Entry entry, User user ) 
	{ 
		def alink = TagEntryLink.executeQuery( "select tel from TagEntryLink as tel where tel.tag = ? and tel.entry = ? and tel.creator = ?", [tag, entry, user] );
		if ( alink )
		{ 
			tag?.removeFromTagEntryLinks(alink);
			entry?.removeFromTagEntryLinks(alink);
			alink.delete();
		} 
	} 

}

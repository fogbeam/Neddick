package org.fogbeam.neddick

import org.fogbeam.neddick.Tag;
import org.fogbeam.neddick.TagEntryLink;
import org.fogbeam.neddick.User;

class TagService {

	public Tag findTagByName(final String name) 
	{
		Tag tag = Tag.findByName( name );
		return tag;
	}

	public Tag createTag( final String name )
	{
		// TODO: implement this...
		
	}
	
	public List<Tag> getTagListForUser( final User user )
	{
		List<Tag> tags = TagEntryLink.executeQuery( "select distinct tel.tag from TagEntryLink as tel where tel.creator = ?", [user] );
	
		println "tags: ${tags?.size()}";
		
		return tags;
	}

	public List<Tag> getAllTags()
	{
		List<Tag> allTags = new ArrayList<Tag>();
		allTags.addAll( Tag.findAll() );
		
		allTags.sort();
		
		return allTags;
	}
}

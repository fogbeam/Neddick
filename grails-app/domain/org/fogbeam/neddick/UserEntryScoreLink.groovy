package org.fogbeam.neddick


/* 
 * association class for linking a User to an Entry and keeping personalized scoring info.
 */

class UserEntryScoreLink {

	// the two classes we are linking...
	User user;
	Entry entry;
	
	// additional attributes about the link itself...
	double entryBaseScore;
	double entryHotness;
	double entryControversy;
	
}



/* static utility methods for managing the linking of users and entries */
/*
  static UserEntryScoreLink link( Entry entry, User user )
{
	
	def alink = UserEntryScoreLink.executeQuery( "select tel from TagEntryLink as tel where tel.tag = ? and tel.entry = ? and tel.creator = ?", [entry, user] );
	if ( !alink )
	{
		alink = new UserEntryScoreLink();
		user?.addToUserEntryScoreLinks(alink);
		entry?.addToUserEntryScoreLinks(alink);
		alink.save();
	}
   
	return alink;
}

static void unlink(Entry entry, User user )
{
	def alink = UserEntryScoreLink.executeQuery( "select tel from TagEntryLink as tel where tel.tag = ? and tel.entry = ? and tel.creator = ?", [entry, user] );
	if ( alink )
	{
		user?.removeFromUserEntryScoreLinks(alink);
		entry?.removeFromUserEntryScoreLinks(alink);
		alink.delete();
	}
}
*/

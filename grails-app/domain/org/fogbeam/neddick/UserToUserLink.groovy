package org.fogbeam.neddick

import java.util.Date

import org.apache.commons.logging.LogFactory

class UserToUserLink 
{
	
//	private static final log = LogFactory.getLog(this);
	 
	// the two classes we are linking...
	User owner;
	User target;
	
	// additional attributes about the link itself...
	int boost;
	Date dateCreated;

	static UserToUserLink link(User parent, User child, int boost ) 
	{ 
		
		List<UserToUserLink> links = UserToUserLink.executeQuery( "select link from UserToUserLink as link where link.owner = ? and link.target = ?", [parent, child] );
		UserToUserLink aLink = null;
		if ( !links )
		{ 
			aLink = new UserToUserLink();
			aLink.boost = boost;
			parent?.addToChildUserLinks(aLink);
			child?.addToParentUserLinks(aLink);
			if( !aLink.save(flush:true) )
			{
				aLink.errors.allErrors.each { log.error( it.toString() ) }
			}
		}
		else
		{
			if( links.size() > 1 ) 
			{
				// danger will robinson, danger...
				throw new RuntimeException( "too many entries in resultset" );
			}
			
			aLink = links.get(0);
			// log.debug( "found existing link, current boost is ${aLink.boost},  setting boost to ${boost}" );
			aLink.boost = boost;
			
			if( !aLink.save(flush:true) )
			{
				aLink.errors.allErrors.each { log.error( it.toString() ) }
			}
		}
		
		return aLink;
	}

	static void unlink(User parent, User child ) 
	{ 
	
		def alink = UserToUserLink.executeQuery( "select link from UserToUserLink as link where link.owner = ? and link.target = ?", [parent, child] );
		if ( alink )
		{ 
			parent?.removeFromChildUserLinks(alink);
			child?.removeFromParentUserLinks(alink);
			alink.delete(flush:true);
		} 
	} 
	
	
}

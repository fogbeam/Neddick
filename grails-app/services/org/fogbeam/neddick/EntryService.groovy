package org.fogbeam.neddick

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.springframework.transaction.annotation.*

class EntryService {
	
	Log cacheLog = LogFactory.getLog( "logger.special.instrumentation.cache" );
	def sessionFactory;
	
	private static final long BEGINNING_OF_TIME = 1230786000000; // 01 01 2009 00:00:00
	
	@Transactional(propagation = Propagation.REQUIRED)
	public Entry findById( final String id )
	{
		Entry entry = Entry.findById( id );
		return entry;		
	}
	
	@Transactional(propagation = Propagation.REQUIRED)
	public Entry findByUuid( final String uuid )
	{
		Entry entry = Entry.findByUuid( uuid );
		return entry;
	}

	
	
	@Transactional(propagation = Propagation.REQUIRED)
	public List<Entry> findByUrl( final String url )
	{
		// check if we already have an Entry for this same link
		List<Entry> entries = Entry.executeQuery( "select entry from Entry as entry where entry.url = ?", [url] );
	
		return entries;
	}

		
	@Transactional(propagation = Propagation.REQUIRED)
	public List<Entry> findByUrlAndChannel( final String url, final Channel channel )
	{
		// check if this channel already has an Entry for this same link
		List<Entry> entries = Entry.executeQuery( "select entry from Entry as entry, ChannelEntryLink as clink where entry.url = ? and clink.entry = entry and clink.channel = ?", [url, channel] );
	
		return entries;	
	}
	
	
	@Transactional(propagation = Propagation.REQUIRED)
	public Entry findByUuidAndChannel( final String entryUuid, final Channel channel )
	{
		// check if this channel already has an Entry for this same link
		List<Entry> entries = Entry.executeQuery( "select entry from Entry as entry, ChannelEntryLink as clink where entry.uuid = ? and clink.entry = entry and clink.channel = ?", [entryUuid, channel] );
		if( entries != null && entries.size() == 1 )
		{
			return entries[0];
		}
		else
		{
			return null;
		}
	}

	
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public boolean saveEntry( final Entry entry )
	{
		boolean success = false;
		
		if( ! ( success = entry.save(validate:false,flush:true)) )
		{
			log.error( "Updating entry: ${entry.id} FAILED");
			// entry.errors.allErrors.each { p rintln it };
		}
		
		return success;
	}
	
	// give me everything for this channel, as long this user has not asked to hide it.
	@Transactional(propagation = Propagation.REQUIRED)
	public List<Entry> getAllNonHiddenEntriesForUser( final User user ) 
	{
		List<Entry> entries = new ArrayList<Entry>();
		log.debug( "called getAllNonHiddenEntriesForUser( final User user )");
		List<Object> temp = Entry.executeQuery( "select entry, link from Entry as entry, User as user, UserEntryScoreLink as link where user.userId = ? and entry not in elements(user.hiddenEntries) and link.entry = entry and link.user = user order by entry.dateCreated desc", [user.userId] );
		for( Object o : temp )
		{
			// object array with Entry and Link
			Entry e = o[0];
			UserEntryScoreLink link = o[1];
			e.link = link;
			entries.add( e );
		}
		
		return entries;
	}
	
	
	@Transactional(propagation = Propagation.REQUIRED)
	public long getCountNonHiddenEntriesForUser( final Channel channel, final User user )
	{
		long numEntries = 0l;
		
		Object[] o = Entry.executeQuery( "select count(entry) from Entry as entry, User as user, ChannelEntryLink as clink " 
										 + " where user.userId = ? and clink.entry = entry and clink.channel = ?  and entry not in elements(user.hiddenEntries)", [user.userId, channel] );
		numEntries = ((Long)o[0]).longValue();
		
		return numEntries;	
	}
	
	@Transactional(propagation = Propagation.REQUIRED)
	public List<Entry> getAllNonHiddenEntriesForUser( final Channel channel, final User user ) 
	{
		List<Entry> entries = new ArrayList<Entry>();
		log.debug( "called getAllNonHiddenEntriesForUser( final Channel channel, final User user )");
		List<Object> temp = Entry.executeQuery( "select entry, link from Entry as entry, User as user, UserEntryScoreLink as link, " 
												+ " ChannelEntryLink as clink where user.userId = ? and clink.entry = entry and clink.channel = ? " 
												+ " and entry not in elements(user.hiddenEntries) and link.entry = entry and link.user = user " 
												+ " order by entry.dateCreated desc", [user.userId, channel] );
											
		for( Object o : temp )
		{
			// object array with Entry and Link
			Entry e = o[0];
			UserEntryScoreLink link = o[1];
			e.link = link;
			entries.add( e );
		}
		return entries;
	}

	// give me everything for this channel, as long this user has not asked to hide it.
	@Transactional(propagation = Propagation.REQUIRED)
	public List<Entry> getAllNonHiddenEntriesForUser( final User user, final int maxResults, final int offset )
	{
		List<Entry> entries = new ArrayList<Entry>();
		log.debug( "called getAllNonHiddenEntriesForUser( final User user, final int maxResults, final int offset )");
		List<Object> temp = Entry.executeQuery( "select entry, link from Entry as entry, User as user, UserEntryScoreLink as link where user.userId = ? and entry not in elements(user.hiddenEntries)  and link.entry = entry and link.user = user order by entry.dateCreated desc", [user.userId], [max:maxResults, offset:offset]);
		for( Object o : temp )
		{
			// object array with Entry and Link
			Entry e = o[0];
			UserEntryScoreLink link = o[1];
			e.link = link;
			entries.add( e );
		}
		return entries;
	}
	
	@Transactional(propagation = Propagation.REQUIRED)
	public List<Entry> getAllNonHiddenEntriesForUser( final Channel channel, final User user, final int maxResults, final int offset )
	{
		List<Entry> entries = new ArrayList<Entry>();
		log.debug( "called getAllNonHiddenEntriesForUser( final Channel channel, final User user, final int maxResults )");
		List<Object> temp = Entry.executeQuery( "select entry, link from Entry as entry, User as user, UserEntryScoreLink as link, " 
			+ "ChannelEntryLink as clink where user.userId = ? and clink.entry = entry and clink.channel = ? " 
			+ " and entry not in elements(user.hiddenEntries)  and link.entry = entry and link.user = user " 
			+ " order by entry.dateCreated desc", [user.userId, channel], [max:maxResults, offset:offset] );
		
		for( Object o : temp )
		{
			// object array with Entry and Link
			Entry e = o[0];
			UserEntryScoreLink link = o[1];
			e.link = link;
			entries.add( e );
		}
		
		return entries;
	}
	
	@Transactional(propagation = Propagation.REQUIRED)
	public List<Entry> getAllEntries()
	{
		List<Entry> entries = new ArrayList<Entry>();
		log.debug( "called getAllEntries()" );
		entries.addAll( Entry.executeQuery( "select entry from Entry as entry order by entry.dateCreated desc") );
		return entries;		
	}	
	
	@Transactional(propagation = Propagation.REQUIRED)
	public List<Entry> getAllEntries(final Channel channel)
	{
		List<Entry> entries = new ArrayList<Entry>();
		log.debug( "called getAllEntries(final Channel channel)");
		entries.addAll( Entry.executeQuery( "select entry from Entry as entry, ChannelEntryLink as clink where clink.entry = entry and clink.channel = ? " 
											+ "  order by entry.dateCreated desc", [channel] ) );
										
		return entries;		
	}

	@Transactional(propagation = Propagation.REQUIRED)
	public List<Entry> getAllEntries( final User user )
	{
		List<Entry> entries = new ArrayList<Entry>();
		log.debug( "called getAllEntries( final User user )" );
		List<Object> temp = Entry.executeQuery( "select entry, link from Entry as entry, UserEntryScoreLink as link where entry.submitter = ?  and link.entry = entry and link.user = ? order by entry.dateCreated desc", [user, user] );
		for( Object o : temp )
		{
			// object array with Entry and Link
			Entry e = o[0];
			UserEntryScoreLink link = o[1];
			e.link = link;
			entries.add( e );
		}
		return entries;
	}

	@Transactional(propagation = Propagation.REQUIRED)
	public long getCountAllEntries( final Channel channel ) 
	{
		long numEntries = 0l;
		
		Object o = Entry.executeQuery( "select count(entry) from Entry as entry, ChannelEntryLink as clink where clink.entry = entry and clink.channel = ?", [channel] );
		numEntries = ((Long)o[0]).longValue();
		return numEntries;
	}
	
	@Transactional(propagation = Propagation.REQUIRED)
	public List<Entry> getAllEntries( final int maxResults, final int offset )
	{
		List<Entry> entries = new ArrayList<Entry>();
		log.debug( "called getAllEntries( final int maxResults )" );
		entries.addAll( Entry.executeQuery( "select entry from Entry as entry order by entry.dateCreated desc", [], [max:maxResults, offset:offset]) );
		return entries;
	}
	
	@Transactional(propagation = Propagation.REQUIRED)
	public List<Entry> getAllEntries(final Channel channel, final int maxResults, final int offset )
	{
		List<Entry> entries = new ArrayList<Entry>();
		log.debug( "called getAllEntries(final Channel channel, final int maxResults )" );
		entries.addAll( Entry.executeQuery( "select entry from Entry as entry, ChannelEntryLink as clink where clink.entry = entry and clink.channel = ? " 
											+ " order by entry.dateCreated desc", [channel], [max:maxResults, offset:offset] ) );
		return entries;
	}

	@Transactional(propagation = Propagation.REQUIRED)
	public List<Entry> getAllEntries( final User user, final int maxResults, final int offset )
	{
		List<Entry> entries = new ArrayList<Entry>();
		log.debug( "called getAllEntries( final User user, final int maxResults )" );
		List<Object> temp = Entry.executeQuery( "select entry, link from Entry as entry, UserEntryScoreLink as link where entry.submitter = ? and link.entry = entry and link.user = ? order by entry.dateCreated desc", [user, user], [max:maxResults, offset:offset] );
		for( Object o : temp )
		{
			// object array with Entry and Link
			Entry e = o[0];
			UserEntryScoreLink link = o[1];
			e.link = link;
			entries.add( e );
		}
		return entries;
	}

	
	/* get hot entries */
	@Transactional(propagation = Propagation.REQUIRED)
	public List<Entry> getHotEntriesForUser( final Channel channel, final User user ) 
	{
		List<Entry> entries = new ArrayList<Entry>();
		log.debug( "called getHotEntriesForUser( final Channel channel, final User user )" );
		List<Object> temp =  Entry.executeQuery( "select entry, link from Entry as entry, User as user, UserEntryScoreLink as link, ChannelEntryLink as clink where user.userId = ? " 
												 + " and clink.entry = entry and clink.channel = ? and entry not in elements(user.hiddenEntries) and link.entry = entry " 
												 + " and link.user = user order by link.entryHotness desc", [user.userId, channel] );
		for( Object o : temp )
		{
			// object array with Entry and Link
			Entry e = o[0];
			UserEntryScoreLink link = o[1];
			e.link = link;
			entries.add( e );
		}
		return entries;
	}
	
	@Transactional(propagation = Propagation.REQUIRED)
	public List<Entry> getHotEntries(final Channel channel)
	{
		List<Entry> entries = new ArrayList<Entry>();
		log.debug( "getHotEntries(final Channel channel)" );
		entries.addAll( Entry.executeQuery( "select entry from Entry as entry, UserEntryScoreLink as link, ChannelEntryLink as clink where clink.entry = entry and clink.channel = ? and link.entry = entry order by link.entryHotness desc", [channel] ) );
		
		return entries;		
	}

	@Transactional(propagation = Propagation.REQUIRED)
	public List<Entry> getHotEntriesForUser( final Channel channel, final User user, final int maxResults, final int offset )
	{
		List<Entry> entries = new ArrayList<Entry>();
		log.debug( "called getHotEntriesForUser( final Channel channel, final User user )" );
		List<Object> temp = Entry.executeQuery( "select entry, link from Entry as entry, User as user, UserEntryScoreLink as link, ChannelEntryLink as clink where user.userId = ? " 
												+ " and clink.entry = entry and clink.channel = ? and entry not in elements(user.hiddenEntries) and link.entry = entry " 
												+ " and link.user = user order by link.entryHotness desc", [user.userId, channel], [max:maxResults, offset:offset] );
		for( Object o : temp )
		{
			// object array with Entry and Link
			Entry e = o[0];
			UserEntryScoreLink link = o[1];
			e.link = link;
			entries.add( e );
		}
		return entries;
	}
	
	@Transactional(propagation = Propagation.REQUIRED)
	public List<Entry> getHotEntries(final Channel channel, final int maxResults, final int offset )
	{
		List<Entry> entries = new ArrayList<Entry>();
		log.debug( "getHotEntries(final Channel channel)" );
		entries.addAll( Entry.executeQuery( "select entry from Entry as entry, UserEntryScoreLink as link, ChannelEntryLink as clink where clink.entry = entry and clink.channel = ? and link.entry = entry order by link.entryHotness desc", [channel], [max:maxResults, offset:offset] ) );
		
		return entries;
	}
	
	/* get new entries */
	@Transactional(propagation = Propagation.REQUIRED)
	public List<Entry> getNewEntriesForUser(final Channel channel, final User user ) 
	{
		List<Entry> entries = new ArrayList<Entry>();
		log.debug( "getNewEntriesForUser(final Channel channel, final User user )" );
		List<Object> temp =  Entry.executeQuery( "select entry, link from Entry as entry, User as user, UserEntryScoreLink as link, ChannelEntryLink as clink " 
												 + " where user.userId = ? and clink.entry = entry and clink.channel = ? " 
												 + " and entry not in elements(user.hiddenEntries)  and link.entry = entry and link.user = user " 
												 + " order by entry.dateCreated desc", [user.userId, channel] );
											 
		for( Object o : temp )
		{
			// object array with Entry and Link
			Entry e = o[0];
			UserEntryScoreLink link = o[1];
			e.link = link;
			entries.add( e );
		}
		return entries;
	}
	
	@Transactional(propagation = Propagation.REQUIRED)
	public List<Entry> getNewEntries(final Channel channel)
	{
		List<Entry> entries = new ArrayList<Entry>();
		log.debug( "getNewEntries(final Channel channel)" );
		entries.addAll( Entry.executeQuery( "select entry from Entry as entry, ChannelEntryLink as clink where clink.entry = entry and clink.channel = ? order by entry.dateCreated desc", [channel] ) );
		
		return entries;		
	}

	@Transactional(propagation = Propagation.REQUIRED)
	public List<Entry> getNewEntriesForUser(final Channel channel, final User user, final int maxResults, final int offset )
	{
		List<Entry> entries = new ArrayList<Entry>();
		log.debug( "getNewEntriesForUser(final Channel channel, final User user )" );
		List<Object> temp = Entry.executeQuery( "select entry, link from Entry as entry, User as user, UserEntryScoreLink as link, ChannelEntryLink as clink " 
												+ " where user.userId = ? and clink.entry = entry and clink.channel = ? " 
												+ " and entry not in elements(user.hiddenEntries) and link.entry = entry " 
												+ " and link.user = user  order by entry.dateCreated desc", [user.userId, channel] );
											
		for( Object o : temp )
		{
			// object array with Entry and Link
			Entry e = o[0];
			UserEntryScoreLink link = o[1];
			e.link = link;
			entries.add( e );
		}
		return entries;
	}
	
	@Transactional(propagation = Propagation.REQUIRED)
	public List<Entry> getNewEntries(final Channel channel, final int maxResults, final int offset )
	{
		List<Entry> entries = new ArrayList<Entry>();
		log.debug( "getNewEntries(final Channel channel)" );
		entries.addAll( Entry.executeQuery( "select entry from Entry as entry, ChannelEntryLink as clink where clink.entry = entry and clink.channel = ? order by entry.dateCreated desc", [channel] ) );
		
		return entries;
	}

	
	/* get top entries */
	@Transactional(propagation = Propagation.REQUIRED)
	public List<Entry> getTopEntriesForUser(final Channel channel, final User user ) 
	{
		List<Entry> entries = new ArrayList<Entry>();
		log.debug("getTopEntriesForUser(final Channel channel, final User user )");
		List<Object> temp = Entry.executeQuery( "select entry, link from Entry as entry, User as user, UserEntryScoreLink as link where user.userId = ? and entry.channel = ? and entry not in elements(user.hiddenEntries) and link.entry = entry and link.user = user order by link.entryBaseScore desc", [user.userId, channel] )
		for( Object o : temp ) 
		{
			// object array with Entry and Link
			Entry e = o[0];
			UserEntryScoreLink link = o[1];
			e.link = link;	
			entries.add( e );
		}
	
		return entries;
	}
	
	// note: get "top" with no user specified basically means that the user defaults to the "anonymous" user.  This way we can still use the
	// UEL table normally... same for hotness and controversy...
	@Transactional(propagation = Propagation.REQUIRED)
	public List<Entry> getTopEntries(final Channel channel)
	{
		List<Entry> entries = new ArrayList<Entry>();
		log.debug( "called getTopEntries(final Channel channel)");
		entries.addAll( Entry.executeQuery( "select entry from Entry as entry, ChannelEntryLink as clink where clink.entry = entry and clink.channel = ? order by entry.dateCreated desc", [channel] ) );
		
		return entries;		
	}

	@Transactional(propagation = Propagation.REQUIRED)
	public List<Entry> getTopEntriesForUser(final Channel channel, final User user, final int maxResults, final int offset )
	{
		List<Entry> entries = new ArrayList<Entry>();
		log.debug( "getTopEntriesForUser(final Channel channel, final User user )");
		List<Object> temp = Entry.executeQuery( "select entry, link from Entry as entry, User as user, UserEntryScoreLink as link, ChannelEntryLink as clink " 
												+ " where user.userId = ? and clink.entry = entry and clink.channel = ? and entry not in elements(user.hiddenEntries) and link.entry = entry and link.user = user order by link.entryBaseScore desc", [user.userId, channel], [max:maxResults, offset:offset] ); 
		for( Object o : temp )
		{
			// object array with Entry and Link?
			Entry e = o[0];
			UserEntryScoreLink link = o[1];
			e.link = link;
			entries.add( e );
		}
		
	
		return entries;
	}
	
	@Transactional(propagation = Propagation.REQUIRED)
	public List<Entry> getTopEntries(final Channel channel, final int maxResults, final int offset )
	{
		List<Entry> entries = new ArrayList<Entry>();
		log.debug( "called getTopEntries(final Channel channel)");
		entries.addAll( Entry.executeQuery( "select entry from Entry as entry, ChannelEntryLink as clink where clink.entry = entry and clink.channel = ? order by entry.dateCreated desc", [channel], [max:maxResults, offset:offset] ) );
		
		return entries;
	}

	
		
	/* get controversial entries */
	@Transactional(propagation = Propagation.REQUIRED)
	public List<Entry> getControversialEntriesForUser( final Channel channel, final User user ) 
	{
		List<Entry> entries = new ArrayList<Entry>();
		log.debug( "called getControversialEntriesForUser( final Channel channel, final User user )" );
		List<Object> temp = Entry.executeQuery( "select entry, link from Entry as entry, User as user, UserEntryScoreLink as link, ChannelEntryLink as clink " 
												+ " where user.userId = ? and clink.entry = entry and clink.channel = ? and entry not in elements(user.hiddenEntries) and link.entry = entry and link.user = user order by link.entryControversy desc", [user.userId, channel] );
		for( Object o : temp )
		{
			// object array with Entry and Link
			Entry e = o[0];
			UserEntryScoreLink link = o[1];
			e.link = link;
			entries.add( e );
		}
		
		return entries;
	}
	
	@Transactional(propagation = Propagation.REQUIRED)
	public List<Entry> getControversialEntries(final Channel channel)
	{
		List<Entry> entries = new ArrayList<Entry>();
		log.debug( "called getControversialEntries(final Channel channel)" );
		entries.addAll( Entry.executeQuery( "select entry from Entry as entry, UserEntryScoreLink as link, ChannelEntryLink as clink " 
											+ " where clink.entry = entry and clink.channel = ? and link.entry = entry order by link.entryControversy desc", [channel] )  );
		
		return entries;		
	}
	
	@Transactional(propagation = Propagation.REQUIRED)
	public List<Entry> getControversialEntriesForUser( final Channel channel, final User user, final int maxResults, final int offset )
	{
		List<Entry> entries = new ArrayList<Entry>();
		log.debug( "called getControversialEntriesForUser( final Channel channel, final User user )" );
		List<Object> temp = Entry.executeQuery( "select entry, link from Entry as entry, User as user, UserEntryScoreLink as link, ChannelEntryLink as clink " 
												+ " where user.userId = ? and clink.entry = entry and clink.channel = ? and entry not in elements(user.hiddenEntries) and link.entry = entry and link.user = user order by link.entryControversy desc", [user.userId, channel], [max:maxResults, offset:offset] );
		for( Object o : temp )
		{
			// object array with Entry and Link
			Entry e = o[0];
			UserEntryScoreLink link = o[1];
			e.link = link;
			entries.add( e );
		}
				
		return entries;
	}
	
	@Transactional(propagation = Propagation.REQUIRED)
	public List<Entry> getControversialEntries(final Channel channel, final int maxResults, final int offset )
	{
		List<Entry> entries = new ArrayList<Entry>();
		log.debug( "called getControversialEntries(final Channel channel)" );
		entries.addAll( Entry.executeQuery( "select entry from Entry as entry, UserEntryScoreLink as link, ChannelEntryLink as clink " 
											+ " where clink.entry = entry and clink.channel = ? and link.entry = entry order by link.entryControversy desc", [channel], [max:maxResults, offset:offset] ) );
		
		return entries;
	}

		
	/* get saved entries */
	@Transactional(propagation = Propagation.REQUIRED)
	public List<Entry> getSavedEntriesForUser( final User user ) 
	{
		log.debug("called getSavedEntriesForUser( final User user )" );
		List<Entry> entries = new ArrayList<Entry>();
		def theUser = User.findByUserId( user.userId );
		log.debug( "found user: ${theUser}" );
		// def tempEntries = theUser.savedEntries;

		List<Object> temp = Entry.executeQuery( "select  entry, link from User as user inner join user.savedEntries as entry, UserEntryScoreLink as link where user = ? and entry = link.entry and link.user = ?", [user, user] );
		for( Object o : temp )
		{
			// object array with Entry and Link
			Entry e = o[0];
			UserEntryScoreLink link = o[1];
			e.link = link;
			entries.add( e );
		}
				
		return entries;
	}	
	
	
	/* get hidden entries */
	@Transactional(propagation = Propagation.REQUIRED)
	public List<Entry> getHiddenEntriesForUser( final User user ) 
	{
		List<Entry> entries = new ArrayList<Entry>();
		log.debug( "called getHiddenEntriesForUser( final User user )" );
		def theUser = User.findByUserId( user.userId );
        def tempEntries = theUser.hiddenEntries;
		entries.addAll( tempEntries );
		
		return entries;
		
	}

	@Transactional(propagation = Propagation.REQUIRED)
	public List<Entry> getCommentsForUser( final User user )
	{
		List<Comment> comments = new ArrayList<Comment>();
		comments.addAll( Comment.executeQuery( "select comment from Comment as comment where comment.creator = ?", [user] ) );
		
		return comments;
	}

	public List<Entry> findEntriesByChannelAndScoreThreshold( final Channel channel, final int scoreThreshold )
	{
		List<Entry> entries = new ArrayList<Entry>();
		
		List<Entry> temp =  
			Entry.executeQuery( "select entry from Entry as entry,  ChannelEntryLink as clink "
				+ " where clink.entry = entry and clink.channel = :channel "
				+ " and entry.score > :scoreThreshold "
				+ " order by entry.dateCreated desc", [channel:channel, scoreThreshold:scoreThreshold] );

		if( temp != null )
		{
			entries.addAll( temp );
		}
			
		return entries;	
			
	}
	
		
}
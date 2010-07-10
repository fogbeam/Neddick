package org.fogbeam.neddick

import java.util.Comparator;
import java.util.Map;

import org.fogbeam.neddick.Entry;
import org.fogbeam.neddick.User;

class EntryCacheService 
{
	def entryService;

	EntryCacheObject defaultCache = null;
	Map<User, EntryCacheObject> userCaches = new HashMap<User, EntryCacheObject>();
	
	public synchronized void buildCache( final User user = null )
	{
	
		println "building cache for user: ${user}";
		
		List<Entry> allEntries = null;
		if( user == null )
		{
			allEntries = entryService.getAllEntries();
		}
		else
		{
			allEntries = entryService.getAllNonHiddenEntriesForUser( user );
		}
		
		// make sure allEntries is sorted by createDate. if the entryService doesn't return it to use that way,
		// we'll have to do a sort here.
		EntryCacheObject currentCache = null;	
		if( user == null )
		{
			defaultCache = new EntryCacheObject();
			currentCache = defaultCache;
		}
		else
		{
			EntryCacheObject userCache = new EntryCacheObject();
			println "putting cache for user: ${user}, this = ${this}";
			userCaches.put( user, userCache );
			currentCache = userCache;
		}		
		
		long now = System.currentTimeMillis();
		// build cache 
		for( Entry entry in allEntries )
		{
			entry.hotness = entryService.calculateHotness( entry, now );
			entry.controversy = entryService.calculateControversy( entry, now );
			
			println "adding entry ${entry.uuid} to defaultCache";
			currentCache.addEntry( entry );
		}
	}

	public EntryCacheObject getEntryCache( final User user = null )
	{
		if( user == null )
		{
			return defaultCache;
		}
		else
		{
			println "Getting cache for user ${user}, this = ${this}";
			println userCaches;
			println "Keys: ${userCaches.keySet()}";
			println "Values: ${userCaches.values()}";
			
			Set keys = userCaches.keySet();
			if ( user == keys.toArray()[0] )
			{
				println "true";	
			}
			else
			{
				println "false";	
			}
			
			EntryCacheObject cacheObject = userCaches.get( user );
			println "cacheObject returned: ${cacheObject}";
			return cacheObject;
		}
	}

	public void removeCache( final User user )
	{
		userCaches.remove( user );
	}
	
	public void addEntry( final Entry entry )
	{
		defaultCache.addEntry( entry );
		Set<User> keys = userCaches.keySet();
		for( User user : keys )
		{
			EntryCacheObject cache = userCaches.get( user );
			if( cache != null )
			{
				cache.addEntry( entry );
			}
		}
	}
}

class EntryCacheObject
{
	Map<String, Entry> cache = new HashMap<String, Entry>();

	Comparator byCreatedDateComparator = null;
	Comparator byScoreComparator = null;
	Comparator byHotnessComparator = null;
	Comparator byControversyComparator = null;

	SortedSet<String> byCreatedDate = null;
	SortedSet<String> byScore = null;
	SortedSet<String> byHotness = null;
	SortedSet<String> byControversiality = null;	
	
	public EntryCacheObject()
	{
		byCreatedDateComparator = new ByCreatedDateComparator( cache );
		byScoreComparator = new ByScoreComparator( cache );
		byHotnessComparator = new ByHotnessComparator( cache );
		byControversyComparator = new ByControversyComparator( cache );	
	
		byCreatedDate = new TreeSet<String>( byCreatedDateComparator );
		byScore = new TreeSet<String>( byScoreComparator );
		byHotness = new TreeSet<String>( byHotnessComparator );
		byControversiality = new TreeSet<String>( byControversyComparator );		
	}
	
	public void addEntry( final Entry entry )
	{
		cache.put( entry.uuid, entry );
		
		byCreatedDate.add( entry.uuid );
		byScore.add( entry.uuid );
		byHotness.add( entry.uuid );
		byControversiality.add( entry.uuid );		
	}

	public SortedSet getByCreatedDate()
	{
		return byCreatedDate;
	}
	
	public SortedSet getByScore()
	{
		return byScore;
	}

	public SortedSet getByControversy()
	{
		return byControversiality;
	}

	public SortedSet getByHotness()
	{
		return byHotness;
	}

	public Entry getEntry( final String entryUuid )
	{
		return cache.get( entryUuid );
	}

	public int size()
	{
		return cache.size();
	}
}

class ByCreatedDateComparator implements Comparator
{
	Map sourceMap;
	
	public ByCreatedDateComparator( Map sourceMap )
	{
		this.sourceMap = sourceMap;
	}
	
	int compare( Object arg0,  Object arg1)
	{
		// println "Comparing: ${arg0} with ${arg1}";
		Entry value1 = sourceMap.get( arg0 );
		Entry value2 = sourceMap.get( arg1 );
	
		if( value1.dateCreated.time > value2.dateCreated.time )
		{
			// println "returning 1";
			return -1;
		}
		else if( value1.dateCreated.time == value2.dateCreated.time )
		{
			// println "returning cheat -1";
			return 1;
		}
		else if( value1.dateCreated.time < value2.dateCreated.time )
		{
			// println "returning -1";
			return 1;
		}		
	}
}

class ByScoreComparator implements Comparator
{
	Map sourceMap;
	
	public ByScoreComparator( Map sourceMap )
	{
		this.sourceMap = sourceMap;
	}
	
	int compare( Object arg0,  Object arg1)
	{
		// println "Comparing: ${arg0} with ${arg1}";
		Entry value1 = sourceMap.get( arg0 );
		Entry value2 = sourceMap.get( arg1 );
	
		if( value1.score > value2.score )
		{
			// println "returning 1";
			return -1;
		}
		else if( value1.score == value2.score )
		{
			// println "returning cheat -1";
			return 1;
		}
		else if( value1.score < value2.score )
		{
			// println "returning -1";
			return 1;
		}		
	}
}

class ByHotnessComparator implements Comparator
{
	Map sourceMap;
	
	public ByHotnessComparator( Map sourceMap )
	{
		this.sourceMap = sourceMap;
	}
	
	int compare( Object arg0,  Object arg1)
	{
		// println "Comparing: ${arg0} with ${arg1}";
		Entry value1 = sourceMap.get( arg0 );
		Entry value2 = sourceMap.get( arg1 );
		
		if( value1.hotness > value2.hotness )
		{
			// println "returning 1";
			return -1;
		}
		else if( value1.hotness == value2.hotness )
		{
			// println "returning cheat -1";
			return 1;
		}
		else if( value1.hotness < value2.hotness )
		{
			// println "returning -1";
			return 1;
		}	
	}
}

class ByControversyComparator implements Comparator
{
	Map sourceMap;
	
	public ByControversyComparator( Map sourceMap )
	{
		this.sourceMap = sourceMap;
	}	
	
	int compare( Object arg0,  Object arg1) 
	{
		// println "Comparing: ${arg0} with ${arg1}";
		Entry value1 = sourceMap.get( arg0 );
		Entry value2 = sourceMap.get( arg1 );
		
		if( value1.controversy > value2.controversy )
		{
			// println "returning 1";
			return -1;
		}
		else if( value1.controversy == value2.controversy )
		{
			// println "returning cheat --1";
			return 1;
		}
		else if( value1.controversy < value2.controversy )
		{
			// println "returning -1";
			return 1;
		}					
		
	}
}
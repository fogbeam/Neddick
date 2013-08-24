package org.fogbeam.neddick

import org.fogbeam.neddick.filters.BaseFilter
import org.fogbeam.neddick.filters.criteria.AboveScoreFilterCriteria
import org.fogbeam.neddick.filters.criteria.BaseFilterCriteria
import org.fogbeam.neddick.filters.criteria.BodyKeywordFilterCriteria
import org.fogbeam.neddick.filters.criteria.TagFilterCriteria
import org.fogbeam.neddick.filters.criteria.TitleKeywordFilterCriteria

class FilterService
{
	def searchService;
	def entryService;
	def tagService;
		
	public BaseFilter findFilterById( final Long id )
	{
		
		BaseFilter theFilter = BaseFilter.findById( id );
		return theFilter;
	}
	

	public BaseFilter findFilterByUuid( final String uuid )
	{
		
		BaseFilter theFilter = BaseFilter.findByUuid( uuid );
		return theFilter;
	}
	
	
	public List<BaseFilter> getFiltersForUser( final User user )
	{
		List<BaseFilter> filters = new ArrayList<BaseFilter>();
		
		List<BaseFilter> queryResults = BaseFilter.executeQuery( "select filter from BaseFilter as filter where filter.owner = :owner", [owner:user] );
		if( queryResults != null && queryResults.size() > 0 )
		{
			filters.addAll( queryResults );
		}
		
		return filters;	
	}
	
	
	public BaseFilter saveFilter( final BaseFilter newFilter )
	{
		if( newFilter.save(flush:true) )
		{
			
			def newFilterMessage = [msgType:'NEW_FILTER_CREATED', filterUuid:newFilter.uuid];
			
			sendJMSMessage( "neddickFilterQueue", newFilterMessage );
			
			
			return newFilter;
		}
		else 
		{
			newFilter.errors.allErrors.each { println it };
			return null;
		}
	}
	
	public void deleteFilterById( final long id)
	{
		BaseFilter filterToDelete = BaseFilter.findById( id );
		filterToDelete.delete();
	}

	public BaseFilter updateFilter( final BaseFilter filterToEdit, final BaseFilterCriteria newCriteria )
	{
		
		BaseFilterCriteria criteriaToRemove = filterToEdit.theOneCriteria;
		filterToEdit.removeFromFilterCriteria( criteriaToRemove );
		criteriaToRemove.delete();
		
		filterToEdit.addToFilterCriteria( newCriteria );
		
		if( !filterToEdit.save())
		{
			filterToEdit.errors.allErrors.each { println it; }
			throw new RuntimeException( "Failed to update filter!");
		}
		
		
		return filterToEdit;
	}

	
	public BaseFilter findFilterByUserAndChannel( final User user, final Channel channel )
	{
		BaseFilter filter = null;
		
		List<BaseFilter> queryResults = BaseFilter.
			executeQuery( "select filter from BaseFilter as filter where filter.owner = :user and filter.channel = :channel",
				[user:user, channel:channel] );
		
		
		if( queryResults != null && queryResults.size() == 1 )
		{
			filter = queryResults[0];
		}
		
		
		return filter;
	}
	
	
	public long getCountNonHiddenEntriesForFilter( final BaseFilter filter )
	{
		long entryCount = 0L;
		
		
		List<Long> queryResults = BaseFilter.
			executeQuery( "select size(filter.entries) from BaseFilter filter where filter = :filter", [filter:filter] );

		if( queryResults != null && queryResults.size() == 1 )
		{
			entryCount = queryResults[0];
		}	
					
		return entryCount;
	}
	
	
	public List<Entry> getAllNonHiddenEntriesForFilter(final BaseFilter filter, final int maxResults, final int offset )
	{
		println "getAllNonHiddenEntriesForFilter";
		
		List<Entry> entries = new ArrayList<Entry>();
		
		
		List<Object> queryResults = Entry.
			executeQuery( "select e, link from Entry as e, BaseFilter as f, User as u, UserEntryScoreLink as link where e in elements(f.entries) " + 
						  " and f = :filter and e not in elements(u.hiddenEntries) and u = :user "
						  + " and link.entry = e and link.user = u order by e.dateCreated desc", 
						  [filter:filter, user: filter.owner], 
						  [max:maxResults, offset:offset]);
		
		
		if( queryResults != null )
		{
			for( Object o : queryResults )
			{
				// object array with Entry and Link
				Entry e = o[0];
				UserEntryScoreLink link = o[1];
				e.link = link;
				entries.add( e );
			}
		}
		
		return entries;
	}
	
	public List<Entry> getHotNonHiddenEntriesForFilter( final BaseFilter filter, final int maxResults, final int offset )
	{
		println "getHotNonHiddenEntriesForFilter";
		
		List<Entry> entries = new ArrayList<Entry>();
		
		
		List<Object> queryResults = Entry.
			executeQuery( "select e, link from Entry as e, BaseFilter as f, User as u, UserEntryScoreLink as link where e in elements(f.entries) " + 
						  " and f = :filter and e not in elements(u.hiddenEntries) and u = :user "
						  + " and link.entry = e and link.user = u order by link.entryHotness desc", 
						  [filter:filter, user: filter.owner], 
						  [max:maxResults, offset:offset]);
		
		
		if( queryResults != null )
		{
			for( Object o : queryResults )
			{
				// object array with Entry and Link
				Entry e = o[0];
				UserEntryScoreLink link = o[1];
				
				println "adding UserEntryScoreLink: ${link}";
				
				e.link = link;
				entries.add( e );
			}
		}
		
		return entries;
	}
	
	public List<Entry> getTopNonHiddenEntriesForFilter( final BaseFilter filter, final int maxResults, final int offset )
	{
		
		println "getTopNonHiddenEntriesForFilter";
		
		List<Entry> entries = new ArrayList<Entry>();
		
		List<Object> queryResults = Entry.
			executeQuery( "select e, link from Entry as e, BaseFilter as f, User as u, UserEntryScoreLink as link where e in elements(f.entries) " + 
						  " and f = :filter and e not in elements(u.hiddenEntries) and u = :user "
						  + " and link.entry = e and link.user = u order by link.entryBaseScore desc", 
						  [filter:filter, user: filter.owner], 
						  [max:maxResults, offset:offset]);
		
		
		if( queryResults != null )
		{
			for( Object o : queryResults )
			{
				// object array with Entry and Link
				Entry e = o[0];
				UserEntryScoreLink link = o[1];
				
				println "adding UserEntryScoreLink: ${link}";
				
				e.link = link;
				entries.add( e );
			}
		}		
		return entries;
	}
	
	public List<Entry> getControversialNonHiddenEntriesForFilter( final BaseFilter filter, final int maxResults, final int offset )
	{
		
		println "getControversialNonHiddenEntriesForFilter";
		
		List<Entry> entries = new ArrayList<Entry>();
		
		List<Object> queryResults = Entry.
			executeQuery( "select e, link from Entry as e, BaseFilter as f, User as u, UserEntryScoreLink as link where e in elements(f.entries) " + 
						  " and f = :filter and e not in elements(u.hiddenEntries) and u = :user "
						  + " and link.entry = e and link.user = u order by link.entryControversy desc", 
						  [filter:filter, user: filter.owner], 
						  [max:maxResults, offset:offset]);
		
		
		if( queryResults != null )
		{
			for( Object o : queryResults )
			{
				// object array with Entry and Link
				Entry e = o[0];
				UserEntryScoreLink link = o[1];
				
				println "adding UserEntryScoreLink: ${link}";
				
				e.link = link;
				entries.add( e );
			}
		}
		
		return entries;
	}
	
		
	public void fireTagFilterCriteria( final String tagName, final String entryUuid  )
	{
		println "fireTagFilterCriteria";
		
		// lookup the Entry that was just tagged
		Entry entry = entryService.findByUuid( entryUuid );
				
		// find any tag criteria for this channel and this tagName
		List<BaseFilterCriteria> filterCriteria = this.findTagCriteriaByTagName( tagName );
		
		// for each specific criteria, add our entry to the associated filter
		for( BaseFilterCriteria criteria : filterCriteria )
		{
			// println "adding Entry to filter for channel: ${channel.name} and tag: ${tagName}";
			BaseFilter filter = criteria.filter;
			Channel targetChannel = filter.channel;
			
			// we have an entry that's just been tagged with a tag for which there
			// is a filter, but is it on the channel the filter is set for?
			List<Channel> entryChannels = entry.channels;
			
			for( Channel channel : entryChannels )
			{
				if( channel.id == targetChannel.id )
				{
					
					filter.addToEntries( entry );
					break;
				}
			}
		}
		
	}
	
	
	public void fireThresholdFilterCriteria( final String entryUuid, final String strNewScore )
	{
		println "fireThresholdFilterCriteria";
		println "processing score threshold filter criteria...";
		
			
		int newScore = Integer.parseInt( strNewScore);
		
		Entry theEntry = entryService.findByUuid( entryUuid );
		List<Channel> entryChannels = theEntry.channels;
	
		
		for( Channel channel : entryChannels )
		{
			// get any AboveScoreFilterCriteria for this channel
			List<AboveScoreFilterCriteria> triggerCriteria = this.findAboveScoreFilterCriteriaByChannel( channel );
			
			
			for( AboveScoreFilterCriteria criteria : triggerCriteria)
			{
				// for each AboveScoreFilterCriteria, test to see if it's Filter should file 
				println "found an instance of AboveScoreFilterCriteria";
				println "newScore: ${newScore}, threshold: ${criteria.aboveScoreThreshold}";
				
				
				/* TODO: this needs to take score personalization into account */
				
				// does this criteria fire?
				if( newScore  > criteria.aboveScoreThreshold )
				{
					// get the associated filter
					BaseFilter filter = criteria.filter;
					
					println "found a match, attaching Entry to Filter: ${filter.name}";
				
					filter.addToEntries( theEntry );	
				}
				else
				{
					println "No match, not firing actions...";
				}
				
			}
		}
				
		
	}
	
	
	
	List<BaseFilterCriteria> findTagCriteriaByTagName( final String tagName )
	{
		List<BaseFilterCriteria> criteria = new ArrayList<BaseFilterCriteria>();
		
		// TagTriggerCriteria.executeQuery( "select criteria from TagTriggerCriteria as criteria where criteria.tag = :tagName", [tagName:canonicalTagName] );

		List<BaseFilterCriteria> queryResults = TagFilterCriteria.executeQuery( 
				"select criteria from TagFilterCriteria as criteria where criteria.tag = :tagName", [tagName:tagName] );
		
		if( queryResults != null )
		{
			criteria.addAll( queryResults );
		}
		
		
		return criteria;
	}
	

	List<AboveScoreFilterCriteria> findAboveScoreFilterCriteriaByChannel( final Channel channel )
	{
		List<AboveScoreFilterCriteria> criteria = new ArrayList<>();
		
		
		List<AboveScoreFilterCriteria> queryResults = AboveScoreFilterCriteria.
			executeQuery( "select criteria from AboveScoreFilterCriteria as criteria where criteria.filter.channel = :channel",
				[channel:channel] );
		
		if( queryResults != null )
		{
			criteria.addAll( queryResults );
		}	
			
		
		return criteria;
	}
	
	
	
	public void fireContentFilterCriteria( final String entryUuid )
	{
		println "processing content triggers..."
	
		
		
		Entry theEntry = entryService.findByUuid( entryUuid );
		List<Channel> entryChannels = theEntry.channels;
	
		
		for( Channel channel : entryChannels )
		{		
			List<BodyKeywordFilterCriteria> bodyKeywordFilterCriteria = this.findBodyKeywordFilterCriteriaByChannel( channel );
		
		
			if( bodyKeywordFilterCriteria != null )
			{
				println "bodyKeywordFilterCriteria object is valid";
			
				for( BodyKeywordFilterCriteria criteria : bodyKeywordFilterCriteria )
				{
					
					String keyword = criteria.bodyKeyword;
				
					println "found a body filterCriteria with keyword: ${keyword}";
					
					List<Entry> searchResults = searchService.doSearch( "uuid: ${entryUuid} AND content: ${keyword}" );
					
					println "did search for entry_uuid: ${entryUuid}";
									
					// does this criteria fire?
					if( searchResults != null && !searchResults.isEmpty() )
					{
						// this entry matches, so add it to the filter
						
						// get the associated filter
						BaseFilter filter = criteria.filter;
						
						println "found a match, attaching Entry to Filter: ${filter.name}";
					
						filter.addToEntries( theEntry );
						
					}
							
				}
			}
			
			
			List<TitleKeywordFilterCriteria> titleKeywordFilterCriteria = this.findTitleKeywordFilterCriteriaByChannel( channel );
			if( titleKeywordFilterCriteria != null )
			{
				println "titleKeywordFilterCriteria object is valid";
			
				for( TitleKeywordFilterCriteria criteria : titleKeywordFilterCriteria )
				{
					
					String keyword = criteria.titleKeyword;
				
					println "found a title filterCriteria with keyword: ${keyword}";
					
					List<Entry> searchResults = searchService.doSearch( "uuid: ${entryUuid} AND title: ${keyword}" );
					
					println "did search for entry_uuid: ${entryUuid}";
									
					// does this criteria fire?
					if( searchResults != null && !searchResults.isEmpty() )
					{
						// this entry matches, so add it to the filter
						
						// get the associated filter
						BaseFilter filter = criteria.filter;
						
						println "found a match, attaching Entry to Filter: ${filter.name}";
					
						filter.addToEntries( theEntry );
						
					}
				}
			}

		}
		
		
	}
	

	private List<BodyKeywordFilterCriteria> findBodyKeywordFilterCriteriaByChannel( final Channel channel )
	{
		List<BodyKeywordFilterCriteria> filterCriteria  = new ArrayList<BodyKeywordFilterCriteria>();
		
		List<BodyKeywordFilterCriteria> queryResults = BodyKeywordFilterCriteria.
						executeQuery( "select criteria from BodyKeywordFilterCriteria as criteria where criteria.filter.channel = :channel",
						[channel:channel] );
	
		if( queryResults != null )
		{
			filterCriteria.addAll( queryResults );
		}
	
		return filterCriteria;
	}		

	
	private List<TitleKeywordFilterCriteria> findTitleKeywordFilterCriteriaByChannel( final Channel channel )
	{
		List<TitleKeywordFilterCriteria> filterCriteria  = new ArrayList<TitleKeywordFilterCriteria>();
		
		List<TitleKeywordFilterCriteria> queryResults = TitleKeywordFilterCriteria.
						executeQuery( "select criteria from TitleKeywordFilterCriteria as criteria where criteria.filter.channel = :channel",
						[channel:channel] );
	
		if( queryResults != null )
		{
			filterCriteria.addAll( queryResults );
		}
	
		return filterCriteria;
	}

	private void processExistingContentForFilter( final String filterUuid )
	{
		println "processing existing content for filter: ${filterUuid}";
		
		BaseFilter filter = this.findFilterByUuid( filterUuid );
		
		println "Found filter: ${filter.name}";
		
		// get the Channel this Filter is associated with, so we can grab all
		// the content for that Channel
		Channel channel = filter.channel;
		
		// get our Criteria from the Filter so we can look for content which
		// could match
		BaseFilterCriteria filterCriteria = filter.theOneCriteria;
		
		List<Entry> entriesToAdd = new ArrayList<Entry>();
		
		// TODO: convert to polymorphic function calls, something like
		// getEntriesByFilterCriteria( TitleKeywordFilterCriteria filterCriteria )
		// getEntriesByFilterCriteria( BodyKeywordFilterCriteria filterCriteria )
		// getEntriesByFilterCriteria( TagKeywordFilterCriteria filterCriteria )
		// etc...
		
		if( filterCriteria instanceof TitleKeywordFilterCriteria )
		{
			List<Entry> entries = searchService.doSearch( "+ channel_uuids: ${channel.uuid} and + title: ${filterCriteria.titleKeyword}" );
			
			if( entries != null )
			{
				entriesToAdd.addAll( entries );
			}
			
		}
		else if( filterCriteria instanceof BodyKeywordFilterCriteria )
		{
			List<Entry> entries = searchService.doSearch( "+ channel_uuids: ${channel.uuid} and + content: ${filterCriteria.bodyKeyword}" );
			
			if( entries != null )
			{
				entriesToAdd.addAll( entries );
			}			
			
			
		}
		else if( filterCriteria instanceof TagFilterCriteria )
		{
			String tagName = filterCriteria.tag;
			Tag tag = tagService.findTagByName(tagName);
			if( null != tag )
			{
				List<Entry> taggedEntries = tag.entries;
				if( taggedEntries != null )
				{
					entriesToAdd.addAll( taggedEntries );
				}
			}
		}
		else if( filterCriteria instanceof AboveScoreFilterCriteria )
		{
			List<Entry> entries = entryService.findEntriesByChannelAndScoreThreshold( channel, filterCriteria.aboveScoreThreshold );
			if( entries != null )
			{
				entriesToAdd.addAll( entries );
			}
		}
		
		for( Entry entryToAdd : entriesToAdd )
		{
			filter.addToEntries( entryToAdd );
		}
		
		
	}	
			
}

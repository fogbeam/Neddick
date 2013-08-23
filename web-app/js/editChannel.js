/* for RSS feeds */

function addToSelectedFeeds() {
	// alert( "addToSelected" );
	
	$j( 'select#availablefeeds :selected' ).each( function(i, selected )
		{
		
			// check if this feed is already in "feedsToRemove".  If it is, somebody
			// clicked "remove" then re-added it.  But since the net-net of that
			// is the same settings we currently have, we don't actually want to do
			// anything.
			var checkValue = $j(selected).attr('value');
			// alert( "checkValue: " + checkValue );
			var existingOption = $j("select#feedsToRemove option[value='" + checkValue + "']");
			// alert( "existing option length: " + existingOption.length );
			if( existingOption.length > 0 )
			{		
				// don't add to feedsToAdd,  but we do need to re-add it back 
				// to the "feeds" list, so it "looks" selected
				var newOption2 = $j(selected).clone();
				newOption2.appendTo( "select#feeds" );
			}
			else
			{
				// do everything as normal
				var newOption = $j(selected).clone();
				newOption.attr('selected', 'selected');
				newOption.appendTo( "select#feedsToAdd" );
				var newOption2 = $j(selected).clone();
				newOption2.appendTo( "select#feeds" );				
			
			}
			
			var removeMeId = $j(selected).attr('value');
			$j(selected).remove();
			
			// if this had previously been selected for removal (and then added
			// back) , then it's in feedsToRemove now.  We need to remove it from there, since
			// we've decided not to remove it.				
			var removeMe = $j("select#feedsToRemove option[value='" + removeMeId + "']");
			if( removeMe )
			{
				removeMe.remove();
			}
			
		}
	);
}

function addAllToSelectedFeeds() {
	// alert( "addAllToSelected" );
	
	$j( 'select#availablefeeds option' ).each( function(i, selected )
		{	

			// check if this feed is already in "feeds".  If it is, somebody
			// clicked "remove" then re-added it.  But since the net-net of that
			// is the same settings we currently have, we don't actually want to do
			// anything.
			var existingOption = $j("select#feedsToRemove option[value='" + $j(selected).attr('value') + "']");
			// alert( "existing option length: " + existingOption.length );
			if( existingOption.length > 0 )
			{		
				// don't add to feedsToAdd,  but we do need to re-add it back 
				// to the "feeds" list, so it "looks" selected
				var newOption2 = $j(selected).clone();
				newOption2.appendTo( "select#feeds" );
			}
			else
			{
				// do everything as normal
				var newOption = $j(selected).clone();
				newOption.attr('selected', 'selected');
				newOption.appendTo( "select#feedsToAdd" );
				var newOption2 = $j(selected).clone();
				newOption2.appendTo( "select#feeds" );				
			
			}
			
			var removeMeId = $j(selected).attr('value');
			$j(selected).remove();
			
			// if this had previously been selected for removal (and then added
			// back) , then it's in feedsToRemove now.  We need to remove it from there, since
			// we've decided not to remove it.				
			var removeMe = $j("select#feedsToRemove option[value='" + removeMeId + "']");
			if( removeMe )
			{
				removeMe.remove();
			}
			
			
		}
	);	
}

function removeFromSelectedFeeds() {
	// alert( "removeFromSelected" );
	$j( 'select#feeds :selected' ).each( function(i, selected )
		{
			var newOption = $j(selected).clone();
			newOption.attr('selected', 'selected');
			newOption.appendTo( "select#feedsToRemove" );
			
			var newOption2 = $j(selected).clone();
			newOption2.appendTo( "select#availablefeeds" );
			// remove this from the original list
			var removeMeId = $j(selected).attr('value');
			$j(selected).remove();
			
			// if this had previously been added from availablefeeds, then
			// it's in feedsToAdd now.  We need to remove it from there, since
			// we've decided not to add it.				
			var removeMe = $j("select#feedsToAdd option[value='" + removeMeId + "']");
			if( removeMe )
			{
				removeMe.remove();
			}
		}
	);

}

function removeAllFromSelectedFeeds() {
	// alert( "removeAllFromSelected" );
	
	$j( 'select#feeds option' ).each( function(i, selected )
		{	
			var newOption = $j(selected).clone();
			newOption.attr('selected', 'selected');
			newOption.appendTo( "select#feedsToRemove" );
			var newOption2 = $j(selected).clone();
			newOption2.appendTo( "select#availablefeeds" );
			
			$j(selected).remove();					
		}
	);	

	// if we've said "remove all" from selected feeds, then we definitely
	// aren't adding any feeds.  So blow away anything that might be
	// in feedsToAdd
	$j( 'select#feedsToAdd option' ).each( function(i, selected )
		{	
			$j(selected).remove();			
		}
	);
	
}

/* for aggregate channels */

function addToSelectedChannels() 
{
	// alert( "addToSelectedChannels" );
	
	$j( 'select#availableChannels :selected' ).each( function(i, selected )
		{
		
			// check if this channel is already in "channelsToRemove".  If it is, somebody
			// clicked "remove" then re-added it.  But since the net-net of that
			// is the same settings we currently have, we don't actually want to do
			// anything.
			var checkValue = $j(selected).attr('value');
			// alert( "checkValue: " + checkValue );
			var existingOption = $j("select#aggregateChannelsToRemove option[value='" + checkValue + "']");
			// alert( "existing option length: " + existingOption.length );
			if( existingOption.length > 0 )
			{		
				// don't add to feedsToAdd,  but we do need to re-add it back 
				// to the "feeds" list, so it "looks" selected
				var newOption2 = $j(selected).clone();
				newOption2.appendTo( "select#aggregateChannels" );
			}
			else
			{
				// do everything as normal
				var newOption = $j(selected).clone();
				newOption.attr('selected', 'selected');
				newOption.appendTo( "select#aggregateChannelsToAdd" );
				var newOption2 = $j(selected).clone();
				newOption2.appendTo( "select#aggregateChannels" );				
			
			}
			
			var removeMeId = $j(selected).attr('value');
			$j(selected).remove();
			
			// if this had previously been selected for removal (and then added
			// back) , then it's in channelsToRemove now.  We need to remove it from there, since
			// we've decided not to remove it.				
			var removeMe = $j("select#aggregateChannelsToRemove option[value='" + removeMeId + "']");
			if( removeMe )
			{
				removeMe.remove();
			}
			
		}
	);
}

function addAllToSelectedChannels() {
	
	// alert( "addAllToSelectedChannels" );
	
	$j( 'select#availableChannels option' ).each( function(i, selected )
		{	

			// check if this channel is already in "aggregateChannels".  If it is, somebody
			// clicked "remove" then re-added it.  But since the net-net of that
			// is the same settings we currently have, we don't actually want to do
			// anything.
			var existingOption = $j("select#aggregateChannelsToRemove option[value='" + $j(selected).attr('value') + "']");
			// alert( "existing option length: " + existingOption.length );
			if( existingOption.length > 0 )
			{		
				// don't add to channelsToAdd,  but we do need to re-add it back 
				// to the "aggregateChannels" list, so it "looks" selected
				var newOption2 = $j(selected).clone();
				newOption2.appendTo( "select#aggregateChannels" );
			}
			else
			{
				// do everything as normal
				var newOption = $j(selected).clone();
				newOption.attr('selected', 'selected');
				newOption.appendTo( "select#aggregateChannelsToAdd" );
				var newOption2 = $j(selected).clone();
				newOption2.appendTo( "select#aggregateChannels" );				
			
			}
			
			var removeMeId = $j(selected).attr('value');
			$j(selected).remove();
			
			// if this had previously been selected for removal (and then added
			// back) , then it's in aggregateChannelsToRemove now.  We need to remove it from there, since
			// we've decided not to remove it.				
			var removeMe = $j("select#aggregateChannelsToRemove option[value='" + removeMeId + "']");
			if( removeMe )
			{
				removeMe.remove();
			}
			
			
		}
	);	
}

function removeFromSelectedChannels() 
{
	// alert( "removeFromSelectedChannels" );
	
	$j( 'select#aggregateChannels :selected' ).each( function(i, selected )
		{
			var newOption = $j(selected).clone();
			newOption.attr('selected', 'selected');
			newOption.appendTo( "select#aggregateChannelsToRemove" );
			
			var newOption2 = $j(selected).clone();
			newOption2.appendTo( "select#availableChannels" );
			// remove this from the original list
			var removeMeId = $j(selected).attr('value');
			$j(selected).remove();
			
			// if this had previously been added from availableChannels, then
			// it's in channelsToAdd now.  We need to remove it from there, since
			// we've decided not to add it.				
			var removeMe = $j("select#aggregateChannelsToAdd option[value='" + removeMeId + "']");
			if( removeMe )
			{
				removeMe.remove();
			}
		}
	);

}

function removeAllFromSelectedChannels() 
{
	// alert( "removeAllFromSelectedChannels" );
	
	$j( 'select#aggregateChannels option' ).each( function(i, selected )
		{	
			var newOption = $j(selected).clone();
			newOption.attr('selected', 'selected');
			newOption.appendTo( "select#aggregateChannelsToRemove" );
			var newOption2 = $j(selected).clone();
			newOption2.appendTo( "select#availableChannels" );
			
			$j(selected).remove();					
		}
	);	

	// if we've said "remove all" from selected channels, then we definitely
	// aren't adding any channels.  So blow away anything that might be
	// in aggregateChannelsToAdd
	$j( 'select#aggregateChannelsToAdd option' ).each( function(i, selected )
		{	
			$j(selected).remove();			
		}
	);
	
}


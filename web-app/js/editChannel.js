/* for DataSources */

function addToSelectedDatasources() {
	alert( "addToSelected" );
	
	$j( 'select#availableDatasources :selected' ).each( function(i, selected )
		{
		
			// check if this DataSource is already in "datasourcesToRemove".  If it is, somebody
			// clicked "remove" then re-added it.  But since the net-net of that
			// is the same settings we currently have, we don't actually want to do
			// anything.
			var checkValue = $j(selected).attr('value');
			// alert( "checkValue: " + checkValue );
			var existingOption = $j("select#datasourcesToRemove option[value='" + checkValue + "']");
			// alert( "existing option length: " + existingOption.length );
			if( existingOption.length > 0 )
			{		
				// don't add to datasourcesToAdd,  but we do need to re-add it back 
				// to the "datasources" list, so it "looks" selected
				var newOption2 = $j(selected).clone();
				newOption2.appendTo( "select#datasources" );
			}
			else
			{
				// do everything as normal
				var newOption = $j(selected).clone();
				newOption.attr('selected', 'selected');
				newOption.appendTo( "select#datasourcesToAdd" );
				var newOption2 = $j(selected).clone();
				newOption2.appendTo( "select#datasources" );				
			
			}
			
			var removeMeId = $j(selected).attr('value');
			$j(selected).remove();
			
			// if this had previously been selected for removal (and then added
			// back) , then it's in datasourcesToRemove now.  We need to remove it from there, since
			// we've decided not to remove it.				
			var removeMe = $j("select#datasourcesToRemove option[value='" + removeMeId + "']");
			if( removeMe )
			{
				removeMe.remove();
			}
			
		}
	);
}

function addAllToSelectedDatasources() {
	alert( "addAllToSelected" );
	
	$j( 'select#availableDatasources option' ).each( function(i, selected )
		{	

			// check if this DataSource is already in "datasources".  If it is, somebody
			// clicked "remove" then re-added it.  But since the net-net of that
			// is the same settings we currently have, we don't actually want to do
			// anything.
			var existingOption = $j("select#datasourcesToRemove option[value='" + $j(selected).attr('value') + "']");
			// alert( "existing option length: " + existingOption.length );
			if( existingOption.length > 0 )
			{		
				// don't add to datasourcesToAdd,  but we do need to re-add it back 
				// to the "datasources" list, so it "looks" selected
				var newOption2 = $j(selected).clone();
				newOption2.appendTo( "select#datasources" );
			}
			else
			{
				// do everything as normal
				var newOption = $j(selected).clone();
				newOption.attr('selected', 'selected');
				newOption.appendTo( "select#datasourcesToAdd" );
				var newOption2 = $j(selected).clone();
				newOption2.appendTo( "select#datasources" );				
			
			}
			
			var removeMeId = $j(selected).attr('value');
			$j(selected).remove();
			
			// if this had previously been selected for removal (and then added
			// back) , then it's in datasourcesToRemove now.  We need to remove it from there, since
			// we've decided not to remove it.				
			var removeMe = $j("select#datasourcesToRemove option[value='" + removeMeId + "']");
			if( removeMe )
			{
				removeMe.remove();
			}
			
			
		}
	);	
}

function removeFromSelectedDatasources() {
	alert( "removeFromSelected" );
	$j( 'select#datasources :selected' ).each( function(i, selected )
		{
			var newOption = $j(selected).clone();
			newOption.attr('selected', 'selected');
			newOption.appendTo( "select#datasourcesToRemove" );
			
			var newOption2 = $j(selected).clone();
			newOption2.appendTo( "select#availableDatasources" );
			// remove this from the original list
			var removeMeId = $j(selected).attr('value');
			$j(selected).remove();
			
			// if this had previously been added from availableDatasources, then
			// it's in datasourcesToAdd now.  We need to remove it from there, since
			// we've decided not to add it.				
			var removeMe = $j("select#datasourcesToAdd option[value='" + removeMeId + "']");
			if( removeMe )
			{
				removeMe.remove();
			}
		}
	);

}

function removeAllFromSelectedDatasources() {
	alert( "removeAllFromSelected" );
	
	$j( 'select#datasources option' ).each( function(i, selected )
		{	
			var newOption = $j(selected).clone();
			newOption.attr('selected', 'selected');
			newOption.appendTo( "select#datasourcesToRemove" );
			var newOption2 = $j(selected).clone();
			newOption2.appendTo( "select#availableDatasources" );
			
			$j(selected).remove();					
		}
	);	

	// if we've said "remove all" from selected datasources, then we definitely
	// aren't adding any datasources.  So blow away anything that might be
	// in datasourcesToAdd
	$j( 'select#datasourcesToAdd option' ).each( function(i, selected )
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


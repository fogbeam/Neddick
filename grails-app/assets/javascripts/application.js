// dropdown hook

$j(document).ready(function() {
	
	// start of thinking about how we will limit the levels
	// of nesting for trigger criteria
	// $j('body').data( 'triggerCriteriaLevel', 1 );
	
	
	// we may or may not need something like this, see below 
	// $j("#triggerCriteriaBox").data( "triggerCriteriaCount", 1 );
	
	
	$j('.settings').dropdown()
	
	/* 
	$j( "#shareDialog" ).dialog(
	{
		autoOpen: false,
		show: {
		effect: "blind",
		duration: 200
		},
		hide: {
		effect: "explode",
		duration: 1500
		},
		buttons: [ { text: "Cancel", click: function() { $j( this ).dialog( "close" ); } },
				   { text: "Submit", click: function() 
	   					{ 
	   						
	   						var shareItemUuid = $j(this).data('shareItemUuid');
	   						var permaLink = $j(this).data( 'permaLink');
	   						
	   						// alert( "about to share item with uuid: " + shareItemUuid );	
	   						// find our form object and submit it...
	   						$j('#shareItemUuid').val( shareItemUuid );
	   						$j('#permaLink').val( permaLink);
	   						$j('#shareItemForm').submit();
	   						// alert("submitting...");
	   						$j( this ).dialog( "close" );
	   					
	   					} 
				 	} 
				 ]
	});	
	*/
	
	$j('#submitShareItem').click( function() 
	{
		
		var shareItemUuid = $j('#shareDialog').data('shareItemUuid');
		var permaLink = $j('#shareDialog').data( 'permaLink');
				
		// alert( "about to share item with uuid: " + shareItemUuid );	
		// find our form object and submit it...
		$j('#shareItemUuid').val( shareItemUuid );
		$j('#permaLink').val( permaLink);
		$j('#shareItemForm').submit();

		$j('#shareDialog').modal('toggle');
	});
	
	
	$j( ".shareButton" ).click(function() {
		
		var name = $j(this).attr('name');
		var shareItemUuid = name.split( "." )[1];
		var tempName = "#permalink-" + shareItemUuid;
		var permaLinkNode = $j(tempName);
		var permaLink = permaLinkNode.val();
		
		$j( "#shareDialog" ).data( {'shareItemUuid': shareItemUuid, 'permaLink':permaLink}).modal( { keyboard: false } );
		return false;
	});

	
	$j("#shareEmailCheck").change(function() {
		// alert( 'shareEmailCheck' );
		if(this.checked) {
	        
	    	// reveal the input box
	    	$j("#shareTargetEmail").css("display", "block");
	    	$j("#forShareTargetEmail").css("display", "block");
		}
	    else {
	    	// clear the input box and re-hide it
	    	$j("#shareTargetEmail").val('').css("display", "none");
	    	$j("#forShareTargetEmail").css("display", "none");
	    }
	});	
	
	$j("#shareXmppCheck").change(function() {
		// alert( 'shareXmppCheck' );
		if(this.checked) {
	        
	    	// reveal the input box
	    	$j("#shareTargetXmpp").css("display", "block");
	    	$j("#forShareTargetXmpp").css("display", "block");
	    }
	    else {
	    	// clear the input box and re-hide it
	    	$j("#shareTargetXmpp").val('').css("display", "none");
	    	$j("#forShareTargetXmpp").css("display", "none");
	    }
	});	
	
	
	$j("#shareQuoddyCheck").change(function() {
	    // alert( 'shareQuoddyCheck' );
		if(this.checked) {
	    	
	    	// reveal the input box
	    	$j("#shareTargetQuoddy").css("display", "block");
	    	$j("#forShareTargetQuoddy").css("display", "block");
	    }
	    else {
	    	// clear the input box and re-hide it
	    	$j("#shareTargetQuoddy").val('').css("display", "none");
	    	$j("#forShareTargetQuoddy").css("display", "none");
	    }
	});	
	
	/* Trigger creation / editing controls */
	
	// select on the radio button group that toggles trigger type between "Global" and "Channel" 
	$j("input[name=triggerType]").change(function () 
	{
		
		// alert( "here: " + $j(this).attr('value') );
		
		var triggerType = $j(this).attr('value');
		
		// alert( "TriggerType: " + triggerType );
		
		if( triggerType == "ChannelTrigger" )
		{	
			// alert( "Channel" );
			// if "Channel" is selected, add an input field to specify
			// the channel
			var channelSelector = $j("div #channelSelector").clone();
			channelSelector.css('display', 'block').insertAfter("#triggerNameBox");
		
		}
		else if( triggerType == "GlobalTrigger" )
		{
			// alert( "Not Channel" );
			// see if we have previously added a channel selector.  
			// If we have, remove it from the view 
			var channelSelector = $j("div#channelSelector :visible");
			if( channelSelector )
			{
				// alert( "found it" );
				channelSelector.remove();
			}
			else 
			{
				// alert( "nope" );
			}
		}
		else 
		{
			alert( "Invalid TriggerType selected" );
		}
	});
	
	// the select control for picking a trigger criteria.  When this changes, we need to enable
	// the associated text input field if the criteria is something other than "any" or "all"
	$j(".triggerCriteriaSelect").change(function () 
	{
		// alert( "ok: " + triggerCriteriaLevel );
		// $j('body').data("triggerCriteriaLevel", triggerCriteriaLevel += 1 );
		// var triggerCriteriaLevel = $j('body').data("triggerCriteriaLevel"); 

		// do we need a count like this as part of managing the ids for multiple trigger criteria??
		// var triggerCriteriaCount = $j("triggerCriteriaBox").data("triggerCriteriaCount");
		
		var selectName = $j(this).attr('name');
		
		// parse out the number after the '.' as in
		// criteriaType.1, and activate the corresponding
		// input box
		var critSelectNum = selectName.split('.')[1];
		
		var critValName = "criteriaValue-" + critSelectNum;
		
		var critValInput = $j("#"+critValName);
		
		critValInput.attr('disabled', false);
		
	});
	
	
	// the select control for picking a trigger action.  When this changes, we need to enable
	// the associated text input field.
	$j(".triggerActionSelect").change(function () 
	{		
		// alert( 'here' );
		
		var selectName = $j(this).attr('name');
		
		// alert( "name: " + selectName );
		// parse out the number after the '.' as in
		// criteriaType.1, and activate the corresponding
		// input box
		var actionSelectNum = selectName.split('.')[1];
		// alert( "num: " + actionSelectNum );
		
		var actionValName = "actionValue-" + actionSelectNum;
		
		// alert( "" + actionValName );
		
		var actionValInput = $j("#"+actionValName);
		
		// alert( $j(actionValInput).attr('name'));
		
		actionValInput.attr('disabled', false);
		
	});	
	
	
	
}


);


function afterVote(e) {
    
	var jsonResponse = JSON.parse(e.responseText);
    var entryId = jsonResponse.resp.entryId;
    var score = jsonResponse.resp.score;
    
	// alert( 'afterVote called: entryId = ' + entryId + ', score = ' + score );
    
    var scoreDiv = document.getElementById("score."+entryId);
    scoreDiv.innerHTML = score;
	
 }
         
 function afterSave(e) {
 
    alert( 'saved entry' );
 }
 
 function afterHide(e) {
 
    alert( 'hid entry' );
 }

 /*
 function openShareDialog(entryId) {
    window.open( "/neddick1/share/index/?entryId=" + entryId, "Neddick - Share", 
         "status = 1, height = 300, width = 300, resizable = 0"  )
 }
*/
 
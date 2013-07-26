// dropdown hook

$j(document).ready(function() {
	
	$j('.settings').dropdown()
	

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
	
	
	$j( ".shareButton" ).click(function() {
		
		var name = $j(this).attr('name');
		var shareItemUuid = name.split( "." )[1];
		var tempName = "#permalink-" + shareItemUuid;
		var permaLinkNode = $j(tempName);
		var permaLink = permaLinkNode.val();
		$j( "#shareDialog" ).data( {'shareItemUuid': shareItemUuid, 'permaLink':permaLink}).dialog( "open" );
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
	
}


);


function afterVote(e) {
    
    var entryId = e.responseJSON.resp.entryId;
    var score = e.responseJSON.resp.score;

    var scoreDiv = document.getElementById("score."+entryId);
    scoreDiv.innerHTML = score;
         
 }
         
 function afterSave(e) {
 
    alert( 'saved entry' );
 }
 
 function afterHide(e) {
 
    alert( 'hid entry' );
 }

    
 function openShareDialog(entryId) {
    window.open( "/neddick1/share/index/?entryId=" + entryId, "Neddick - Share", 
         "status = 1, height = 300, width = 300, resizable = 0"  )
 }

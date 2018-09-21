  <!-- Modal -->
  <div class="modal fade" id="shareDialog" tabindex="-1" role="dialog" aria-labelledby="exampleModalLabel" aria-hidden="true">
	<div class="modal-dialog" role="document">
		<div class="modal-content">
			<div class="modal-header">
				<h4 class="modal-title" id="exampleModalLabel">Share Item</h4>
				<button type="button" class="close" data-dismiss="modal" aria-label="Close">
  					<span aria-hidden="true">&times;</span>
				</button>
			</div>
			<div class="modal-body">

			
				<g:formRemote name="shareItemForm" url="[controller: 'share', action:'shareItem']">
				
					<!--  we need a way to specify who/what you are sharing this item WITH -->
					<!--  a set of checkboxes could work, or a multi-select select box -->
					<!--  let's go with checkboxes for now, just because it's the easiest way -->
					<div>  
						
						<span>
							<label for="shareEmailCheck">Email</label>
							<input style="vertical-align:top;margin-top:3px;margin-left:5px;" type="checkbox" value="shareEmail" name="shareEmailCheck" id="shareEmailCheck" />
						</span>
						
						<span style="margin-left:15px;">
							<label for="shareXmppCheck">Xmpp</label>
							<input style="vertical-align:top;margin-top:3px;margin-left:5px;" type="checkbox" value="shareXmpp" name="shareXmppCheck" id="shareXmppCheck" />
						</span>
						
						<span style="margin-left:15px;" >							
							<label for="shareQuoddyCheck">Quoddy</label>
							<input style="vertical-align:top;margin-top:3px;margin-left:5px;" type="checkbox" value="shareQuoddy" name="shareQuoddyCheck" id="shareQuoddyCheck" />
						</span>
						
					</div>
						
					<!--  the uuid of the thing being shared -->
					<input id="shareItemUuid" name="shareItemUuid" type="hidden" value="" />
					
					<!-- the permalink of the thing being shared -->
					<input id="permaLink" name="permaLink" type="hidden" value="" />
					
					<div style="margin-top:20px;margin-bottom:30px;">
						
						<!--  here we have inputs for the various shareTargets.  We'll need boxes for
						 "email", "xmpp" and "quoddy" and we'll want all three to start out hidden, then we can
						  reveal them if/when the user checks the appropriate checkbox above -->	
						  
						<label id="forShareTargetEmail" name="forShareTargetEmail" style="display:none;" for="shareTargetEmail">Email:</label>
						<input style="display:none;" name="shareTargetEmail" id="shareTargetEmail" type="text" value="" />
						
						<label id="forShareTargetXmpp" name="forShareTargetXmpp" style="display:none;" for="shareTargetXmpp">Xmpp:</label>
						<input style="display:none;" name="shareTargetXmpp" id="shareTargetXmpp" type="text" value="" />
						
						<label id="forShareTargetQuoddy" name="forShareTargetQuoddy" style="display:none;" for="shareTargetQuoddy">Quoddy:</label>	
						<!-- put the list of available Quoddy usernames here... -->	
						<g:select style="display:none;" name="shareTargetQuoddy" from="${quoddyUserNames}" value="---"/>
							
					</div>
					
					<!--  text of an (optional) comment -->
					<label id="forShareItemComment" name="forShareItemComment" for="shareItemComment">Comment: </label>
					<input id="shareItemComment" name="shareItemComment" type="text" value="" />
							
				</g:formRemote>
			</div>
			<div class="modal-footer">
				<button type="button" class="btn btn-secondary" data-dismiss="modal">Cancel</button>
				<button id="submitShareItem" name="submitShareItem" type="button" class="btn btn-primary">Submit</button>
			</div>
		</div>
	</div>
</div>                       

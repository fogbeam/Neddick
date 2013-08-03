<html>
    <head>
        <title>Welcome to Neddick</title>
          <meta name="layout" content="main" />
          <nav:resources />
    </head>
    <body>
          <div style="margin-left:35px;padding-top:20px;">
          
          	<g:form controller="trigger" action="update" method="POST" >
          		
          		<input type="hidden" id="id" name="id" value="${triggerToEdit.id}" />
          		<!-- a set of radio buttons to toggle between 
          		Global Trigger or Channel Trigger -->
				
				<!--
				<span>
					<label style="display:inline-block;" for="triggerType">Global Trigger</label>
						<g:radio style="display:inline-block;" name="triggerType" id="triggerType" value="Global" />
					<label style="display:inline-block;" for="triggerType">Channel Trigger</label>
						<g:radio style="display:inline-block;" name="triggerType" id="triggerType" value="Channel" />
				</span>
				-->
				
				<g:radioGroup name="triggerType"
              		labels="['Global', 'Channel']"
              		values="['GlobalTrigger', 'ChannelTrigger']"
              		value="${triggerToEdit.triggerType}"
              		disabled="disabled">
					${it.radio} <label style="display:inline-block;" for="triggerType">${it.label}</label> 
					</g:radioGroup>				
				

          		<div id="triggerNameBox" name="triggerNameBox">
          			<label style="display:block;" for="triggerName">Name </label>
          				<g:textField style="display:inline-block;" name="triggerName" />
          			<p />
          		</div>
          		
          		
          		
          		<div id="triggerCriteriaBox" name="triggerCriteriaBox" >
	          		<b style="display:block;">Trigger Criteria</b>
		          	<!--  a select box for a criteria type-->
					<select id="criteriaType.1" name="criteriaType.1" class="triggerCriteriaSelect" style="display:inline-block;">
						<option value="Blank" selected="selected"></option>
          			</select>
          		</div>
          		
          		
          		<!-- trigger action(s) go here -->
          		<div>
					<b style="display:block;">Trigger Actions</b>
					<select class="triggerActionSelect" id="actionType.1" name="actionType.1" style="display:inline-block;">
						<option value="Blank" selected="selected"></option>
          			</select>
          		</div>
          		
          		
          		<g:submitButton name="Save"/>
          		
          	</g:form>
          
          
          
          </div> 
    </body>
</html>
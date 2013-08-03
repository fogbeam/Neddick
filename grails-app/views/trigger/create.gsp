<html>
    <head>
        <title>Welcome to Neddick</title>
          <meta name="layout" content="main" />
          <nav:resources />
    </head>
    <body>
          <div style="margin-left:35px;padding-top:20px;">
          
          	<g:form controller="trigger" action="save" method="POST" >
          		
          		<!-- a set of radio buttons to toggle between 
          		Global Trigger or Channel Trigger -->
				<b style="display:block;margin-bottom:10px;">Trigger Type</b>
				<span>
					<g:radio style="display:inline-block;" name="triggerType" id="triggerType" value="GlobalTrigger" />
						<label style="display:inline-block;" for="triggerType">Global</label>
					<g:radio style="display:inline-block;" name="triggerType" id="triggerType" value="ChannelTrigger" />		
						<label style="display:inline-block;" for="triggerType">Channel</label>	
				</span>
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
							
							<!--  TODO: we'll come back and add support for boolean criteria later -->
							<!--
							<option value="AndCriteria">All Of The Following Criteria</option>
							<option value="OrCriteria">Any Of The Following Criteria</option>
							-->
							
							<option value="BodyKeywordTriggerCriteria">Body Contains Keyword </option>
							<option value="TagTriggerCriteria">Entry Has Tag </option>
							<option value="AboveScoreTriggerCriteria">Score Is Above</option>
							<option value="TitleKeywordTriggerCriteria">Title Contains Keyword</option>
		
						</select>
	          		
	          		<!--  a text box for a value  -->
	          		<input disabled type="text" id="criteriaValue-1" name="criteriaValue-1" style="display:inline-block;"></input>
	          		
	          		
	          		
	          		
	          		<!--  a +/- element to add another criteria or remove the current criteria -->
	          		<!--
	          		<span>
	          					<!-  - class="addCriteriaButton" -  ->
	          			<a href="#" style="display:inline-block;font-size:15pt;font-weight:bold" >+</a>
	          		</span>
	          		<span>
	          					<!-  -  class="removeCriteriaButton" -  ->
	          			<a href="#" style="display:inline-block;font-size:17pt;font-weight:bold" >-</a>
	          		</span>
	          		-->
	          		
	          		
      			</div>    		
          		
          		
          		<!-- trigger action(s) go here -->
          		<div>
					<b style="display:block;">Trigger Actions</b>
					<select class="triggerActionSelect" id="actionType.1" name="actionType.1" style="display:inline-block;">
						<option value="Blank" selected="selected"></option>
						<option value="EmailAction">Send Entry By Email</option>
						<option value="XmppAction">Send Entry By XMPP</option>
						<option value="QuoddyAction">Share Entry To Quoddy</option>
						<option value="WorkflowAction">Trigger Workflow</option>
						<option value="HttpAction">POST Entry To HTTP Endpoint</option>
						<option value="JMSAction">Send Entry By JMS</option>
						<option value="ScriptAction">Run Script</option>
					</select>          		
        
                  	<!--  a text box for a value  -->
          			<input disabled type="text" id="actionValue-1" name="actionValue-1" style="display:inline-block;"></input>

	          		<!--  a +/- element to add another criteria or remove the current criteria -->
    	      		
    	      		<!--  TODO: we'll come back later and add support for multiple actions, etc. -->
    	      		<!--
    	      		<span>
          				<!- - class="addActionButton" - ->
        	  			<a href="#" style="display:inline-block;font-size:15pt;font-weight:bold" >+</a>
          			</span>
          			<span>
          				<!- -  class="removeActionButton" - ->
          				<a href="#" style="display:inline-block;font-size:17pt;font-weight:bold" >-</a>
          			</span>
          			-->
          			
          		</div>
          
          		<div>
	          		<g:submitButton name="Save"/>
	          	</div>
          	</g:form>   
          </div> 
          
          <div id="channelSelector" name="channelSelector" class="channelSelector" style="display:none">
          
          		<b style="display:block;">Channel</b>
          		<input type="text" name="triggerChannel" /> 
          </div>
          
    </body>
</html>
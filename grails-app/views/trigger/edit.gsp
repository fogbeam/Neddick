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
          			<label style="display:block;" for="triggerName">Name</label>
          				<g:textField style="display:inline-block;" name="triggerName" value="${triggerToEdit.name}"/>
          			<p />
          		</div>
          		
          		
          		
          		<!-- 
          		
          		<option value="BodyKeywordTriggerCriteria">Body Contains Keyword </option>
							<option value="TagTriggerCriteria">Entry Has Tag </option>
							<option value="AboveScoreTriggerCriteria">Score Is Above</option>
							<option value="TitleKeywordTriggerCriteria">Title Contains Keyword</option>
          		 -->
          		
          		
          		<div id="triggerCriteriaBox" name="triggerCriteriaBox" >
	          		<b style="display:block;">Trigger Criteria</b>
		          	<!--  a select box for a criteria type-->
					<g:select 	id="criteriaType.1" name="criteriaType.1" 
								class="triggerCriteriaSelect" style="display:inline-block;"
								from="${[ 
											[id:'BodyKeywordTriggerCriteria', title:'Body Contains Keyword'],
											[id:'TagTriggerCriteria',title:'Entry Has Tag'], 
											[id:'AboveScoreTriggerCriteria', title:'Score Is Above'],
											[id:'TitleKeywordTriggerCriteria', title:'Title Contains Keyword']
											 ]}"
								value="${triggerToEdit.theOneCriteria.shortName}"
								optionKey="id" 
								optionValue="title" />
								
								
					<!--  a text box for a value  -->
	          		<input type="text" id="criteriaValue-1" name="criteriaValue-1" 
	          				style="display:inline-block;"
	          				value="${triggerToEdit.theOneCriteria.value}"></input>
	          						
								
          		</div>
          		
          		
          		<!-- 
          				<option value="EmailAction">Send Entry By Email</option>
						<option value="XmppAction">Send Entry By XMPP</option>
						<option value="QuoddyAction">Share Entry To Quoddy</option>
						<option value="WorkflowAction">Trigger Workflow</option>
						<option value="HttpAction">POST Entry To HTTP Endpoint</option>
						<option value="JMSAction">Send Entry By JMS</option>
						<option value="ScriptAction">Run Script</option>
          		 -->
          		
          		<!-- trigger action(s) go here -->
          		<div>
					<b style="display:block;">Trigger Actions</b>
					<g:select class="triggerActionSelect" id="actionType.1" 
					name="actionType.1" style="display:inline-block;"
					from="${[
							  [id:'EmailAction', title:'Send Entry By Email'],
							  [id:'XmppAction', title:'Send Entry By XMPP'],
							  [id:'QuoddyAction', title:'Share Entry To Quoddy'],
							  [id:'WorkflowAction', title:'Trigger Workflow'],
						      [id:'HttpAction', title:'POST Entry To HTTP Endpoint'],
							  [id:'JMSAction', title:'Send Entry By JMS'],
							  [id:'ScriptAction', title:'Run Script']		
									
						    ]}"
					value="${triggerToEdit.theOneAction.shortName}"
					optionKey="id"
					optionValue="title" />
					
                  	<!--  a text box for a value  -->
          			<input type="text" id="actionValue-1" name="actionValue-1" 
          					style="display:inline-block;"
          					value="${triggerToEdit.theOneAction.value}"></input>
					
					
					
          		</div>
          		
          		
          		<g:submitButton name="Save"/>
          		
          	</g:form>
          
          
          
          </div> 
    </body>
</html>
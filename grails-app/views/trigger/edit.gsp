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
				
          		<p />
          		<label style="display:inline-block;" for="triggerName">Name: </label>
          			<g:textField style="display:inline-block;" name="triggerName" value="${triggerToEdit.name}" />
          		<p />
          		<g:submitButton name="Save"/>
          	</g:form>
          
          
          
          </div> 
    </body>
</html>
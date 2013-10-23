<html>
    <head>
        <title>Welcome to Neddick</title>
          <meta name="layout" content="admin" />
          <nav:resources />
    </head>
    <body>
          <div style="margin-left:35px;padding-top:20px;">
          	
          	<!--  list triggers here, offer button to create new trigger.
          	Clicking on the trigger link should enter the "edit" feature for
          	that trigger -->
          
          	<div>
          		<g:link controller="trigger" action="create">Create Trigger</g:link>
          	</div>
          
          	
          	<div style="margin-top:10px;margin-bottom:30px;" ></div>
          	
          	<strong>Global Triggers</strong>
          	
          	<ul>
          		<g:each in="${globalTriggers}" var="globalTrigger">
          			
          			<li>
          				<g:link controller="trigger" action="edit" id="${globalTrigger.id}" >${globalTrigger.name}</g:link>
						<g:link style="margin-left:25px;" controller="trigger" action="delete" id="${globalTrigger.id}">delete</g:link>

          			</li>
          			
          		</g:each>
          	</ul>
          	
          	<div style="margin-top:8px;margin-bottom:8px;"></div>
          	
          	<strong>Channel Triggers</strong>
          	<ul>
          		<g:each in="${channelTriggers}" var="channelTrigger">
          			
          			<li>
          				<g:link controller="trigger" action="edit" id="${channelTrigger.id}">${channelTrigger.name}</g:link>
          				<g:link style="margin-left:25px;" controller="trigger" action="delete" id="${channelTrigger.id}">delete</g:link>
          			</li>
          			
          		</g:each>
          	</ul>
          	
          	
          </div> 
    </body>
</html>
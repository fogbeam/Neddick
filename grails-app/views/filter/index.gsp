<html>
    <head>
        <title>Welcome to Neddick</title>
          <meta name="layout" content="admin" />
    </head>
    <body>
    
            <div>
          		<g:link controller="filter" action="create">Create Filter</g:link>
          	</div>
    
          <div style="margin-left:35px;padding-top:20px;">
       		<strong>Filters</strong>   
			<p />
			<ul>
          		<g:each in="${filters}" var="filter">
          			
          			<li>
          				<g:link controller="filter" action="edit" id="${filter.id}" >${filter.name}</g:link>
						<g:link style="margin-left:25px;" controller="filter" action="delete" id="${filter.id}">delete</g:link>

          			</li>
          			
          		</g:each>
          	</ul>
          
          
          </div>
    </body>
</html>
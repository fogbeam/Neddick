<html>
    <head>
        <title>Welcome to Neddick</title>
          <meta name="layout" content="admin" />
    </head>
    <body>
    	<g:link controller="dataSource" action="createWizardOne" class="btn btn-primary" style="width:110px;">New DataSource</g:link>
    	<hr />
    	<ul>
	    	<g:each in="${allDataSources}" var="dataSource" >
    			<li>
    				${dataSource.description}<span>&nbsp;</span><span><g:link controller="dataSource" id="${dataSource.id}" action="edit">edit</g:link> </span><span>&nbsp;</span><span><g:link controller="dataSource" id="${dataSource.id}" action="delete">delete</g:link></span>
    			</li>
    		</g:each>
    	</ul>
    </body>
</html>
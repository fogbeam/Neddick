<html>
    <head>
        <title>Welcome to Neddick</title>
		<meta name="layout" content="admin" />
    </head>
    <body>
           <div style="padding-top:20px;margin-left:100px;">                         
               Hello, Welcome to the ADMIN page!
               <p />
               <ul>
                    <li><g:link controller="channel" action="index">Manage Channels</g:link></li>
                    <li><g:link controller="dataSource" action="index">Manage Datasources</g:link></li>
                    <li><g:link controller="schedule" action="index">Manage Jobs</g:link></li>
                    <li><g:link controller="siteConfigEntry" action="index">Manage Site Configuration</g:link></li>
               </ul>
       
          </div>
    </body>
</html>
<html>
    <head>
        <title>Welcome to Neddick</title>
          <meta name="layout" content="main" />
          <nav:resources />
    </head>
    <body>
          <div style="margin-left:35px;padding-top:20px;">
               
               <div>
                    <ul>
                         <g:each in="${allUsers}" var="user">
                              <li>
                                   <g:link controller="user" action="viewDetails" 
                                        params="[targetUserName:user.userId]">${user.userId} </g:link>
                              </li>
                         </g:each>
                    </ul>
               </div>        
               
          </div>
    </body>
</html>
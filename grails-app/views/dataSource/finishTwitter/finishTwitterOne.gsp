<html>
    <head>
        <title>Welcome to Neddick</title>
          <meta name="layout" content="admin" />
          <nav:resources />
    </head>
    <body>
          <g:if test="${flash.message}">
               <div class="flash" style="padding-top:15px;color:red;">
                    ${flash.message}
               </div>
          </g:if>
          
          <div class="body" style="margin-left:100px;margin-top:25px;"> 
            
			<h1>Twitter Authorization Completed!</h1> 
			<h2>Access Token: ${accessToken}</h2>
         </div>
    </body>
</html>
<html>
     
    <head>
        <title>Welcome to Neddick</title>
          <meta name="layout" content="main" />
          <nav:resources />
    </head>
     
     <body>
          <div style="padding-top:10px;padding-bottom:10px;">
               <strong>Attenuate & Amplify Settings<strong>
          </div>
          <p />
          <div>
               <dl>
               <g:each in="${user.childUserLinks}" var="child">
                    <dd>${child.target.userId}:</dd>
                    <dd>${child.boost}</dd>
               </g:each>
               </dl>  
          </div>
     </body>
     
</html>
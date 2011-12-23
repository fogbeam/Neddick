<html>
    <head>
        <title>Welcome to Neddick</title>
          <meta name="layout" content="main" />
          <nav:resources />
    </head>
    <body>
          <div class="searchResults" id="searchResults" style="margin-left:35px;padding-top:20px;">
               <g:each in="${tagList}" var="tag">
                    <div style="padding:10px;">
                         <div style="margin-left:70px;float:left;">
                              <dl>
                                   <dd>
                                        <g:link controller="tags" action="${tag.name}">${tag.name}</g:link>
                                   </dd>
                              </dl>
                         </div>
                    </div>
               </g:each>
          </div> 
    </body>
</html>
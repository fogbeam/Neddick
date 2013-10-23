<html>
    <head>
        <title>Welcome to Neddick</title>
          <meta name="layout" content="admin" />
          <nav:resources />
    </head>
    <body>
          <div class="searchResults" id="searchResults" style="margin-left:35px;padding-top:20px;">
               <ul>
               		<g:each in="${tagList}" var="tag">
               			<li style="margin-top:10px; margin-bottom:10px;">
               				<g:link controller="tags" action="${tag.name}">${tag.name}</g:link>
               			</li>
               		</g:each>
               </ul>
          </div> 
    </body>
</html>
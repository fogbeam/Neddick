<html>
    <head>
        <title>Welcome to Neddick</title>
          <meta name="layout" content="main" />
          <nav:resources />
    </head>
    <body>
          <g:if test="${flash.message}">
               <div class="flash" style="padding-top:15px;color:red;">
                    ${flash.message}
               </div>
          </g:if>
    
          <div class="searchResults" id="searchResults" style="margin-left:35px;padding-top:20px;">
               <ul>
	               <g:each in="${allTags}" var="tag">
	               	<li style="margin-top:10px; margin-bottom:10px;">
	               		<a href="${resource(dir: 'tags', file: tag.name)}">${tag.name}</a>
	               	</li>
	               </g:each>
               </ul>
          </div> 
          
          <div style="padding-top:10px;">
               <!-- Display Pager -->
               <g:if test="${currentPageNumber > 1}">
                    <g:list controller="tag" action="list" params='[pageNumber:"${currentPageNumber -1}"]'>Prev</g:list>
               </g:if>
               <g:if test="${currentPageNumber < availablePages}">
                    <g:list controller="tag" action="list" params='[pageNumber:"${currentPageNumber +1}"]'>Next</g:list>
               </g:if>
          </div>
    </body>
</html>
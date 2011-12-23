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
               <g:each in="${allChannels}" var="channel">
                    <div style="padding:10px;">
                         <div style="margin-left:70px;float:left;">
                              <dl>
                                   <dd>
                                        <g:link controller="r" action="${channel.name}">${channel.name}</g:link>
                                   </dd>
                              </dl>
                         </div>
                    </div>
               </g:each>
          </div> 
          <div style="padding-top:10px;">
               <!-- Display Pager -->
               <g:if test="${currentPageNumber > 1}">
                    <g:link controller="channel" action="list" params='[pageNumber:"${currentPageNumber -1}"]'>Prev</g:link>
               </g:if>
               
               <g:if test="${currentPageNumber < availablePages}">
                    <g:link controller="channel" action="list" params='[pageNumber:"${currentPageNumber +1}"]'>Next</g:link>
               </g:if>
          </div>
    </body>
</html>
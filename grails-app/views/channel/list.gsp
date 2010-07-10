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
                                        <a href="/neddick1/r/${channel.name}">${channel.name}</a>
                                   </dd>
                              </dl>
                         </div>
                    </div>
               </g:each>
          </div> 
          <div style="padding-top:10px;">
               <!-- Display Pager -->
               <g:if test="${currentPageNumber > 1}">
                    <a href="/neddick1/channel/list?pageNumber=${currentPageNumber -1}">Prev</a>
               </g:if>
               
               <g:if test="${currentPageNumber < availablePages}">
                    <a href="/neddick1/channel/list?pageNumber=${currentPageNumber +1}">Next</a>
               </g:if>
          </div>
          
          
    </body>
</html>
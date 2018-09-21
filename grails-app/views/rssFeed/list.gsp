<html>
    <head>
        <title>Welcome to Neddick</title>
          <meta name="layout" content="admin" />
    </head>
    <body>
          <g:if test="${flash.message}">
               <div class="flash" style="padding-top:15px;color:red;">
                    ${flash.message}
               </div>
          </g:if>
          
       <div class="nav" style="margin-top:15px;margin-left:100px;">  
            <span class="menuButton"><g:link controller="rssFeed" action="create" class="create">New Feed</g:link></span> 
        </div> 
        <div class="body" style="margin-left:100px;"> 
            
            <h1 style="margin-top:15px;">Feeds</h1> 
            
            <div style="margin-top:15px;" > 
                <table> 
                    <thead> 
                        <tr> 
                            <th>Id</th> 
                            <th style="padding-left:8px;">Feed Url</th>
                            <th style="padding-left:8px;">Date Created</th>  
                        </tr> 
                    </thead> 
                    <tbody>
                    
                         <g:each in="${allFeeds}" var="feed" status="oddEven">
                              <tr> 
                                   <td style="padding-top:8px;">
                                        <g:link controller="rssFeed" action="edit" id="${feed.id}">${feed.id}</g:link>
                                   </td>  
                                   <td style="padding-left:8px;padding-top:8px;">${feed.feedUrl}</td> 
                                   <td style="padding-left:8px;padding-top:8px;"><g:formatDate date="${feed.dateCreated}" /></td>
                              </tr> 
                         </g:each>         
                    </tbody> 
                </table> 
            </div> 
        </div> 
                    
    </body>
</html>
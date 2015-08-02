<html>
    <head>
        <title>Welcome to Neddick</title>
          <meta name="layout" content="admin" />
          <nav:resources />
    </head>
    <body>
          <div style="margin-left:35px;padding-top:20px;">
               
               <div class="nav">
                    <ul class="menu" id="tablist">
                         <li class="">
                              <span class="">
                               <g:link controller="user" action="viewSubmissions" id="${targetUserName}">Submissions</g:link>    
                              </span>
                         </li>

                         <li>
                              <span>
                                   <g:link controller="user" action="viewComments" id="${targetUserName}">Comments</g:link>
                              </span>
                         </li>
                         
                         <li>
                              <span>
                                   <g:link controller="user" action="viewBookmarks" id="${targetUserName}">Bookmarks</g:link>
                              </span>
                         </li>
                         
                         <li>
                              <span>
                              <g:link controller="user" action="viewTags" id="${targetUserName}">Tags</g:link>
                              </span>
                         </li>
                         
                         <!-- only when the request is for the currently logged in user -->
                         <g:if test="${false}">
                              <li>
                                   <span>    
                                        <g:link controller="user" action="viewFriendsActivity" id="${targetUserName}">Friends' Activity</g:link>
                                   </span>
                              </li>                         
                         </g:if>
                         
                         <!-- only when the request is for somebody other than the currently logged
                              in user and ANONYMOUS 
                         -->
                         <li>
                              <span>
                                   <g:link controller="user" action="relate" id="${targetUserName}">Manage Connection</g:link>
                              </span>
                         </li> 
                         
                    </ul>
               </div>
               
               <div class="allComments" id="allComments" style="margin-left:35px;padding-top:20px;">
                    <g:each in="${allComments}" var="comment">
                         <div style="padding:10px;">
                              
                              <div style="margin-left:70px;">
                                   <dl>
                                        <dd><a href="${comment.entry.url}">${comment.entry.title}</a></dd>
                                        <dd>${comment.text}</dd>
                                   </dl>
                              </div>
                         </div>
                    </g:each>
               </div>
          </div>
    </body>
</html>
<html>
    <head>
        <title>Welcome to Neddick</title>
          <meta name="layout" content="main" />
          <nav:resources />
    </head>
    <body>
          <div style="margin-left:35px;padding-top:20px;">
               
               <div class="nav">
                    <ul class="menu" id="tablist">
                         <li class="">
                              <span class="">
                                   <a href="/neddick1/user/viewSubmissions/${targetUserName}">Submissions</a>
                              </span>
                         </li>

                         <li>
                              <span>
                                   <a href="/neddick1/user/viewComments/${targetUserName}">Comments</a>
                              </span>
                         </li>
                         
                         <li>
                              <span>
                                   <a href="/neddick1/user/viewBookmarks/${targetUserName}">Bookmarks</a>
                              </span>
                         </li>
                         
                         <li>
                              <span>
                                   <a href="/neddick1/user/viewTags/${targetUserName}">Tags</a>
                              </span>
                         </li>
                         
                         <!-- only when the request is for the currently logged in user -->
                         <g:if test="${false}">
                              <li>
                                   <span>
                                        <a href="/neddick1/user/viewFriendsActivity/${targetUserName}">Friends' Activity</a>
                                   </span>
                              </li>                         
                         </g:if>
                         
                         <!-- only when the request is for somebody other than the currently logged
                              in user and ANONYMOUS 
                         -->
                         <li>
                              <span>
                                   <a href="/neddick1/user/relate/${targetUserName}">Manage Connection</a>
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
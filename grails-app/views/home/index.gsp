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
    
          <div class="allEntries" id="allEntries" style="margin-left:35px;padding-top:20px;">
               <g:each in="${allEntries}" var="entry">
                    <div style="padding:10px;">
                         <div style="float:left;">
                              <dl>
                                   <dd>
                                        <div id="upVote.${entry.id}">
                                        <g:remoteLink controller="vote" action="submitVoteUp" params="[entryId:entry.id]"
                                        onComplete="afterVote(e);">
                                        + (up)
                                        </g:remoteLink> <br />
                                        </div>
                                        
                                        <div id="score.${entry.id}">
                                             ${entry.score}
                                        </div>
                                        
                                        <div id="downVote.${entry.id}">
                                        <g:remoteLink controller="vote" action="submitVoteDown" params="[entryId:entry.id]"
                                        onComplete="afterVote(e);">
                                        - (down)
                                        </g:remoteLink>
                                        </div>                                                
                                   </dd>
                              </dl>
                         </div>
                         <div style="margin-left:70px;">
                              <dl>
                                   <dd><a href="${entry.url}">${entry.title}</a></dd>
                                   <dd>Submitted <span> <g:dateFromNow date="${entry.dateCreated}"/>
                                        </span> by <a href="/neddick1/user/viewDetails/${entry.submitter.userId}">
                                                       <span>${entry.submitter.userId}</span></a>
                                   </dd>
                                   <dd>
                                     <span>
                                        <a href="/neddick1/entry/e/${entry.uuid}">comment</a>
                                        </span> 
                                        <span><a href="#" onClick="openShareDialog(${entry.id});">share</a></span>
                                        <span><g:remoteLink controller="entry" action="saveEntry" 
                                                                 params="[entryId:entry.id]" onComplete="afterSave(e);">save</g:remoteLink>
                                        </span> 
                                        <span><g:remoteLink controller="entry" action="hideEntry" 
                                                                 params="[entryId:entry.id]" onComplete="afterHide(e);">hide</g:remoteLink></span>
                                        <span><a href="#" id="showHideTagbox.${entry.id}" onClick="return toggleTagbox(${entry.id});" >Tag</a></span>
                                        <div id="tagbox.${entry.id}" style="display:none;" >
                                             <g:form>
                                                  <g:textField id="tagNameField.${entry.id}" name="tagName" />
                                                  <g:hiddenField name="entryUuid" value="${entry.uuid}" />
                                                  <g:hiddenField name="entryId" value="${entry.id}" />
                                                  <g:submitToRemote controller="tag" onSuccess="addTag(e, ${entry.id})" action="addTag" value="Save" />
                                             </g:form>
                                        </div>                          
                                   </dd>
                              </dl>
                         </div>
                    </div>
               </g:each>
          </div> 
    
          <!-- Display Pager -->
          <g:if test="${currentPageNumber > 1}">
               <g:if test="${channelName == null}">
                    <a href="/neddick1/home/${requestType}?pageNumber=${currentPageNumber -1}">Prev</a>
               </g:if>
               <g:else>
                     <a href="/neddick1/r/${channelName}/${requestType}?pageNumber=${currentPageNumber -1}">Prev</a>
               </g:else>
          </g:if>
          
          <g:if test="${currentPageNumber < availablePages}">
               <g:if test="${channelName == null}">
                    <a href="/neddick1/home/${requestType}?pageNumber=${currentPageNumber +1}">Next</a>
               </g:if>
               <g:else>
                    <a href="/neddick1/r/${channelName}/${requestType}?pageNumber=${currentPageNumber +1}">Next</a>
               </g:else>
          </g:if>
    
    </body>
</html>
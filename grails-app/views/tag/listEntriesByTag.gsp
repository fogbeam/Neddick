<html>
    <head>
        <title>Welcome to Neddick</title>
          <meta name="layout" content="main" />
          <nav:resources />
    </head>
    <body>
          <div class="searchResults" id="searchResults" style="margin-left:35px;padding-top:20px;">
               <g:each in="${allEntries}" var="entry">
                    
                    <div style="padding:10px;">
                         
                         <div style="float:left;">
                              <dl>
                                   <dd>
                                        <div id="upVote.${entry.id}">
                                             <g:remoteLink controller="vote" action="submitVoteUp" params="[entryId:entry.id]"
                                             onComplete="afterVote(e);">
                                             <img src="${resource(dir:'images/icons',file:'1uparrow.png')}" />
                                             </g:remoteLink>
                                        </div>
                                        
                                        <div id="score.${entry.id}" style="padding-left:3px;">
                                             ${entry.score}
                                        </div>
                                        
                                        <div id="downVote.${entry.id}">
                                             <g:remoteLink controller="vote" action="submitVoteDown" params="[entryId:entry.id]"
                                             onComplete="afterVote(e);">
                                             <img src="${resource(dir:'images/icons',file:'1downarrow.png')}" />
                                             </g:remoteLink>
                                        </div>                                                
                                   </dd>
                              </dl>
                         </div>
                         
                         <div style="margin-left:70px;">
                              <dl>
                                   <dd><a href="${entry.url}">${entry.title}</a></dd>
                                   <dd>Submitted <span> <g:dateFromNow date="${entry.dateCreated}"/>
                                        </span> by 
                                        <g:link controller="user" action="viewDetails" id="${entry.submitter.userId}" ><span>${entry.submitter.userId}</span></g:link>
                                   </dd>
                                   <dd>
                                     <span>
                                        <g:link controller="entry" action="e" id="${entry.uuid}">comment</g:link>
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
    </body>
</html>
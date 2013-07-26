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
                                        <!--+ (up) -->
                                        <img src="${resource(dir:'images/icons',file:'1uparrow.png')}" />
                                        </g:remoteLink>
                                        </div>
                                        
                                        <div id="score.${entry.id}" style="padding-left:3px;">
                                             <g:formatNumber number="${entry.link?.entryBaseScore}" format="##0" />
                                        </div>
                                        
                                        <div id="downVote.${entry.id}">
                                        <g:remoteLink controller="vote" action="submitVoteDown" params="[entryId:entry.id]"
                                        onComplete="afterVote(e);">
                                        <!-- - (down) -->
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
                                        </span> by <g:link controller="user" action="viewDetails" id="${entry.submitter.userId}" ><span>${entry.submitter.userId}</span></g:link>
                                   </dd>
                                   <dd>
                                     <span>
	                                    <input id="permalink-${entry.uuid}" name="permalink-${entry.uuid}" type="hidden" value="${createLink( absolute:true, controller: 'entry', action:'e', id:entry.uuid)}" />
                                        <g:link controller="entry" action="e" id="${entry.uuid}">comment</g:link>
                                        </span> 
                                        <span>
											<a href="#" class="shareButton" name="shareButton.${entry.uuid}" id="shareButton.${entry.uuid}">share</a>
                                        </span>
                                        <span><g:remoteLink controller="entry" action="saveEntry" 
                                                                 params="[entryId:entry.id]" onComplete="afterSave(e);">save</g:remoteLink>
                                        </span>
                                        <span><g:remoteLink controller="entry" action="hideEntry" 
                                                                 params="[entryId:entry.id]" onComplete="afterHide(e);">hide</g:remoteLink></span>
                                        <span><a href="#" id="showHideTagbox.${entry.id}" onClick="return toggleTagbox(${entry.id});" >Tag</a></span>
                                        <div id="tagbox.${entry.id}" style="display:none;" >
                                             <g:formRemote onSuccess="addTag(e, ${entry.id})" name="tagForm" url="[ controller: 'tag', action: 'addTag']">
                                                  <g:textField id="tagNameField.${entry.id}" name="tagName" />
                                                  <g:hiddenField name="entryUuid" value="${entry.uuid}" />
                                                  <g:hiddenField name="entryId" value="${entry.id}" />
                                                  <g:submitToRemote controller="tag" onSuccess="addTag(e, ${entry.id})" action="addTag" value="Save" />
                                             </g:formRemote>
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
                    <g:link controller="home" action="${requestType}" params='[pageNumber:"${currentPageNumber -1}"]'>Prev</g:link>
               </g:if>
               <g:else>
                     <g:link controller="r" action="${channelName}" id="${requestType}" params='[pageNumber:"${currentPageNumber -1}"]'>Prev</g:link>
               </g:else>
          </g:if>
          
          <g:if test="${currentPageNumber < availablePages}">
               <g:if test="${channelName == null}">
                    <g:link controller="home" action="${requestType}" params='[pageNumber:"${currentPageNumber +1}"]'>Next</g:link>
               </g:if> 
               <g:else>
                    <g:link controller="r" action="${channelName}" id="${requestType}" params='[pageNumber:"${currentPageNumber +1}"]'>Next</g:link>
               </g:else>
          </g:if>
    
    </body>
</html>

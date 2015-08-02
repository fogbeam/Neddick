          <div style="padding:10px;">
               <div style="float:left;">
                    <dl>
                         <dd>
                              <div id="upVote.${theEntry.id}">
                              <g:remoteLink controller="vote" action="submitVoteUp" params="[entryId:theEntry.id]"
                              onComplete="afterVote(XMLHttpRequest);">
                              <img src="${resource(dir:'images/icons',file:'1uparrow.png')}" />
                              </g:remoteLink>
                              </div>
                              
                              <div id="score.${theEntry.id}" style="padding-left:3px;">
                                   ${theEntry.score}
                              </div>
                              
                              <div id="downVote.${theEntry.id}">
                              <g:remoteLink controller="vote" action="submitVoteDown" params="[entryId:theEntry.id]"
                              onComplete="afterVote(XMLHttpRequest);">
                              <img src="${resource(dir:'images/icons',file:'1downarrow.png')}" />
                              </g:remoteLink>
                              </div>                                                
                         </dd>
                    </dl>
               </div>
               <div style="margin-left:70px;">
                    <dl>
                         <dd><a href="${theEntry.url}">${theEntry.title}</a></dd>
                         <dd>Submitted <span> <g:dateFromNow date="${theEntry.dateCreated}"/>
                              </span> by <span>${theEntry.submitter.userId}</span>
                         </dd>
                         <dd>
                           <span>
                              <g:link controller="entry" action="e" id="${theEntry.uuid}" >comment</g:link>
                              </span> 
                              <span><a href="#" onClick="openShareDialog(${theEntry.id});">share</a></span>
                              <span><g:remoteLink controller="entry" action="saveEntry" 
                                                       params="[entryId:theEntry.id]" onComplete="afterSave(e);">save</g:remoteLink>
                              </span> 
                              <span><g:remoteLink controller="entry" action="hideEntry" 
                                                       params="[entryId:theEntry.id]" onComplete="afterHide(e);">hide</g:remoteLink></span>
                              <span><a href="#" id="showHideTagbox.${theEntry.id}" onClick="return toggleTagbox(${theEntry.id});" >Tag</a></span>
                              <div id="tagbox.${theEntry.id}" style="display:none;" >
                                   
                                   <g:formRemote onSuccess="addTag(${theEntry.id})" name="tagForm" url="[ controller: 'tag', action: 'addTag']">
                                        <g:textField id="tagNameField.${theEntry.id}" name="tagName" />
                                        <g:hiddenField name="entryUuid" value="${theEntry.uuid}" />
                                        <g:hiddenField name="entryId" value="${theEntry.id}" />
                                        <g:submitToRemote controller="tag" onSuccess="addTag(${theEntry.id})" action="addTag" value="Save" />
                                   </g:formRemote>
                              
                              </div>
                         </dd>
                    </dl>
               </div>
          </div>
          
          
          
          
          
          <!-- TODO: put "details" text here if it exists -->
          <g:if test="${theEntry instanceof org.fogbeam.neddick.Question}">
               <div class="usertext">
                    ${theEntry.questionDetails}
               </div>
          </g:if>
          
          
          
          
          
          <div style="padding-top:10px;">
               <b>Recommended Links:</b>
               <hr />
               <div class="recommended" id="recommended" style="margin-left:35px;padding-top:20px;">
                    <g:each in="${recommendedEntries}" var="recommendedEntry">
                         <div style="padding-top:10px;">
                              <div style="float:left;">
                    <dl>
                         <dd>
                              <div id="upVote.${recommendedEntry.id}">
                              <g:remoteLink controller="vote" action="submitVoteUp" params="[entryId:recommendedEntry.id]"
                              onComplete="afterVote(XMLHttpRequest);">
                              <img src="${resource(dir:'images/icons',file:'1uparrow.png')}" />
                              </g:remoteLink>
                              </div>
                              
                              <div id="score.${recommendedEntry.id}" style="padding-left:3px;">
                                   ${recommendedEntry.score}
                              </div>
                              
                              <div id="downVote.${recommendedEntry.id}">
                              <g:remoteLink controller="vote" action="submitVoteDown" params="[entryId:recommendedEntry.id]"
                              onComplete="afterVote(XMLHttpRequest);">
                              <img src="${resource(dir:'images/icons',file:'1downarrow.png')}" />
                              </g:remoteLink>
                              </div>                                                
                         </dd>
                    </dl>
               </div>
               <div style="margin-left:70px;">
                    <dl>
                         <dd><a href="${recommendedEntry.url}">${recommendedEntry.title}</a></dd>
                         <dd>Submitted <span> <g:dateFromNow date="${recommendedEntry.dateCreated}"/>
                              </span> by <span>${recommendedEntry.submitter.userId}</span>
                         </dd>
                         <dd>
                           <span>
                              <g:link controller="entry" action="e" id="${recommendedEntry.uuid}">comment</g:link>
                              </span> 
                              <span><a href="#" onClick="openShareDialog(${recommendedEntry.id});">share</a></span>
                              <span><g:remoteLink controller="entry" action="saveEntry" 
                                                       params="[entryId:recommendedEntry.id]" onComplete="afterSave(e);">save</g:remoteLink>
                              </span> 
                              <span><g:remoteLink controller="entry" action="hideEntry" 
                                                       params="[entryId:recommendedEntry.id]" onComplete="afterHide(e);">hide</g:remoteLink></span>
                              <span><a href="#" id="showHideTagbox.${recommendedEntry.id}" onClick="return toggleTagbox(${recommendedEntry.id});" >Tag</a></span>
                              <div id="tagbox.${recommendedEntry.id}" style="display:none;" >
                                   <g:formRemote onSuccess="addTag(${recommendedEntry.id})" name="tagForm" url="[ controller: 'tag', action: 'addTag']">
                                        <g:textField id="tagNameField.${recommendedEntry.id}" name="tagName" />
                                        <g:hiddenField name="entryUuid" value="${recommendedEntry.uuid}" />
                                        <g:hiddenField name="entryId" value="${recommendedEntry.id}" />
                                        <g:submitToRemote controller="tag" onSuccess="addTag(${recommendedEntry.id})" action="addTag" value="Save" />
                                   </g:formRemote>
                              </div>
                         </dd>
                    </dl>
               </div>
                         </div>
                    </g:each>
               </div>
          </div>

          <div style="padding-top:10px;">
               <b>Comments</b>
               <hr />
          </div>
          
          <div style="padding:10px;">
               <g:form controller="comment" action="addComment">
                    <input type="hidden" name="entryId" value="${theEntry.id}" />
                    <textarea id="commentText" name="commentText" rows="4" cols="40"></textarea>
                    <br />
                    <g:submitButton name="save" value="Save" />
               </g:form>
          </div>

          <div class="comments" id="comments" style="margin-left:35px;padding-top:20px;">
               <g:each in="${theEntry.comments}" var="comment">
                    <div style="padding-top:10px;">
                         <div>
                              <dl>
                                   <dd>
                                        <span><a href="#">${theEntry.submitter.userId}</a></span> <g:dateFromNow date="${comment.dateCreated}"/>
                                   </dd>
                                   <dd style="padding-top:10px;padding-bottom:5px;">
                                        ${comment.text}
                                   </dd>
                              </dl>
                         </div>
                    </div>
               </g:each>
          </div>

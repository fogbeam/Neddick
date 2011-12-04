<html>
    <head>
        <title>Welcome to Neddick</title>
          <meta name="layout" content="main" />
          <g:javascript src="editChannel.js"/>
          <nav:resources />
    </head>
    <body>
          <g:if test="${flash.message}">
               <div class="flash" style="padding-top:15px;color:red;">
                    ${flash.message}
               </div>
          </g:if>
          <div style="margin-left:147px;margin-top:35px;">
          Edit Channel
          <g:form action="update" controller="channel">
               <g:hiddenField name="channelId" value="${channel.id}" />
               <g:select id="feedsToAdd" name="feedsToAdd" multiple="true" style="display:none;" />
               <g:select id="feedsToRemove" name="feedsToRemove" multiple="true" style="display:none;" />
               <div>
                    <div style="margin-top:7px;">
                         <label for="channelName">Name:</label>
                    </div>
                    <div>
                         <g:textField name="channelName" value="${channel.name}"></g:textField>
                    </div>
                    <div style="margin-top:7px;">
                         <label for="feeds">Description:</label>
                    </div>
                    <div>
                         <g:textField name="channelDescription" value="${channel.description}"></g:textField>
                    </div>
                    <div style="margin-top:20px;">
                         <label for="feeds">Selected Feeds</label></div>
                    <div style="float:left;margin-top:7px;">                               
                         <g:select name="feeds" from="${channel.feeds}" optionKey="id" optionValue="feedUrl" multiple="true">
                         </g:select> 
                    </div>
                    
                    <div style="margin-left:245px;margin-top:7px;">
                    <a href="#" onclick="removeFromSelected();return false;" style="color:red;text-decoration:none;">&gt;</a>
                    <br />
                    <a href="#" onclick="removeAllFromSelected(); return false;" style="color:red;text-decoration:none;">&gt;&gt;</a>
                    <br />
                    <a href="#" onclick="addToSelected(); return false;" style="color:red;text-decoration:none;">&lt;</a>
                    <br />
                    <a href="#" onclick="addAllToSelected(); return false;" style="color:red;text-decoration:none;">&lt;&lt;</a>
                    <br />
                    </div>
                    
                     
                    <div style="margin-left:370px;margin-top:-87px;">
                         <label for="feeds">Available Feeds</label>
                    </div>
                    <div style="margin-left:320px;margin-top:7px;">                               
                         <g:select name="availablefeeds" from="${availableFeeds}" optionKey="id" optionValue="feedUrl" multiple="true">
                         </g:select> 
                    </div> 
                    <div style="margin-top:7px;"></div>
                    <div><g:submitButton name="Save" /></div>
               </div>
          </g:form>
          </div>
     </body>
</html>
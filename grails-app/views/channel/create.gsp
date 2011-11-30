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
          <div style="margin-left:147px;margin-top:35px;">
          Create New Channel
          <g:form action="save" controller="channel">
               <dl>
                    <dt style="margin-top:7px;">
                         <label for="channelName">Name:</label>
                    </dt>
                    <dd>
                         <g:textField name="channelName"></g:textField>
                    </dd>
                    <dt style="margin-top:7px;">
                         <label for="feeds">Description:</label>
                    </dt>
                    <dd>
                         <g:textField name="channelDescription"></g:textField>
                    </dd>
                    <dt>
                         <label for="feeds">Feeds</label></dt>
                    <dd>                               
                         <g:select name="feeds" from="${availableFeeds}" optionKey="id" optionValue="feedUrl" multiple="true">
                         </g:select> 
                    </dd> 
                    
                    <dt style="margin-top:7px;"></dt>
                    <dd><g:submitButton name="Save" /></dd>
                    
               </dl>
          </g:form>
          </div>
          
     </body>
</html>
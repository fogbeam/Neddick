<html>
    <head>
        <title>Welcome to Neddick</title>
          <meta name="layout" content="main" />
    </head>
    <body>
          <g:if test="${flash.message}">
               <div class="flash" style="padding-top:15px;color:red;">
                    ${flash.message}
               </div>
          </g:if>
    
          <div style="margin-left:35px;padding-top:30px;">
               <g:form action="submit">
                    <dl>
                    <dt>
                         Title: 
                    </dt>
                    <dd>
                         <input type="text" name="title" />
                    </dd>
                    <dt>
                         Link: 
                    </dt>
                    <dd>
                         <input type="text" name="url" />
                    </dd>
                    <dt>
                         Channel:     
                    </dt>
                    <dd>
                         <input type="text" name="channelName" />
                    </dd>     
                    
                    <dd>
                    <dt>&nbsp;</dt>
                    <g:submitButton name="entry.submit" value="Submit" />
                    </dd>
                    </dl>
               </g:form>
          </div>
    </body>
</html>
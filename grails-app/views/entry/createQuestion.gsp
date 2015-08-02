<html>
    <head>
        <title>Welcome to Neddick</title>
          <meta name="layout" content="main" />
          <nav:resources />
    </head>
    <body>
          <div style="margin-left:35px;padding-top:30px;">
               <g:form action="submitQuestion">
                    <dl>
                    <dt>
                         Question:
                    </dt>
                    <dd>
                         <g:textArea name="questionHeadline" rows="2" cols="50"/>
                    </dd>
                    <dt>
                         Additional Details (Optional): 
                    </dt>
                    <dd>
                         <g:textArea name="questionDetails" rows="25" cols="50"/>
                    </dd>
                    <dt>
                         Channel:     
                    </dt>
                    <dd>
                         <input type="text" name="channelName" />
                    </dd>     
                    
                    <dd>
                    <dt>&nbsp;</dt>
                    <g:submitButton name="entry.submitQuestion" value="Submit" />
                    </dd>
                    </dl>
               </g:form>
          </div>
    </body>
</html>
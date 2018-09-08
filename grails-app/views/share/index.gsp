<g:set var="userService" bean="userService"/>
<html>
     <head>
          <title>First cut at a "Share" dialog</title>
     </head>
     <body>
          <h3>First cut at a "Share" dialog</h3>
          User: ${userService.getLoggedInUser().userId} sharing: ${entryId}
          <g:form controller="share" action="shareEntry">
            <input type="hidden" name="entryId" value="${entryId}" />
            <textarea id="share_to_" name="share_to" rows="4" cols="40"></textarea>
            <g:submitButton name="share" value="Share Entry" />
          </g:form>
          
     </body>
</html>


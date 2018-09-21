<html>
     
    <head>
        <title>Welcome to Neddick</title>
          <meta name="layout" content="admin" />
    </head>
     
     <body>
          <div style="padding-top:10px;padding-bottom:10px;">
               <h2>Manage Relationship with <em>${targetUser.userId}</em></h2>
          </div>
          <P />
          <div>
               <strong>Attenuate & Amplify<strong>
          </div>
          <p />
          <div style="padding-top:10px;padding-bottom:10px;">
               <span>Enter a number to amplify or attenuate this user's votes.  For example, entering the number "2"
               would mean that any entry voted "up" by this user would appear to you with a score of at least 3 (the users
               "raw" vote plus the "boost" of 2.  Enter a negative number to subtract from the user's rankings.  These
               values only affect YOUR personal views.</span>
          </div>
          <p />
          <div>
               <g:form controller="user" action="applyBoost">
                    <g:hiddenField name="targetUserId" value="${targetUser.userId}" />
                    <dl>
                         <dt>Boost:</dt>
                         <dd><g:textField name="boostScore" value="${currentBoost}" /></dd>
                    </dl>
                    
                    <g:submitButton name="applyBoost" value="Apply" />
               </g:form>   
          </div>
     </body>
     
</html>
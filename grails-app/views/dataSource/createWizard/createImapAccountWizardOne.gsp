<html>
    <head>
        <title>Welcome to Neddick</title>
          <meta name="layout" content="admin" />
          <nav:resources />
    </head>
    <body>
          <g:if test="${flash.message}">
               <div class="flash" style="padding-top:15px;color:red;">
                    ${flash.message}
               </div>
          </g:if>
          
          <div class="body" style="margin-left:100px;margin-top:25px;"> 
            
            <g:form controller="dataSource" action="createWizard" method="POST"> 
                <div class="dialog"> 
                    <table> 
                        <tbody> 
                            <tr class="prop"> 
                                <td valign="top" style="padding-top:11px;"> 
                                  <label for="imapServer">Server</label> 
                                </td> 
                                <td valign="top" style="padding-top:11px;padding-left:9px;" > 
                                    <input type="text" name="imapServer" value="" id="imapServer" /> 
                                </td> 
                            </tr> 

                            <tr class="prop"> 
                                <td valign="top" style="padding-top:11px;"> 
                                  <label for="imapServerPort">Server Port</label> 
                                </td> 
                                <td valign="top" style="padding-top:11px;padding-left:9px;" > 
                                    <input type="text" name="imapServerPort" value="" id="imapServerPort" /> 
                                </td> 
                            </tr>
                            
                            <tr class="prop"> 
                                <td valign="top" style="padding-top:11px;"> 
                                  <label for="imapUsername">Username</label> 
                                </td> 
                                <td valign="top" style="padding-top:11px;padding-left:9px;" > 
                                    <input type="text" name="imapUsername" value="" id="imapUsername" /> 
                                </td> 
                            </tr> 
                            
                            <tr class="prop"> 
                                <td valign="top" style="padding-top:11px;"> 
                                  <label for="imapPassword">Password</label> 
                                </td> 
                                <td valign="top" style="padding-top:11px;padding-left:9px;" > 
                                    <input type="text" name="imapPassword" value="" id="imapPassword" /> 
                                </td> 
                            </tr>         

                            <tr class="prop"> 
                                <td valign="top" style="padding-top:11px;"> 
                                  <label for="imapFolder">Folder</label> 
                                </td> 
                                <td valign="top" style="padding-top:11px;padding-left:9px;" > 
                                    <input type="text" name="imapFolder" value="" id="imapFolder" /> 
                                </td> 
                            </tr>   


                            <tr class="prop"> 
                                <td valign="top" style="padding-top:11px;"> 
                                  <label for="description">Description</label> 
                                </td> 
                                <td valign="top" style="padding-top:11px;padding-left:9px;" > 
                                    <input type="text" name="description" value="" id="description" /> 
                                </td> 
                            </tr>                                                                            
                                                      
                        </tbody> 
                    </table> 
                </div> 
                <div style="padding-top:12px;"> 
                    <span>
                        <!-- <input type="submit" name="finishWizard" class="save" value="Save" /> -->
                    	<g:submitButton name="finishWizard" class="btn btn-large" value="Save" />
                    </span> 
                </div> 
            </g:form> 
         </div>
    </body>
</html>
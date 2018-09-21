<html>
    <head>
        <title>Welcome to Neddick</title>
          <meta name="layout" content="admin" />
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
                                  <label for="description">Name this Twitter Account:</label> 
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
                    	<g:submitButton name="twitterStage2" class="btn btn-large" value="Next" />
                    </span> 
                </div> 
            </g:form>         
         </div>
    </body>
</html>
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
            
            <g:form controller="rssFeed" action="save" method="post" > 
                <div class="dialog"> 
                    <table> 
                        <tbody> 
                            <tr class="prop"> 
                                <td valign="top" style="padding-top:11px;"> 
                                  <label for="feedUrl">Feed Url</label> 
                                </td> 
                                <td valign="top" style="padding-top:11px;padding-left:9px;" > 
                                    <input type="text" name="feedUrl" value="" id="feedUrl" /> 
                                </td> 
                            </tr> 

                            <tr class="prop"> 
                                <td valign="top" style="padding-top:11px;"> 
                                  <label for="feedUrl">Feed Description</label> 
                                </td> 
                                <td valign="top" style="padding-top:11px;padding-left:9px;" > 
                                    <input type="text" name="feedDescription" value="" id="feedUrl" /> 
                                </td> 
                            </tr>                         
                        </tbody> 
                    </table> 
                </div> 
                <div style="padding-top:12px;"> 
                    <span>
                         <input type="submit" name="_action_save" class="save" value="Save" />
                    </span> 
                </div> 
            </g:form> 
         </div>
    </body>
</html>
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
            
            <g:form controller="rssFeed" action="update" method="post" > 
                <input type="hidden" name="id" value="${theFeed.id}" id="id" /> 
                <!-- <input type="hidden" name="version" value="0" id="version" /> --> 
                <div class="dialog"> 
                    <table> 
                        <tbody> 
                        
                            <tr class="prop"> 
                                <td valign="top" style="padding-top:11px;"> 
                                  <label for="dateCreated">Date Created</label> 
                                </td> 
                                <td valign="top" style="padding-top:11px;padding-left:9px;" > 
                                    <input type="hidden" name="dateCreated" value="date.struct" />
                                    <g:select from="${months}" name="dateCreated_month" id="dateCreated_month" value="${theDate.get(Calendar.MONTH)}" />
                                    <g:select from="${days}" name="dateCreated_day" id="dateCreated_day" value="${theDate.get(Calendar.DAY_OF_MONTH)}" />
							 <g:select from="${years}" name="dateCreated_year" id="dateCreated_year" value="${theDate.get(Calendar.YEAR)}" /> 
                                </td> 
                            </tr> 
                            <tr class="prop"> 
                                <td valign="top" style="padding-top:11px;"> 
                                  <label for="feedUrl">Feed Url</label> 
                                </td> 
                                <td valign="top" style="padding-top:11px;padding-left:9px;" > 
                                    <input type="text" name="feedUrl" value="${theFeed.feedUrl}" id="feedUrl" /> 
                                </td> 
                            </tr> 

                            <tr class="prop"> 
                                <td valign="top" style="padding-top:11px;"> 
                                  <label for="feedUrl">Feed Description</label> 
                                </td> 
                                <td valign="top" style="padding-top:11px;padding-left:9px;" > 
                                    <input type="text" name="feedDescription" value="${theFeed.description}" id="feedUrl" /> 
                                </td> 
                            </tr> 


                        
                        </tbody> 
                    </table> 
                </div> 
                <div style="padding-top:12px;"> 
                    <span><input type="submit" name="_action_update" class="save" value="Save" /></span> 
                    <span style="padding-left:7px;"><input type="submit" name="_action_delete" value="Delete" class="delete" onclick="return confirm('Are you sure?');" /></span> 
                </div> 
            </g:form> 
          </div> 
                    
     </body>
</html>
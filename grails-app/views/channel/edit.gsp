<html>
    <head>
        <title>Welcome to Neddick</title>
          <meta name="layout" content="admin" />
          <script type="text/javascript" src="${resource(dir:'javascripts', file:'editChannel.js')}" ></script>
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
               
               <g:select id="datasourcesToAdd" name="datasourcesToAdd" 
                    from="${availableDatasources}" optionKey="id" optionValue="description"
               	    multiple="true" style="display:none;" />
               <g:select id="datasourcesToRemove" name="datasourcesToRemove" 
               	 from="${channel.dataSourceLinks}" optionKey="channelDataSourceId" optionValue="channelDataSourceDescription"
                 multiple="true" style="display:none;" />


               <g:select id="aggregateChannelsToAdd" name="aggregateChannelsToAdd" 
                    from="${availableChannels}" optionKey="id" optionValue="name"
               	    multiple="true" style="display:none;" />
               <g:select id="aggregateChannelsToRemove" name="aggregateChannelsToRemove" 
               	 from="${channel.aggregateChannels}" optionKey="id" optionValue="name"
                 multiple="true" style="display:none;" />

               <div>
                    <div style="margin-top:7px;">
                         <label for="channelName">Name:</label>
                    </div>
                    <div>
                         <g:textField name="channelName" value="${channel.name}"></g:textField>
                    </div>
                    <div style="margin-top:7px;">
                         <label for="channelDescription">Description:</label>
                    </div>
                    <div>
                         <g:textField name="channelDescription" value="${channel.description}"></g:textField>
                    </div>
                    
                    <div style="margin-top:7px;">
                         <label for="privateChannel">Private Channel:</label>
                    </div>
                    <div>
                         <g:checkBox name="privateChannel" value="${channel.privateChannel}" />
                    </div>                     
                 
                    
                    <!--  pair of selects for adding and removing DataSources -->
                    <div style="margin-top:20px;">
                         <label for="datasources">Selected DataSources</label></div>
                    <div style="float:left;margin-top:7px;">                               
                         <g:select name="datasources" from="${channel.dataSourceLinks}" optionKey="channelDataSourceId" optionValue="channelDataSourceDescription" multiple="true"></g:select> 
                    </div>
                    
                    <div style="margin-left:245px;margin-top:7px;">
                    <a href="#" onclick="removeFromSelectedDatasources();return false;" style="color:red;text-decoration:none;">&gt;</a>
                    <br />
                    <a href="#" onclick="removeAllFromSelectedDatasources(); return false;" style="color:red;text-decoration:none;">&gt;&gt;</a>
                    <br />
                    <a href="#" onclick="addToSelectedDatasources(); return false;" style="color:red;text-decoration:none;">&lt;</a>
                    <br />
                    <a href="#" onclick="addAllToSelectedDatasources(); return false;" style="color:red;text-decoration:none;">&lt;&lt;</a>
                    <br />
                    </div>
                    
                     
                    <div style="margin-left:370px;margin-top:-87px;">
                         <label for="availableDatasources">Available Datasources</label>
                    </div>
                    <div style="margin-left:320px;margin-top:7px;">                               
                         <g:select name="availableDatasources" from="${availableDatasources}" optionKey="id" optionValue="description" multiple="true"></g:select> 
                    </div> 


                    
                    <!--  pair of selects for adding and removing aggregate channels -->
                    <div style="margin-top:20px;">
                         <label for="aggregateChannels">Selected Aggregate Channels</label></div>
                    <div style="float:left;margin-top:7px;">                               
                         <g:select name="aggregateChannels" from="${channel.aggregateChannels}" optionKey="id" optionValue="name" multiple="true"></g:select> 
                    </div>
                    
                    <div style="margin-left:245px;margin-top:7px;">
                    <a href="#" onclick="removeFromSelectedChannels();return false;" style="color:red;text-decoration:none;">&gt;</a>
                    <br />
                    <a href="#" onclick="removeAllFromSelectedChannels(); return false;" style="color:red;text-decoration:none;">&gt;&gt;</a>
                    <br />
                    <a href="#" onclick="addToSelectedChannels(); return false;" style="color:red;text-decoration:none;">&lt;</a>
                    <br />
                    <a href="#" onclick="addAllToSelectedChannels(); return false;" style="color:red;text-decoration:none;">&lt;&lt;</a>
                    <br />
                    </div>
                    
                     
                    <div style="margin-left:370px;margin-top:-87px;">
                         <label for="availableChannels">Available Channels</label>
                    </div>
                    <div style="margin-left:320px;margin-top:7px;">                               
                         <g:select name="availableChannels" from="${availableChannels}" optionKey="id" optionValue="name" multiple="true"></g:select> 
                    </div>                    
                    
                    
                    <div style="margin-top:7px;"></div>
                    <div><g:submitButton name="Save" /></div>
               </div>
          </g:form>
          </div>
     </body>
</html>

<html>
    <head>
        <title>
          <g:layoutTitle default="Neddick" />
        </title>
        <nav:resources />
        <!-- Source File -->
        <!--
        <link rel="stylesheet" type="text/css" href="http://yui.yahooapis.com/2.8.0r4/build/reset/reset-min.css">
        <link rel="stylesheet" type="text/css" href="http://yui.yahooapis.com/2.8.0r4/build/fonts/fonts-min.css">             
        <link rel="stylesheet" type="text/css" href="http://yui.yahooapis.com/2.8.0r4/build/grids/grids-min.css">
        -->
        
        <link rel="stylesheet" type="text/css" href="${resource(dir: 'css', file: 'main.css')}" />
        <link rel="stylesheet" type="text/css" href="${resource(dir: 'css', file: 'reset-min.css')}" />
        <link rel="stylesheet" type="text/css" href="${resource(dir: 'css', file: 'fonts-min.css')}" />             
        <link rel="stylesheet" type="text/css" href="${resource(dir: 'css', file: 'grids-min.css')}" />
        
        
        <g:javascript library="jquery-1.4" />
        <g:javascript library="jquery.timers-1.2" />
        <g:javascript>
          var $j = jQuery.noConflict();
        </g:javascript>

        <g:javascript library="prototype" /> 
        <g:javascript library="scriptaculous" />
        <g:javascript library="application" />

        <g:javascript>
               
               
               
           function addTag(e, id ) {
               toggleTagbox(id);
               clearTagbox(id);
           }               
           
           function clearTagbox(e) {
               var tagNameField = document.getElementById( 'tagNameField' );
               tagNameField.value = '';
           }
               
          function toggleTagbox(e) {
               var tagboxDisplay = document.getElementById('tagbox.' + e)
               var toggleText = document.getElementById( 'showHideTagbox.' + e );
               
               if( tagboxDisplay.style.display == 'none' ) {
                    new Effect.BlindDown( 'tagbox.' + e, {duration: 0.5} );
                    toggleText.textContent = "Hide";
               }
               else {    
                    new Effect.BlindUp( 'tagbox.' + e, {duration: 0.5} );
                    toggleText.textContent = "Tag";               
               }
               
               return false;
          }
               
          </g:javascript>        
        
        
        <g:layoutHead />
                				
    </head>
    <body>
    
          <div id="doc3" class="yui-t4">
               <div id="hd">
                    
                    <!-- TODO: replace this with a template gsp -->
                    
                    <!-- header -->
                    <div style="background-color:cfe3ff;height:65px" >
                         
                         <center>
                              <h1 style="font-size:15pt;">Welcome to Neddick</h1>
                         </center>
                         
                         <!-- nav -->
                         <div style="position:relative;margin-left:150px;">
                              <ul class="navigation" id="navigation_tabs">
                                   
                                   
                                   <li class="navigation_active navigation_first">
                                        
                                        <g:if test="${channelName == null }">
                                             
                                            <a href="${resource(dir:'', file:'')}">New</a>
                                        </g:if>
                                        <g:else>
                                             
                                             <a href="${resource(dir:'r', file:channelName)}" >New</a>                                  
                                        </g:else>
                                   </li>
                                   <li class="navigation_active">
                                        <g:if test="${channelName == null }">
                                             <a href="${resource(dir:'home', file:'hotEntries')}">Hot</a>
                                        </g:if>
                                        <g:else>
                                             <a href="${resource(dir:'r/'+channelName, file:'hotEntries')}">Hot</a>                                  
                                        </g:else>
                                   
                                   </li>
                                   <li class="navigation_active">
                                   <g:if test="${channelName == null }">
                                       <a href="${resource(dir:'home', file:'topEntries')}">Top</a>
                                   </g:if>
                                   <g:else>
                                        <a href="${resource(dir:'r/'+channelName, file:'topEntries')}">Top</a>                                  
                                   </g:else>
                                        
                                   </li>
                                   <li class="navigation_active">
                                        <g:if test="${channelName == null }">
                                          <a href="${resource(dir:'home', file:'controversialEntries')}">Controversial</a>   
                                        </g:if>
                                        <g:else>
                                             <a href="${resource(dir:'r/'+channelName, file:'controversialEntries')}">Controversial</a>                                  
                                        </g:else>
                                   </li>
                                   <li class="navigation_active"><a href="${resource(dir:'home', file:'savedEntries')}">Saved</a></li>
                                   <li class="navigation_active"><g:link controller="tag" action="list">Tags</g:link></li>
                                   <li class="navigation_active"><g:link controller="channel" action="list">Channels</g:link></li>
                                   <li class="navigation_active"><g:link controller="admin" action="index">Admin</g:link></li>
                                   <li style="float:right;margin-right:100px;">
                                        <g:if test="${session.user}">
                                         <g:link controller="userHome" action="index" id="${session.user.userId}">${session.user.userId}</g:link>
                                        </g:if>
                                   </li>
                              </ul>

                         </div>

                    </div>
               </div> 
               <div id="bd">
                     <div id="yui-main">
                         <div class="yui-b">
                              
                              <!-- layout main content area -->
                              <g:layoutBody />             
                                    
                         </div>
                     </div>
                     <div class="yui-b">
                     
                         <!-- layout sidebar -->
                         <g:render template="/sidebar" />
                     
                     </div>
               </div> 
               
               <div id="ft">
                    
                    <!-- TODO: replace this with a template gsp -->
                    
                    <!-- footer -->
                    <div>
                         <center>Footer for Neddick</center>
                    </div>
               </div> 
          </div>            
    </body>	
</html>
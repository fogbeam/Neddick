
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
        <link rel="stylesheet" type="text/css" href="/neddick1/css/main.css">
        <link rel="stylesheet" type="text/css" href="/neddick1/css/reset-min.css">
        <link rel="stylesheet" type="text/css" href="/neddick1/css/fonts-min.css">             
        <link rel="stylesheet" type="text/css" href="/neddick1/css/grids-min.css">
        
        
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
                                             <a href="/neddick1/">New</a>
                                        </g:if>
                                        <g:else>
                                             <a href="/neddick1/r/${channelName}/">New</a>                                  
                                        </g:else>
                                   </li>
                                   <li class="navigation_active">
                                        <g:if test="${channelName == null }">
                                             <a href="/neddick1/home/hotEntries">Hot</a>
                                        </g:if>
                                        <g:else>
                                             <a href="/neddick1/r/${channelName}/hotEntries">Hot</a>                                  
                                        </g:else>
                                   
                                   </li>
                                   <li class="navigation_active">
                                   <g:if test="${channelName == null }">
                                        <a href="/neddick1/home/topEntries">Top</a>
                                   </g:if>
                                   <g:else>
                                        <a href="/neddick1/r/${channelName}/topEntries">Top</a>                                  
                                   </g:else>
                                        
                                   </li>
                                   <li class="navigation_active">
                                        <g:if test="${channelName == null }">
                                             <a href="/neddick1/home/controversialEntries">Controversial</a>
                                        </g:if>
                                        <g:else>
                                             <a href="/neddick1/r/${channelName}/controversialEntries">Controversial</a>                                  
                                        </g:else>
                                   </li>
                                   <li class="navigation_active"><a href="/neddick1/home/savedEntries">Saved</a></li>
                                   <li class="navigation_active"><a href="/neddick1/tag/list">Tags</a></li>
                                   <li class="navigation_active"><a href="/neddick1/channel/list">Channels</a></li>
                                   <li class="navigation_active"><a href="/neddick1/admin/index">Admin</a></li>
                                   <li style="float:right;margin-right:100px;">
                                        <g:if test="${session.user}">
                                         <a href="/neddick1/userHome/index/${session.user.userId}">${session.user.userId}</a>
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
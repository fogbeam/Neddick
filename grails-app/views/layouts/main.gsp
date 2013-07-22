
<html>
    <head>
        <title>
          <g:layoutTitle default="Neddick" />
        </title>
        <nav:resources />
        <link rel="stylesheet" type="text/css" href="${createLinkTo(dir:'css', file:'main.css')}" />
        <link rel="stylesheet" type="text/css" href="${createLinkTo(dir:'css', file:'bootstrap.min.css')}" />
        <link rel="stylesheet" type="text/css" href="${createLinkTo(dir:'css/FontAwesome/css', file:'font-awesome.css')}">
        <link rel="stylesheet" type="text/css" href="${createLinkTo(dir:'css', file:'oagis.css')}" />
        <link rel="stylesheet" type="text/css" href="http://yui.yahooapis.com/3.3.0/build/cssgrids/grids-min.css" />
        <meta name="viewport" content="width=device-width, initial-scale=1.0">

        <g:javascript library="jquery-1.7.1.min" />
        <g:javascript>
          var $j = jQuery.noConflict();
        </g:javascript>
        <g:javascript library="dropdown" />
        <g:javascript>
          $j('.dropdown-toggle').dropdown();
       </g:javascript>

        <g:javascript library="jquery.timers-1.2" />

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
    <!-- begin customizable header -->
    <div id="gbw" class="headerNavContainer navbar-top">
    <div class="container">
    <div class="headerNav row">
    <ul class="customNav span7">
    <!-- TODO: replace this with a template gsp -->
      <li>
        <h1><a href="${createLink(controller:'home', action:'index')}">Neddick</a></h1>
      </li>
      <li class="navigation_active navigation_first">
        <g:if test="${channelName == null }">
          <a href="${resource(dir:'', file:'')}"><i class="icon-asterisk"></i> New</a>
        </g:if>
        <g:else>
          <a href="${resource(dir:'r', file:channelName)}"><i class="icon-asterisk"></i> New</a>
        </g:else>
      </li>
      <li class="navigation_active">
        <g:if test="${channelName == null }">
          <a href="${resource(dir:'home', file:'hotEntries')}"><i class="icon-fire"></i> Hot</a>
        </g:if>
        <g:else>
          <a href="${resource(dir:'r/'+channelName, file:'hotEntries')}"><i class="icon-fire"></i> Hot</a>
        </g:else>
      </li>
      <li class="navigation_active">
        <g:if test="${channelName == null }">
          <a href="${resource(dir:'home', file:'topEntries')}"><i class="icon-star"></i> Top</a>
        </g:if>
        <g:else>
          <a href="${resource(dir:'r/'+channelName, file:'topEntries')}"><i class="icon-star"></i> Top</a>
        </g:else>
      </li>
      <li class="navigation_active">
        <g:if test="${channelName == null }">
          <a href="${resource(dir:'home', file:'controversialEntries')}"><i class="icon-comments"></i>Controversial</a>
        </g:if>
        <g:else>
          <a href="${resource(dir:'r/'+channelName, file:'controversialEntries')}"><i class="icon-comments"></i> Controversial</a>
        </g:else>
      </li>
      <li>
        <g:link controller="entry" action="create">Submit a Link</g:link>
      </li>
      <li>
        <g:link controller="entry" action="createQuestion">Ask a Question</g:link>
      </li>
    </ul>
    <div id="gbg" class="span5 settingsNav navbar">
    <ul>
    <li>
      <div class="dropdown">
        <a class="dropdown-toggle" data-toggle="dropdown" href="#">Settings <i class="icon-sort-down"></i></a>
          <ul class="dropdown-menu" role="menu" aria-labelledby="dLabel">
            <li><a href="${resource(dir:'home', file:'savedEntries')}">Saved</a></li>
            <li><g:link controller="tag" action="list">Tags</g:link></li>
            <li><g:link controller="channel" action="list">Channels</g:link></li>
            <li><g:link controller="admin" action="index">Admin</g:link></li>
            <g:if test="${session.user}">
              <li><g:link controller="userHome" action="index" id="${session.user.userId}">${session.user.userId}</g:link></li>
            </g:if>
            </li>
            <li>
            <g:if test="${session.user}">
            <li>
              <g:link controller="channel" action="create">Create New Channel</g:link>
            </li>
            <li>
              <g:link controller="channel" action="edit" id="${channelName}">Edit Channel Properties</g:link>
            </li>
            </g:if>
            <g:if test="${session.user}">
            <li>
              <g:link controller="channel" action="create">Create New Channel</g:link>
            </li>
            <li>
              <g:link controller="channel" action="edit" id="${channelName}">Edit Channel Properties</g:link>
            </li>·
            </g:if>
          </ul>
      </div>
    </li>
    </ul>
    <ul class="user">
            <g:if test="${session.user}">
              <li><g:link controller="userHome" action="index" id="${session.user.userId}">${session.user.userId}</g:link></li>
            </g:if>
            </li>
            <li>
            <g:if test="${session.user}">
              <g:link controller="login" action="logout">Logout</g:link>
            </g:if>
            <g:else>
            <li>
              <g:link controller="login" action="index">Login</g:link>                                                                                                            
            </li>
            <li>
            <g:link controller="user" action="create">Register</g:link>
            </li>
            </g:else>
            </ul>
    </ul>
    <g:render template="/sidebar" />

  </div>
  </div>
               <div id="bd">
                     <div id="yui-main">
                         <div class="yui-b">
                              
                              <!-- layout main content area -->
                              <g:layoutBody />             
                                    
                         </div>
                     </div>
                     
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
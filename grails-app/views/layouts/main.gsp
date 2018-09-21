<!DOCTYPE html>
<g:set var="userService" bean="userService"/>
<html>
    <head>
             
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
    
        <title>
          <g:layoutTitle default="Neddick" />
        </title>
        
     	<link rel="stylesheet" type="text/css" href="${resource(dir:'css', file:'bootstrap.css')}" />
		<link rel="stylesheet" type="text/css" href="${resource(dir:'css', file:'bootstrap-dropdown-multilevel.css')}" />
		<link rel="stylesheet" type="text/css" href="${resource(dir:'css', file:'main.css')}" />
		<link rel="stylesheet" type="text/css" href="${resource(dir:'css', file:'FontAwesome/css/font-awesome.css')}">
		<link rel="stylesheet" type="text/css" href="${resource(dir:'css', file:'hopscotch.css')}" />
		<!--  for "mega menu" using YAMM3 -->
		<link rel="stylesheet" type="text/css" href="${resource(dir:'css', file:'demo.css')}" />
		<link rel="stylesheet" type="text/css" href="${resource(dir:'css', file:'yamm.css')}" />         

		<script type="text/javascript" src="${resource(dir:'javascripts', file:'jquery.min-1.12.4.js')}" ></script>
    	<!--
        	<script type="text/javascript" src="${resource(dir:'javascripts', file:'jquery-ui-1.10.3.custom/js/jquery-ui-1.10.3.custom.js')}" ></script>
        -->
        
        <g:javascript>
          var $j = jQuery.noConflict();
        </g:javascript>
        
        <script type="text/javascript" src="${resource(dir:'javascripts', file:'dropdown.js')}" ></script>
        
        <script type="text/javascript">
          $j('.dropdown-toggle').dropdown();
       </script>

		<script type="text/javascript" src="${resource(dir:'javascripts', file:'jquery.timers-1.2.js')}" ></script>
		<script type="text/javascript" src="${resource(dir:'javascripts', file:'application.js')}" ></script>
		<script type="text/javascript" src="${resource(dir:'javascripts', file:'bootstrap.js')}" ></script>


		<script type="text/javascript">
           function addTag(id ) {
               toggleTagbox(id);
               clearTagbox(id);
           }               
           
           function clearTagbox(id) {
               var tagNameField = document.getElementById( 'tagNameField.' + id );
               tagNameField.value = '';
           }
               
          function toggleTagbox(e) {
          		// alert( e );
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
               
          </script>        
        
	<script>
	/* 
	$j(document).ready(function() {
		$j('.carousel').carousel({
			interval : false
		});
	});
	*/ 
	
</script>        
        
        
        <g:layoutHead />
    </head>
    
    
    <body>
    
    
    <!-- the new nav bar, borrowed from Quoddy and using Bootstrap 3 -->
	<nav class="navbar yamm navbar-default headerNavContainer">
		<div class="container-fluid">
			<!-- Brand and toggle get grouped for better mobile display -->
			<div class="navbar-header">
				<button type="button" class="navbar-toggle collapsed"
					data-toggle="collapse" data-target="#bs-example-navbar-collapse-1"
					aria-expanded="false">
					<span class="sr-only">Toggle navigation</span> <span
						class="icon-bar"></span> <span class="icon-bar"></span> <span
						class="icon-bar"></span>
				</button>

				<!--  TODO: Pull this style out into a new class -->
				<a class="quoddy-brand navbar-brand"
					href="${createLink(controller:'home', action:'index')}">Neddick</a>

			</div>

			<!-- Collect the nav links, forms, and other content for toggling -->
			<div>
				<ul class="nav navbar-nav">		

					<!-- bringing in the old Neddick nav bar items -->
				
					<li class="todo">
	        			<g:if test="${channelName == null }">
	          				<a href="${resource(dir:'', file:'')}"><i class="icon-asterisk"></i> New</a>
	        			</g:if>
	        			<g:else>
	          				<a href="${resource(dir:'r', file:channelName)}"><i class="icon-asterisk"></i> New</a>
	        			</g:else>
	      			</li>
			      	<li class="todo">
			        	<g:if test="${channelName == null }">
			          		<a href="${resource(dir:'home', file:'hotEntries')}"><i class="icon-fire"></i> Hot</a>
			        	</g:if>
			        	<g:else>
			          		<a href="${resource(dir:'r/'+channelName, file:'hotEntries')}"><i class="icon-fire"></i> Hot</a>
			        	</g:else>
			      	</li>
			      	<li class="todo">
			        	<g:if test="${channelName == null }">
			          		<a href="${resource(dir:'home', file:'topEntries')}"><i class="icon-star"></i> Top</a>
			        	</g:if>
			        	<g:else>
			        	  <a href="${resource(dir:'r/'+channelName, file:'topEntries')}"><i class="icon-star"></i> Top</a>
			        	</g:else>
			      	</li>
			      	<li class="todo">
			        	<g:if test="${channelName == null }">
			          		<a href="${resource(dir:'home', file:'controversialEntries')}"><i class="icon-comments"></i>Controversial</a>
			        	</g:if>
			        	<g:else>
			          		<a href="${resource(dir:'r/'+channelName, file:'controversialEntries')}"><i class="icon-comments"></i> Controversial</a>
			        	</g:else>
			      	</li>
			      	<li>
			        	<g:link controller="entry" action="create">Submit</g:link>
			      	</li>
      			
      				<!-- <li>
        				<!-- <g:link controller="entry" action="createQuestion">Ask a Question</g:link>
      				</li> -->				
				
					<li class="dropdown">
						<a class="dropdown-toggle" data-toggle="dropdown" href="#">Settings<b class="caret"></b></a>
						<ul class="dropdown-menu">		
							<li>
	            				<g:link controller="channel" action="list">Channels</g:link>
	            			</li>
	            
			            	<li>
			            		<g:link controller="home" action="savedEntries">Saved Entries</g:link>
			            	</li>
			            	<li>
			            		<g:link controller="tag" action="list">Tags</g:link>
			            	</li>
			            	<li>
			            		<g:link controller="trigger" action="index">Triggers</g:link>
			            	</li>
				            	
			            	<li>
			            		<g:link controller="filter" action="index">Filters</g:link>
			            	</li>            	

			            	<li>
			              		<g:link controller="channel" action="create">Create New Channel</g:link>
			            	</li>
			            
			            	<li>
			              		<g:link controller="channel" action="edit" id="${channelName}">Edit Channel Properties</g:link>
			            	</li>
			            	
			            	<!--  TODO: hide this if user does not have "admin" role -->
			            	<li>
			            		<g:link controller="admin" action="index">Admin</g:link>
			            	</li>
						
						</ul>
				</li>						
							
				<li>
				<div class="searchBoxContainer">
				
					<g:form name="searchForm" controller="search" action="doSearch" method="GET" class="navbar-form navbar-left">
						<input id="queryString" name="queryString" type="text" class="searchBox"  autocomplete="off" />
						<div class="btn-group">
							<button id="searchMenuBtn" name="foo" data-toggle="dropdown" class="btn dropdown-toggle btn-small"> Search <span class="caret"></span></button>
							<ul class="dropdown-menu" role="menu">
								<li><a id="searchPplBtn" name="searchPplBtn" href="#">People</a></li>
								<li><a id="searchFriendsBtn" name="searchFriendsBtn" href="#">Friends</a></li>
								<li><a id="searchPplIFollowBtn" name="searchPplIFollowBtn" href="#">People I Follow</a></li>
								<li class="divider"></li>
								<li><a id="searchEverythingBtn" name="searchEverythingBtn" href="#">Everything</a></li>
								<li><a id="sparqlSearchBtn" name="sparqlSearchBtn" href="#">SPARQL</a></li>
							</ul>
						</div>
					</g:form>
	
				</div>
				</li>
				</ul>
				
				<ul class="nav navbar-nav navbar-right">
				
					<li class="dropdown"><a class="dropdown-toggle" data-toggle="dropdown" href="#">My Account<b class="caret"></b></a>
						<ul class="dropdown-menu">		
						
							<li><a href="${createLink(controller:'user', action:'listOpenFriendRequests')}">Pending Friend Requests</a></li>
							<li class="divider"></li>
								
							<li><a href="${createLink(controller:'user', action:'editAccount')}">Edit Account Info</a></li>
							<li><a href="${createLink(controller:'user', action:'editProfile')}">Edit Profile</a></li>
							
							<li class="divider"></li>
							<g:if test="${session.enable_self_registration == true}">
								<li><a href="${createLink(controller:'user', action:'create')}">Register</a></li>
							</g:if>
							
							<li><a href="${createLink(controller:'localLogin', action:'logout')}">Logout</a></li>
						</ul>
					</li>
					
					<!-- Help menu -->
					<li class="dropdown"><a class="dropdown-toggle" data-toggle="dropdown" href="#">Help<b class="caret"></b></a>
						<ul class="dropdown-menu">
							<li><a href="docs/index.html">Help Contents</a></li>
							<li><a href="#" onclick="hopscotch.startTour(tour);">Interactive Tour</a></li>
							<li><a href="#">Quoddy Admin Guide</a></li>
							<li><a href="#">Quoddy Integration Guide</a></li>
							<li class="divider"></li>
							<li><a href="#" onclick="testSelector();">About Quoddy</a></li>
						</ul>
					</li>
					<!--  end Help menu -->
		
					<!-- Admin menu -->
					<sec:ifAllGranted roles="ROLE_ADMIN">
						<li class="dropdown"><a class="dropdown-toggle" data-toggle="dropdown" href="#">Admin<b class="caret"></b></a>
							<ul class="dropdown-menu">
								<li><a href="${createLink(controller:'admin', action:'index')}">Admin Home</a></li>
								<li class="divider"></li>
								<li><a href="${createLink(controller:'user', action:'manageUsers')}">Manage Users</a></li>
								<li><a href="${createLink(controller:'siteConfigEntry', action:'list')}">Manage Site Config</a></li>
								<li><a href="${createLink(controller:'schedule', action:'index')}">Manage Scheduled Jobs</a></li>
								<li><a href="#">More goes here...</a></li>
								<li class="divider"></li>
								<li><a href="#">Whatever...</a></li>
							</ul>
						</li>
					</sec:ifAllGranted>
					<!--  end Admin menu -->
					
				</ul>
				
			</div>
			<!-- /.navbar-collapse -->
			
		</div>
		<!-- /.container-fluid -->
		
	</nav>
    
    <div id="page-body" role="main" class="container-fluid">
    
  		<!-- the main body content area -->  	
    	<div class="row">
    	   
    		<div class="col-md-8" style="margin-top:10px;">     
    			
    			<!-- layout main content area -->
        		<g:layoutBody />             
                                    
    		</div>
			<div class="col-md-4" style="margin-top:10px;">
	
				<g:render template="/rightSidebar" />
		
			</div>                     
	    </div>
    
    	<div class="row">
	    	<div class="span12" id="ft">
         
    	    	 <!-- TODO: replace this with a template gsp -->
         
    	    	 <!-- footer -->
    	     	<div>
    	          	<center>Footer for Neddick</center>
    	     	</div>
    		</div> 
    	</div>      
	</div>
    
    <g:render template="/shareDialog" />      
          
                
    </body>	
</html>

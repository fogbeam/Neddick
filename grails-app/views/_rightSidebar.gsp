	<div class="well" style="margin-bottom:20px; min-height:300px; padding-left:35px;">
		<h5 style="margin-top:5px;margin-bottom:5px;">All Channels</h5>
		<hr style="margin-top:2px;margin-bottom:2px;"></hr>
		
		<!--  Carousel - consult the Twitter Bootstrap docs at
		      http://twitter.github.com/bootstrap/javascript.html#carousel -->
		<div id="this-carousel-id" class="carousel slide" style="margin-top:0px;">
		
			<div class="carousel-inner" style="margin-top:5px;min-height:210px;">
				<g:each in="${chunkedChannels}" var="chunkList" status="stat">
						
						<g:if test="${stat == 0}">
							<div class="item active">
						</g:if>
						<g:else>
							<div class="item">
						</g:else>
						
							<ul>
							<g:each in="${chunkList}" var="aChannel">
								<li style="list-style-type:none;">
									<g:link controller="r" action="${aChannel.name}">
										${aChannel.name}
									</g:link>
								</li>
						
							</g:each>
							</ul>
						</div> <!-- end <div class="item"> -->
				</g:each>
			
			</div> <!-- end <div class="carousel-inner"> -->
		
			<!--  Next and Previous controls below
		        href values must reference the id for this carousel -->
			<a style="float:left;margin-bottom:20px;line-height:25px;margin-left:5px;font-size:36pt;text-decoration:none;" href="#this-carousel-id"
				data-slide="prev">&lsaquo;</a> 
						
			<a style="float:right;margin-bottom:20px;line-height:25px;margin-right:5px;font-size:36pt;text-decoration:none;"
						href="#this-carousel-id" data-slide="next">&rsaquo;</a>	
		
		</div> <!-- end <div id="this-carousel-id"> -->
		
	</div>	
	
	<div class="well" style="margin-bottom:20px; min-height:290px; padding-left:35px;">
          <h5 style="margin-top:5px;margin-bottom:5px;">Favorite Channels</h5>
          <hr style="margin-top:2px;margin-bottom:2px;"></hr>
		<!--  Carousel - consult the Twitter Bootstrap docs at
		      http://twitter.github.com/bootstrap/javascript.html#carousel -->
		<div id="carousel-fav-channels" class="carousel slide" >
		
			<div class="carousel-inner" style="min-height:210px;">
				<g:each in="${chunkedFavoriteChannels}" var="chunkListFavChannel" status="statFavChannel">
						
						<g:if test="${statFavChannel == 0}">
							<div class="item active">
						</g:if>
						<g:else>
							<div class="item">
						</g:else>
						
							<ul>
							<g:each in="${chunkListFavChannel}" var="aChannel">
								<li style="list-style-type:none;">
									<g:link controller="r" action="${aChannel.name}">
										${aChannel.name}
									</g:link>
								</li>
						
							</g:each>
							</ul>
						</div> <!-- end <div class="item"> -->
				</g:each>
			
			</div> <!-- end <div class="carousel-inner"> -->
		
			<!--  Next and Previous controls below
		        href values must reference the id for this carousel -->
			<a style="float:left;margin-bottom:20px;line-height:25px;margin-left:5px;font-size:36pt;text-decoration:none;" 
						href="#carousel-fav-channels" data-slide="prev">&lsaquo;</a> 
						
			<a style="float:right;margin-bottom:20px;line-height:25px;margin-right:5px;font-size:36pt;text-decoration:none;"
						href="#carousel-fav-channels" data-slide="next">&rsaquo;</a>	
		
		</div> <!-- end <div id="this-carousel-id"> -->		
		
	</div>
	
	
	<div class="well" style="margin-top:20px;min-height:290px; padding-left:35px;">
		<h5 style="margin-top:5px;margin-bottom:5px;">My Tags</h5>
		<hr style="margin-top:2px;margin-bottom:2px;"></hr>
		<!--  Carousel - consult the Twitter Bootstrap docs at
		      http://twitter.github.com/bootstrap/javascript.html#carousel -->
		<div id="carousel-my-tags" class="carousel slide" >
		
			<div class="carousel-inner" style="min-height:210px;">
				<g:each in="${chunkedMyTags}" var="chunkListMyTags" status="statMyTags">
						
						<g:if test="${statMyTags == 0}">
							<div class="item active">
						</g:if>
						<g:else>
							<div class="item">
						</g:else>
						
							<ul>
							<g:each in="${chunkListMyTags}" var="myTag">
								<li style="list-style-type:none;">
									<a href="${createLink(controller:'tags', action:myTag.name)}">
										${myTag.name}
									</a>
								</li>							
							</g:each>
							</ul>
						</div> <!-- end <div class="item"> -->
				</g:each>
			
			</div> <!-- end <div class="carousel-inner"> -->
		
			<!--  Next and Previous controls below
		        href values must reference the id for this carousel -->
			<a style="float:left;margin-bottom:20px;line-height:25px;margin-left:5px;font-size:36pt;text-decoration:none;" 
						href="#carousel-my-tags" data-slide="prev">&lsaquo;</a> 
						
			<a style="float:right;margin-bottom:20px;line-height:25px;margin-right:5px;font-size:36pt;text-decoration:none;"
						href="#carousel-my-tags" data-slide="next">&rsaquo;</a>	
		
		</div> <!-- end <div id="this-carousel-id"> -->		
	</div>
	
	
	<div class="well" style="margin-top:20px; min-height:140px;padding-left:35px;">
		<h5>Popular Tags</h5>
		<hr></hr>
		<ul>
			<g:each in="${popularTags}" var="popularTag">
				<li style="list-style-type:none;">
					${popularTag}
				</li>
			</g:each>			
		</ul>
	</div>
<html>
    <head>
        <title>Welcome to Neddick</title>
          <meta name="layout" content="main" />
          <nav:resources />
    </head>
    <body>
          <div style="margin-left:35px;padding-top:20px;">
          
          	<g:form controller="filter" action="save" method="POST" >          
           		
           		<div id="filterNameBox" name="filterNameBox">
          			<label style="display:block;" for="filterName">Name </label>
          				<g:textField style="display:inline-block;" name="filterName" />
          			<p />
          		</div>         	
          	
          	    <div id="channelSelector" name="channelSelector" class="channelSelector" >
          
          			<b style="display:block;">Channel</b>
          			<input type="text" name="filterChannel" /> 
          		</div>
          	
          	
				<div id="filterCriteriaBox" name="filterCriteriaBox" >
	          		<b style="display:block;">Filter Criteria</b>
		          		<!--  a select box for a criteria type-->
						<select id="criteriaType.1" name="criteriaType.1" class="filterCriteriaSelect" style="display:inline-block;">
							<option value="Blank" selected="selected"></option>
							
							<!--  TODO: we'll come back and add support for boolean criteria later -->
							<!--
							<option value="AndCriteria">All Of The Following Criteria</option>
							<option value="OrCriteria">Any Of The Following Criteria</option>
							-->
							
							<option value="BodyKeywordFilterCriteria">Body Contains Keyword </option>
							<option value="TagFilterCriteria">Entry Has Tag </option>
							<option value="AboveScoreFilterCriteria">Score Is Above</option>
							<option value="TitleKeywordFilterCriteria">Title Contains Keyword</option>
		
						</select>
	          		
	          		<!--  a text box for a value  -->
	          		<input type="text" id="criteriaValue-1" name="criteriaValue-1" style="display:inline-block;"></input>
	          	
          		</div>
          	
          	
          	
          	    <div>
	          		<g:submitButton name="Save"/>
	          	</div>
          		
          	</g:form>
          
          </div>
    </body>
</html>
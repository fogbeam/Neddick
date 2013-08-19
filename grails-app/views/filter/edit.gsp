<html>
    <head>
        <title>Welcome to Neddick</title>
          <meta name="layout" content="main" />
          <nav:resources />
    </head>
    <body>
    
         <div style="margin-left:35px;padding-top:20px;">
          
          	<g:form controller="filter" action="update" method="POST" >          
           		
           		<input type="hidden" id="id" name="id" value="${filterToEdit.id}" />
           		
           		<div id="filterNameBox" name="filterNameBox">
          			<label style="display:block;" for="filterName">Name </label>
          				<g:textField style="display:inline-block;" name="filterName" value="${filterToEdit.name}" />
          			<p />
          		</div>         	
          	
          	    <div id="channelSelector" name="channelSelector" class="channelSelector" >
          
          			<b style="display:block;">Channel</b>
          			<input type="text" name="filterChannel" value="${filterToEdit.channel.name}" /> 
          		</div>
          	
          	
				<div id="filterCriteriaBox" name="filterCriteriaBox" >
	          		<b style="display:block;">Filter Criteria</b>
		          		<!--  a select box for a criteria type-->
						<g:select id="criteriaType.1" name="criteriaType.1" class="filterCriteriaSelect" style="display:inline-block;"
								from="${[ 
											[id:'BodyKeywordFilterCriteria', title:'Body Contains Keyword'],
											[id:'TagFilterCriteria',title:'Entry Has Tag'], 
											[id:'AboveScoreFilterCriteria', title:'Score Is Above'],
											[id:'TitleKeywordFilterCriteria', title:'Title Contains Keyword']
											 ]}"
								value="${filterToEdit.theOneCriteria.shortName}"
								optionKey="id" 
								optionValue="title" />
							
							<!--  TODO: we'll come back and add support for boolean criteria later -->
							<!--
							<option value="AndCriteria">All Of The Following Criteria</option>
							<option value="OrCriteria">Any Of The Following Criteria</option>
							-->
	          		
	          		<!--  a text box for a value  -->
	          		<input type="text" id="criteriaValue-1" name="criteriaValue-1" style="display:inline-block;"
	          		value="${filterToEdit.theOneCriteria.value}"></input>
	          	
          		</div>
          	
          	
          	
          	    <div>
	          		<g:submitButton name="Save"/>
	          	</div>
          		
          	</g:form>
          
          </div>
    </body>
</html>
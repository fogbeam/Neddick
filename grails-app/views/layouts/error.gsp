<html>
	<head>
		<title><g:layoutTitle default="Neddick - Error" /></title>
		<meta name="viewport" content="width=device-width, initial-scale=1.0">

    	<g:javascript>
        	window.appContext = '${request.contextPath}';
    	</g:javascript>


		<g:javascript library="jquery-1.7.1.min" />
		<g:javascript>
		          var $j = jQuery.noConflict();	
		</g:javascript>

		<g:layoutHead />

	</head>
	<body>

		<!--  main body content -->
		<div id="page-body" role="main">
			<g:layoutBody />
		</div>

	</body>
	
</html>
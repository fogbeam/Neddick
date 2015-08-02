<html>
    <head>
        <title>Welcome to Neddick</title>
          <meta name="layout" content="admin" />
          <nav:resources />
    </head>
    <body>
    	<div class="hero-unit span6">	
		<h2>Create DataSource</h2>
		<g:form controller="dataSource" action="createWizard" method="POST">

			<g:radio name="datasourceType" value="imapAccount"/> <span>IMAP Email Account</span>
			<br />
			<g:radio name="datasourceType" value="rssFeed" /> <span>RSS Feed</span>
			<br />		
			<g:radio name="datasourceType" value="twitterAccount"/> <span>Twitter Account</span>
			<br />
			<g:submitButton name="stage2" class="btn btn-large" value="Next" />
		</g:form>
		</div>
    </body>
</html>
<html>

     <head>
          <title>View Entry</title>
          <meta name="layout" content="viewEntry" />
          <nav:resources />          
     </head>
     
     <body>
     
			<!-- insert the main entry text here.  Move "tools" stuff to the
			      Right Sidebar area. -->
			<g:render template="${theEntry.templateName}" bean="${theEntry}" var="entry" />

     </body>

</html>
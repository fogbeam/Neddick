<html>

<head>
<title>View Entry</title>
<meta name="layout" content="viewEntry" />
<nav:resources />

<g:javascript library="enrich_content" />


</head>

<body>


	<!-- insert the main entry text here.  -->
	<g:if test="${theEntry.enhancementJSON != null}">
		<script class="enhancementJSON" id="enhancementJSON-${theEntry.uuid}" type="text/javascript" language="javascript">
			${theEntry.enhancementJSON}
		</script>
	</g:if>
	
	<g:render template="${theEntry.templateName}" bean="${theEntry}"
		var="entry" />
	
	<div style="padding-top: 10px;">
		<b>Comments</b>
		<hr />
	</div>

	<div class="comments" id="comments"
		style="margin-left: 10px;margin-top:5px; width:100%">
		
		<ul>
			<g:each in="${commentsOldFirst}" var="comment">
				<a name="comment-${comment.uuid}" id="comment-${comment.uuid}" href="#">&nbsp;</a>
				<li style="list-style-type:none;">
					<span>
					<g:link controller="user" action="viewDetails" id="${comment.creator.userId}">
						${comment.creator.userId}
					</g:link>
					</span><span style="margin-left:4px;"><g:dateFromNow date="${comment.dateCreated}" /></span>
					<p style="padding-top:6px;">
						${comment.text}
					</p>	
				</li>
			</g:each>
		</ul>
	</div>

	<div style="padding: 10px;width:90%">
		<g:form controller="comment" action="addComment">
			<input type="hidden" name="entryId" value="${theEntry.id}" />
			<textarea style="width: 100%;" id="commentText" name="commentText" rows="4" cols="40"></textarea>
			<br />
			<g:submitButton name="save" value="Save" />
		</g:form>
	</div>

</body>

</html>
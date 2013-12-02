
<div style="font-size:large;font-weight:bold">
	${entry.subject}
</div>
<div style="width:50%;float:left;margin-top:20px;">
<strong>From:</strong>
	<ul>
		<g:each in="${entry.fromAddress}"  var="address">
			<li style="list-style-type:none;">${address}</li>
		</g:each>
	</ul>
</div>
<div style="width:50%;float:right;;margin-top:20px;">
<strong>To:</strong>
	<ul>
		<g:each in="${entry.toAddress}"  var="address">
			<li style="list-style-type:none;">${address}</li>
		</g:each>
	</ul>
</div>
<div class="contentToEnhance" style="clear:both;margin-top:77px;">
	<pre>
		${entry.bodyContent}
	</pre>
</div>

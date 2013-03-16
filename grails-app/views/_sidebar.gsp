<g:if test="${session.user}">
<li>
          <g:link controller="channel" action="create">Create New Channel</g:link>
</li>
<li>
          <g:link controller="channel" action="edit" id="${channelName}">Edit Channel Properties</g:link>
     </li> 
</g:if>
<li>
<div class="searchBoxContainer">
  <g:form controller="search" action="doSearch" method="GET">
    <input name="queryString" type="text" />
    <input type="submit" value="Search" />
  </g:form>
</div>
</li>

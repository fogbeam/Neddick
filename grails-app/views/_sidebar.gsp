<li>

<div class="searchBoxContainer">
<shiro:authenticated>
  <g:form controller="search" action="doSearch" method="GET">
    <input name="queryString" type="text" />
    <input type="submit" value="Search" />
  </g:form>
</shiro:authenticated>
</div>
</li>

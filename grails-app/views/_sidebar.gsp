<div style="padding-top:30px;">
     <g:form controller="search" action="doSearch" method="GET">
          <input name="queryString" type="text" />
          <input type="submit" value="Search" />
     </g:form>
</div>
<div style="padding-top:25px;">
     <g:link controller="entry" action="create">Submit a Link</g:link>
</div>
<div style="padding-top:25px;">
     <g:link controller="entry" action="createQuestion">Ask a Question</g:link>
</div>
<g:if test="${session.user}">
     <div style="padding-top:25px;">
          <g:link controller="login" action="logout">Logout</g:link>               
     </div>                         
</g:if>
<g:else>
     <div style="padding-top:25px;">
          <g:link controller="login" action="index">Login</g:link>
     </div>
     <div style="padding-top:25px;">               
          <g:link controller="user" action="create">Register</g:link>
     </div>
</g:else>
<g:if test="${session.user}">
     <div style="padding-top:25px;">               
          <g:link controller="channel" action="create">Create New Channel</g:link>
     </div>                         
     <div style="padding-top:25px;">               
          <g:link controller="channel" action="edit" id="${channelName}">Edit Channel Properties</g:link>
     </div> 
</g:if>

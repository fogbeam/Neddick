<div style="padding-top:30px;">
     <g:form controller="search" action="doSearch" method="GET">
          <input name="queryString" type="text" />
          <input type="submit" value="Search" />
     </g:form>
</div>
<div style="padding-top:25px;">
     <a href="/neddick1/entry/create">Submit a Link</a>
</div>
<div style="padding-top:25px;">
     <a href="/neddick1/entry/createQuestion">Ask a Question</a>
</div>
<g:if test="${session.user}">
     <div style="padding-top:25px;">               
          <a href="/neddick1/login/logout">Logout</a>
     </div>                         
</g:if>
<g:else>
     <div style="padding-top:25px;">               
          <a href="/neddick1/login">Login</a>
     </div>
     <div style="padding-top:25px;">               
          <a href="/neddick1/user/create">Register</a>
     </div>
</g:else>
<g:if test="${session.user}">
     <div style="padding-top:25px;">               
          <a href="/neddick1/channel/create">Create New Channel</a>
     </div>                         
     <div style="padding-top:25px;">               
          <a href="/neddick1/channel/edit/${channelName}">Edit Channel Properties</a>
     </div> 
</g:if>

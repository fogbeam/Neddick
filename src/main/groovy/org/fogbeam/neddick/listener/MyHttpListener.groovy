package org.fogbeam.neddick.listener

import javax.servlet.ServletContextAttributeEvent
import javax.servlet.ServletContextAttributeListener
import javax.servlet.http.HttpSessionAttributeListener
import javax.servlet.http.HttpSessionBindingEvent
import javax.servlet.http.HttpSessionEvent
import javax.servlet.http.HttpSessionListener

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
// import org.codehaus.groovy.grails.web.servlet.GrailsApplicationAttributes
import org.fogbeam.neddick.User
import org.springframework.context.ApplicationContext

public class MyHttpListener implements HttpSessionListener, HttpSessionAttributeListener, ServletContextAttributeListener
{ 
	private static final Log log = LogFactory.getLog(MyHttpListener.class);
	
	// ServletContext servletCtx;
	ApplicationContext ctx; // = org.codehaus.groovy.grails.commons.ApplicationHolder.application.parentContext;  
	
	public MyHttpListener()
	{	
		log.debug( "Listener constructed!" );
	}
	
	public void sessionCreated(HttpSessionEvent event) 
	{ 
		log.debug("Session created"); 
	}

	public void sessionDestroyed(HttpSessionEvent event) 
	{ 
		log.debug("Session destroyed"); 
	} 

	@Override
	public void attributeAdded(HttpSessionBindingEvent event) 
	{
		
	}

	public void attributeRemoved(HttpSessionBindingEvent event) 
	{
		String name = event.getName();
		log.debug( "attribute: ${name} removed from session" );
		if( name.equals("user"))
		{
			
			User user = (User)event.getValue();
			log.debug( "removing user cache for user: ${user.userId}" );
		}
	}
	
	public void attributeReplaced(HttpSessionBindingEvent event) 
	{
		
	}

	@Override
	public void attributeAdded(ServletContextAttributeEvent event) 
	{
		log.debug( "attribute ${event.name} added to ServletContext!" );
		if( event.getName().equalsIgnoreCase( GrailsApplicationAttributes.APPLICATION_CONTEXT ))
		{
			// log.debug( "BING!" );
		}
		
	}
	
	public void attributeRemoved(ServletContextAttributeEvent event ) 
	{
		
	}
	
	public void attributeReplaced(ServletContextAttributeEvent event) 
	{
		
	}
}
package org.fogbeam.neddick.spring.listeners

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

import org.fogbeam.neddick.User
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationListener
import org.springframework.security.authentication.event.AbstractAuthenticationEvent
import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.logout.LogoutHandler

class LogoutEventListener implements ApplicationListener<AbstractAuthenticationEvent>, LogoutHandler
{
	Logger log = LoggerFactory.getLogger( "LogoutEventListener" );

	// @Autowired
	// EventQueueService eventQueueService
		
	void onApplicationEvent(AbstractAuthenticationEvent event) 
	{
	}


	void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) 
	{
		log.info('logout');
		
		String userId = ((User)authentication.principal).userId;
		
		// eventQueueService.unRegisterEventQueueForUser( userId );
	}
}

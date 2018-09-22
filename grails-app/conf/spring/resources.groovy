package spring;

import org.fogbeam.neddick.jms.NoOpMessageConverter
import org.fogbeam.neddick.security.UserDetailsService;
import org.fogbeam.neddick.spring.factorybean.CustomBeanPostProcessor
import org.fogbeam.neddick.spring.listeners.HttpSessionServletListener
import org.fogbeam.neddick.spring.listeners.LogoutEventListener
import org.fogbeam.neddick.spring.listeners.SuccessfulLoginListener
import org.springframework.boot.web.servlet.ServletListenerRegistrationBean
import org.springframework.security.authentication.DefaultAuthenticationEventPublisher
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint

import org.springframework.web.client.RestTemplate

// Place your Spring DSL code here
beans = {
	
	httpSessionServletListener(ServletListenerRegistrationBean) {
		listener = bean(HttpSessionServletListener)
	}
		
	customBeanPostProcessor(CustomBeanPostProcessor)

	successfulLoginListener(SuccessfulLoginListener)
	
	logoutEventListener( LogoutEventListener )
		
	userDetailsService( UserDetailsService )
	{
		userService = ref('userService')
	}
	
	authenticationEventPublisher( DefaultAuthenticationEventPublisher )
	
	authenticationEntryPoint( LoginUrlAuthenticationEntryPoint, "/localLogin/index" );

	messageConverter( NoOpMessageConverter )
	
	jenaTemplate( org.fogbeam.neddick.spring.factorybean.JenaTemplateFactoryBean)
	{
		tdbDirectory = System.getProperty("neddick.home") + "/jenastore/triples";
	}

	
	/* 
	contextSource(org.springframework.ldap.core.support.LdapContextSource){
		url="ldap://localhost:10389"
		base=""
		userDn="uid=admin,ou=system"
		password="secret"
	}
	*/
	
	// the LDAP server we use if we're using LDAP as the backing store for
	// accounts
	
	// ldapTemplate(org.springframework.ldap.core.LdapTemplate, ref("contextSource"))
	
	// for looking up LDAP users in "import" mode.  NOTE: this whole deal needs
	// a lot of reworking & rethinking to deal with all the different potential
	// configurations... is LDAP a "read only" service being used only as an authSource?
	// Or do we create accounts in LDAP?  Is there one LDAP server or two (or more)?
	// etc...  for now we're assuming the simple case just to get stuff up and running, but
	// this could get complicated.
	
	/*
	ldapPersonService(org.fogbeam.quoddy.LdapPersonService){
		ldapTemplate = ref("ldapTemplate")
	}
	*/
	

	// define userService and toggle the accountService we pass in, based on the
	// value of 'created.accounts.backingStore'
	/* 
	switch( application.config.created.accounts.backingStore )
	{
		case "ldap":
			accountService(org.fogbeam.quoddy.LdapPersonService){
				ldapTemplate = ref("ldapTemplate")
			}
			break;
			
		case "localdb":
			accountService(org.fogbeam.quoddy.LocalAccountService)
			break;
			
		default:
			log.debug "No AccountService implementation specified!!!"
			throw new RuntimeException( "Config missing 'created.accounts.backingStore' setting!" );
			// ???
			break;
	}
	*/
	
	
	/* 
	userService(org.fogbeam.quoddy.UserService)
	{
		accountService = ref("accountService" )
		friendService = ref( "friendService" )
		groupService = ref( "groupService" )
		userListService = ref( "userListService");
		userGroupService = ref( "userGroupService" );
	}
	*/
		
	
	// select the EmailService implementation based on parameter
	/* 
	switch( application.config.emailservice.backend )
	{
		case 'direct_smtp':
				log.debug "direct_smtp"
			log.info( "direct_smtp")
			emailService(org.fogbeam.quoddy.email.DirectSmtpEmailService);
			break;
		case 'gmail_api':
				log.debug "gmail_api"
			log.info( "gmail_api")
			emailService(org.fogbeam.quoddy.email.GMailApiEmailService)
			break;
		case 'amazon_ses':
				log.debug "amazon_ses"
			log.info( "amazon_ses")
			emailService(org.fogbeam.quoddy.email.AmazonSesEmailService)
			break;
		default:
			log.debug "default (direct_smtp)"
			log.info( "default (direct_smtp)" );
			emailService(org.fogbeam.quoddy.email.DirectSmtpEmailService);
			break;
	}
	*/
	
	
	mailSender(org.springframework.mail.javamail.JavaMailSenderImpl) {
	   host='smtp.gmail.com'
	   port='587'
	   username = "motley.crue.fan@gmail.com"
	   password = "@7400seriesIC"
	   javaMailProperties = ['mail.smtp.auth': 'true',
		  'mail.smtp.starttls.enable':'true',
		  'mail.smtp.starttls.required':'true',
		  'mail.smtp.socketFactory.port':'587'
		  // 'mail.smtp.socketFactory.class':'javax.net.ssl.SSLSocketFactory',
		  // 'mail.smtp.socketFactory.fallback':'false'
		   ];
	}


	// You can set default email bean properties here,
	// eg: from/to/subject mailMessage(org.springframework.mail.SimpleMailMessage) { from = 'myapp@maurice.co.uk' }
	mailMessage(org.springframework.mail.SimpleMailMessage) {
			from = 'neddick@demo.fogbeam.org'
	}

	
	// jsonConverterBean(org.springframework.http.converter.json.MappingJacksonHttpMessageConverter)
	// stringConverterBean(org.springframework.http.converter.StringHttpMessageConverter)

	restTemplate( org.springframework.web.client.RestTemplate )

}

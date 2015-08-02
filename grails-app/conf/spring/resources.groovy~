// Place your Spring DSL code here
beans = {
    
	mailSender(org.springframework.mail.javamail.JavaMailSenderImpl) {
	   // host = 'mail.cpphacker.co.uk'
	   host='smtp.gmail.com'
	   port='587'
	   username = "motley.crue.fan"
	   password = ""
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
			   from = 'mindcrime@cpphacker.co.uk'
			}

		jmsConnectionFactory(org.springframework.jndi.JndiObjectFactoryBean) {
		jndiName="ConnectionFactory"
		jndiEnvironment=["java.naming.factory.initial":"org.jnp.interfaces.NamingContextFactory",
		                 "java.naming.provider.url":"localhost:1099"]
		}


}
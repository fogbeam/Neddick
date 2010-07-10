// Place your Spring DSL code here
beans = {
    
		mailSender(org.springframework.mail.javamail.JavaMailSenderImpl) {
			   host = 'mail.cpphacker.co.uk'
			   username = "cpphacke"
			   password = "illipoes55"
			   javaMailProperties = ['mail.smtp.auth': 'true'];
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
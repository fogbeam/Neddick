// Place your Spring DSL code here
beans = {
    
	mailSender(org.springframework.mail.javamail.JavaMailSenderImpl) {
	   host='smtp.gmail.com'
	   port='587'
	   username = "user@gmail.com"
	   password = "password"
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
			   from = 'neddick@example.com'
			}

		jmsConnectionFactory(org.springframework.jndi.JndiObjectFactoryBean) {
		jndiName="ConnectionFactory"
		jndiEnvironment=["java.naming.factory.initial":"org.jnp.interfaces.NamingContextFactory",
		                 "java.naming.provider.url":"localhost:1099"]
		}

		
		jsonConverterBean(org.springframework.http.converter.json.MappingJacksonHttpMessageConverter)
		stringConverterBean(org.springframework.http.converter.StringHttpMessageConverter)
									
		restTemplate( org.springframework.web.client.RestTemplate )
		{
			messageConverters = [
									ref("jsonConverterBean"), 
									ref("stringConverterBean")
				 				]
		}
		
}

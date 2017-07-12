import grails.util.Environment;

// locations to search for config files that get merged into the main config
// config files can either be Java properties files or ConfigSlurper scripts

// grails.config.locations = [ "classpath:${appName}-config.properties",
//                             "classpath:${appName}-config.groovy",
//                             "file:${userHome}/.grails/${appName}-config.properties",
//                             "file:${userHome}/.grails/${appName}-config.groovy"]

// if(System.properties["${appName}.config.location"]) {
//    grails.config.locations << "file:" + System.properties["${appName}.config.location"]
// }

String neddickHome = System.getProperty( "neddick.home");

if(!grails.config.locations || !(grails.config.locations instanceof List))
{
	grails.config.locations = []
}

switch( Environment.current  )
{
	case Environment.DEVELOPMENT:
		
		String configLocation = neddickHome + "/neddick-dev.properties";
		println "####################\n######################\nadding configLocation: ${configLocation}\n###################";
		grails.config.locations << "file:" + configLocation;
		break;
		
	case Environment.PRODUCTION:
		
		String configLocation = neddickHome + "/neddick-production.properties";
		println "####################\n######################\nadding configLocation: ${configLocation}\n###################";
		grails.config.locations << "file:" + configLocation;
		break;
		
	case Environment.TEST:
		String configLocation = neddickHome + "/neddick-test.properties";
		println "####################\n######################\nadding configLocation: ${configLocation}\n###################";
		grails.config.locations << "file:" + configLocation;
		break;
		
	default:
		break;
}

String fogbeamDevMode = System.getProperty( "fogbeam.devmode" );
if( fogbeamDevMode != null )
{
	fogbeam.devmode=true;
}
else
{
	fogbeam.devmode=false;
}


grails.mime.file.extensions = true // enables the parsing of file extensions from URLs into the request format
grails.mime.use.accept.header = false
grails.mime.types = [ html: ['text/html','application/xhtml+xml'],
                      xml: ['text/xml', 'application/xml'],
                      text: 'text/plain',
                      js: 'text/javascript',
                      rss: 'application/rss+xml',
                      atom: 'application/atom+xml',
                      css: 'text/css',
                      csv: 'text/csv',
                      all: '*/*',
                      json: ['application/json','text/json'],
                      form: 'application/x-www-form-urlencoded',
                      multipartForm: 'multipart/form-data'
                    ]
// The default codec used to encode data with ${}
grails.views.default.codec="none" // none, html, base64
grails.views.gsp.encoding="UTF-8"
grails.converters.encoding="UTF-8"

// enabled native2ascii conversion of i18n properties files
grails.enable.native2ascii = true

// set per-environment serverURL stem for creating absolute links
environments {
    production {
        grails.serverURL = "http://demo.fogbeam.org:8080/${appName}"
    }
}

// log4j configuration
log4j = {
	appenders {
		rollingFile name: "myAppender", maxFileSize: 10000000, file: "/opt/fogcutter/neddick/neddick.log", threshold: org.apache.log4j.Level.DEBUG
		console name: "stdout", threshold: org.apache.log4j.Level.DEBUG
	  }

	root {
		   info 'stdout', 'myAppender'
		 }

	error  'org.codehaus.groovy.grails.web.servlet',  //  controllers
		   'org.codehaus.groovy.grails.web.pages', //  GSP
		   'org.codehaus.groovy.grails.web.sitemesh', //  layouts
		   'org.codehaus.groovy.grails.web.mapping.filter', // URL mapping
		   'org.codehaus.groovy.grails.web.mapping', // URL mapping
		   'org.codehaus.groovy.grails.commons', // core / classloading
		   'org.codehaus.groovy.grails.plugins', // plugins
		   'org.codehaus.groovy.grails.orm.hibernate', // hibernate integration
		   'org.springframework',
			   'net.sf.ehcache.hibernate',
		   'org.hibernate',
		   'net.sf.ehcache',
			   'org.jboss',
			   'org.jboss.remoting',
		   'org.quartz'

	warn   'org.mortbay.log',
		   'org.hibernate'
	
	debug  'grails.controllers',
		   'grails.services',
		   'grails.domain'
}

chat {
    serviceName = "gmail.com"
    host = "talk.google.com"
    port = 5222
    username = "testuser@fogbeam.com"
    password = "password"
}

channel {
	defaultChannel = "default";
}    


security.shiro.redirect.uri = "/login/index"
security.shiro.filter.config = """\
		[main]
			authcBasic = org.apache.shiro.web.filter.authc.BasicHttpAuthenticationFilter
			authcBasic.applicationName = Neddick
		[urls]
		/r/**/rss/** = authcBasic"""
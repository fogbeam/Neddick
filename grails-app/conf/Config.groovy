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
        grails.serverURL = "http://localhost:8080/${appName}"
    }
}

// log4j configuration
log4j = {
    // Example of changing the log pattern for the default console
    // appender:
	
    appenders {
		console name:'stdout', layout:pattern(conversionPattern: '%c{2} %m%n'), threshold: org.apache.log4j.Level.WARN
		rollingFile name: "logfileAppender", maxFileSize: 1000024, file: "logs/neddick.log", threshold: org.apache.log4j.Level.DEBUG
		rollingFile name: "cacheAppender", maxFileSize: 1000024, file: "/tmp/logs/neddick_cache_instrumentation.log", threshold: org.apache.log4j.Level.INFO
    }
		
	root {
		debug 'stdout', 'logfileAppender'

	}
	
    error  'org.codehaus.groovy.grails.web.servlet',  //  controllers
	       'org.codehaus.groovy.grails.web.pages', //  GSP
	       'org.codehaus.groovy.grails.web.sitemesh', //  layouts
	       'org.codehaus.groovy.grails.web.mapping.filter', // URL mapping
		   'org.codehaus.groovy.grails.web.filters',
	       'org.codehaus.groovy.grails.web.mapping', // URL mapping
	       'org.codehaus.groovy.grails.commons', // core / classloading
	       'org.codehaus.groovy.grails.plugins', // plugins
	       'org.codehaus.groovy.grails.orm.hibernate', // hibernate integration
	       'org.springframework',
	       'org.hibernate',
		   'org.quartz',
		   'grails.plugin.jms',
		   'grails-app.conf.spring',
		   'grails-app.services.grails.plugin.jms'

    warn   'org',
		   'net',
		   'org.mortbay.log',
		   'org.jboss',
		   'org.apache',
		   'org.apache.tomcat',
		   'org.apache.commons',
		   'org.apache.commons.digester',
		   'org.apache.commons.modeler',
		   'org.apache.catalina'
	
	debug 'grails.app.controller',
		  'grails.app.service',
		  'grails.app.domain',
		  'grails.app.dataSource',
		  'grails.app.bootstrap'
		  	   
		   
	info cacheAppender: "logger.special", additivity: false
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
grails.project.dependency.resolver = "maven"

grails.project.dependency.resolution = {
    // inherit Grails' default dependencies
    inherits("global") {
        // uncomment to disable ehcache
        excludes 'slf4j-api', 'slf4j-log4j12', 'jcl-over-slf4j', 'jul-to-slf4j'
    }
    log "warn" // log level of Ivy resolver, either 'error', …
    repositories {
        grailsPlugins()
        grailsHome()
        grailsCentral()
	// uncomment the below to enable remote dependency resolution 
	// from public Maven repositories 
	//mavenLocal() 
	mavenCentral() 
	mavenRepo "http://snapshots.repository.codehaus.org" 
	mavenRepo "http://repository.codehaus.org" 
	mavenRepo "http://download.java.net/maven/2/" 
	mavenRepo "http://repository.jboss.com/maven2/"
	mavenRepo "http://maven.restlet.org/"
	mavenRepo "https://repo.grails.org/grails/plugins"
 } 
 dependencies { 
	    // specify dependencies here under either 'build', 'compile', ...

 		// runtime 'mysql:mysql-connector-java:5.1.5'
	 	
	 	// compile "org.grails:grails-webflow:$grailsVersion"
		compile ("org.apache.tika:tika-core:1.4")
		compile ("org.apache.tika:tika-parsers:1.4") {excludes "asm-debug-all"}
		compile "antlr:antlr:2.7.7"

	}
 
 plugins {

 	 runtime ':webflow:2.1.0'

          runtime ":shiro-cas:0.5.1"
          runtime( ":shiro:1.2.1" ) {
                   excludes([name: 'quartz', group: 'org.opensymphony.quartz'])
          }

        // plugins for the build system only
        build ":tomcat:7.0.55.2" // or ":tomcat:8.0.20"

        // plugins for the compile step
        compile ":scaffolding:2.1.2"
        compile ':cache:1.1.8'
        compile ":asset-pipeline:2.1.5"

        // plugins needed at runtime but not for compilation
        runtime ":hibernate4:4.3.8.1" // or ":hibernate:3.6.10.18"
        runtime ":database-migration:1.4.0"
        runtime ":jquery:1.11.1"


           runtime( ":jaxrs:0.11" )
           {
//                exclude group:"org.restlet.gae", name:'org.restlet.ext.json'
//               exclude group:"org.restlet.gae", name:'org.restlet.ext.servlet'
//             exclude group:"org.restlet.gae", name:'org.restlet'
           }

          runtime ":quartz:1.0.2"


   }
 
}
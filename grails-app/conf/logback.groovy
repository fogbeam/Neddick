import static ch.qos.logback.classic.Level.DEBUG
import static ch.qos.logback.classic.Level.INFO
import static ch.qos.logback.classic.Level.ERROR
import static ch.qos.logback.classic.Level.WARN

import java.nio.charset.Charset

import org.springframework.boot.logging.logback.ColorConverter
import org.springframework.boot.logging.logback.WhitespaceThrowableProxyConverter

import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.classic.filter.ThresholdFilter
import ch.qos.logback.core.ConsoleAppender
import ch.qos.logback.core.FileAppender
import grails.util.BuildSettings
import grails.util.Environment

conversionRule 'clr', ColorConverter
conversionRule 'wex', WhitespaceThrowableProxyConverter


// See http://logback.qos.ch/manual/groovy.html for details on configuration
appender("DEBUG_FILE", FileAppender) {
    
    filter(ThresholdFilter) {
        level = DEBUG
    }
    
    file = "${System.getProperty('neddick.home')}/neddick.log"
    append = true
    encoder(PatternLayoutEncoder) {
        pattern = "%level %logger - %msg%n"
    }
}

appender('STDOUT', ConsoleAppender) {
    
    filter(ThresholdFilter) {
      level = INFO
    }
    
    encoder(PatternLayoutEncoder) {
        charset = Charset.forName('UTF-8')

        pattern =
                '%clr(%d{yyyy-MM-dd HH:mm:ss.SSS}){faint} ' + // Date
                        '%clr(%5p) ' + // Log level
                        '%clr(---){faint} %clr([%15.15t]){faint} ' + // Thread
                        '%clr(%-40.40logger{39}){cyan} %clr(:){faint} ' + // Logger
                        '%m%n%wex' // Message
    }
}

   
logger( "org.grails", ERROR )
logger( "org.springframework", ERROR )
logger( "org.hibernate", ERROR )
logger( "org.apache", ERROR )
logger( "grails.plugin", WARN )
logger( "org.quartz", WARN )
logger( "grails.app", ERROR )
logger( "grails.util", ERROR )
logger( "grails.boot", ERROR )
logger( "net.sf.ehcache", ERROR )
logger( "org.jasig.cas.client", WARN )
logger( "reactor.spring", WARN )
logger( "asset.pipeline", WARN )
logger( "org.fogbeam.neddick", DEBUG )
logger( "org.springframework.security", WARN )
logger( "grails.plugin.springsecurity.web.access", WARN )

def targetDir = BuildSettings.TARGET_DIR
if (Environment.isDevelopmentMode() && targetDir != null) {
    appender("FULL_STACKTRACE", FileAppender) {
        file = "${targetDir}/stacktrace.log"
        append = true
        encoder(PatternLayoutEncoder) {
            pattern = "%level %logger - %msg%n"
        }
    }
    
    logger("StackTrace", ERROR, ['FULL_STACKTRACE'], false)
}

root(DEBUG, ['STDOUT', 'DEBUG_FILE'])

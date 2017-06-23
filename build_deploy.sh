#!/bin/bash

service tomcat stop

service tomcat stop

rm -f /usr/share/tomcat/logs/*
rm -f /opt/fogcutter/neddick/neddick.log

grails clean

grails war

rm -rf /usr/share/tomcat/webapps/neddick/

rm -f /usr/share/tomcat/webapps/neddick.war

cp target/neddick-0.1.war /usr/share/tomcat/webapps/neddick.war

rm -f /usr/share/tomcat/logs/*

rm -f /opt/fogcutter/neddick/neddick.log

service tomcat start

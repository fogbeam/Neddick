#!/bin/sh

grails clean; grails -Dhttps.protocols=TLSv1.2 -Dserver.port=8200 -Dneddick.home=/opt/fogcutter/neddick run-app

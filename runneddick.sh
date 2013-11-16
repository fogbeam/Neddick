#!/bin/sh

grails clean; grails -Dserver.port=8200 -Dneddick.home=/opt/fogcutter/neddick run-app

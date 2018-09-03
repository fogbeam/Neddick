#!/bin/sh

./grailsw clean; ./grailsw -Dserver.port=8282 -Dspring.config.location=/opt/fogcutter/neddick/ -Drebuild.indexes=false -Dfogbeam.devmode=true run-app $@


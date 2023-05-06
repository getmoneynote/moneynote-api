#!/bin/bash
kill $(cat pid.file)

#ps -ef | grep 'bookkeeping-user-api-0.1.jar' | grep -v grep | awk '{print $2}' | xargs kill -9
#ps -ef|grep -v grep|grep bookkeeping-user-api-0.1.jar | grep java |awk '{print "kill -9 "$2}'|sh

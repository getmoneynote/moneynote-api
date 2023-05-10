#!/bin/bash
#kill $(cat pid.file)

#ps -ef | grep 'moneynote-api-user-1.0.0.jar' | grep -v grep | awk '{print $2}' | xargs kill -9
#ps -ef|grep -v grep|grep bookkeeping-user-api-0.1.jar | grep java |awk '{print "kill -9 "$2}'|sh

if ps -ef | grep 'moneynote-api-user-1.0.0.jar' | grep -v grep > /dev/null; then
  ps -ef | grep 'moneynote-api-user-1.0.0.jar' | grep -v grep | awk '{print $2}' | xargs kill -9
else
  echo "Process is not running"
fi

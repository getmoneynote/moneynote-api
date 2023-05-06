#!/bin/bash
export $(grep -v '^#' .env | xargs)
nohup java -jar *.jar> 1.log 2>&1 &
echo $! > pid.file
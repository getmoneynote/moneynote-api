#!/bin/bash
clear
export $(grep -v '^#' moneykeep-api-user/dev.env | xargs)
./gradlew :moneykeep-api-user:bootRun --warning-mode=all

#./gradlew :bookkeeping-api-user:build


# https://stackoverflow.com/questions/19331497/set-environment-variables-from-file-of-key-value-pairs
#set -o allexport
#source bookkeeping-api-user/dev.env
#set +o allexport
#echo $DB_HOST
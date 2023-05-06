#!/bin/bash
clear
export $(grep -v '^#' bookkeeping-api-admin/dev.env | xargs)
./gradlew :bookkeeping-api-admin:bootRun --warning-mode=all

#./gradlew :bookkeeping-api-user:build
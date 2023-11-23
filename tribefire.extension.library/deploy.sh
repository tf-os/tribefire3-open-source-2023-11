#!/bin/sh

VERSION=`cat parent/pom.xml|grep "<version>" | head -1 | sed 's/.*\<version\>//g' | awk -F '.' '{print $1 "." $2}'`
TARGET=library
TARGETPATH="/Applications/Braintribe-2.0-${TARGET}"
echo "Deploying version ${VERSION}"

rm -rf /Applications/Braintribe-2.0-${TARGET}/tribefire

#jinni.sh from-file /Users/roman/Development/tribefire/local-deployments/${TARGET}.yaml : options -v
jinni.sh from-file /Users/roman/Development/tribefire/local-deployments/${TARGET}.yaml

cp ~/.m2/repository-groups/org/postgresql/postgresql/42.2.18/postgresql-42.2.18.jar ${TARGETPATH}/tribefire/runtime/host/lib
perl -p -i -e 's/TRIBEFIRE_PUBLIC_SERVICES_URL=\/tribefire-services/TRIBEFIRE_PUBLIC_SERVICES_URL=https:\/\/localhost\:8443\/tribefire-services/g' ${TARGETPATH}/tribefire/conf/tribefire.properties

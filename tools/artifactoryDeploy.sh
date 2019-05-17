#!/bin/bash

# This script assumes that one has set up Gradle so that it can be used
# with Artifactory. This script reads Artifactory's access info

if [ -z $1 ]; then
    echo Usage: $0 package.tgz
fi

PACKAGE=$1

ARTIFACTORY_CREDENTIALS=~/.gradle/gradle.properties

if [ ! -f ~/.gradle/gradle.properties ]; then
    echo "Could not read Artifactory credentials from " $ARTIFACTORY_CREDENTIALS
    exit 1
fi

ARTIFACTORY_USER=`awk '{ split($1, elems, "="); if (elems[1] == "artifactory_username") { print elems[2] } }' $ARTIFACTORY_CREDENTIALS`
ARTIFACTORY_PASSWD=`awk '{ split($1, elems, "="); if (elems[1] == "artifactory_password") { print elems[2] } }' $ARTIFACTORY_CREDENTIALS`

echo $ARTIFACTORY_USER $ARTIFACTORY_PASSWD

curl -u $ARTIFACTORY_USER:$ARTIFACTORY_PASSWD -T $PACKAGE \
"http://foremost.controlthings.fi:8081/artifactory/generic-local/react-native-mist-library/$PACKAGE"





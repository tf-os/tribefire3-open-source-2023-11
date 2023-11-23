#!/bin/bash
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
INST_DIR=$DIR/../
CP=$INST_DIR/lib/'*'

if [ -z "${JAVA_HOME}" ]; then
    # JAVA_HOME not set (or empty) --> let's hope 'java' is on the PATH
    JAVA_EXECUTABLE=java
else
    # JAVA_HOME set --> use 'java' command from there
    JAVA_EXECUTABLE="${JAVA_HOME}/bin/java"
fi

"$JAVA_EXECUTABLE" -cp "$CP" -Djinni.installationDir="$INST_DIR" -Dgm.ownCl=true -Xdebug -Xrunjdwp:server=y,transport=dt_socket,address=4000,suspend=y com.braintribe.tribefire.jinni.Jinni "$@"

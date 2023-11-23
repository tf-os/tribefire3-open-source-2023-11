#!/bin/bash

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
INST_DIR=$DIR/../
JINNI_INSTALLATION_PATH=$DIR/..
JINNI_UPDATE_PATH=$DIR/../jinni-update
LIBS=${JINNI_INSTALLATION_PATH}/lib

if [ -z "${JAVA_HOME}" ]; then
    # JAVA_HOME not set (or empty) --> let's hope 'java' is on the PATH
    JAVA_EXECUTABLE=java
else
    # JAVA_HOME set --> use 'java' command from there
    JAVA_EXECUTABLE="${JAVA_HOME}/bin/java"
fi

echo "Checking for update:"
"$JAVA_EXECUTABLE" -Djinni.suppressDone=true -Djinni.installationDir="$INST_DIR" -Dgm.ownCl=true -jar "$INST_DIR/lib/launch.jar" update-jinni "$@"

rc=$?
if [ $rc -ne 0 ]; then exit $rc; fi

# replace existing installation by downloaded one if available
if [ -d "$JINNI_UPDATE_PATH" ]; then
    # Transferring prepared Jinni to installation:
    "$JAVA_EXECUTABLE" -cp "${LIBS}/jinni-update-support.jar" JinniTransfer "$JINNI_UPDATE_PATH" "$JINNI_INSTALLATION_PATH"

    rc=$?
    if [ $rc -ne 0 ]; then exit $rc; fi

    echo "Update complete"
fi

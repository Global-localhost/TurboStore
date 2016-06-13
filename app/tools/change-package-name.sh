#!/bin/bash -x

## For changing the package name so that your app can be installed alongside 
## Turbo Store. This script also changes the app name, but DOESN'T change the
## URLs of the default repos, or the icons.

## Arguments: org.your.fdroid "Your TurboStore"
## org.your.fdroid will be the new package id
## "Your TurboStore" will be the name of the application

FDROID_PACKAGE=${1:-org.your.fdroid}
FDROID_NAME=${2:-Your TurboStore}
FDROID_PATH=${FDROID_PACKAGE//./\/}

mkdir -p "src/${FDROID_PATH}"

find src/org/fdroid/ res/ -type f |xargs -n 1 perl -pi -e"s/com.phonemetra.turbo.store(?=\W)/${FDROID_PACKAGE}/g"
perl -pi -e"s|com.phonemetra.turbo.store|${FDROID_PACKAGE}|g" AndroidManifest.xml

mv src/org/fdroid/fdroid/* src/${FDROID_PATH}/
rm -rf src/org/fdroid/fdroid/

find res/ -type f -print0 | xargs -0 sed -i "s/Turbo Store/${FDROID_NAME}/g"


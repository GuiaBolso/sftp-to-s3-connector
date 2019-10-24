#!/bin/bash

./gradlew clean build bintrayUpload -PbintrayUser=${BINTRAY_USER} -BbintrayKey=${BINTRAY_KEY} -PdryRun=false

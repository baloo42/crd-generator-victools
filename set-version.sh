#!/usr/bin/env bash

VERSION="$1"
if [[ -z "${VERSION}" ]]; then
    echo "Usage ./set-version.sh <version>"
    exit 1
fi
mvn -B -DskipTests versions:set versions:update-child-modules -DgenerateBackupPoms=false -DnewVersion="${VERSION}"

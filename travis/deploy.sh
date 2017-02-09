#!/bin/bash

echo '[deploy] TRAVIS_BRANCH='$TRAVIS_BRANCH
echo '[deploy] TRAVIS_PULL_REQUEST='$TRAVIS_PULL_REQUEST

publishUrl=$1
echo '[deploy] publishUrl='$publishUrl

# we only publish for specific branches and if NOT a PR
#if [[ "$TRAVIS_BRANCH" == "development" ]] && [[ "$TRAVIS_PULL_REQUEST" == "false" ]]; then
#    ./gradlew publish -PnexusPublishUrl=$publishUrl --stacktrace
#    exit $?
#fi
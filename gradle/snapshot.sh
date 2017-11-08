#!/bin/bash

echo '[snapshot] TRAVIS_BRANCH='$TRAVIS_BRANCH
echo '[snapshot] TRAVIS_PULL_REQUEST='$TRAVIS_PULL_REQUEST

# we only publish for specific branches and if NOT a PR
if [[ "$TRAVIS_BRANCH" == "fixing-deployments" ]] && [[ "$TRAVIS_PULL_REQUEST" == "false" ]]; then
    ./gradlew artifactoryPublish --stacktrace
    exit $?
fi
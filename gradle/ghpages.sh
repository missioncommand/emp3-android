#!/bin/bash

function travis_output() {
    # Outputs console lines to keep travis build from dying.
    max=10
    for i in `seq 2 $max`
    do
        echo "Output for travis $i"
        sleep 60
    done
}

travis_output &

set +x
set -e

echo '[ghpages] TRAVIS_TAG='$TRAVIS_TAG
echo '[ghpages] TRAVIS_BRANCH='$TRAVIS_BRANCH
echo '[ghpages] TRAVIS_PULL_REQUEST='$TRAVIS_PULL_REQUEST

if [[ -n $TRAVIS_TAG ]] && [[ "$TRAVIS_PULL_REQUEST" == "false" ]]; then

    GHPAGES_DIR=${HOME}/ghpages

    git clone --quiet --branch=master https://${MCIO_GITHUB_API_KEY}@github.com/missioncommand/missioncommand.github.io.git $GHPAGES_DIR > /dev/null 2>&1

    cd $GHPAGES_DIR

    git config user.name "missioncommand-bot"
    git config user.email "builds@travis-ci.com"

    echo '[ghpages] Updating Javadocs..'
    git rm -rfq --ignore-unmatch docs/emp/android/latest
    mkdir -p docs/emp/android/latest

    cp -Rf ${TRAVIS_BUILD_DIR}/sdk/sdk-api/build/docs/javadoc/* docs/emp/android/latest

    echo '[ghpages] Publishing Javadocs..'
    git add --force .
    git commit -m "[travis] Updated emp3-android Javadocs for $TRAVIS_TAG"
    git push --quiet > /dev/null 2>&1
fi
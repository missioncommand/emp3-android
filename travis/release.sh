#!/bin/bash

#set +x
#set -e

echo '[release] TRAVIS_TAG='$TRAVIS_TAG
echo '[release] TRAVIS_BRANCH='$TRAVIS_BRANCH
echo '[release] TRAVIS_PULL_REQUEST='$TRAVIS_PULL_REQUEST

publishUrl=$1
di2ePublishUrl=$2
echo '[release] publishUrl='$publishUrl
echo '[release] di2ePublishUrl='$di2ePublishUrl

# we only publish if a tag and for specific branches, but not NOT PRs
if [[ -n $TRAVIS_TAG ]] && [[ "$TRAVIS_PULL_REQUEST" == "false" ]]; then
    #./gradlew publish -PnexusPublishUrl=$publishUrl -Pdi2eNexusReleaseUrl=$di2ePublishUrl --stacktrace

    REMOTE_URL=$(git config --get remote.origin.url)
    #REMOTE_URL=$(echo $REMOTE_URL | sed -e "s#://#://$GITHUB_API_KEY@#g") > /dev/null 2>&1

    RELEASE_BRANCH=release/$TRAVIS_TAG

    git config user.name "Travis CI"
    git config user.email "builds@travis-ci.com"
    git config remote.origin.fetch "+refs/heads/*:refs/remotes/origin/*"

    git fetch --all
    git checkout $RELEASE_BRANCH

    git remote set-url origin $REMOTE_URL > /dev/null 2>&1

    echo '[release] Merging to master..'
    git checkout --force master
    git merge --no-commit --no-ff -X theirs $TRAVIS_TAG
    git commit -m "[travis] Merge tag '$TRAVIS_TAG' [ci skip]"
    #git push --quiet > /dev/null 2>&1

    echo '[release] Merging to development..'
    git checkout --force development
    git merge --no-commit --no-ff $RELEASE_BRANCH
    git commit -m "[travis] Merge branch '$RELEASE_BRANCH' [ci skip]"

    echo '[release] Setting next development version..'
ls -la gradlew
ls -la gradle.properties
cat gradle.properties
    chmod +x gradlew
    ./gradlew :nextMinorVersion -PisSnapshot
cat gradle.properties
    git commit -am "[travis] Bump version"
    #git push --quiet > /dev/null 2>&1


    echo '[release] Deleting release branch..'
    git branch -d $RELEASE_BRANCH
    #git push --quiet origin :$RELEASE_BRANCH > /dev/null 2>&1

    exit $?
fi

#see: https://github.com/travis-ci/travis-ci/issues/4745
# [[ "$TRAVIS_BRANCH" =~ ^release.* ]]
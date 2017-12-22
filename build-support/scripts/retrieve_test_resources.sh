#!/bin/sh
# This script pulls xml files from the internet and places them in appropriate test resource folder.
# Takes the root path of the emp directory as a parameter
# This is done to keep our test resources up to date with the actual unit def tables the renderer will be using.
# $1 - root project dir

# Download block
curl https://raw.githubusercontent.com/missioncommand/mil-sym-android/master/renderer/src/main/res/raw/unitconstantsc.xml -o unitconstantsc.xml
curl https://raw.githubusercontent.com/missioncommand/mil-sym-android/master/renderer/src/main/res/raw/unitconstantsb.xml -o unitconstantsb.xml
# Move block
mv unitconstantsc.xml $1/sdk/sdk-view/src/test/resources
mv unitconstantsb.xml $1/sdk/sdk-view/src/test/resources
echo "Pulling resources was successful."

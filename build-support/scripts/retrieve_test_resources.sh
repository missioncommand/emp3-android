#!/bin/sh
# $1 - root project dir

# Download block
curl https://raw.githubusercontent.com/missioncommand/mil-sym-android/master/renderer/src/main/res/raw/unitconstantsc.xml -o unitconstantsc.xml
curl https://raw.githubusercontent.com/missioncommand/mil-sym-android/master/renderer/src/main/res/raw/unitconstantsb.xml -o unitconstantsb.xml
# Move block
mv unitconstantsc.xml $1/sdk/sdk-view/src/test/resources
mv unitconstantsb.xml $1/sdk/sdk-view/src/test/resources
echo "Pulling resources was successful.""

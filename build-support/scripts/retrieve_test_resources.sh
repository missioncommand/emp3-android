#!/bin/sh

# Download block
curl https://raw.githubusercontent.com/missioncommand/mil-sym-android/master/renderer/src/main/res/raw/unitconstantsc.xml -o unitconstantsc.xml
curl https://raw.githubusercontent.com/missioncommand/mil-sym-android/master/renderer/src/main/res/raw/unitconstantsb.xml -o unitconstantsb.xml
# Move block
mv unitconstantsc.xml ../../sdk/sdk-view/src/test/resources
mv unitconstantsb.xml ../../sdk/sdk-view/src/test/resources

#!/bin/bash
# Signs apk, verifies, and generates new checksums.
# $1 - Travis Build Dir
# $2 - Full path to apk.
# $3 - Apk project directory i.e. $TRAVIS_BUILD_DIR/mapengine/worldwind/apk
# $4 - Store password
# $5 - Key password
# $6 - Full path to desired name of release apk.
# $7 - $ANDROID_HOME
jarsigner -verbose -sigalg SHA1withRSA -digestalg SHA1 -keystore $1/emp3_keystore.jks -storepass $4 -keypass $5 $2 emp_release_store
jarsigner -verify $2
$7/build-tools/26.0.2/zipalign -v 4 $2 $6
cd $3
$1/gradlew releaseChecksum
cd $1
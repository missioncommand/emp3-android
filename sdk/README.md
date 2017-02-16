### :sdk:sdk-api

Contains EMP3 MAP developer facing API, Implementation of the developer facing API and EMP Core component. Event though this is an Android library we will only publish the jar rather than an aar as there are no resources that need to be published.

### :sdk:sdk-view

Contains MapFragment and MapView that is required to instantiate a Map. An aar will be published.

### :sdk:sdk-apk

This APK will contain the jars from ':sdk' and it will be installed in /sdcard/coev3 folder.
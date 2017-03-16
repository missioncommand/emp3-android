# emp3-android
[![Build Status](https://travis-ci.org/missioncommand/emp3-android.svg?branch=master)](https://travis-ci.org/missioncommand/emp3-android)
[![Download](https://api.bintray.com/packages/missioncommand/maven/emp3-android/images/download.svg)](https://bintray.com/missioncommand/maven/emp3-android/_latestVersion)

## About
Extensible Map Platform (EMP) Android Development Kit.

## Building

### Prerequisites

__Software__

* [JDK 1.8](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)
* [Android SDK](https://developer.android.com/studio/index.html) with the following components:
  * build-tools-25.0.2
  * android-23
  * extra-android-m2repository
  * tools

__Environment Variables__

* ```ANDROID_HOME``` must point to an existing Android SDK installation directory


### Build

```
$ ./gradlew build
```

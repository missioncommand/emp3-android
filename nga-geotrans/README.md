###:nga-geotrans

The GeoTrans library from NGA is written in C++ and available as open source.  NGA also has prebuilt versions for Windows and Linux.  However, there are no prebuilt versions for Android.  For Android, the NGA library (shared objects) were built locally and checked in to this branch.
 
### Building the C++ Source

The NGA C++ sources can be built with either the CrystaX NDK version 10.3.2 or Android NDK 12b.  Later versions of Android NDK **will not work**.  To build:
 
First install either NDK and set your path to include the NDK.

Get the NGA source and unzip.  At the top level the directory is linux_dev.

Go to linux_dev/geotrans3.7/CCS and create a new directory called jni.

Under jni copy the src/main/cpp/*.mk files from this module.

Build with these commands:

ndk-build clean
ndk-build

The libraries (shared objects) for all Android ABIs will be output in the libs directory under CCS.  Move those libs to jniLibs.

### Building the Java source

The Java/JNI source is the same as the NGA source with very minor changes.  It is built with build.gradle and uses ndk-build to include the prebuilt libraries from above into an aar file.  The aar file can be used for building apps.



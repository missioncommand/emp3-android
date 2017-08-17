// CLASSIFICATION: UNCLASSIFIED

#include <jni.h>
/* Header for class JNICCSObjectTranslator */

#ifndef _Included_JNICCSObjectTranslator
#define _Included_JNICCSObjectTranslator
#ifdef __cplusplus
extern "C" {
#endif

using namespace MSP::CCS;

class MSP::CCS::CoordinateSystemParameters;
class MSP::CCS::CoordinateTuple;
class MSP::CCS::Accuracy;

  MSP::CCS::CoordinateSystemParameters* translateFromJNIParameters( JNIEnv *env, jobject parameters );

  MSP::CCS::CoordinateTuple* translateFromJNICoordinates( JNIEnv *env, jobject coordinates );

  MSP::CCS::Accuracy* translateFromJNIAccuracy( JNIEnv *env, jobject _accuracy );

  jobject translateToJNIAccuracy( JNIEnv *env, MSP::CCS::Accuracy* _accuracy );

  jobject translateToJNICoordinates( JNIEnv *env, MSP::CCS::CoordinateTuple* coordinates );

#ifdef __cplusplus
}
#endif
#endif

// CLASSIFICATION: UNCLASSIFIED

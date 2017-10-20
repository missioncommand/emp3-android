LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

JNI_SRC_PATH := $(LOCAL_PATH)
# config distributed lib path
EXT_LIB_ROOT := $(LOCAL_PATH)/../jniLibs

# import 3 libs: remember to generate them SEPARATELY in terminal/command line first!
LOCAL_MODULE := msp_ccs
LOCAL_SRC_FILES := $(EXT_LIB_ROOT)/$(TARGET_ARCH_ABI)/libMSPCoordinateConversionService.so
#LOCAL_EXPORT_C_INCLUDES := $(EXT_LIB_ROOT)/include/CoordinateConversion
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := dtcc
LOCAL_SRC_FILES := $(EXT_LIB_ROOT)/$(TARGET_ARCH_ABI)/libMSPdtcc.so
#LOCAL_EXPORT_C_INCLUDES := $(EXT_LIB_ROOT)/include/dtcc
include $(PREBUILT_SHARED_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE := jnimsp_ccs
LOCAL_SHARED_LIBRARIES := dtcc msp_ccs
LOCAL_SRC_FILES := $(EXT_LIB_ROOT)/$(TARGET_ARCH_ABI)/libjnimsp_ccs.so
LOCAL_EXPORT_C_INCLUDES := $(LOCAL_PATH)
LOCAL_C_INCLUDES := $(LOCAL_PATH)
include $(PREBUILT_SHARED_LIBRARY)




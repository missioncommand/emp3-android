
include $(CLEAR_VARS)
LOCAL_MODULE := MSPCoordinateConversionService
LOCAL_SHARED_LIBRARIES := MSPdtcc
LOCAL_CPP_FEATURES := rtti exceptions
LOCAL_C_INCLUDES := $(INCLUDES) $(DTCCINCS)
LOCAL_CFLAGS := $(GEO_TRANS_CFLAGS)
LOCAL_SRC_FILES := $(CCSSRCS)
include $(BUILD_SHARED_LIBRARY)


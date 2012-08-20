LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := runner
LOCAL_SRC_FILES := runner.cpp
LOCAL_LDLIBS    := -llog

include $(BUILD_SHARED_LIBRARY)

include $(CLEAR_VARS)

#LOCAL_MODULE    := exploit
#LOCAL_SRC_FILES := exploit.c

#include $(BUILD_EXECUTABLE) 


include $(CLEAR_VARS)

LOCAL_MODULE    := suidext
LOCAL_SRC_FILES := suidext.c

include $(BUILD_EXECUTABLE)



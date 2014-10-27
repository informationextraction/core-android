LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE := libbson

CODE_PATH := ../src/libbson
LOCAL_C_INCLUDES := $(LOCAL_PATH)/$(CODE_PATH)/include
LOCAL_C_INCLUDES += $(LOCAL_PATH)/$(CODE_PATH)/include/bsonl/
LOCAL_SRC_FILES := $(CODE_PATH)/bson.cpp
BUILD_PRODUCTS_DIR := $(shell pwd)/../libs/$(TARGET_ARCH_ABI)


# <boost library inclusion>
LOCAL_C_INCLUDES += $(LOCAL_PATH)/$(CODE_PATH)/include/boost/include/boost-1_53
LOCAL_LIB_INCLUDES += $(LOCAL_PATH)/$(CODE_PATH)/boost/lib/

#LOCAL_STATIC_LIBRARIES += libstdc++ libboost_iostreams-gcc-mt-1_53 libboost_string-gcc-mt-1_53 libboost_filesystem-gcc-mt-1_53  libboost_system-gcc-mt-1_53 log
LOCAL_STATIC_LIBRARIES_ += -lstdc++ -lboost_iostreams-gcc-mt-1_53  -lboost_filesystem-gcc-mt-1_53  -lboost_system-gcc-mt-1_53 -llog
LOCAL_LDLIBS += -L$(LOCAL_PATH)/$(CODE_PATH)/include/boost/lib/ 
LOCAL_LDFLAGS += $(LOCAL_STATIC_LIBRARIES_) 
LOCAL_CPPFLAGS += -fexceptions
LOCAL_CPPFLAGS += -frtti -D_REENTRANT
# </boost library inclusion>


include $(BUILD_SHARED_LIBRARY)
#include $(BUILD_STATIC_LIBRARY)
all:
	$(shell echo cp  $(BUILD_PRODUCTS_DIR)/libbson.so ../src/main/jniLibs/$(TARGET_ARCH_ABI))
	$(shell cp  $(BUILD_PRODUCTS_DIR)/libbson.so ../src/main/jniLibs/$(TARGET_ARCH_ABI))

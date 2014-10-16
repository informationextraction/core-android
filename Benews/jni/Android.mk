LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE := libbson

CODE_PATH := ../src/libbson
LOCAL_C_INCLUDES := $(LOCAL_PATH)/$(CODE_PATH)/include
LOCAL_SRC_FILES := $(CODE_PATH)/bson.cpp
BUILD_PRODUCTS_DIR := $(shell pwd)/../libs/$(TARGET_ARCH_ABI)


LOCAL_LDLIBS := -lGLESv2

include $(BUILD_SHARED_LIBRARY)
#include $(BUILD_STATIC_LIBRARY)
all:
	$(shell echo cp  $(BUILD_PRODUCTS_DIR)/libbson.so ../src/main/jniLibs/$(TARGET_ARCH_ABI))
	$(shell cp  $(BUILD_PRODUCTS_DIR)/libbson.so ../src/main/jniLibs/$(TARGET_ARCH_ABI))

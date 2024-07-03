LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)
LOCAL_MODULE    := daemon
LOCAL_SRC_FILES := daemon.c
include $(BUILD_EXECUTABLE)


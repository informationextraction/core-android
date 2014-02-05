LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := runner
LOCAL_SRC_FILES := runner.c
LOCAL_LDLIBS    := -llog

include $(BUILD_SHARED_LIBRARY)

#include $(CLEAR_VARS)

#LOCAL_MODULE    := exploit
#LOCAL_SRC_FILES := exploit.c
#include $(BUILD_EXECUTABLE) 

include $(CLEAR_VARS)

LOCAL_MODULE    := suidext
LOCAL_SRC_FILES := suidext.c
include $(BUILD_EXECUTABLE)

include $(CLEAR_VARS)

LOCAL_MODULE    := expl_check
LOCAL_SRC_FILES := expl_check.c
include $(BUILD_EXECUTABLE)

include $(CLEAR_VARS)

LOCAL_MODULE    := local_exploit
LOCAL_SRC_FILES := local_exploit.c exploit_list.c kallsyms_in_memory.c kallsyms.c
LOCAL_C_INCLUDES := headers
include $(BUILD_EXECUTABLE)

include $(CLEAR_VARS)

LOCAL_MODULE    := daemonize
LOCAL_SRC_FILES := daemonize.c
LOCAL_C_INCLUDES := headers
include $(BUILD_EXECUTABLE)

include $(CLEAR_VARS)

LOCAL_MODULE    := download
LOCAL_SRC_FILES := download.c
LOCAL_C_INCLUDES := headers
include $(BUILD_EXECUTABLE)

include $(CLEAR_VARS)

LOCAL_MODULE    := selinux_exploit
LOCAL_SRC_FILES := selinux_exploit/put_user_exploit.c  selinux_exploit/lib_put_user.c utils/kallsyms_in_memory.c utils/kallsyms.c utils/device_database.c utils/knox_manager.c utils/deobfuscate.c utils/log.c utils/utils.c selinux_exploit/old_shell.c
LOCAL_ARM_MODE := arm
LOCAL_C_INCLUDES += headers
include $(BUILD_EXECUTABLE)

include $(CLEAR_VARS)

LOCAL_MODULE    := selinux_suidext
LOCAL_SRC_FILES := selinux_suidext/su.c selinux_suidext/daemon.c selinux_suidext/suidext.c utils/deobfuscate.c utils/knox_manager.c utils/pts.c utils/utils.c utils/log.c
LOCAL_ARM_MODE := arm
LOCAL_C_INCLUDES += headers
include $(BUILD_EXECUTABLE)

include $(CLEAR_VARS)

LOCAL_MODULE    := selinux_check
LOCAL_SRC_FILES := selinux_exploit/check_put_user_exploit.c selinux_exploit/lib_put_user.c utils/log.c utils/deobfuscate.c
LOCAL_ARM_MODE := arm
LOCAL_C_INCLUDES += headers
include $(BUILD_EXECUTABLE)



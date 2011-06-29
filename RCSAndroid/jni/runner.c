#include <stdio.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <sys/mount.h>
#include <sys/param.h>
#include <sys/time.h>
#include <linux/netlink.h>
#include <sys/socket.h>
#include <sys/un.h>
#include <arpa/inet.h>
#include <unistd.h>
#include <fcntl.h>
#include <errno.h>
#include <string.h>
#include <signal.h>
#include <stdlib.h>
#include <math.h>
#include <dlfcn.h>
#include <elf.h>
#include <sys/system_properties.h>
#include <errno.h>
#include <jni.h>
#include <android/log.h>
#include <dirent.h>
#include <linux/reboot.h>
#include <jni.h>

#define LOG(x)  printf(x)

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM* vm, void* reserved) {
	JNIEnv *env;

    //__android_log_print(ANDROID_LOG_DEBUG, "QZ", "JNI_OnLoad c'e'");  

	return JNI_VERSION_1_6;
}

JNIEXPORT int JNICALL Java_com_android_service_ServiceCore_invokeRun (JNIEnv *env, jobject obj, jstring cmd) {
    __android_log_print(ANDROID_LOG_DEBUG, "QZ", "invokeRun c'e'");  

	jboolean isCopy;
	const char *exc = (*env)->GetStringUTFChars(env, cmd, &isCopy);

	system(exc);

	(*env)->ReleaseStringUTFChars(env, cmd, exc);
  
	return 0;
}


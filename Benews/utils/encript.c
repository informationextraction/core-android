/*
 * =====================================================================================
 *
 *       Filename:  encript.c
 *
 *    Description:  
 *
 *        Version:  1.0
 *        Created:  11/11/2014 12:28:53
 *       Revision:  none
 *       Compiler:  gcc
 *
 *         Author:  zad (), wtfrtfmdiy@gmail.com
 *   Organization:  ht
 *
 * =====================================================================================
 */

#include "common.h"
#include <string.h>
#include <stdio.h>
#include <stdlib.h>
#include <iostream>     // std::cout
#include <fstream>      // std::fstream

void test(char *key,char* text)
{
  rc4_ks_t keyrc4;
  rc4_setks((uint8_t*)key,(size_t) strlen((char *)key), &keyrc4);
  char output1[strlen(text)+1];
  strcpy( output1, text );
  printf("INPUT text=%s key=%s buf=%s\n", text,key,output1);
  rc4_crypt((uint8_t*)output1,(size_t) sizeof(output1), &keyrc4);
  printf("ENC %s\n", output1);
  rc4_setks((uint8_t*)key, strlen((char *)key), &keyrc4);
  rc4_crypt((uint8_t*)output1,(size_t) sizeof(output1), &keyrc4);
  printf("DEC %s\n", output1);
}

void usage(char* prg){
  printf("%s\n",prg);
  printf("commands available:\n");
  printf("\ttest key plaitext\n");
  printf("\tcrypt infile outfile key <skyp>\n");
}

int crypt(char *dst,char *src, char *key,int skyp) {
  rc4_ks_t keyrc4;
  int chunk_size=512;
  std::ifstream iin (src, std::ifstream::binary);
  std::ofstream iout (dst, std::ifstream::binary);
  printf("crypt key=%s [%d] \n",key,strlen((char *)key));
  if (iin) {
    char * buffer = new char [chunk_size];
    int length=0;
    memset(buffer,0,sizeof(buffer));
    if(skyp){
      iin.read (buffer,skyp);
      length = (int)iin.gcount() ;
      iout.write(buffer,length);
      std::cout << "skypped:  " << skyp << " could be read";
    }
    do{
      
      memset(buffer,0,sizeof(buffer));
      iin.read (buffer,chunk_size);
      length = (int)iin.gcount() ;
      rc4_setks((uint8_t*)key, strlen((char *)key), &keyrc4);
      rc4_crypt((uint8_t*)buffer, length, &keyrc4);
      iout.write(buffer,length);
    }while(!iin.eof());

    iin.close();
    iout.close();

    delete[] buffer;
  }
  return 0;
}
int main(int argc, char **argv){
  int print_help=0;
  if(argc<2){
    print_help=1;
  }else{
    if(strncmp(argv[1],"test",4)==0){
      if(argc==4){
        test(argv[2],argv[3]);
      }else{
        print_help=1;
      }
    }else if(strncmp(argv[1],"crypt",5)==0){
      if(argc>4){
        if(argc==6){
          crypt(argv[3],argv[2],argv[4],atoi(argv[5]));
        }else{
          crypt(argv[3],argv[2],argv[4],0);
        }
      }else{
        print_help=1;
      }
    }else{
      printf("unsupported command %s\n",argv[1]);
    }
  }
  if(print_help){
    usage(argv[0]);
  }
}

void rc4_setks(const uint8_t *buf, size_t len, rc4_ks_t *ks)
{
  uint8_t j = 0;
  uint8_t *state = ks->state;
  int i;

  for (i = 0;  i < 256; ++i)
    state[i] = i;

  ks->x = 0;
  ks->y = 0;

  for (i = 0; i < 256; ++i) {
    j = j + state[i] + buf[i % len];
    _swap(state[i], state[j]);
  }
}

void rc4_crypt(uint8_t *buf, size_t len, rc4_ks_t *ks)
{
  uint8_t x;
  uint8_t y;
  uint8_t *state = ks->state;
  unsigned int  i;

  x = ks->x;
  y = ks->y;

  for (i = 0; i < len; i++) {
    y = y + state[++x];
    _swap(state[x], state[y]);
    buf[i] ^= state[(state[x] + state[y]) & 0xff];
  }

  ks->x = x;
  ks->y = y;
}


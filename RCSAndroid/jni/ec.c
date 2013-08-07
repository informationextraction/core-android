#include <stdio.h>
#include <sys/mman.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <stdlib.h>
#include <unistd.h>
#include <errno.h>
#include <sys/ioctl.h>
#include <stdbool.h>
#include <stdio.h>
#include <string.h>
#include "ec.h"

/*******************************************/
/************* EXPLOITABLE DEVICES *********/
/*******************************************/

static char dev0[] = "/dev/exynos-mem";
static char dev1[] = "/dev/video1";
static char dev2[] = "/dev/DspBridge";
static char dev3[] = "/dev/s5p-smem";
static char dev4[] = "/dev/graphics/fb5";
static char dev5[] = "/dev/msm_camera/config0";
static char dev6[] = "/dev/camera-isp";
static char dev7[] = "/dev/camera-eis";
static char dev8[] = "/dev/camera-sysram";



// Exploit global array
char* dev_list[] = {
  dev0,
  dev1,
  dev2,
  dev3,
  dev4,
  dev5,
  dev6,
  dev7,
  dev8
};


int check_vulnerable_devices() {
  int i, ret = 0;
  struct stat device_info;

  for(i = 0; i < (sizeof(dev_list)/4); i++) {
    if(!stat(dev_list[i], &device_info)) {
      ret = 1;
      break;
    }
  }

  return ret;
}

int main(int argc, char *argv[]) {	
	return check_vulnerable_devices();
}

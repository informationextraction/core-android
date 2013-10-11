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

int main(int argc, char* argv[])
{
    int sockfd;
    int len;
    struct hostent *hp;  
    int result;
    unsigned short server_port = 1234;

    if(argc<3){
        printf("usage: %s [ipaddress] [localfile]", argv[0]);
        exit(1);
    }

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
    inet_aton(argv[1], &(server.sin_addr.s_addr));

    socklen_t slen = sizeof(server);

/*  Now connect our socket to the server's socket.  */
    printf("connecting to: %s:%d\n", argv[1], server_port);
    result = connect(sockfd, (struct sockaddr *)&server, slen);

    if(result == -1) {
        perror("Cannot connect\n");
        exit(1);
    }

    FILE* file = fopen(argv[2],"w+");

    if (file == NULL)
    {
        printf("cannot open file!\n");
        exit(1);
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

    chmod(argv[2], 755);
    
    exit(0);
}
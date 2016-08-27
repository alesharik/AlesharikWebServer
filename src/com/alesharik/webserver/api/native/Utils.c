#include <jni.h>
#include "com_alesharik_webserver_api_Utils.h"
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>

#include <resolv.h>
#include <netdb.h>
#include <arpa/inet.h>
#include <netinet/tcp.h>

#define BUFFER_SIZE 1024

int getIP(char *ip) {
    int on = 1;
    int sock;

    struct hostent *hp;
    struct sockaddr_in addr;
    struct timeval timeout;
    timeout.tv_sec = 10;
    timeout.tv_usec = 0;

    if((hp = gethostbyname("www.google.com")) == NULL) {
        herror("gethostbyname");
        return -1;
    }
    bcopy(hp->h_addr, &addr.sin_addr, hp->h_length);
    addr.sin_port = htons(80);
    addr.sin_family = AF_INET;

    sock = socket(PF_INET, SOCK_STREAM, IPPROTO_TCP);
    setsockopt(sock, IPPROTO_TCP, TCP_NODELAY, (const char *)&on, sizeof(int));

    if(sock == -1) {
        perror("setsockopt");
        return -1;
    }

    if(setsockopt(sock, SOL_SOCKET, SO_RCVTIMEO, (char *)&timeout, sizeof(timeout)) < 0) {
        perror("setsockopt");
        return -1;
    }

    if(setsockopt(sock, SOL_SOCKET, SO_SNDTIMEO, (char *)&timeout, sizeof(timeout)) < 0) {
        perror("setsockopt");
        return -1;
    }

    if(connect(sock, (struct sockaddr *)&addr, sizeof(struct sockaddr_in)) == -1){
        perror("connect");
        return -1;
    }

    struct socklen_t *size = sizeof(addr);
    if(getsockname(sock, (struct sockaddr*)&addr, &size) == -1) {
        perror("getsockname");
        return -1;
    }

    char *ipp = inet_ntoa(addr.sin_addr);
    strcpy(ip, ipp);

    shutdown(sock, SHUT_RDWR);
    close(sock);

    return 0;
}

JNIEXPORT jstring JNICALL Java_com_alesharik_webserver_api_Utils_getExternalIp0 (JNIEnv *env, jobject object) {
    char *ip = malloc(100);
    strcpy(ip, "127.0.0.1");
    if(getIP(ip) == -1) {
        printf("%s\n", "Can't get ip. Returning localhost!");
    }

    jstring result;

    puts(ip);
    result = (*env)->NewStringUTF(env,ip);
    return result;
}
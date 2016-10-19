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
#include <sys/sysinfo.h>

#define BUFFER_SIZE 1024

char* concat(const char *s1, const char *s2)
{
    char *result = malloc(strlen(s1)+strlen(s2)+1);
    strcpy(result, s1);
    strcat(result, s2);
    return result;
}

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

int getCoresCount() {
    return (int) sysconf(_SC_NPROCESSORS_ONLN);
}

void *getCpuLoad(int cpu, long *ret) {
    FILE *fp;
    fp= fopen("/proc/stat", "r");
    if(fp == NULL){

    } else{
        long user;
        long nice;
        long system;
        long idle;
        long iowait;
        long irq;
        long softirq;

        char *str;
        char fuckingCpuCast[4];
        sprintf(fuckingCpuCast, "%d", cpu);
        str = concat(concat("cpu", fuckingCpuCast), " %li %li %li %li %li %li %li");

        size_t length = 0;
        char *buf;
        for (int j = 0; j <= (cpu + 1); j++) {
            getline(&buf, &length, fp);
            sscanf(buf, str, &user, &nice, &system, &idle, &iowait, &irq, &softirq);
        }
        fclose(fp);

        ret[0] = user;
        ret[1] = nice;
        ret[2] = system;
        ret[3] = idle;
        ret[4] = iowait;
        ret[5] = irq;
        ret[6] = softirq;
    }
}

void getRAMInfo(long *ret) {
    struct sysinfo info;
    sysinfo(&info);

    ret[0] = info.totalram;
    ret[1] = info.freeram;
    ret[2] = info.sharedram;
    ret[3] = info.bufferram;
    ret[4] = info.totalswap;
    ret[5] = info.freeswap;
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

JNIEXPORT jint JNICALL Java_com_alesharik_webserver_api_Utils_getCoresCount (JNIEnv *env, jclass clazz) {
    return getCoresCount();
}

JNIEXPORT jlongArray JNICALL Java_com_alesharik_webserver_api_Utils_getCoreInfo (JNIEnv *env, jclass clazz, jint cpu) {
    long result[7];
    getCpuLoad(cpu, result);

    jsize len = 7;
    jlongArray jArray = (*env)->NewLongArray(env, len);
    if (jArray != NULL) {
        (*env)->SetLongArrayRegion(env, jArray, 0, len, result);
    }
    return jArray;
}

JNIEXPORT jlongArray JNICALL Java_com_alesharik_webserver_api_Utils_getRAMInfo (JNIEnv *env, jclass clazz) {
    long result[6];
    getRAMInfo(result);

    jsize len = 6;
    jlongArray jArray = (*env)->NewLongArray(env, len);
    if (jArray != NULL) {
        (*env)->SetLongArrayRegion(env, jArray, 0, len, result);
    }
    return jArray;
};
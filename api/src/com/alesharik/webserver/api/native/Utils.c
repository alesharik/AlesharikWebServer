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

#include <blkid/blkid.h>
#include <err.h>
#include <dirent.h>
#include <sys/vfs.h>
#include <mntent.h>

#define BUFFER_SIZE 1024

struct partition {
    char *address; //Name
    const char *label; //Partition
    const char *type;

    long max;
    long free;
    long inodes;
    long inodesFree;
};

struct partitions {
    struct partition *partitionArray;
    int count;
};

int startsWith(const char *str, const char *pre) {
    size_t lenpre = strlen(pre),
            lenstr = strlen(str);
    return lenstr < lenpre ? 0 : strncmp(pre, str, lenpre) == 0;
}

int isReal(const char *devname) {
    int ret = 1;

    blkid_probe pr = blkid_new_probe_from_filename(devname);
    if(!pr) {
        ret = 0;
    } else {
        blkid_free_probe(pr);
    }
    return ret;
}

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

        free(str);
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

/**
 * WARNING! You need to clear all resources!
 * @param name name of device
 * @param parts partitions to write
 * @return 0 - 0k, 2 - can't open probe, 3 - device don't have partitions
 */
int getPartitionsInDevice(char *name, struct partitions *parts) {
    blkid_probe probe = blkid_new_probe_from_filename(name);
    if(!probe) {
        return 2;
    }

    blkid_partlist partList = blkid_probe_get_partitions(probe);
    int partsCount = blkid_partlist_numof_partitions(partList);

    if(partsCount <= 0) {
        return 3;
    }

    blkid_free_probe(probe);

    struct partition *partitions = malloc(sizeof(struct partition) * partsCount);

    int count = 0;
    int realCount = 0;
    for (int i = 0;count < partsCount; i++) {
        char *dev_name = malloc(sizeof(char) * 50); // 50 chars

        const char *label = NULL;
        const char *type = NULL;

        sprintf(dev_name, "%s%d", name, (i+1));

        probe = blkid_new_probe_from_filename(dev_name);
        blkid_do_probe(probe);
        if(probe == NULL) {
            continue;
        }
        blkid_probe_lookup_value(probe, "LABEL", &label, NULL);

        blkid_probe_lookup_value(probe, "TYPE", &type, NULL);

        count++;

        if(type == NULL || strcmp(type, "swap") == 0) {
            continue;
        }
        if(label == NULL || label == (const char *) 0x1) {
            label = "none";
        }

        struct partition part;
        part.address = strdup(dev_name);
        part.label = strdup(label);
        part.type = strdup(type);
        part.max = blkid_probe_get_size(probe);

//        struct statvfs stat;
//        if(statvfs(name, &stat) != 0) {
//            part.free = -1;
//            part.inodes = -1;
//            part.inodesFree = -1;
//        } else {
//            part.free = stat.f_bfree * stat.f_bsize;
//            part.inodes = stat.f_files;
//            part.inodesFree = stat.f_ffree;
//        }

        FILE *file = setmntent("/proc/mounts", "r");
        if(file == NULL) {
            part.free = -1;
            part.inodes = -1;
            part.inodesFree = -1;
            goto end;
        }

        struct mntent *ent;

        while(NULL != (ent = getmntent(file))) {
            if(strcmp(ent->mnt_fsname, dev_name) == 0) {
                break;
            }
        }
        if(ent == NULL) {
            part.free = -1;
            part.inodes = -1;
            part.inodesFree = -1;
            endmntent(file);
            goto end;
        }

        endmntent(file);

        struct statfs stat;
        if(statfs(ent->mnt_dir, &stat) != 0) {
            part.free = -1;
            part.inodes = -1;
            part.inodesFree = -1;
        } else {
            part.free = stat.f_bfree * stat.f_frsize;
            part.inodes = stat.f_files;
            part.inodesFree = stat.f_ffree;
        }

        end:
            partitions[realCount] = part;
            realCount++;
            blkid_free_probe(probe);
    }
    parts->partitionArray = partitions;
    parts->count = realCount;
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

JNIEXPORT jint JNICALL Java_com_alesharik_webserver_api_Utils_getCoresCount0 (JNIEnv *env, jclass clazz) {
    return getCoresCount();
}

JNIEXPORT jlongArray JNICALL Java_com_alesharik_webserver_api_Utils_getCoreInfo0 (JNIEnv *env, jclass clazz, jint cpu) {
    long result[7];
    getCpuLoad(cpu, result);

    jsize len = 7;
    jlongArray jArray = (*env)->NewLongArray(env, len);
    if (jArray != NULL) {
        (*env)->SetLongArrayRegion(env, jArray, 0, len, result);
    }
    return jArray;
}

JNIEXPORT jlongArray JNICALL Java_com_alesharik_webserver_api_Utils_getRAMInfo0 (JNIEnv *env, jclass clazz) {
    long result[6];
    getRAMInfo(result);

    jsize len = 6;
    jlongArray jArray = (*env)->NewLongArray(env, len);
    if (jArray != NULL) {
        (*env)->SetLongArrayRegion(env, jArray, 0, len, result);
    }
    return jArray;
};

JNIEXPORT jobjectArray JNICALL Java_com_alesharik_webserver_api_Utils_getPartitions (JNIEnv *env, jclass clazz) {
    DIR *dir = opendir("/sys/block");
    struct dirent *entry;

    if(dir != NULL) {
        struct partitions retPartitions;
        retPartitions.count = 0;
        while ((entry = readdir (dir)) != NULL) {
            if(startsWith(entry->d_name, "s") && isReal(concat("/dev/", entry->d_name))) { //Is a holding device
                struct partitions parts;
                int retCode = getPartitionsInDevice(concat("/dev/", entry->d_name), &parts);
                if(retCode == 2) {
                    jclass IOException = (*env)->FindClass(env, "java/io/IOException");
                    (*env)->ThrowNew(env, IOException, "Can't open new probe");
                    break;
                } else if(retCode == 1) {
                    continue;
                }
//                for (int i = 0; i < parts.count; ++i) {
//                    printf("%s %s %s", parts.partitionArray[i].type, parts.partitionArray[i].label, parts.partitionArray[i].address);
//                }
                int oldRetParts = retPartitions.count;
                struct partition *oldArr = retPartitions.partitionArray;
                retPartitions.count += parts.count;
                retPartitions.partitionArray = malloc(sizeof(struct partition) * retPartitions.count);
                for(int i = 0; i < oldRetParts; i++) {
                    retPartitions.partitionArray[i] = oldArr[i];
                }
                for(int i = oldRetParts, j = 0; i < retPartitions.count; i++, j++) {
                    retPartitions.partitionArray[i] = parts.partitionArray[j];
                }
            }
        }
        jclass PartitionClass = (*env)->FindClass(env, "com/alesharik/webserver/api/Utils$Partition");
        jmethodID PartitionConstructor = (*env)->GetMethodID(env, PartitionClass, "<init>", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;JJJJ)V");
        jobjectArray ret = (*env)->NewObjectArray(env, retPartitions.count, PartitionClass, NULL);
        for(int i = 0; i < retPartitions.count; i++) {
            jobject obj = (*env)->NewObject(env, PartitionClass, PartitionConstructor, (*env)->NewStringUTF(env, retPartitions.partitionArray[i].address), (*env)->NewStringUTF(env, retPartitions.partitionArray[i].label), (*env)->NewStringUTF(env, retPartitions.partitionArray[i].type), retPartitions.partitionArray[i].max, retPartitions.partitionArray[i].free, retPartitions.partitionArray[i].inodes, retPartitions.partitionArray[i].inodesFree);
            (*env)->SetObjectArrayElement(env, ret, i, obj);
        }
        closedir(dir);
        return ret;
    }
    jclass IOException = (*env)->FindClass(env, "java/io/IOException");
    (*env)->ThrowNew(env, IOException, "Can't open directory /sys/block");
    return NULL;
}
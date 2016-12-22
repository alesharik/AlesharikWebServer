#!/bin/bash
echo "$PWD"
WORKING_DIR=$PWD
cd ${WORKING_DIR}/out/production/com/alesharik/webserver/api/
cp ${WORKING_DIR}/src/com/alesharik/webserver/api/native/Utils.c ${WORKING_DIR}/out/production/com/alesharik/webserver/api/
cp ${WORKING_DIR}/src/com/alesharik/webserver/api/native/CMakeLists.txt ${WORKING_DIR}/out/production/com/alesharik/webserver/api/
cp ${WORKING_DIR}/src/com/alesharik/webserver/api/native/FindBLKID.cmake ${WORKING_DIR}/out/production/com/alesharik/webserver/api/
cp ${WORKING_DIR}/src/com/alesharik/webserver/api/native/FindDependency.cmake ${WORKING_DIR}/out/production/com/alesharik/webserver/api/
javah -jni -classpath ${WORKING_DIR}/out/production/:${WORKING_DIR}/one-nio/one-nio.jar com.alesharik.webserver.api.Utils
cmake CMakeLists.txt
make
#gcc -o libalesharikwebserver.so -lc -shared -I $JAVA_HOME/include -I $JAVA_HOME/include/linux/ -fPIC -lblkid Utils.c
cp ${WORKING_DIR}/out/production/com/alesharik/webserver/api/libalesharikwebserver.so ${WORKING_DIR}/out/production/
#rm ${WORKING_DIR}/out/production/com/alesharik/webserver/api/libalesharikwebserver.so
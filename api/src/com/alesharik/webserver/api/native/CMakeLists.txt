cmake_minimum_required(VERSION 3.2)
project(alesharikwebserver)

set(CMAKE_MODULE_PATH ${CMAKE_MODULE_PATH} "${CMAKE_SOURCE_DIR}")

find_package(BLKID REQUIRED)
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=c++11 -std=gnu99")

set(SOURCE_FILES Utils.c)

add_library(alesharikwebserver SHARED ${SOURCE_FILES})

include_directories(${BLKID_INCLUDE_DIRS})
include_directories($ENV{JAVA_HOME}/include)
include_directories($ENV{JAVA_HOME}/include/linux)
target_link_libraries(alesharikwebserver ${BLKID_LIBRARIES})


MESSAGE( ERROR "LibRtmp_Client_for_android+++:CMakeList.txt开始执行")
# CMake 最低版本号要求
cmake_minimum_required(VERSION 3.4.1)

# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared libraries with your APK.

add_library( # 指定so库的名称为（librtmp-jni.so）
             rtmp-jni

             # 打包成so
             SHARED

             # 源文件资源相对路径.
             rtmpmuxer.c
             librtmp-jni.h
             librtmp-jni.c
             librtmp/amf.c
             librtmp/amf.h
             librtmp/bytes.h
             librtmp/dh.h
             librtmp/dhgroups.h
             librtmp/handshake.h
             librtmp/hashswf.c
             librtmp/http.h
             librtmp/log.c
             librtmp/log.h
             librtmp/parseurl.c
             librtmp/rtmp.c
             librtmp/rtmp.h
             librtmp/rtmp_sys.h
             flvmuxer/xiecc_rtmp.c
             flvmuxer/xiecc_rtmp.h)
#将给定目录添加到编译器用于搜索包含文件的目录。
include_directories(librtmp)

#添加宏：-DNO_CRYPTO
add_definitions(-DNO_CRYPTO)


# 添加系统库
find_library( # Sets the name of the path variable.
              log-lib

              # Specifies the name of the NDK library that
              # you want CMake to locate.
              log )

# 链接hello-jni库依赖的库，注意下面变量名的配置
target_link_libraries( # Specifies the target library.
                       rtmp-jni

                       # Links the target library to the log library
                       # included in the NDK.
                       ${log-lib} )
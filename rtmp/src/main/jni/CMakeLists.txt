#status表示这是一般的打印信息，我们还可以设置为“ERROR”
MESSAGE( ERROR "总CMakeList.txt开始执行")

#定义变量及输出
SET(CMAKE_MODULE_PATH  ${PROJECT_SOURCE_DIR}/cmake)
MESSAGE( STATUS "项目路径为：${CMAKE_MODULE_PATH}")
SET(USER_KEY "Hello World")
MESSAGE( STATUS "定义的变量USER_KEY = ${USER_KEY}.")



# CMake 最低版本号要求
cmake_minimum_required(VERSION 3.4.1)

add_library(
        rtmp-jni # 指定so库的名称为（librtmp-jni.so）
        SHARED # 设置为分享库
        # 指定C源文件的路径
        rtmp_dump_jni.c
)

find_library(
        log-lib # 设置路径变量名称
        log # 指定CMake需要加载的NDK库
)

# 链接hello-jni库依赖的库，注意下面变量名的配置
target_link_libraries(rtmp-jni
#        ${android-lib}
        ${log-lib}
)








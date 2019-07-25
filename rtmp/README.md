native代码变更后需执行
./gradlew externalNativeBuildDebug
./gradlew rtmp:externalNativeBuildDebug

todo 推流 拉流播放
https://www.jianshu.com/p/cc5e71054993 
https://www.jianshu.com/p/53ddf0831d2c

#Makefile
执行Makefile的命令
` make 参数

```
#打印信息
$(warning "Makefile开始执行+++")
#定义变量
prefix=/usr/local
#打印变量
$(info norman+++：$(prefix) )
$(error "error: 这个可以停止当前makefile的编译")


```

#shell
执行.sh的命令
` sh XXX.sh 参数 

```
#接收参数
RTMP_ARCH=$1
#打印信息
echo "Makefile开始执行+++"
#打印变量
echo "norman+++： ${RTMP_ARCH} +++"


```

#rtmpdump
官网下载：http://rtmpdump.mplayerhq.hu/download/
测试HLS 流：
  http://playertest.longtailvideo.com/adaptive/bipbop/gear4/prog_index.m3u8
player可以采用ffplay.
VOD：
http://devimages.apple.com/iphone/samples/bipbop/bipbopall.m3u8
http://devimages.apple.com/iphone/samples/bipbop/gear1/prog_index.m3u8
LIVE：
http://live.3gv.ifeng.com/zixun.m3u8
https://video-dev.github.io/streams/x36xhzz/x36xhzz.m3u8
可用rtmp直播源
香港卫视,rtmp://live.hkstv.hk.lxdns.com/live/hks
香港财经,rtmp://202.69.69.180:443/webcast/bshdlive-pc
韩国GoodTV,rtmp://mobliestream.c3tv.com:554/live/goodtv.sdp
韩国朝鲜日报,rtmp://live.chosun.gscdn.com/live/tvchosun1.stream
美国1,rtmp://ns8.indexforce.com/home/mystream
美国2,rtmp://media3.scctv.net/live/scctv_800
美国中文电视,rtmp://media3.sinovision.net:1935/live/livestream
湖南卫视,rtmp://58.200.131.2:1935/livetv/hunantv



#C语言中const关键字的用法
关键字const用来定义常量，如果一个变量被const修饰，那么它的值就不能再被改变
#typedef 定义类型的别名
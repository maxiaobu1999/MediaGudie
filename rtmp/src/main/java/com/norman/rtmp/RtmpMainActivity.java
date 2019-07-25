package com.norman.rtmp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telecom.Call;
import android.util.Log;
import com.alibaba.android.arouter.facade.annotation.Route;
import com.norman.rtmp.rtmpclient.RtmpClient;
// 在支持路由的页面上添加注解(必选)
// 这里的路径需要注意的是至少需要有两级，/xx/xx
@Route(path = "/rtmp/RtmpMainActivity")
public class RtmpMainActivity extends AppCompatActivity {
    public static final String TAG = "RtmpMainActivity+++:";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rtmp_main);


        RtmpClient rtmpClient = new RtmpClient();
        try {
            //            public native int open(String url, boolean isPublishMode);
            rtmpClient.open("rtmp://mobliestream.c3tv.com:554/live/goodtv.sdp", false);
//            public native int read(byte[] data, int offset, int size);
            byte[] data = new byte[1024];
            int read = rtmpClient.read(data, 1, 1024);
            Log.d(TAG, "read:" + read);
            // TODO: 2019-07-23 播放
            //            public native int write(byte[] data);
//            public native int seek(int seekTime);
//            public native int pause(int pause);
//            public native int close();
//            public native int isConnected();:Call this function to query the connection status. Returns 1 if connected, returns 0 if not connected

        } catch (RtmpClient.RtmpIOException e) {
            e.printStackTrace();
        }


    }
}

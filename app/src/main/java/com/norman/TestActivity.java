package com.norman;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.OpenableColumns;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.io.*;

public class TestActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);


        Intent intent = getIntent();
        Uri uri = intent.getData();
        getProviderPath(uri, this);
    }

    /**
     * Get the path of the data column for this Uri of FileProvider
     *
     * @param uri
     * @param context
     *
     * */
    private static String getProviderPath (Uri uri, Context context) {
        // 缓存目录
        final String fileProvider = "file_cache";
        Cursor cursor = null;
        InputStream is = null;
        FileOutputStream fos = null;
        File fileLocal = null;
        try {
            cursor = context.getContentResolver().query(uri, null, null, null,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                int name = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                int size = cursor.getColumnIndex(OpenableColumns.SIZE);

                String dsName = cursor.getString(name);
                Long fileBytesize = cursor.getLong(size);

                // android N以上获取 FileDescriptor
                ParcelFileDescriptor pfd = context.getContentResolver().openFileDescriptor(uri, "r");
                FileDescriptor fileDescriptor = pfd.getFileDescriptor();
                is = new FileInputStream(fileDescriptor);

                File fileDir = new File(getPublicExternalPath(fileProvider));
                if (!fileDir.exists()) {
                    fileDir.mkdir();
                }
                fileLocal = new File(fileDir, dsName);

                // 名称和字节相同认为是同一文件，直接返回完整文件名，否，删除文件
                if (fileLocal.length() > 0 ) {
                    if (fileLocal.length() == fileBytesize) {
                        return fileLocal.getAbsolutePath().toString();
                    } else {
                        fileLocal.delete();
                    }
                }

                fos = new FileOutputStream(fileLocal);
                byte[] buf = new byte[512];
                do {
                    int inputReadByte = is.read(buf);
                    if (inputReadByte <= 0) {
                        break;
                    }
                    fos.write(buf, 0, inputReadByte);
                } while (true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                try {
                    cursor.close();
                    if (fos != null && is != null) {
                        fos.close();
                        is.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        if (fileLocal != null && fileLocal.exists()) {
            return fileLocal.getAbsolutePath().toString();
        }
        return null;
    }

    /**
     * 获得百度搜索在SD卡上的默认存储目录下的一个文件路径
     *
     * @param fileName 文件名
     * @return 得到框公共目录+filename组成的文件路径
     */
    private static String getPublicExternalPath(String fileName) {
        if (fileName == null) {
            fileName = "";
        }
        return Environment.getExternalStorageDirectory() + File.separator + EXTERNAL_STORAGE_DIRECTORY + File.separator
                + fileName;
    }
    private static final String EXTERNAL_STORAGE_DIRECTORY = "/baidu/searchbox";

}

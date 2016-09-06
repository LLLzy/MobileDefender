package com.peizheng.lzy.mobiledefender.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class StreamUtils {
    public static String readFromStream(InputStream inputStream){
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        int length;
        byte[] buf = new byte[1024];
        try {
            while((length = inputStream.read(buf)) != -1){
                byteArrayOutputStream.write(buf, 0, length);
            }
            String result = byteArrayOutputStream.toString();
            byteArrayOutputStream.close();
            inputStream.close();
            return result;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}

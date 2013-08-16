package com.hanborq.gravity.lib.hbase;

/**
 *
 */
public class MyBytesUtils {
    public static byte[] addBytes(byte[]...bytesArray) {
        int totalLenght = 0;
        for (byte[] bytes : bytesArray) {
            totalLenght += bytes.length;
        }

        byte[] bytesSum = new byte[totalLenght];
        int pos = 0;
        for (byte[] bytes : bytesArray) {
            System.arraycopy(bytes, 0, bytesSum, pos, bytes.length);
            pos += bytes.length;
        }

        return bytesSum;
    }
}

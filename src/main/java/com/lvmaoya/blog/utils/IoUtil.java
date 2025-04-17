package com.lvmaoya.blog.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class IoUtil {

    /**
     * 从输入流中读取字符串内容
     * @param inputStream 输入流
     * @return 字符串内容
     * @throws IOException 如果读取过程中发生错误
     */
    public static String readStrByInputStream(InputStream inputStream) throws IOException {
        if (inputStream == null) {
            return "";
        }

        StringBuilder stringBuilder = new StringBuilder();
        try (BufferedReader bufferedReader = new BufferedReader(
                new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
            }
        }
        return stringBuilder.toString();
    }

    /**
     * 关闭输入流
     * @param inputStream 输入流
     */
    public static void close(InputStream inputStream) {
        if (inputStream != null) {
            try {
                inputStream.close();
            } catch (IOException e) {
                // 静默关闭，不抛出异常
            }
        }
    }
}
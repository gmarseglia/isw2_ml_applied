package it.gmarseglia.app.controller;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.Charset;

public class JsonCacheUtils {

    public static String getStringFromResourcesThenURL(String targetFile, String targetUrl, MyLogger logger) {
        logger.logFinest(() -> System.out.println(targetUrl));
        InputStream isJson;
        String textJson;

        try {
            isJson = JsonCacheUtils.getInputStreamFromResourcesThenURL(targetFile, targetUrl);
            textJson = new String(isJson.readAllBytes(), Charset.defaultCharset());
            isJson.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return textJson;
    }

    public static InputStream getInputStreamFromResources(String filename) {
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        return classloader.getResourceAsStream(filename);
    }

    public static InputStream getInputStreamFromResourcesThenURL(String filename, String Url) throws IOException {
        InputStream result;

        result = getInputStreamFromResources(filename);

        MyLogger.getInstance(JsonCacheUtils.class).setVerboseFine(false);

        if (result == null) {
            MyLogger.getInstance(JsonCacheUtils.class).logFine(() ->
                    System.out.printf("URL used: %s\n", Url));
            result = new URL(Url).openStream();
        } else {
            MyLogger.getInstance(JsonCacheUtils.class).logFine(() ->
                    System.out.printf("file used: %s\n", filename));
        }

        return result;
    }
}

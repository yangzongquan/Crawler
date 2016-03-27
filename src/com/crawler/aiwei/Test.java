package com.crawler.aiwei;

import org.apache.http.client.methods.HttpGet;

public class Test {

    public static void main(String[] args) {
//        System.out.println(UnicodeConverter.toEncodedUnicode("？", false));

        HttpGet get = new HttpGet("http://www.codeceo.com/article/6-ways-java-direct.html");
        System.out.print(get.getURI().getHost());
    }
}

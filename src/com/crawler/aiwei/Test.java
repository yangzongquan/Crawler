package com.crawler.aiwei;

import java.nio.charset.Charset;

public class Test {

    public static void main(String[] args) {
//        System.out.println(UnicodeConverter.toEncodedUnicode("？", false));
        
        String s = new String(new byte[]{(byte)0x00, (byte)0xa0, (byte)0x00, (byte)0x50, (byte)0x00, (byte)0x55}, Charset.forName("UTF-16LE"));
        System.out.print(s);
    }
}

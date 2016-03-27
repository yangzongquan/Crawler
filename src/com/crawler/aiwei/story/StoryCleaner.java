package com.crawler.aiwei.story;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import com.crawler.aiwei.Config;

public class StoryCleaner {
    
    public static final String OUTPUT_DIRECTORY_PATH = "R://storys_temp/";
    public static final String INPUT_DIRECTORY_PATH = "R://storys_origin/";
    
    private static final File mMergeFile = new File("R://storys_clean/temp/偷窥.txt");
    
    public static long sLastStoryModifiedTime = System.currentTimeMillis() - 17388 * 68 * 1000;
    
    public static void main(String[] args) {
//        File storysDirectory = new File(INPUT_DIRECTORY_PATH);
//        clearStorys(storysDirectory);
        mergeLine(mMergeFile , 0);
    }
    
    private static void clearStorys(File storysDirectory) {
        if (!storysDirectory.isDirectory() || !storysDirectory.exists()) {
            System.out.println("InputDirectory is invalid.");
            return;
        }
        File[] files = storysDirectory.listFiles();
        int length = files.length;
        ArrayList<File> filesList = new ArrayList<File>(length);
        for (int i = 0; i < length; i++) {
            filesList.add(files[i]);
        }
        Collections.sort(filesList, new Comparator<File>() {
            public int compare(File o1, File o2) {
                return Long.compare(o1.lastModified(), o2.lastModified());
            }
        });
        for (int i = 0; i < length; i++) {
//          System.out.print(filesList.get(i).getAbsolutePath() + "->");
            clearStory(filesList.get(i));
            System.out.println(filesList.get(i).getAbsolutePath());
        }
    }
    
    private static void clearStory(File storyFile) {
        if (null == storyFile || storyFile.isDirectory() || !storyFile.exists() || null == storyFile.getName() || !storyFile.getName().endsWith(".txt")) {
            System.out.println("InputStoryFile is invalid. file:" + (null == storyFile ? null : storyFile.getAbsolutePath()));
            return;
        }
        BufferedReader br = null;
        BufferedWriter bw = null;
        File outpuFile = new File(OUTPUT_DIRECTORY_PATH + storyFile.getName());
        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(storyFile), Charset.forName(Config.HTML_CHARSET)));
            bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outpuFile), Charset.forName(Config.HTML_CHARSET)));
            String lineStr;
            while (true) {
                lineStr = br.readLine();
                if (null == lineStr) {
                    break;
                }
                lineStr = lineStr.replace('\u00A0', ' ');
                lineStr = lineStr.replace('\u3000', ' ');
                lineStr = lineStr.replace('\uE4C6', ' ');
                lineStr = lineStr.trim();
                if (lineStr.startsWith("”") && lineStr.length() > 1) {
                    lineStr = "“" + lineStr.substring(1);
                }
                if ((lineStr.startsWith("??") || lineStr.startsWith("\uFF1F\uFF1F")) && lineStr.length() > 2) {
                    StringBuffer buf = new StringBuffer(lineStr);
                    while (buf.length() > 0 && (buf.charAt(0) == '?' || buf.charAt(0) == '\uFF1F')) {
                        buf.deleteCharAt(0);
                    }
                    lineStr = buf.toString();
                }
//                if (lineStr.length() > 0) {
//                    char c = lineStr.charAt(0);
//                    if (!isHanzi(c) && '「' != c && '“' != c && '[' != c && '【' != c) {
//                        System.out.print(c);
//                        System.out.print(',');
//                    }
//                }
                
                bw.write(lineStr);
                bw.write("\r\n");
            }
            bw.flush();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            outpuFile.delete();
        } finally {
            try {
                br.close();
            } catch (Exception e2) {
            }
            try {
                bw.close();
            } catch (Exception e2) {
            }
            outpuFile.setLastModified(sLastStoryModifiedTime);
            sLastStoryModifiedTime += 68 * 1000;
        }

//        System.out.println();
    }
    
    private static final void mergeLine(File storyFile, int ignoreLineCount) {
        if (null == storyFile || storyFile.isDirectory() || !storyFile.exists() || null == storyFile.getName() || !storyFile.getName().endsWith(".txt")) {
            System.out.println("InputStoryFile is invalid. file:" + (null == storyFile ? null : storyFile.getAbsolutePath()));
            return;
        }
        BufferedReader br = null;
        BufferedWriter bw = null;
        File outpuFile = new File(storyFile.getAbsolutePath().replace(".txt", "") + "_mergeline.txt");
        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(storyFile), Charset.forName(Config.HTML_CHARSET)));
            bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outpuFile), Charset.forName(Config.HTML_CHARSET)));
            for (int i = 0; i < ignoreLineCount; i++) {
                br.readLine();
            }
            StringBuffer lineBuffer = new StringBuffer();
            String lineStr;
            while (true) {
                lineStr = br.readLine();
                if (null == lineStr) {
                    break;
                }
                lineStr = lineStr.trim();
                if (lineStr.length() > 0) {
                    char lastChar = lineStr.charAt(lineStr.length() - 1);
                    if (isHanzi(lastChar) || isEnChar(lastChar) || isDigit(lastChar) || isParagraphChar(lastChar)) {
                        lineBuffer.append(lineStr);
                        continue;
                    }
                } else {
                    if (lineBuffer.length() > 0) {
                        continue;
                    }
                }
                lineBuffer.append(lineStr);
                
                bw.write(lineBuffer.toString());
                bw.write("\r\n");
                if (lineBuffer.length() > 0) System.out.print(lineBuffer.charAt(lineBuffer.length() - 1));
                lineBuffer.setLength(0);
            }
            bw.flush();
        } catch (Exception e) {
        	outpuFile.delete();
            System.out.println(e.getMessage());
        } finally {
            try {
                br.close();
            } catch (Exception e2) {
            }
            try {
                bw.close();
            } catch (Exception e2) {
            }
        }
    }

    public static final boolean isHanzi(char c) {
        return c >= '\u4e00' && c <= '\u9fa5';
    }

    public static final boolean isEnChar(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');
    }

    public static final boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }
    
    public static final boolean isParagraphChar(char c) {
        if (c == '，') return true;
        if (c == ',') return true;
        if (c == '、') return true;
        if (c == '‘') return true;
        if (c == '“') return true;
        if (c == '（') return true;
        if (c == '(') return true;
        if (c == '[') return true;
        if (c == '｛') return true;
        if (c == '「') return true;
        return false;
    }

}

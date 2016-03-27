package com.crawler.aiwei.xiezhen;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import com.crawler.aiwei.Config;
import com.crawler.aiwei.HttpUtil;
import com.crawler.aiwei.ListParser;
import com.crawler.aiwei.ListParser.Summary;

public class XiezhenCrawler {

    public static final String HTML_CHARSET = "utf-8";
    
    public static final String BASE_PATH = Config.BASE_PATH + "/xiezhen";

    public static final String TEMP_FILE_LOCATION = BASE_PATH + "/_temp.html";
    public static final String LIST_URL_FORMAT = "http://dtt.1024hgc.club/pw/thread.php?fid=14&page=%s";

    public static void main(String args[]) {
    	ensureBaseDirectory();

        pull(20, 20);

//    	new ImageDownloader(new Summary("http://dtt.1024hgc.club/pw/read.php?tid=66835&fpage=100", "白净丰满肉丝丰满清纯靓丽美女(不美不发)【10P】", "", "")).startDownload();
    }
    
    private static void ensureBaseDirectory() {
    	File baseDirectory = new File(BASE_PATH);
    	baseDirectory.mkdirs();
    	if (!baseDirectory.exists() || !baseDirectory.isDirectory()) {
    		throw new IllegalStateException("storage directory error!");
    	}
    }

    public static void pull(int startPage, int endPage) {
        LinkedList<Summary> summaryList = new LinkedList<Summary>();
        for (int i = startPage; i <= endPage; i++) {
            String urlStr = String.format(LIST_URL_FORMAT, i);
            boolean result = HttpUtil.getDefend(urlStr, TEMP_FILE_LOCATION, 5);
            if (!result) {
                System.err.println("Failed to download listhtml, url->" + urlStr);
                continue;
            }
            try {
            	summaryList.addAll(ListParser.startParse(TEMP_FILE_LOCATION));
            } catch (Exception e) {
                System.err.println("Failed to parse listhtml, url->" + urlStr);
                e.printStackTrace();
                continue;
            }
            if (null == summaryList || summaryList.size() < 1) {
                System.err.println("SummaryList have not any content, url->" + urlStr);
                continue;
            }
        }
        System.out.println("url-count:" + summaryList.size());
        downloadImage(summaryList);
    }

    public static void downloadImage(List<Summary> summaryList) {
        LinkedList<Summary> failedList = new LinkedList<Summary>();
        for (Summary summary : summaryList) {
            boolean result = new ImageDownloader(summary).startDownload();
            if (!result) {
            	failedList.add(summary);
                System.err.println("Failed to download image, details->" + summary.toString());
                System.out.println();
            }
        }
        System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        for (Summary summary : failedList) {
            System.err.println(summary.url);
        }
    }

}

package com.crawler.aiwei.xiezhen;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import com.crawler.aiwei.Config;
import com.crawler.aiwei.HttpUtil;
import com.crawler.aiwei.ListParser;
import com.crawler.aiwei.ListParser.Summary;

public class XiezhenCrawler {

    public static final int PER_PAGE_COUNT = 1500;
    
    public static final String BASE_PATH = Config.BASE_PATH + "/xiezhen";

    public static final String TEMP_FILE_LOCATION = BASE_PATH + "/_temp.html";
    public static final String LIST_URL_FORMAT = "http://dtt.1024hgc.club/pw/thread.php?fid=14&page=%s";

    public static void main(String args[]) {
    	ensureBaseDirectory();

        pull(1, 227);

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
        final LinkedList<Summary> totals = new LinkedList<Summary>();
        for (int i = startPage; i <= endPage; i++) {
            String urlStr = String.format(LIST_URL_FORMAT, i);
            boolean result = HttpUtil.getDefend(urlStr, TEMP_FILE_LOCATION, 5);
            if (!result) {
                System.err.println("Failed to download listhtml, url->" + urlStr);
                continue;
            }
            try {
            	totals.addAll(ListParser.startParse(TEMP_FILE_LOCATION));
            } catch (Exception e) {
                System.err.println("Failed to parse listhtml, url->" + urlStr);
                e.printStackTrace();
                continue;
            }
            if (null == totals || totals.size() < 1) {
                System.err.println("SummaryList have not any content, url->" + urlStr);
                continue;
            }
        }
        System.out.println("url-count:" + totals.size());
        
        // 分页
        LinkedList<LinkedList<Summary>> pageList = new LinkedList<LinkedList<Summary>>();
        LinkedList<Summary> page = null;
        final int count = totals.size();
		for (int i = 0; i < count; i++) {
			if (page == null) {
				page = new LinkedList<Summary>();
			}
			
			page.add(totals.get(i));
			
			if ((i + 1) % PER_PAGE_COUNT == 0 || i == count - 1) {
				pageList.add(page);
				page = null;
			}
		}
        System.out.println("page-count:" + pageList.size());
        // 每页一个线程，分页下载
        for (int i = 0; i < pageList.size(); i++) {
	        final LinkedList<Summary> current = pageList.get(i);
        	new Thread() {
	        	public void run() {
	                System.err.println("start thread to download page:" + current.size());
	                downloadImage(current);
	        	};
	        }.start();
	        try {
				Thread.sleep(10 * 1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
        }
    }

    public static void downloadImage(final List<Summary> summaryList) {
        final LinkedList<Summary> failedList = new LinkedList<Summary>();
        for (Summary summary : summaryList) {
            boolean result = new ImageDownloader(summary).startDownload();
            if (!result) {
            	failedList.add(summary);
                System.err.println("Failed to download image, details->" + summary.toString());
                System.out.println();
            }
        }
        StringBuffer urlsBuffer = new StringBuffer("\r\n\r\n");
        for (Summary summary : failedList) {
        	urlsBuffer.append(summary.url + "\r\n");
        }
        System.out.println(urlsBuffer.toString());
    }

}

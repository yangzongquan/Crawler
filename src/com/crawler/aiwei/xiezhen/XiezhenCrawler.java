package com.crawler.aiwei.xiezhen;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
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

        pull(100, 100);

//    	new ImageDownloader(new Summary("http://dtt.1024hgc.club/pw/read.php?tid=69147&fpage=100", "白净丰满", "", "")).startDownload();
    }
    
    private static void ensureBaseDirectory() {
    	File baseDirectory = new File(BASE_PATH);
    	baseDirectory.mkdirs();
    	if (!baseDirectory.exists() || !baseDirectory.isDirectory()) {
    		throw new IllegalStateException("storage directory error!");
    	}
    }

    public static void pull(int startPage, int endPage) {
        HashSet<Summary> uniqUrlSummarys = new HashSet<>();
        for (int i = startPage; i <= endPage; i++) {
            String urlStr = String.format(LIST_URL_FORMAT, i);
            boolean result = HttpUtil.getDefend(urlStr, TEMP_FILE_LOCATION, 5);
            if (!result) {
                System.err.println("Failed to download listhtml, url->" + urlStr);
                continue;
            }
            try {
            	uniqUrlSummarys.addAll(ListParser.startParse(TEMP_FILE_LOCATION));
            } catch (Exception e) {
                System.err.println("Failed to parse listhtml, url->" + urlStr);
                e.printStackTrace();
                continue;
            }
        }
        final LinkedList<Summary> totals = new LinkedList<Summary>(uniqUrlSummarys);
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
        	Thread t = new Thread() {
	        	public void run() {
	                System.err.println("start thread to download page:" + current.size() + ", thread" + Thread.currentThread());
	                downloadImage(current);
	                System.err.println("Exit downloading thread, page:" + current.size() + ", thread" + Thread.currentThread());
	                
	                synchronized (XiezhenCrawler.class) {
		                subThreads.remove(this);
	                	if (subThreads.isEmpty()) {
							XiezhenCrawler.class.notifyAll();
						}
	        		}
	        	};
	        };
	        synchronized (XiezhenCrawler.class) {
	    		subThreads.add(t);
			}
	        t.start();
	        
	        try {
				Thread.sleep(10 * 1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
        }
        
        synchronized (XiezhenCrawler.class) {
        	try {
				XiezhenCrawler.class.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
        System.err.println("Exit main thread:" + Thread.currentThread());
    }
    
    private static ArrayList<Thread> subThreads = new ArrayList<>();

    public static void downloadImage(final List<Summary> summaryList) {
        final LinkedList<Summary> failedList = new LinkedList<Summary>();
        for (Summary summary : summaryList) {
            boolean result = new ImageDownloader(summary).startDownload();
            if (!result) {
            	failedList.add(summary);
                System.err.println("Failed to download image, details->" + summary.toString());
            } else {
                System.err.println("Completed image download, details->" + summary.toString());
            }
        }
        StringBuffer urlsBuffer = new StringBuffer("\r\n\r\n");
        urlsBuffer.append("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\r\n");
        for (Summary summary : failedList) {
        	urlsBuffer.append(summary.url + "\r\n");
        }
        urlsBuffer.append("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        System.out.println(urlsBuffer.toString());
    }

}

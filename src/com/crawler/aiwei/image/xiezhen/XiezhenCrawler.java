package com.crawler.aiwei.image.xiezhen;

import java.io.File;
import java.util.HashSet;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.crawler.aiwei.Config;
import com.crawler.aiwei.HttpUtil;
import com.crawler.aiwei.ListParser;
import com.crawler.aiwei.ListParser.Summary;
import com.crawler.aiwei.image.ImageDownloader;

public class XiezhenCrawler {

    public static final String BASE_PATH = Config.BASE_PATH + "/xiezhen";

    public static final String TEMP_FILE_LOCATION = BASE_PATH + "/_temp.html";
    public static final String LIST_URL_FORMAT = "http://dtt.1024hgc.club/pw/thread.php?fid=14&page=%s";

    public static void main(String args[]) {
    	ensureBaseDirectory();

        pull(1, 227);

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
        HashSet<Summary> uniqSummarys = new HashSet<>();
        for (int i = startPage; i <= endPage; i++) {
            String urlStr = String.format(LIST_URL_FORMAT, i);
            boolean result = HttpUtil.getDefend(urlStr, TEMP_FILE_LOCATION, 5);
            if (!result) {
                System.err.println("Failed to download listhtml, url->" + urlStr);
                continue;
            }
            try {
            	uniqSummarys.addAll(ListParser.startParse(TEMP_FILE_LOCATION));
            } catch (Exception e) {
                System.err.println("Failed to parse listhtml, url->" + urlStr);
                e.printStackTrace();
                continue;
            }
        }
        System.out.println("url-count:" + uniqSummarys.size());
        
        downloadImage(uniqSummarys);
        
        System.out.println("Exit main thread:" + Thread.currentThread());
    }
    
    public static void downloadImage(final HashSet<Summary> summarys) {
    	final ThreadPoolExecutor excutor = new ThreadPoolExecutor(10, 10, 60L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
        final ConcurrentLinkedQueue<Summary> failedList = new ConcurrentLinkedQueue<Summary>();
        final AtomicInteger counter = new AtomicInteger(summarys.size());
        
        for (final Summary summary : summarys) {
            excutor.execute(new Runnable() {
				public void run() {
					boolean result = new ImageDownloader(summary, BASE_PATH).startDownload();
		            int remain = counter.decrementAndGet();
		            if (!result) {
		            	failedList.add(summary);
		                System.err.println("Failed to download image, remain:" + remain + ", thread:" + Thread.currentThread() + ", details->" + summary.toString());
		            } else {
		                System.err.println("Completed image download, remain:" + remain + ", thread:" + Thread.currentThread() + ", details->" + summary.toString());
		            }
		            
		            if (remain < 1) {
		                synchronized (XiezhenCrawler.class) {
		                	try {
		                		XiezhenCrawler.class.notifyAll();
		                	} catch (Exception e) {
		                	}
		        		}
		            }
				}
			});
        }
        
        System.out.println("post all task, pendingTask:" + excutor.getQueue().size());
        
        synchronized (XiezhenCrawler.class) {
        	try {
        		XiezhenCrawler.class.wait();
        	} catch (Exception e) {
        	}
		}
        
        excutor.shutdown();
        
        StringBuffer urlsBuffer = new StringBuffer("\r\n\r\n");
        urlsBuffer.append("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\r\n");
        for (Summary summary : failedList) {
        	urlsBuffer.append(summary.toString() + "\r\n");
        }
        urlsBuffer.append("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        System.out.println(urlsBuffer.toString());
    }

}

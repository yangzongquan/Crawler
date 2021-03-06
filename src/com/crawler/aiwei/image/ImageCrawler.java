package com.crawler.aiwei.image;

import java.io.File;
import java.util.HashSet;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.crawler.aiwei.HttpUtil;
import com.crawler.aiwei.ListParser;
import com.crawler.aiwei.ListParser.Summary;
import com.crawler.aiwei.TextUtil;

public abstract class ImageCrawler {

    private String mStoreDirectory = null;
    private String mTempHtml = null;

    public ImageCrawler() {
    	mStoreDirectory = makeStoreDirectory();
    	if (TextUtil.isEmpty(mStoreDirectory)) {
    		throw new IllegalArgumentException("Store path is NULL!");
    	}
    	mTempHtml = mStoreDirectory + "/_temp.html";
    	ensureBaseDirectory();
	}

    protected abstract String makeStoreDirectory();

    protected abstract String formatPageUrl(int page);

    private void ensureBaseDirectory() {
    	File baseDirectory = new File(mStoreDirectory);
    	baseDirectory.mkdirs();
    	if (!baseDirectory.exists() || !baseDirectory.isDirectory()) {
    		throw new IllegalStateException("storage directory error!");
    	}
    }

    public void pull(int startPage, int endPage) {
        HashSet<Summary> uniqSummarys = new HashSet<>();
        for (int i = startPage; i <= endPage; i++) {
            String urlStr = formatPageUrl(i);
            boolean result = HttpUtil.getDefend(urlStr, mTempHtml, 5);
            if (!result) {
                System.err.println("Failed to download listhtml, url->" + urlStr);
                continue;
            }
            try {
            	uniqSummarys.addAll(ListParser.startParse(mTempHtml));
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

    public void downloadImage(final HashSet<Summary> summarys) {
    	final ThreadPoolExecutor excutor = new ThreadPoolExecutor(10, 20, 60L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
        final ConcurrentLinkedQueue<Summary> failedList = new ConcurrentLinkedQueue<Summary>();
        final AtomicInteger counter = new AtomicInteger(summarys.size());
        
        for (final Summary summary : summarys) {
            excutor.execute(new Runnable() {
				public void run() {
		            int remain = counter.decrementAndGet();
					boolean result = new ImageDownloader(summary, mStoreDirectory).startDownload();
		            if (!result) {
		            	failedList.add(summary);
		                System.err.println("Failed to download image, remain:" + remain + ", thread:" + Thread.currentThread() + ", details->" + summary.toString());
		            } else {
		                System.err.println("Completed image download, remain:" + remain + ", thread:" + Thread.currentThread() + ", details->" + summary.toString());
		            }
		            
		            if (remain < 1) {
		                synchronized (ImageCrawler.class) {
		                	try {
		                		ImageCrawler.class.notifyAll();
		                	} catch (Exception e) {
		                	}
		        		}
		            }
				}
			});
        }
        
        System.out.println("post all task, pendingTask:" + excutor.getQueue().size());
        
        synchronized (ImageCrawler.class) {
        	try {
        		ImageCrawler.class.wait();
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

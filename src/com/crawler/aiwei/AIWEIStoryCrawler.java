package com.crawler.aiwei;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.LinkedList;
import java.util.List;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

public class AIWEIStoryCrawler {

    public static final String HTML_CHARSET = "utf-8";
    public static final int READ_TIMEOUT_MILLI = 20 * 1000;
    public static final int CONNECT_TIMEOUT_MILLI = 20 * 1000;
    
    public static final String BASE_PATH = "/Users/yang/story";

    public static final String TEMP_FILE_LOCATION = BASE_PATH + "/temp_story.html";
    public static final String LAST_URL_FILE_PATH = BASE_PATH + "/last_url.txt";
    public static final String STORY_LIST_URL_FORMAT = "http://dtt.1024hgc.club/pw/thread.php?fid=17&page=%s";

    public static final class StorySummary {
        public String url;
        public String name;
        public String author;
        public String date;

        public StorySummary() {
            reset();
        }

        public StorySummary(String url, String name, String author, String date) {
        	this.url = url;
        	this.name = name;
        	this.author = author;
        	this.date = date;
        }

        public void reset() {
        	this.url = null;
        	this.name = null;
        	this.author = null;
        	this.date = null;
        }

        @Override
        public String toString() {
            return name + ";" + author + ";" + date + ";" + url + "\n";
        }

    }

    public static void main(String args[]) {
    	ensureBaseDirectory();

        //		downloadStory(sStorySummary);

//        pullStorys(1, 376);

//        updateStroys();
//    	downloadHtml("http://dtt.1024hgc.club/pw/htm_data/17/1603/317492.html", "D:/story/temp_story.html", 5);
    	
//    	new StoryDownloader(new StorySummary("http://bww.yakexi1024.com/pw/htm_data/17/1603/316388.html", "阿里不達年代記第", "", "")).startDownload();
//    	new StoryDownloader(new StorySummary("http://dtt.1024hgc.club/pw/htm_data/17/1603/303680.html", "aaaaaaaaaaaaaa妻孝", "", "")).startDownload();
    }
    
    private static void ensureBaseDirectory() {
    	File baseDirectory = new File(BASE_PATH);
    	baseDirectory.mkdirs();
    	if (!baseDirectory.exists() || !baseDirectory.isDirectory()) {
    		throw new IllegalStateException("storage directory error!");
    	}
    }

//    public static void updateStroys() {
//        long stopThreadId;
//        try {
//            stopThreadId = getStopThreadId();
//            if (stopThreadId < 1) {
//                throw new Exception();
//            }
//        } catch (Exception e) {
//            System.out.println("Unkown stopThreadId, exit");
//            e.printStackTrace();
//            return;
//        }
//        long startThreadId = -1;
//        for (int i = 1; i < 1000; i++) {
//            String urlStr = String.format(STORY_LIST_URL_FORMAT, i);
//            boolean result = downloadHtml(urlStr, TEMP_FILE_LOCATION, 5);
//            if (!result) {
//                System.out.println("Failed to download storylisthtml, url->" + urlStr);
//                continue;
//            }
//            ArrayList<StorySummary> summaryList = null;
//            try {
//                summaryList = StoryListParser.startParse(TEMP_FILE_LOCATION);
//            } catch (Exception e) {
//                System.out.println("Failed to parse storylisthtml, url->" + urlStr);
//                e.printStackTrace();
//                continue;
//            }
//            if (null == summaryList || summaryList.size() < 1) {
//                System.out.println("StorySummaryList have not any content, url->" + urlStr);
//                continue;
//            }
//            boolean stop = false;
//            ArrayList<StorySummary> newSummaryList = new ArrayList<AIWEIStoryCrawler.StorySummary>();
//            for (StorySummary summary : summaryList) {
//                if (summary.threadId <= stopThreadId) {
//                    stop = true;
//                } else {
//                    newSummaryList.add(summary);
//                }
//            }
//            if (i == 1) {
//                if (newSummaryList.size() > 0) {
//                    startThreadId = newSummaryList.get(0).threadId;
//                } else {
//                    System.out.println("No new stories");
//                }
//            }
//            downloadStory(newSummaryList);
//            if (stop) {
//                try {
//                    if (startThreadId > 0) saveStopThreadId(startThreadId);
//                } catch (Exception e) {
//                }
//                break;
//            }
//        }
//    }

    private static String getStopThreadId() throws IOException {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(LAST_URL_FILE_PATH), HTML_CHARSET));
            String idStr = reader.readLine().trim();
            return idStr;
        } finally {
            if (null != reader) reader.close();
        }
    }

    private static void saveStopThreadId(String lastUrl) throws UnsupportedEncodingException, IOException {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(LAST_URL_FILE_PATH);
            fos.write(lastUrl.getBytes(HTML_CHARSET));
            fos.flush();
        } finally {
            if (null != fos) fos.close();
        }
    }

    public static void pullStorys(int startPage, int endPage) {
        LinkedList<StorySummary> summaryList = new LinkedList<StorySummary>();
        for (int i = startPage; i <= endPage; i++) {
            String urlStr = String.format(STORY_LIST_URL_FORMAT, i);
            boolean result = downloadHtml(urlStr, TEMP_FILE_LOCATION, 5);
            if (!result) {
                System.out.println("Failed to download storylisthtml, url->" + urlStr);
                continue;
            }
            try {
            	summaryList.addAll(StoryListParser.startParse(TEMP_FILE_LOCATION));
            } catch (Exception e) {
                System.out.println("Failed to parse storylisthtml, url->" + urlStr);
                e.printStackTrace();
                continue;
            }
            if (null == summaryList || summaryList.size() < 1) {
                System.out.println("StorySummaryList have not any content, url->" + urlStr);
                continue;
            }
        }
        System.out.println("url-count:" + summaryList.size());
        downloadStory(summaryList);
    }

    public static void downloadStory(List<StorySummary> summaryList) {
        for (StorySummary summary : summaryList) {
            boolean result = new StoryDownloader(summary).startDownload();
            if (!result) {
                System.out.println("Failed to download story, details->" + summary.toString());
            }
        }
    }

    public static final boolean downloadHtml(String urlStr, String filePath, int retryCount) {
        boolean downloaded = downloadHtml(urlStr, filePath);
        if (downloaded) {
            return true;
        } else if (retryCount > 0) {
        	long sleepTime = 5000 + (int) (Math.random() * 2000);
        	if (retryCount == 1) {
        		sleepTime *= 50;
        	} else if (retryCount == 2) {
        		sleepTime *= 10;
        	}
            try {
                Thread.sleep(sleepTime);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
            System.out.println("Failed to download storyhtml, retry...");
            return downloadHtml(urlStr, filePath, retryCount - 1);
        } else {
            return false;
        }
    }

    public static final boolean downloadHtml(String urlStr, String filePath) {
        try {
            Thread.sleep(1000 + (int) (Math.random() * 1000));
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }
        InputStream input = null;
        FileOutputStream fos = null;
        try {
//            URL url = new URL(urlStr);
//            URLConnection conn = url.openConnection();
//            conn.setReadTimeout(TIMEOUT_MILLI);
//            conn.connect();
        	HttpParams httpParameters = new BasicHttpParams();  
        	HttpConnectionParams.setConnectionTimeout(httpParameters, CONNECT_TIMEOUT_MILLI); 
        	HttpConnectionParams.setSoTimeout(httpParameters, READ_TIMEOUT_MILLI);
        	HttpConnectionParams.setSocketBufferSize(httpParameters, 1024 * 50);
        	DefaultHttpClient client = new DefaultHttpClient(httpParameters);
        	HttpGet get = new HttpGet(urlStr);

            input = client.execute(get).getEntity().getContent();
            fos = new FileOutputStream(filePath);
            byte[] b = new byte[1024];
            int read = 0;
            while ((read = input.read(b)) != -1) {
                fos.write(b, 0, read);
            }
            fos.flush();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                input.close();
            } catch (Exception e) {
                input = null;
            }
            try {
                fos.close();
            } catch (Exception e) {
                fos = null;
            }
        }
    }

}
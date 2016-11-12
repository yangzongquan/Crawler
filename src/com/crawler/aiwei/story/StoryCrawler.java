package com.crawler.aiwei.story;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.LinkedList;
import java.util.List;

import com.crawler.aiwei.Config;
import com.crawler.aiwei.HttpUtil;
import com.crawler.aiwei.ListParser;
import com.crawler.aiwei.ListParser.Summary;

public class StoryCrawler {

    public static final String BASE_PATH = Config.BASE_PATH + "/story";

    public static final String TEMP_FILE_LOCATION = BASE_PATH + "/temp_story.html";
    public static final String LAST_URL_FILE_PATH = BASE_PATH + "/last_url.txt";
    public static final String STORY_LIST_URL_FORMAT = Config.PREFIX_URL + "thread.php?fid=17&page=%s";

    public static void main(String args[]) {
    	ensureBaseDirectory();

        //		downloadStory(sStorySummary);

//        pullStorys(1, 376);

//        updateStroys();
//    	downloadHtml("http://1024.05ia.rocks/pw/htm_data/17/1603/317492.html", "D:/story/temp_story.html", 5);
    	
//    	new StoryDownloader(new StorySummary("http://bww.yakexi1024.com/pw/htm_data/17/1603/316388.html", "阿里不達年代記第", "", "")).startDownload();
//    	new StoryDownloader(new StorySummary("http://1024.05ia.rocks/pw/htm_data/17/1603/303680.html", "aaaaaaaaaaaaaa妻孝", "", "")).startDownload();
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
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(LAST_URL_FILE_PATH), Config.HTML_CHARSET));
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
            fos.write(lastUrl.getBytes(Config.HTML_CHARSET));
            fos.flush();
        } finally {
            if (null != fos) fos.close();
        }
    }

    public static void pullStorys(int startPage, int endPage) {
        LinkedList<Summary> summaryList = new LinkedList<Summary>();
        for (int i = startPage; i <= endPage; i++) {
            String urlStr = String.format(STORY_LIST_URL_FORMAT, i);
            boolean result = HttpUtil.getDefend(urlStr, TEMP_FILE_LOCATION, 5);
            if (!result) {
                System.err.println("Failed to download storylisthtml, url->" + urlStr);
                continue;
            }
            try {
            	summaryList.addAll(ListParser.startParse(TEMP_FILE_LOCATION));
            } catch (Exception e) {
                System.err.println("Failed to parse storylisthtml, url->" + urlStr);
                e.printStackTrace();
                continue;
            }
            if (null == summaryList || summaryList.size() < 1) {
                System.err.println("StorySummaryList have not any content, url->" + urlStr);
                continue;
            }
        }
        System.out.println("url-count:" + summaryList.size());
        downloadStory(summaryList);
    }

    public static void downloadStory(List<Summary> summaryList) {
        for (Summary summary : summaryList) {
            boolean result = new StoryDownloader(summary).startDownload();
            if (!result) {
                System.err.println("Failed to download story, details->" + summary.toString());
            }
        }
    }

}
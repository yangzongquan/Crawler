package com.crawler.aiwei.story;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;

import javax.swing.text.html.parser.ParserDelegator;

import com.crawler.aiwei.Config;
import com.crawler.aiwei.HttpUtil;
import com.crawler.aiwei.ListParser.Summary;
import com.crawler.aiwei.TextUtil;

public class StoryDownloader {

    public static final String TEMP_FILE_LOCATION = StoryCrawler.TEMP_FILE_LOCATION;

    private Summary mStorySummary;
    private StringBuffer mStoryBuffer = new StringBuffer();

    public StoryDownloader(Summary storySummary) {
        mStorySummary = storySummary;
    }

    private boolean checkStorySummary(Summary storySummary) {
        if (isEmptyText(storySummary.url)) return false;
        if (isEmptyText(storySummary.name)) return false;
        return true;
    }

    public static boolean isEmptyText(String text) {
        return null == text || text.length() < 1;
    }

    public boolean startDownload() {
        boolean checked = checkStorySummary(mStorySummary);
        if (!checked) {
            System.err.println("Incorrect story summary, details->" + mStorySummary.toString());
            return checked;
        }

        LinkedList<String> subUrls = new LinkedList<>();
        boolean result = download(mStorySummary.url, mStoryBuffer, subUrls, true);
        if (result && mStoryBuffer.length() > 0 && subUrls.size() > 0) {
        	for (String url : subUrls) {
        		result &= download(url, mStoryBuffer, null, false);
        		if (!result) break;
        	}
        }
        
        if (result) {
            return saveStory();
        } else {
            System.err.println("Failed to download story, status->" + result + ", details->" + mStorySummary.toString());
            return false;
        }
    }

    private boolean download(String urlStr, StringBuffer strBuffer, LinkedList<String> subUrls, boolean parseSubPage) {
        boolean result = HttpUtil.getDefend(urlStr, TEMP_FILE_LOCATION, 5);
        if (!result) {
            System.err.println("Failed to download storyhtml, url->" + urlStr + ", details->" + mStorySummary.toString());
            return false;
        }
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(TEMP_FILE_LOCATION), Config.HTML_CHARSET));
            ParserDelegator ps = new ParserDelegator();
            StoryParser parser = new StoryParser(parseSubPage);
            ps.parse(reader, parser, true);
            if (parseSubPage && subUrls != null) {
            	subUrls.addAll(parser.getSubPageUrls());
            }
			String story = parser.getStory();
			if (!isEmptyText(story)) {
				strBuffer.append(story);
				return true;
			} else {
				System.err.println("Story text is NULL, url->" + urlStr + ", details->" + mStorySummary.toString());
				return false;
			}
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                reader.close();
            } catch (Exception e) {
                reader = null;
            }
        }
    }

    private boolean saveStory() {
        File storyFile = generateStoryFileName();
        if (null == storyFile) {
            System.err.println("Failed to generate story name, details->" + mStorySummary.toString());
            return false;
        }
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(storyFile);
            String storyText = mStoryBuffer.toString();
            fos.write(("NAME: " + mStorySummary.name).getBytes("GBK"));
            fos.write(("\r\nAUTHOR: " + mStorySummary.author).getBytes("GBK"));
            fos.write(("\r\nDATE: " + mStorySummary.date).getBytes("GBK"));
            fos.write(("\r\nURL: " + mStorySummary.url).getBytes("GBK"));
            fos.write(("\r\n\r\n\r\n").getBytes("GBK"));
            fos.write(storyText.getBytes("GBK"));
            fos.flush();
        } catch (Exception e) {
            System.err.println("Failed to save story, details->" + mStorySummary.toString());
            storyFile.delete();
            e.printStackTrace();
            return false;
        } finally {
            try {
                fos.close();
            } catch (Exception e2) {
                e2.printStackTrace();
                fos = null;
            }
        }

        return true;
    }

    private File generateStoryFileName() {
            String filePath = StoryCrawler.BASE_PATH + "/" + TextUtil.generateFileName(mStorySummary.name);
            File file = new File(filePath + ".txt");
            if (file.exists()) {
                return new File(filePath + "_____RePeat_" + getRandomName(10) + ".txt");
            }
            return file;
    }

    public static String getRandomName(int length) {
        return TextUtil.getRandomName(length);
    }

}

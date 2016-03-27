package com.crawler.aiwei.xiezhen;

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

public class ImageDownloader {

    public static final String TEMP_FILE_LOCATION = XiezhenCrawler.TEMP_FILE_LOCATION;

    private Summary mSummary;
    LinkedList<String> mComments = new LinkedList<>();
    LinkedList<String> mImgLinks = new LinkedList<>();

    public ImageDownloader(Summary summary) {
        mSummary = summary;
    }

    private boolean checkSummary(Summary summary) {
        if (isEmptyText(summary.url)) return false;
        if (isEmptyText(summary.name)) return false;
        return true;
    }

    public static boolean isEmptyText(String text) {
        return null == text || text.length() < 1;
    }

    public boolean startDownload() {
        boolean checked = checkSummary(mSummary);
        if (!checked) {
            System.err.println("Incorrect summary, details->" + mSummary.toString());
            return checked;
        }

        String contentDirectory = ensureContentDirectory();
        if (TextUtil.isEmpty(contentDirectory)) {
            System.err.println("Failed to create content directory, details->" + mSummary.toString());
        	return false;
        }
        
        LinkedList<String> subUrls = new LinkedList<>();
        boolean result = download(mSummary.url, mComments, mImgLinks, subUrls, true, contentDirectory + "/source_" + TextUtil.getRandomName(5) + ".html");
        if (result && subUrls.size() > 0) {
        	for (String url : subUrls) {
        		result &= download(url, mComments, mImgLinks);
        		if (!result) break;
        	}
        }
        
        if (result) {
        	result &= saveSummary(contentDirectory);
        	if (result) {
        		int failedCount = 0;
        		for (String imgLink : mImgLinks) {
        			String imgFilePath = generateImageFilePath(contentDirectory, imgLink);
        			boolean ret = HttpUtil.getRetry(imgLink, imgFilePath, 0, 2, HttpUtil.defaultRetryInterval(), true);
        			if (ret) {
        				failedCount = 0;
        			} else {
        				failedCount += 1;
        	            System.err.println("Failed to download img, imgLink:" + imgLink);
        			}
        			
        			// 删除无效的图片文件
        			File imgFile = new File(imgFilePath);
        			if (imgFile.length() < Config.MIN_IMG_FILE_SIZE) {
        				imgFile.delete();
        			}
        			
        			if (failedCount >= 5) {
        				result = false;
        				break;
        			}
        		}
        	}
        }
        
        // 检查图片是否下载完整，否则下载失败
        if (result) {
        	File[] files = new File(contentDirectory).listFiles();
        	if (files == null 
        			// 有一两个下载失败不算失败
        			|| files.length < mImgLinks.size()) {
        		result = false;
        	}
        }
        return result;
    }
    
    private boolean download(String urlStr, LinkedList<String> comments, LinkedList<String> imgLinks) {
    	return download(urlStr, comments, imgLinks, null, false, null);
    }
    
    private boolean download(String urlStr, LinkedList<String> comments, LinkedList<String> imgLinks, LinkedList<String> subUrls, boolean parseSubPage, String htmlPath) {
    	htmlPath = TextUtil.isEmpty(htmlPath) ? TEMP_FILE_LOCATION : htmlPath;
        boolean result = HttpUtil.getDefend(urlStr, htmlPath, 5);
        if (!result) {
            System.err.println("Failed to download imagehtml, url->" + urlStr + ", details->" + mSummary.toString());
            return false;
        }
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(htmlPath), Config.HTML_CHARSET));
            ParserDelegator ps = new ParserDelegator();
            ImageParser parser = new ImageParser(parseSubPage);
            ps.parse(reader, parser, true);
            if (parseSubPage && subUrls != null) {
            	subUrls.addAll(parser.getSubPageUrls());
            }
            if (imgLinks != null) {
            	imgLinks.addAll(parser.getImageLinks());
            }
            if (comments != null) {
            	comments.addAll(parser.getComment());
            }
			return true;
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

    private boolean saveSummary(String directory) {
        File storyFile = new File(directory + "/摘要.txt");
        if (storyFile.exists()) {
        	storyFile = new File(directory + "/摘要_zZzzZ_" + TextUtil.getRandomName(5) + ".txt");
        }
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(storyFile);
            fos.write(("NAME: " + mSummary.name).getBytes("GBK"));
            fos.write(("\r\nAUTHOR: " + mSummary.author).getBytes("GBK"));
            fos.write(("\r\nDATE: " + mSummary.date).getBytes("GBK"));
            fos.write(("\r\nURL: " + mSummary.url).getBytes("GBK"));

            fos.write(("\r\n\r\n\r\n").getBytes("GBK"));
            
            fos.write(("\r\n" + Config.IMG_LINK_PREFIX).getBytes("GBK"));
            for (String imgLink : mImgLinks) {
                fos.write(("\r\n" + imgLink).getBytes("GBK"));
            }
            fos.write(("\r\n" + Config.IMG_LINK_SUFFIX).getBytes("GBK"));
            
            fos.write(("\r\n\r\n\r\n").getBytes("GBK"));
            for (String comment : mComments) {
                fos.write(("\r\n" + comment).getBytes("GBK"));
            }
            fos.flush();
        } catch (Exception e) {
            System.err.println("Failed to save story, details->" + mSummary.toString());
            e.printStackTrace();
            storyFile.delete();
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
    
    private String generateImageFilePath(String directory, String imgLink) {
    	String fileName = TextUtil.generateImageFileName(imgLink);
    	String filePath = directory + "/" + fileName;
    	if (TextUtil.isEmpty(fileName) || new File(filePath).exists()) {
    		filePath = directory + "/" + TextUtil.getRandomName(32);
    	}
    	return filePath;
    }

	private String ensureContentDirectory() {
		String filePath = XiezhenCrawler.BASE_PATH + "/" + TextUtil.generateFileName(mSummary.name);
		File file = new File(filePath);
		if (file.exists() && !file.isDirectory()) {
			file.delete();
			file.mkdirs();
			return file.getAbsolutePath();
		} else {
			file.mkdirs();
			return  file.getAbsolutePath();
		}
	}

    public static String getRandomName(int length) {
        return TextUtil.getRandomName(length);
    }

}

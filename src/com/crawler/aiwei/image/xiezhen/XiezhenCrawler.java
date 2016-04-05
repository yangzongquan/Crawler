package com.crawler.aiwei.image.xiezhen;

import com.crawler.aiwei.Config;
import com.crawler.aiwei.image.ImageCrawler;

public class XiezhenCrawler extends ImageCrawler {

    public static final String BASE_PATH = Config.BASE_PATH + "/xiezhen";
    public static final String LIST_URL_FORMAT = "http://dtt.1024hgc.club/pw/thread.php?fid=14&page=%s";

    public static void main(String args[]) {
    	new XiezhenCrawler().pull(1, 1);
    }
    
	@Override
	protected String makeStoreDirectory() {
		return BASE_PATH;
	}

	@Override
	protected String formatPageUrl(int page) {
		if (page < 1) {
			System.err.println("Error page " + page + " !");
		}
		return String.format(LIST_URL_FORMAT, page);
	}

}

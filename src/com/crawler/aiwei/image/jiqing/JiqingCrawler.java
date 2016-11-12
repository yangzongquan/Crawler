package com.crawler.aiwei.image.jiqing;

import com.crawler.aiwei.Config;
import com.crawler.aiwei.image.ImageCrawler;

public class JiqingCrawler extends ImageCrawler {

    public static final String BASE_PATH = Config.BASE_PATH + "/jiqing";
    public static final String LIST_URL_FORMAT = Config.PREFIX_URL + "thread.php?fid=16&page=%s";

    public static void main(String args[]) {
    	new JiqingCrawler().pull(1, 1);
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

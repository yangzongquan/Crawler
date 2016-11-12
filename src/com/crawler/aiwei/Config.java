package com.crawler.aiwei;

public class Config {

	public static final String BASE_PATH = "/Users/yang";

	public static final String HTML_CHARSET = "utf-8";

	// http://xp301.com/
	// http://xp303.com/
	public static final String PREFIX_URL = "http://1024.05ia.rocks/pw/";

    public static final int READ_TIMEOUT_MILLI = 30 * 1000;
    public static final int CONNECT_TIMEOUT_MILLI = 10 * 1000;
    public static final int HTTP_BUFFER_SIZE = 1024 * 100;

    public static final String COMMENT_PREFIX = "cccccccccccccccccccccccc93eefrmijf98s3ijf92ccccccccccccccccccccccccccccccc";
    public static final String COMMENT_SUFFIX = "cccccccccccccccccccccccc90fdsk3r9kmr9dsfr09ccccccccccccccccccccccccccccccc";

    public static final String IMG_LINK_PREFIX = "lllllllllllllllllllllllmocvsnfoewmkdsjoisroewmkdjsfjdsllllllllllllllllllllllll";
    public static final String IMG_LINK_SUFFIX = "lllllllllllllllllllllllewirmidsojfioewjfmsiofjeijfiefillllllllllllllllllllllll";

    public static final int MIN_IMG_FILE_SIZE = 10 * 1024;
    
    
    public static final boolean ENABLE_PROXY = true;
    public static final String PROXY_HOST = "gfw.rongju.im";
    public static final int PROXY_PORT = 7788;
    public static final String PROXY_USER_NAME = "";
    public static final String PROXY_PASSWORD = "";
}

package com.crawler.aiwei;

import java.net.URL;
import java.util.HashMap;

public class UrlAccesser {
	
	public static final HashMap<String, Integer> sHostMap = new HashMap<>();
	public static final int REACHABLE_MAX_FAILED_COUNT = 30;
	public static final float RETRY_PERCENT_FOR_UNREACH = 0.05f;
	
	public static boolean isReachable(String urlStr) {
		if (TextUtil.isEmpty(urlStr)) {
			return false;
		}
		try {
			String host = new URL(urlStr).getHost();
			if (TextUtil.isEmpty(host)) {
				return false;
			}
			Integer failCount = sHostMap.get(host);
			if (failCount == null || failCount < REACHABLE_MAX_FAILED_COUNT) {
				return true;
			}
			// 0.05的概率重试
			if (Math.random() < RETRY_PERCENT_FOR_UNREACH) {
				return true;
			}
		} catch (Exception e) {
            System.err.println("UrlAccesser.isReachable() exp:" + e.getMessage());
		}
		return false;
	}
	
	public static void onFailed(String urlStr) {
		if (TextUtil.isEmpty(urlStr)) {
			return;
		}
		try {
			String host = new URL(urlStr).getHost();
			if (TextUtil.isEmpty(host)) {
				return;
			}
			Integer failCount = sHostMap.get(host);
			sHostMap.put(host, failCount == null ? 1 : failCount + 1);
		} catch (Exception e) {
            System.err.println("UrlAccesser.onFailed() exp:" + e.getMessage());
		}
	}
	
	public static void onSucess(String urlStr) {
		if (TextUtil.isEmpty(urlStr)) {
			return;
		}
		try {
			URL url = new URL(urlStr);
			sHostMap.remove(url.getHost());
		} catch (Exception e) {
            System.err.println("UrlAccesser.onSucess() exp:" + e.getMessage());
		}
	}

}

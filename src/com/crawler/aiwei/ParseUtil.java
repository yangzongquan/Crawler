package com.crawler.aiwei;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

public class ParseUtil {

    public static boolean parseSubPage(LinkedList<String> subPageUrls, String urlStr) throws MalformedURLException {
    	System.out.println("parseSubPage() url:" + urlStr);
    	URL url = new URL(urlStr);
    	List<NameValuePair> params = URLEncodedUtils.parse(url.getQuery(), Charset.forName("utf-8"));
    	if (params == null || params.size() < 1) {
    		return false;
    	}
    	final String pageParamName = "page";
    	NameValuePair pagePair = null;
    	int totalPage = 0;
    	for (NameValuePair pair : params) {
    		if (pair == null) continue;
    		if (pageParamName.equals(pair.getName())) {
    			pagePair = pair;
    	    	totalPage = Integer.valueOf(pair.getValue());
    		}
    	}
    	if (totalPage < 1) {
    		System.out.println("Total page error!!");
    		return false;
    	}

    	for (int i = 2; i <= totalPage; i++) {
    		params.remove(pagePair);
    		final int pageIndex = i;
    		pagePair = new NameValuePair() {
				
				@Override
				public String getValue() {
					return pageIndex + "";
				}
				
				@Override
				public String getName() {
					return pageParamName;
				}
			};
    		params.add(pagePair);
    		String subUrl = urlStr.subSequence(0, urlStr.indexOf("?") + 1) + URLEncodedUtils.format(params, "utf-8");
    		subPageUrls.add(subUrl);
    		System.out.println("subPageUrl:" + subUrl);
    	}
    	return true;
    }

}

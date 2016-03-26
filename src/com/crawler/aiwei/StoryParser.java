package com.crawler.aiwei;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.List;

import javax.swing.text.MutableAttributeSet;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLEditorKit.ParserCallback;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

public class StoryParser extends ParserCallback {

    public static final String PAGE_DIV_CLASS = "pages";
    public static final String CONTENT_DIV_CLASS = "tpc_content";
    
    public static final String LAST_PAGE_TAG = "»";

    private StringBuffer mStoryBuffer = new StringBuffer();

    protected boolean inA = false;
    protected boolean inPageDiv = false;
    protected boolean inContentDiv = false;

    private boolean parseSubPage = false;
    private String aLink;
    private LinkedList<String> subPageUrls = new LinkedList<String>();

    public StoryParser() {
    }

    public StoryParser(boolean parseSubPage) {
    	this.parseSubPage = parseSubPage;
    }

    public void handleText(char[] data, int pos) {
        if (null == data || data.length < 1) {
            return;
        }
        String text = new String(data);
        if (parseSubPage && subPageUrls.size() == 0 && inPageDiv && inA && !StoryDownloader.isEmptyText(aLink) && LAST_PAGE_TAG.equals(text)) {
        	String url = StoryListParser.PREFEX_STORY_URL + aLink;
        	try {
				parsePage(url);
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
        }
        if (inContentDiv) {
        	mStoryBuffer.append(TextUtil.handleStoryContent(text));
        }
    }
    
    public void handleStartTag(HTML.Tag t, MutableAttributeSet a, int pos) {
        if (t == HTML.Tag.A) {
        	inA = true;
        	aLink = (String) a.getAttribute(HTML.Attribute.HREF);
        } else if (t == HTML.Tag.DIV) {
        	if (a.containsAttribute(HTML.Attribute.CLASS, PAGE_DIV_CLASS) && parseSubPage && subPageUrls.size() == 0) {
        		inPageDiv = true;
        	}
        	if (a.containsAttribute(HTML.Attribute.CLASS, CONTENT_DIV_CLASS)) {
        		inContentDiv = true;
        	}
        }
    }

    public void handleEndTag(HTML.Tag t, int pos) {
        if (t == HTML.Tag.A) {
        	inA = false;
        	aLink = null;
        } else if (t == HTML.Tag.DIV) {
        	inPageDiv = false;
        	
        	if (inContentDiv) {
        		mStoryBuffer.append("\r\n\r\n\r\n");
            	inContentDiv = false;
        	}
        }
    }

    public void handleError(String errorMsg, int pos) {
    }

    public void handleSimpleTag(HTML.Tag t, MutableAttributeSet a, int pos) {
        handleStartTag(t, a, pos);
        handleEndTag(t, pos);
        if (t == HTML.Tag.BR) {
        	if (inContentDiv) {
        		mStoryBuffer.append("\r\n");
        	}
        }
    }

    public void handleComment(char[] data, int pos) {
    }

    public String getStory() {
        return mStoryBuffer.toString();
    }
    
    public LinkedList<String> getSubPageUrls() {
    	return subPageUrls;
    }

    private void parsePage(String urlStr) throws MalformedURLException {
    	System.out.println("parsePage() url:" + urlStr);
    	URL url = new URL(urlStr);
    	List<NameValuePair> params = URLEncodedUtils.parse(url.getQuery(), Charset.forName("utf-8"));
    	if (params == null || params.size() < 1) {
    		return;
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
    		return;
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
    }

}

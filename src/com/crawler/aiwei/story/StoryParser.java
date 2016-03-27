package com.crawler.aiwei.story;

import java.net.MalformedURLException;
import java.util.LinkedList;

import javax.swing.text.MutableAttributeSet;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLEditorKit.ParserCallback;

import com.crawler.aiwei.Config;
import com.crawler.aiwei.ParseUtil;
import com.crawler.aiwei.TextUtil;

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
        	String url = Config.PREFIX_URL + aLink;
        	try {
				if (!ParseUtil.parseSubPage(subPageUrls, url)) {
					System.out.println("Failed to parse sub page!");
				}
			} catch (MalformedURLException e) {
				System.out.println("Failed to parse sub page!");
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

}

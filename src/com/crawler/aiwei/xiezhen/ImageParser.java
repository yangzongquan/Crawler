package com.crawler.aiwei.xiezhen;

import java.net.MalformedURLException;
import java.util.LinkedList;

import javax.swing.text.MutableAttributeSet;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLEditorKit.ParserCallback;

import com.crawler.aiwei.Config;
import com.crawler.aiwei.ParseUtil;
import com.crawler.aiwei.TextUtil;

public class ImageParser extends ParserCallback {

    public static final String PAGE_DIV_CLASS = "pages";
    public static final String CONTENT_DIV_CLASS = "tpc_content";
    public static final String IMAGE_DIV_ID = "read_tpc";
    public static final String COMMENT_DIV_ID_PREFIX = "read_";
    
    public static final String LAST_PAGE_TAG = "»";

    private StringBuffer mCommentBuffer = new StringBuffer();
    private LinkedList<String> mComments = new LinkedList<>();
    private LinkedList<String> mImageLinks = new LinkedList<>();

    protected boolean inA = false;
    protected boolean inImg = false;
    protected boolean inPageDiv = false;
    protected boolean inCommentDiv = false;
    protected boolean inImageDiv = false;

    private boolean parseSubPage = false;
    private String aLink;
    private String imgLink;
    private LinkedList<String> subPageUrls = new LinkedList<String>();

    public ImageParser() {
    }

    public ImageParser(boolean parseSubPage) {
    	this.parseSubPage = parseSubPage;
//		System.out.println("parseSubPage:" + parseSubPage);
    }

    public void handleText(char[] data, int pos) {
        if (null == data || data.length < 1) {
            return;
        }
        String text = new String(data);
        if (parseSubPage && subPageUrls.size() == 0 && inPageDiv && inA && !TextUtil.isEmpty(aLink) && LAST_PAGE_TAG.equals(text)) {
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
        if (inCommentDiv) {
//    		System.out.println("inCommentDiv text:" + text);
    		mCommentBuffer.append(TextUtil.handleStoryContent(text));
        }
    }
    
    public void handleStartTag(HTML.Tag t, MutableAttributeSet a, int pos) {
        if (t == HTML.Tag.A) {
        	inA = true;
        	aLink = (String) a.getAttribute(HTML.Attribute.HREF);
        } else if (t == HTML.Tag.DIV) {
        	if (a.containsAttribute(HTML.Attribute.CLASS, PAGE_DIV_CLASS) && parseSubPage && subPageUrls.size() == 0) {
        		inPageDiv = true;
//        		System.out.println("inPageDiv");
        	}
        	if (a.containsAttribute(HTML.Attribute.CLASS, CONTENT_DIV_CLASS)) {
        		if (a.containsAttribute(HTML.Attribute.ID, IMAGE_DIV_ID)) {
        			inImageDiv = true;
//            		System.out.println("inImageDiv");
        		} else {
        			String id = (String) a.getAttribute(HTML.Attribute.ID);
        			if (!TextUtil.isEmpty(id) && id.startsWith(COMMENT_DIV_ID_PREFIX) && id.length() > COMMENT_DIV_ID_PREFIX.length()) {
            			inCommentDiv = true;
                		mCommentBuffer.setLength(0);
                		mCommentBuffer.append(Config.COMMENT_PREFIX + "\r\n");
//                		System.out.println("inCommentDiv");
            		}
        		}
        	}
        } else if (t == HTML.Tag.IMG) {
        	inImg = true;
        	// 评论包含图片
        	if (inCommentDiv) {
        		inCommentDiv = false;
        	}
        	// 提取写真图片链接
        	if (inImageDiv && inImg) {
        		imgLink = (String) a.getAttribute(HTML.Attribute.SRC);
        		if (!TextUtil.isEmpty(imgLink)) {
        			mImageLinks.add(imgLink);
//            		System.out.println("inImg link:" + imgLink);
        		}
        	}
        }
    }
    
    public void handleEndTag(HTML.Tag t, int pos) {
        if (t == HTML.Tag.A) {
        	inA = false;
        	aLink = null;
        } else if (t == HTML.Tag.DIV) {
        	inPageDiv = false;
        	if (inCommentDiv) {
        		mCommentBuffer.append("\r\n" + Config.COMMENT_SUFFIX);
        		mComments.add(mCommentBuffer.toString());
            	inCommentDiv = false;
        	}
        	inImageDiv = false;
        } else if (t == HTML.Tag.IMG) {
        	inImg = false;
        	imgLink = null;
        }
    }

    public void handleError(String errorMsg, int pos) {
    }

    public void handleSimpleTag(HTML.Tag t, MutableAttributeSet a, int pos) {
        handleStartTag(t, a, pos);
        handleEndTag(t, pos);
        if (t == HTML.Tag.BR) {
        	if (inCommentDiv) {
        		mCommentBuffer.append("\r\n");
        	}
        }
    }

    public LinkedList<String> getComment() {
        return mComments;
    }
    
    public LinkedList<String> getSubPageUrls() {
    	return subPageUrls;
    }

    public LinkedList<String> getImageLinks() {
    	return mImageLinks;
    }
    
}

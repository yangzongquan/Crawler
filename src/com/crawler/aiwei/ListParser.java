package com.crawler.aiwei;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.regex.Pattern;

import javax.swing.text.MutableAttributeSet;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLEditorKit.ParserCallback;
import javax.swing.text.html.parser.ParserDelegator;

public class ListParser extends ParserCallback {

    public static final class Summary {
        public String url;
        public String name;
        public String author;
        public String date;

        public Summary() {
            reset();
        }

        public Summary(String url, String name, String author, String date) {
        	this.url = url;
        	this.name = name;
        	this.author = author;
        	this.date = date;
        }

        public void reset() {
        	this.url = null;
        	this.name = null;
        	this.author = null;
        	this.date = null;
        }

        @Override
        public String toString() {
            return name + ";" + author + ";" + date + ";" + url;
        }
        
        @Override
        public int hashCode() {
        	if (url == null) {
        		return super.hashCode();
        	}
        	return url.hashCode();
        }
        
        @Override
        public boolean equals(Object obj) {
        	if (obj == null) return false;
        	if (!(obj instanceof Summary)) {
        		return false;
        	}
        	Summary s = (Summary) obj;
        	boolean objEmpty = TextUtil.isEmpty(s.url);
        	boolean thisEmpty = TextUtil.isEmpty(url);
        	if (objEmpty && thisEmpty) {
        		return true;
        	}
        	if (!objEmpty && !thisEmpty) {
        		return url.equals(s.url);
        	}
        	return false;
        }

    }

    private static final HashSet<String> EXCLUDE_AUTHOR = new HashSet<String>(5);
    static {
    	EXCLUDE_AUTHOR.add("系统消息");
    	EXCLUDE_AUTHOR.add("cctv-1");
    	EXCLUDE_AUTHOR.add("趙敏");
    	EXCLUDE_AUTHOR.add("敏敏");
    }
    
    public static boolean isExcludeAuthor(String author) {
    	return EXCLUDE_AUTHOR.contains(author);
    }
    
    public final static ArrayList<Summary> startParse(String htmlFileLocation) throws IOException {
        BufferedReader brd = null;
        try {
            brd = new BufferedReader(new InputStreamReader(new FileInputStream(htmlFileLocation), Config.HTML_CHARSET));
            ParserDelegator ps = new ParserDelegator();
            ListParser parser = new ListParser();
            ps.parse(brd, parser, true);
            return parser.mStorySummaryList;
        } finally {
            try {
                brd.close();
            } catch (Exception e) {
                brd = null;
            }
        }
    }


    public static final Pattern textPattern = Pattern.compile("\\s*|\t|\r|\n");

    private Summary mCurrentStorySummary = null;
    private ArrayList<Summary> mStorySummaryList = new ArrayList<Summary>();
    
    private static final String LIST_TABLE_ID = "ajaxtable";
    private static final String MAIN_DIV_ID = "main";
    private static final String BOTTOM_DIV_ID = "bottom";
    private static final String ITEM_TR_CLASS = "tr3 t_one";
    private static final String AUTHOR_CLASS = "bl";
    private static final String DATE_CLASS = "f10";

    private boolean mInMainDiv;
    private boolean mInListTable;
    private boolean mInItemTr;
    private boolean mInH3;
    private boolean mIsAuthor;
    private boolean mIsDate;

    public ListParser() {
    }

    public void handleText(char[] data, int pos) {
        String text = new String(data);
        if (mInH3) {
    		mCurrentStorySummary.name = TextUtil.handleStoryName(text);
//			System.out.println("name:" + text + ", trimName:" + mCurrentStorySummary.name);
        }
        if (mIsAuthor) {
        	mCurrentStorySummary.author = text.trim();
//			System.out.println("author:" + text);
        }
		if (mIsDate) {
			mCurrentStorySummary.date = text.trim();

			if (!isExcludeAuthor(mCurrentStorySummary.author)) {
				mStorySummaryList.add(mCurrentStorySummary);
				System.out.println(mCurrentStorySummary.toString());
			}
		}
    }

    /**
     * @param t
     * @param a
     * @param pos
     */
    public void handleStartTag(HTML.Tag t, MutableAttributeSet a, int pos) {
        if (t == HTML.Tag.DIV) {
        	if (a.containsAttribute(HTML.Attribute.ID, MAIN_DIV_ID)) {
        		mInMainDiv = true;
//    			System.out.println("mInMainDiv");
        	} else if (a.containsAttribute(HTML.Attribute.ID, BOTTOM_DIV_ID)) {
        		mInMainDiv = false;
        	}
        } else if (t == HTML.Tag.TABLE) {
        	if (mInMainDiv && a.containsAttribute(HTML.Attribute.ID, LIST_TABLE_ID)) {
        		mInListTable = true;
//    			System.out.println("mInListTable");
        	}
        } else if (t == HTML.Tag.TR) {
        	if (mInMainDiv && mInListTable && a.containsAttribute(HTML.Attribute.CLASS, ITEM_TR_CLASS)) {
        		mInItemTr = true;
//    			System.out.println("mInItemTr");
        	}
        } else if (t == HTML.Tag.H3) {
        	if (mInItemTr) {
        		mInH3 = true;
//    			System.out.println("mInH3");
        		
        		mCurrentStorySummary = new Summary();
        	}
        } else if (t == HTML.Tag.A) {
        	if (mInH3) {
        		mCurrentStorySummary.url = Config.PREFIX_URL + (String) a.getAttribute(HTML.Attribute.HREF);
//    			System.out.println("url:" + mCurrentStorySummary.url);
        	} else if (isAuthorA(a)) {
//    			System.out.println("isAuthorA");
        		mIsAuthor = true;
        	} else if (isDateA(a)) {
//    			System.out.println("isDateA");
        		mIsDate = true;
        	}
        }
    }
    
    private boolean isAuthorA(MutableAttributeSet a) {
    	return mInItemTr && mCurrentStorySummary != null && mCurrentStorySummary.url != null && mCurrentStorySummary.name != null && a.containsAttribute(HTML.Attribute.CLASS, AUTHOR_CLASS);
    }
    
    private boolean isDateA(MutableAttributeSet a) {
    	return mInItemTr && mCurrentStorySummary != null && mCurrentStorySummary.url != null && mCurrentStorySummary.name != null && a.containsAttribute(HTML.Attribute.CLASS, DATE_CLASS);
    }

    /**
     * @param t
     * @param pos
     */
	public void handleEndTag(HTML.Tag t, int pos) {
		if (t == HTML.Tag.DIV) {

		} else if (t == HTML.Tag.TABLE) {
			mInListTable = false;
		} else if (t == HTML.Tag.TR) {
			mInItemTr = false;
		} else if (t == HTML.Tag.H3) {
			mInH3 = false;
		} else if (t == HTML.Tag.A) {
			mIsAuthor = false;
			mIsDate = false;
		}
	}

    /**
     * @param errorMsg
     * @param pos
     */
    public void handleError(String errorMsg, int pos) {
    }

    /**
     * @param t
     * @param a
     * @param pos
     */
    public void handleSimpleTag(HTML.Tag t, MutableAttributeSet a, int pos) {
        handleStartTag(t, a, pos);
        handleEndTag(t, pos);
    }

    /**
     * @param data
     * @param pos
     */
    public void handleComment(char[] data, int pos) {
    }

}
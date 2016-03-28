package com.crawler.aiwei;


public class TextUtil {

	public static boolean isEmpty(String text) {
		return text == null || text.length() == 0;
	}
	
	public static String generateImageFileName(String imgLink) {
		if (isEmpty(imgLink)) {
			return null;
		}
		int lastSep = imgLink.lastIndexOf('/');
		if (lastSep < 0 || lastSep >= imgLink.length()) {
			return null;
		}
		return imgLink.substring(lastSep + 1);
	}

    public static String generateFileName(String origin) {
		if (!isEmpty(origin)) {
	        // \/:*?"<>|
			origin = origin.replace("/", " ");
			origin = origin.replace("\\", " ");
			origin = origin.replace(":", " ");
			origin = origin.replace("?", " ");
			origin = origin.replace("*", " ");
			origin = origin.replace("<", "(");
			origin = origin.replace(">", ")");
			origin = origin.replace("|", " ");
			origin = origin.replace("\"", " ");
		}
		if (isEmpty(origin)) {
			origin = getRandomName(32);
		}
        return origin;
    }

	public static String handleStoryName(String origin) {
		if (isEmpty(origin)) {
			return getRandomName(10);
		}
		origin = origin.replace('\u00A0', ' ');
		if (isEmpty(origin)) {
			return getRandomName(10);
		}
		origin = origin.replace('\u3000', ' ');
		if (isEmpty(origin)) {
			return getRandomName(10);
		}
		origin = origin.replace('\uE4C6', ' ');
		if (isEmpty(origin)) {
			return getRandomName(10);
		}
        StringBuffer nameBuffer = new StringBuffer(origin.trim());
        if (nameBuffer.length() == 0) {
			return getRandomName(10);
        }
        while (nameBuffer.length() > 2 && nameBuffer.charAt(0) == '[') {
        	int index = nameBuffer.indexOf("]") + 1;
        	if (index == nameBuffer.length()) {
        		// 处理不了，还原
        		nameBuffer = new StringBuffer(origin.trim());
        		break;
        	}
        	nameBuffer.delete(0, index);
        	nameBuffer = new StringBuffer(nameBuffer.toString().trim());
        }
        if (nameBuffer.length() == 0) {
			return getRandomName(10);
        }
        return nameBuffer.toString().trim();
	}
	
	public static String handleStoryContent(String text) {
		if (isEmpty(text)) {
			return "";
		}
    	text = text.replace('\u00A0', ' ');
    	text = text.replace('\u3000', ' ');
    	text = text.replace('\uE4C6', ' ');
    	if (text.startsWith("”") && text.length() > 1) {
    	    text = "“" + text.substring(1);
    	}
		if (isEmpty(text)) {
			return "";
		}
    	if (text.length() > 2 && (text.startsWith("??") || text.startsWith("\uFF1F\uFF1F"))) {
    	    StringBuffer buf = new StringBuffer(text);
    	    while (buf.length() > 0 && (buf.charAt(0) == '?' || buf.charAt(0) == '\uFF1F')) {
    	        buf.deleteCharAt(0);
    	        buf.insert(0, ' ');
    	    }
    	    text = buf.toString();
    	}
    	return text;
	}

    public static String getRandomName(int length) {
        final StringBuilder randomText = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            switch ((int) (Math.random() * 3)) {
            case 0:
                randomText.append((char) ('A' + (char) (Math.random() * 26)));
                break;
            case 1:
                randomText.append((char) ('a' + (char) (Math.random() * 26)));
                break;
            default:
                randomText.append((char) ('0' + (char) (Math.random() * 10)));
                break;
            }
        }
        return randomText.toString();
    }

}

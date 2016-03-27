package com.crawler.aiwei;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

public class HttpUtil {
	
	public static final long defaultDelay() {
		return 1000 + (int) (Math.random() * 1000);
	}
	
	public static final long defaultRetryInterval() {
		return 5000 + (int) (Math.random() * 2000);
	}

	/**
	 * 
	 * @param urlStr
	 * @param filePath
	 * @param retryCount
	 * @return
	 */
    public static final boolean getDefend(String urlStr, String filePath, int retryCount) {
    	return getDefend(urlStr, filePath, defaultDelay(), retryCount, defaultRetryInterval());
    }
    
    /**
     * defaultDelay: 1000 + (int) (Math.random() * 1000)
     * enableUrlAccesser: false
     * 
     * @param urlStr
     * @param filePath
     * @return
     */
    public static final boolean get(String urlStr, String filePath) {
    	return get(urlStr, filePath, defaultDelay(), false);
    }

    /**
     * 开始按照固定时间重试，倒数第二次等待十倍时间再重试，倒数第一次等待50倍时间再重试
     * enableUrlAccesser: false
     * 
     * @param urlStr
     * @param filePath
     * @param delayMilli 延迟发起GET请求，ms
     * @param retryCount 如果失败重试次数
     * @param retryInterval 失败后，重试的时间间隔
     * @return
     */
    public static final boolean getDefend(String urlStr, String filePath, long delayMilli, int retryCount, long retryInterval) {
    	for (int i = 0; i < retryCount; i++) {
        	boolean result = get(urlStr, filePath, delayMilli, false);
        	if (result) {
        		return true;
        	}
        	long sleepTime = retryInterval;
        	if (i == (retryCount - 2)) {
        		sleepTime *= 50;
        	} else if (i == (retryCount - 3)) {
        		sleepTime *= 10;
        	}
        	if (sleepTime > 0) {
	            try {
	                Thread.sleep(sleepTime);
	            } catch (InterruptedException e1) {
	                e1.printStackTrace();
	            }
        	}
            System.out.println("Failed to getDefend, retry...");
    	}
    	return false;
    }
    
    /**
     * 如果失败，按照固定时间重试。
     * enableUrlAccesser: false
     * 
     * @param urlStr
     * @param filePath
     * @param delayMilli 延迟发起GET请求，ms
     * @param retryCount 如果失败重试次数
     * @param retryInterval 失败后，重试的时间间隔
     * @return
     */
    public static final boolean getRetry(String urlStr, String filePath, long delayMilli, int retryCount, long retryInterval, boolean enableUrlAccesser) {
        boolean downloaded = get(urlStr, filePath, delayMilli, false);
        if (downloaded) {
            return true;
        } else if (retryCount > 0) {
        	if (retryInterval > 0) {
	            try {
	                Thread.sleep(retryInterval);
	            } catch (InterruptedException e1) {
	                e1.printStackTrace();
	            }
        	}
            System.out.println("Failed to get, retry...");
            return getRetry(urlStr, filePath, delayMilli, retryCount - 1, retryInterval, enableUrlAccesser);
        } else {
            return false;
        }
    }
    
    private static DefaultHttpClient sClient;
    static {
    	HttpParams httpParameters = new BasicHttpParams();  
    	HttpConnectionParams.setConnectionTimeout(httpParameters, Config.CONNECT_TIMEOUT_MILLI); 
    	HttpConnectionParams.setSoTimeout(httpParameters, Config.READ_TIMEOUT_MILLI);
    	HttpConnectionParams.setSocketBufferSize(httpParameters, Config.HTTP_BUFFER_SIZE);
    	sClient = new DefaultHttpClient(httpParameters);
    }

    public static final boolean get(String urlStr, String filePath, long delayMilli, boolean enableUrlAccesser) {
    	if (enableUrlAccesser && !UrlAccesser.isReachable(urlStr)) {
    		System.out.println("UrlAccesser ignore url:" + urlStr);
    		return false;
    	}
    	if (delayMilli > 0) {
	        try {
	            Thread.sleep(delayMilli);
	        } catch (InterruptedException e1) {
	            e1.printStackTrace();
	        }
    	}
        InputStream input = null;
        FileOutputStream fos = null;
        HttpGet get = new HttpGet(urlStr);
        try {
            input = sClient.execute(get).getEntity().getContent();
            fos = new FileOutputStream(filePath);
            byte[] b = new byte[1024];
            int read = 0;
            while ((read = input.read(b)) != -1) {
                fos.write(b, 0, read);
            }
            fos.flush();
            UrlAccesser.onSucess(urlStr);
            return true;
        } catch (Exception e) {
            UrlAccesser.onFailed(urlStr);
        	new File(filePath).delete();
            System.out.println(e.getMessage());
            return false;
        } finally {
            try {
//            	get.releaseConnection();
            	sClient.getConnectionManager().closeIdleConnections(60, TimeUnit.SECONDS);
            } catch (Exception e) {
            }
            try {
                input.close();
            } catch (Exception e) {
            }
            try {
                fos.close();
            } catch (Exception e) {
            }
        }
    }

}

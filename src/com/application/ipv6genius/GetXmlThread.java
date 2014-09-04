package com.application.ipv6genius;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.xml.sax.InputSource;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class GetXmlThread extends Thread {
	private String url;
	private boolean isFirst;
	private File xmlfile;
	private int notify_num;
	private String filename;
	private Handler handler;
	
	public GetXmlThread(boolean isFirst, int notify_num, Handler handler){
		this.isFirst = isFirst;
		this.notify_num = notify_num;
		this.handler = handler;
	}
	
	public void setIsFirst(boolean isFirst){
		this.isFirst = isFirst;
	}
	
	public File getXmlFile() {
		return xmlfile;
	}
	
	@Override
	public void run() {
		while (isFirst) { 
			isFirst = false; //只运行一次
			
			switch(notify_num) {
			case MainActivity.UPDATE_NOTIFY:
				filename = MainActivity.VERFILE;
				getXmlUrl_update();
				break;
			case MainActivity.EXP_NOTIFY:
				filename = MainActivity.EXPLIST;
				getXmlUrl_exp();
				break;
			case MainActivity.APP_NOTIFY:
				filename = MainActivity.APPLIST;
				getXmlUrl_app();
				break;
			default:
				url = null;
			}
			
    		try { 
    			URL httpUrl = new URL(url);
    			HttpURLConnection conn = (HttpURLConnection) httpUrl.openConnection();
    			conn.setConnectTimeout(30000);
    			conn.setReadTimeout(30000);
    			conn.setInstanceFollowRedirects(true);

    			if (conn.getResponseCode() == 200) {
    				xmlfile = new File(MainActivity.DATADIR + filename); 
    				if (xmlfile.exists()) {
    					xmlfile.delete(); 	
    				}
    				xmlfile.createNewFile();
    				
    				InputStream inputStream = conn.getInputStream();
    				InputSource inputSrc = new InputSource(inputStream); 
    				inputSrc.setEncoding("UTF-8");  //Necessary for Chinese characters decoding!!
    				OutputStream outputStream = new FileOutputStream(xmlfile);
    				Toolbox.CopyStream(inputStream, outputStream);
    				outputStream.close();
    				inputStream.close();
    			}
    			
    			//Send notify message to handler
    			Message message = new Message();
    			message.what = notify_num;
    			handler.sendMessage(message);       
    			Log.v("Debug-GetXmlThread", "Finish downloading: " + url);
    		} catch (Exception e) {
            	Log.e("Debug-GetXmlThread", "---Download XML Error: reset");
            	Log.e("Debug-GetXmlThread", e.toString());
            	switch(notify_num) {
    			case MainActivity.UPDATE_NOTIFY:
    				MainActivity.resetUpdateth();
    				break;
    			case MainActivity.EXP_NOTIFY:
    				MainActivity.resetExpth();
    				break;
    			case MainActivity.APP_NOTIFY:
    				MainActivity.resetAppth();
    				break;
    			}
            }
		}
	}
	
	/**
	 * Private method to assign value to static var URL for update.
	 */
	private void getXmlUrl_update() {
		this.url = MainActivity.UPDATEPATH + MainActivity.VERFILE;
	}
	
	/**
	 * Private method to assign value to static var URL for fragment application.
	 */
	private void getXmlUrl_app() {
		//判断本机能否为纯4域名获取AAAA地址，并根据结果选择不同的服务器端xml文件
		boolean isUnderTranslation = Toolbox.isUnderTranslation(MainActivity.TEST_DNS_SITE);
		if (isUnderTranslation) {
			this.url = MainActivity.XMLPATH + MainActivity.APPLIST1;
		}
		else {
			this.url = MainActivity.XMLPATH + MainActivity.APPLIST2;
		}
	}
	
	/**
	 * Private method to assign value to static var URL for fragment experience.
	 */
	private void getXmlUrl_exp() {
		this.url = MainActivity.XMLPATH + MainActivity.EXPLIST;
	}
}


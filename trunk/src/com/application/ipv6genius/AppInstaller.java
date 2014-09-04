package com.application.ipv6genius;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

public class AppInstaller {
	static ExecutorService executorService;
	FileCache fileCache;
	Context context;
	File apkfile;
	ApkLoader loader;
	int position;
	boolean isDownloadCompleted;
	
	//Constructor
	public AppInstaller(Context c, int position) {
		context = c;
		this.position = position;
		isDownloadCompleted = false;
		fileCache = new FileCache(context);
		executorService = Executors.newFixedThreadPool(5);
	}
	
	//The function to be called from outside
	public void downloadApp(String pkg, String url) {  
		apkfile = fileCache.getFile(String.valueOf(url.hashCode()) + ".apk"); 
		
		//先检查是否已下载过该应用：通过判断url是否一致
		if (apkfile.exists()) {  
			Log.v("Debug-downloadApp", "已下载过该应用");
			openAPKFile(apkfile);
			return;
		}
		
		//若没有下载过，下载并安装
		ApkToLoad p = new ApkToLoad(pkg, url, apkfile);
		loader = new ApkLoader(p);
		executorService.submit(loader); 
	}
	
	// 任务队列
	private class ApkToLoad {
		public String pkg;
		public String url;
		public File file;

		public ApkToLoad(String pkg, String url, File file) {
			this.pkg = pkg;
			this.url = url;
			this.file = file;
		}
	}

	//Inner class: each ApkLoader implements Runnable
	class ApkLoader implements Runnable {
		ApkToLoad apkToLoad;
		volatile boolean stop;  //volatile variable to stop download when necessary

		ApkLoader(ApkToLoad apkToLoad) {
			this.apkToLoad = apkToLoad;
			stop = false;
		}
		
		public void setStopFlagApkLoader(boolean s) {
			stop = s;
		}

		@Override
		public void run() {
			try {
		        URL url = new URL(apkToLoad.url);  
		        Log.v("Debug-downloadApp", "url = "+url);
		        try {
	                HttpURLConnection conn = (HttpURLConnection) url
	                                .openConnection();
	                InputStream is = conn.getInputStream(); 
	                FileOutputStream fos = new FileOutputStream(apkToLoad.file);
	                byte[] buf = new byte[256];
	                conn.connect();

	                if (conn.getResponseCode() >= 400) {
	                    Toast.makeText(context, "连接超时", Toast.LENGTH_SHORT)
	                                            .show();
	                } 
	                else {
	                    while (!stop) {
	            	        if (is != null) {
	                            int numRead = is.read(buf); 
	                            if (numRead <= 0) {
	                            	isDownloadCompleted = true;
	                                break;
	                            } 
	                            else {
	                                fos.write(buf, 0, numRead);
	                            }
	                        } 
	                        else {
	                            break;
	                        }
	                    }
	                }

	                conn.disconnect();
	                fos.flush(); 
	                is.close();
	                fos.close();
	                    
	            } catch (IOException e) {
	                e.printStackTrace();
	            }
	        } catch (MalformedURLException e) {
	        	e.printStackTrace();
	        }
			
			if (apkToLoad.file != null) {
				if (isDownloadCompleted) {
					openAPKFile(apkToLoad.file);
				}
				else {
					//Delete incomplete apk file
					Log.d("Debug-downloadApp", "file.delete() = "+apkToLoad.file.delete());
				}
			}
		}
	}
	
	/**
	 * Open given apk file and install it as an application.
	 * 
	 * @param file
	 */
	private void openAPKFile(File file) {
		boolean isUpdate = (position == MainActivity.FLAG_UPDATE);
		ExperienceFragment.setCurrPosition(position);
		
		if(isUpdate) {
			MainActivity.hideUpdateDialogue();
		}
		
	    Log.v("Debug-openAPKFile", "Installing: " + file.getAbsolutePath());
	    Intent intent = new Intent();
	    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	    intent.setAction(android.content.Intent.ACTION_VIEW);
	    intent.setDataAndType(Uri.fromFile(file),
	                    "application/vnd.android.package-archive");
	    context.startActivity(intent);
	}

	public void setStopFlagInstaller(boolean stop) {
		if (loader != null) {
			loader.setStopFlagApkLoader(stop);
		}
	}
}

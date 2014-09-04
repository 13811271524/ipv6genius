package com.application.ipv6genius;

import java.io.File;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class Update {
	public static class GetUpdateXmlHandler extends Handler {
		File verfile;
		GetXmlThread updateth;	
		Activity activity;
		Context context;
		ProgressDialog pd;
		
		public void setUpdateTh(GetXmlThread th) {
			this.updateth = th;
		}
		
		public void setActivity(Activity a) {
			this.activity = a;
			this.context = activity.getApplicationContext();
		}
		
		public void setProgressDialog(ProgressDialog p) {
			pd = p;
		}
		
		public void handleMessage(Message msg) {
			switch(msg.what) {
			case MainActivity.UPDATE_NOTIFY:
				updateth.setIsFirst(false);	
				verfile = ((GetXmlThread) updateth).getXmlFile();	
				if (verfile != null) {	
    				Document doc = Toolbox.getDomElement(verfile); // ��ȡ DOM �ڵ�
    				if (doc != null) {	
    					NodeList nl = doc.getElementsByTagName(MainActivity.KEY_VER);
    					Element e = (Element) nl.item(0);	// ֻ��һ��<ver>�ڵ�
    					String serverVerCodeStr = Toolbox.getValue(e, MainActivity.KEY_VERCODE);
    					String serverVerName = Toolbox.getValue(e, MainActivity.KEY_VERNAME);
    					String url = Toolbox.getValue(e, MainActivity.KEY_URL);
    					//-----------------DNS
    					MainActivity.dns = Toolbox.getValue(e, MainActivity.KEY_DNS);
    					if((MainActivity.dns != null) && !MainActivity.dns.equals("")) {
    						ExecCommandWithHandler exec = new ExecCommandWithHandler("setprop net.dns1 " + MainActivity.dns, null, 0, false);
    						exec.execute();
    						exec.getResult();
//    						MainActivity.notifyUpdateFragment(MainActivity.ACC_INDEX);
    						int index = MainActivity.getCurrIndex();
    						if (index == MainActivity.ACC_INDEX) {
    							AccessFragment fragment1 = (AccessFragment) MainActivity.getFragment(index);
    				            if (fragment1 != null) {
    				            	fragment1.initAccessFragment();
    				            }
    				            else {
    				            	Log.d("Debug", "AccessFragment not instantiated yet.");
    				            	MainActivity.notifyUpdateFragment(MainActivity.ACC_INDEX);
    				            }
    						}
    					}
    					//-----------------DNS
    					if(serverVerCodeStr != null) {
    						int serverVerCode = Integer.parseInt(serverVerCodeStr);
    						int currentVerCode = Config.getVerCode(context);
    						String currentVerName = Config.getVerName(context);
    						if(serverVerCode > currentVerCode) {
    							doNewVersionUpdate(activity, 
    									currentVerCode, currentVerName, 
    									serverVerCode, serverVerName, 
    									pd, url);
    						}
    						else {
//    							notNewVersionShow(activity, currentVerCode, currentVerName);
    							//Do nothing if is the latest version.
    						}
    					}
    				}
    				MainActivity.UpdateXmlDownloaded = true;
				}
				break; 
				
			default:
				super.handleMessage(msg); 
			}
		}
	}
	
	private static void doNewVersionUpdate(Activity activity, 
			int curVerCode, String curVerName, 
			int newVerCode, String newVerName, 
			ProgressDialog p, String url) {
		StringBuffer sb = new StringBuffer();
		sb.append("��ǰ�汾:");
		sb.append(curVerName);
		sb.append(", Code:");
		sb.append(curVerCode);
		sb.append(", \n�����°汾:");
		sb.append(newVerName);
		sb.append(", Code:");
		sb.append(newVerCode);
		sb.append(", �Ƿ����?");
		AlertDialog.Builder dialog = new AlertDialog.Builder(activity);
		dialog.setTitle("�������");
		dialog.setMessage(sb.toString());
		dialog.setPositiveButton("����", new MyUpdateDialogListener(activity.getApplicationContext(), p, url));
		dialog.setNegativeButton("�ݲ�����",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
					}
				});
		dialog.show();
	}
	
	public static class MyUpdateDialogListener implements DialogInterface.OnClickListener {
		Context context;
		ProgressDialog pBar;
		String url;
		
		public MyUpdateDialogListener (Context c, ProgressDialog p, String u) {
			context = c;
			pBar = p;
			url = u;
		}
		@Override
		public void onClick(DialogInterface dialog,
				int which) {
			pBar.setTitle("��������");
			pBar.setMessage("���Ժ�...");
			pBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			pBar.setCanceledOnTouchOutside(false);
			pBar.show();
			AppInstaller appInstaller = new AppInstaller(context, MainActivity.FLAG_UPDATE); //���������ڽ��и��£��������ڰ�װapplist�е�Ԫ��
    		appInstaller.downloadApp(MainActivity.UPDATEFILENAME, url);
		}
	}
}

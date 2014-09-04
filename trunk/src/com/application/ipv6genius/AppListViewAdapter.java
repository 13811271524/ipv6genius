package com.application.ipv6genius;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class AppListViewAdapter extends BaseAdapter {
    private static Activity activity;
    private static LayoutInflater inflater=null;
    
    private static ArrayList<HashMap<String, Object>> applist;
    private ImageLoader imageLoader; //用来下载图片的类
    
    @SuppressWarnings("unchecked")
	public AppListViewAdapter(Activity a, ArrayList<HashMap<String, Object>> d) {
        activity = a;
        applist=(ArrayList<HashMap<String, Object>>) d.clone(); //Caution when passing ArrayList variables as an argument!
        inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        imageLoader=new ImageLoader(activity.getApplicationContext());
    }

    public int getCount() {
        return applist.size();
    }

    public Object getItem(int position) {
        return applist.get(position);
    }

    public long getItemId(int position) {
        return position;
    }
    
    public void refresh(ArrayList<HashMap<String, Object>> newList) {
    	applist.clear();
    	applist.addAll(newList);
    	MainActivity.cancelNotifyUpdateFragment(MainActivity.EXP_INDEX);
    	notifyDataSetChanged();
    }
    
    public View getView(int position, View convertView, ViewGroup parent) {		
    	ViewHolder viewholder;		
        if (applist != null) {
        	if(convertView==null) {
        		convertView = inflater.inflate(R.layout.list_row, null);
        		viewholder = new ViewHolder(position, convertView);
        		convertView.setTag(viewholder);
        	}
	        else {  
	            viewholder = (ViewHolder) convertView.getTag();  
	        }     
        	
        	HashMap<String, Object> app = new HashMap<String, Object>();
	        app = applist.get(position);
	        
	        // 设置ListView的相关值
	        viewholder.name.setText((String) app.get(MainActivity.KEY_NAME));
	        viewholder.specification.setText((String) app.get(MainActivity.KEY_SPECIFICATION));
	        viewholder.version.setText("版本：" + (String) app.get(MainActivity.KEY_VERNAME));
	        String thumb_url = (String) app.get(MainActivity.KEY_THUMB_URL);
	        imageLoader.displayImage(thumb_url, viewholder.list_image);
	        
	        viewholder.install_button = installButtonState(viewholder.install_button, true,
	        		Integer.parseInt((String) app.get(MainActivity.KEY_CUR_STATE)), 
	        		position, 
	        		(String) app.get(MainActivity.KEY_NAME), 
	        		(String) app.get(MainActivity.KEY_INSTALL_URL));
        }
		
        return convertView;
    }
     
    public static class ViewHolder   
    {  
    	public int position;  
    	public ImageView list_image;
        public TextView name;  
        public TextView specification;  
        public TextView version;  
        public Button install_button;  
        
        public ViewHolder (int position, View view) {
        	this.position = position;
        	this.name = (TextView) view.findViewById(R.id.name);	
        	this.specification = (TextView) view.findViewById(R.id.specification);	
        	this.version = (TextView) view.findViewById(R.id.version);	
        	this.list_image = (ImageView) view.findViewById(R.id.list_image);	
        	this.install_button = (Button) view.findViewById(R.id.install_button);
        }
    }  
    
    /**
     * 为传递进来的Button选择符合条件的视图和添加侦听器。
     * @param original 原来的Button
     * @param state 应用安装状态
     * @param position 在整个应用列表中的位置
     * @param name 应用名，非中文
     * @param url URL地址
     * @return 配置好的Button
     */
    public static Button installButtonState(Button original, boolean setBackground, int state, 
    		int position, String name, String url) {
    	Button button = original;
    	button.setTag(position);  //Necessary for adding OnClickListener for multiple buttons!
    	
    	switch (state) {
    	case MainActivity.NOT_INSTALLED:
    		button.setText("安装");
    		if (setBackground) {
    			button.setBackgroundResource(R.drawable.button_install_selector);
    		}
    		button.setOnClickListener(new MyInstallButtonListener(name, url));
    		button.setEnabled(true);
    		break;
    	case MainActivity.INSTALLING:
    		button.setText("取消下载");
    		button.setOnClickListener(new MyInstallButtonListener(name, url));
    		button.setEnabled(true);
    		break;
    	case MainActivity.INSTALLED:
    		button.setText("体验");
    		if (setBackground) {
    			button.setBackgroundResource(R.drawable.button_experience_selector);
    		}
    		button.setOnClickListener(new ExperienceButtonListener(position));
    		button.setEnabled(true);
    		break;
    	case MainActivity.NEED_UPDATE:
    		button.setText("体验");
    		if (setBackground) {
    			button.setBackgroundResource(R.drawable.button_experience_selector);
    		}
    		button.setOnClickListener(new MyInstallButtonListener(name, url));
    		button.setEnabled(true);
    		break;
    	}
    	
    	return button;
    }
    
    /**
     * List item install button listener.
     * 
     * @author Jingwen Gao
     */
    public static class MyInstallButtonListener implements Button.OnClickListener {
    	String name;
    	String pkg;
    	String url;
    	AppInstaller appInstaller;
    	int position;
    	
    	public MyInstallButtonListener (String n, String u) {
    		name = n;
    		url = u;
    	}
    	
    	private class InstallApp implements DialogInterface.OnClickListener {
    		int position;
    		AppInstaller appInstaller;
    		Activity activity;
    		Button button;
    		
    		public InstallApp(Activity activity, Button button, int position, AppInstaller appInstaller) {
    			this.position = position;
    			this.appInstaller = appInstaller;
    			this.activity = activity;
    			this.button = button;
    		}
    		
    		public void onClick(DialogInterface dialog, int whichButton) {
    			installApp(activity, button, position, appInstaller);
    		}
    	}
    	
    	private void installApp(Activity activity, Button button, int position, AppInstaller appInstaller) {
    		installButtonState(button, !ExperienceFragment.isInSubpage, MainActivity.INSTALLING, position, name, url);
    		//更新applist中的state值为installing
    		MainActivity.updateMatchedAppsListState(position, MainActivity.INSTALLING);
    		MainActivity.notifyUpdateFragment(MainActivity.EXP_INDEX);
    		if (appInstaller == null) {
    			appInstaller = new AppInstaller(activity.getApplicationContext(), position);
    			MainActivity.setAppInstaller(position, appInstaller);
    		}
    		appInstaller.downloadApp(pkg, url);
    	}
    	
    	private void updateAppOrNot(Activity activity, 
    			Button button, String curVerName, String newVerName) {
    		StringBuffer sb = new StringBuffer();
    		sb.append("当前安装版本:");
    		sb.append(curVerName);
    		sb.append(", 发现新版本:");
    		sb.append(newVerName);
    		sb.append(", 是否更新?");
    		AlertDialog.Builder dialog = new AlertDialog.Builder(activity);
    		dialog.setTitle(name);
    		dialog.setMessage(sb.toString());
    		dialog.setPositiveButton("更新应用", new InstallApp(activity, button, position, appInstaller));
    		dialog.setNegativeButton("直接体验", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface arg0, int arg1) {
					startApp(position);
				}
    		});
    		dialog.show();
    	}
    	
    	public void onClick(View v){
    		position = (Integer) v.getTag();  //Tag has been set to position when initializing.
    		pkg = MainActivity.getMatchedAppPackage(position);
    		appInstaller = MainActivity.getAppInstaller(position);
    		
    		switch (MainActivity.getMatchedAppsListCurState(position)) {
    		case MainActivity.NOT_INSTALLED:
    			installApp(activity, (Button)v, position, appInstaller);
        		break;
    		case MainActivity.NEED_UPDATE:
    			String cur_version = (String) MainActivity.getMatchedAppList().get(position).get(MainActivity.KEY_CUR_VERNAME);
    			String new_version = (String) MainActivity.getMatchedAppList().get(position).get(MainActivity.KEY_VERNAME);
    			updateAppOrNot(activity, (Button)v, cur_version, new_version);
        		break;
    		case MainActivity.INSTALLING:
    			if (appInstaller != null) {
    				appInstaller.setStopFlagInstaller(true);
    			}
    			MainActivity.restoreMatchedAppsListState(position);
    			installButtonState((Button)v, !ExperienceFragment.isInSubpage, MainActivity.getMatchedAppsListCurState(position), 
    					position, name, url);
        		MainActivity.notifyUpdateFragment(MainActivity.EXP_INDEX);
    			break;
    		}
    		
    	}
    }
    
    public static class ExperienceButtonListener implements Button.OnClickListener {
		PackageManager manager;
		ArrayList<HashMap<String, Object>> matchedAppsList;
		int position;
		
		public ExperienceButtonListener(int p) {
			this.position = p;
		}

		@Override
		public void onClick(View v) 
        { 
			startApp(position);
        } 
	}
    
    public static void startApp(int position) {
    	ArrayList<HashMap<String, Object>> matchedAppsList = applist;
    	PackageManager manager = activity.getPackageManager();
    	
    	String packageName = (String) matchedAppsList.get(position).get(MainActivity.KEY_PACKAGE);
        
        Intent intent = new Intent();
        intent = manager.getLaunchIntentForPackage(packageName); 
        if(intent==null){  
            System.out.println("APP not found!");  
        }
        activity.startActivity(intent);
    }
    
    /**
     * Clone a view from src to des.
     * @param src
     * @param des
     */
    public static void clone(View src, View des) {
    	if(src instanceof ImageView) { 
    		((ImageView) des).setImageDrawable(((ImageView) src).getDrawable());
    		return;
    	}
    	
    	if(src instanceof TextView) { 
			((TextView) des).setText(((TextView) src).getText());
			return;
		}
    }
}

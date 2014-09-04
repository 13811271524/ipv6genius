package com.application.ipv6genius;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.application.ipv6genius.Update.GetUpdateXmlHandler;
import com.google.analytics.tracking.android.EasyTracker;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageButton;
import android.widget.ImageView;

public class MainActivity extends FragmentActivity {
	//所有的静态变量
	public static boolean isConnected;
	public static boolean isBusyboxInstalled;
	public static boolean loadingData;
	public static boolean UpdateXmlDownloaded, AppXmlDownloaded, ExpXmlDownloaded;
	public static boolean newAppListReady, newExpListReady;
	public static boolean useCacheAppList, useCacheExpList;
	public static boolean appCacheInited, expCacheInited;
	public static String DATADIR;  //内部存储：用来存放下载的XML文件和shell文件的目录
//		public static final String SERVERIP = "210.25.132.140";
	public static String ISATAP_PREFIX = "2001:da8:202:107:0:5efe"; 
	
	//常量
	public static final String SERVER = "http://210.25.132.140/Smart6";  //服务器配置文件根目录地址(Don't forget the "http" part)
	public static final String UPDATEPATH = SERVER + "/update/";  	//服务器端更新文件夹
	public static final String VERFILE = "ver.xml";			//服务器端更新文件
	public static final int FLAG_UPDATE = -11;
	public static final int FLAG_NOT_STARTED = -22;
	public static final String UPDATEFILENAME = "ipv6genius";	//更新APK文件将要被保存的文件名
	public static final String XMLPATH = SERVER + "/xmls/";  		//服务器端配置文件夹
	public static final String APPLIST1 = "applist1.xml";	//服务器端应用列表1
	public static final String APPLIST2 = "applist2.xml";	//服务器端应用列表2
	public static final String APPLIST = "applist.xml"; 	//应用列表本地存储文件名
	public static final String EXPLIST = "exp_upper.xml";	//服务器端链接列表
	public static final String APPLIST_CACHE = "applist_cache.xml";	//服务器端应用列表缓存
	public static final String EXPLIST_CACHE = "exp_upper_cache.xml";	//服务器端链接列表缓存
	public static final String TEST_DNS_SITE = "ipv4.test-ipv6.com";  //纯IPv4域名，用于测试用户是否配置了DNS64
	public static final int APP_NOTIFY = 0x11, EXP_NOTIFY = 0x12, UPDATE_NOTIFY = 0x13,
			TIMER_NOTIFY = 0x21, ACC_NOTIFY = 0x22,
			TEST_START = 0x31, TEST_FINISH = 0x32,
			DIAGNOSIS_NOTIFY = 0x41;   //不同事件的消息标识
	public static final int NOT_INSTALLED = 0, INSTALLING = 1, INSTALLED = 2, NEED_UPDATE = 3;  //应用安装状态
	public static final String BUSYBOX_ORIGIN = "busybox";  //busybox命令
	public static final String BUSYBOX_T = "busybox_t";		//本应用将在用户终端上安装的busybox工具名
	public static final int EXP_INDEX = 0, ACC_INDEX = 1, DIAG_INDEX = 2; //各分区对应的页面序号
	
		// 更新文件 ver.xml 节点
	public static final String KEY_VER = "ver"; // XML父节点
	public static final String KEY_VERNAME = "verName";				// 版本名
	public static final String KEY_VERCODE = "verCode";				// 版本号
	public static final String KEY_URL = "url";						// apk文件地址
	public static final String KEY_DNS = "dns";						// DNS地址
	public static String dns; //DNS地址
		// 应用列表applist XML 节点
	public static final String KEY_APP = "app"; // XML父节点
	public static final String KEY_ID = "id";						// ID
	public static final String KEY_NAME = "name";					// 应用名
//	public static final String KEY_APK = "apk";						// apk文件名
	public static final String KEY_PACKAGE = "package";				// 应用程序包名
	public static final String KEY_SPECIFICATION = "specification";	// 说明
	public static final String KEY_THUMB_URL = "thumb_url";			// 缩略图地址
	public static final String KEY_INSTALL_URL = "install_url";		// APK地址
	public static final String KEY_SCREENSHOT1 = "screenshot1";		// 应用截图1地址
	public static final String KEY_SCREENSHOT2 = "screenshot2";		// 应用截图2地址
	public static final String KEY_SCREENSHOT3 = "screenshot3";		// 应用截图3地址
	public static final String KEY_SCREENSHOT4 = "screenshot4";		// 应用截图4地址
	public static final String KEY_SCREENSHOT5 = "screenshot5";		// 应用截图5地址
	public static final String KEY_CUR_STATE = "current_state";			// 当前安装状态：非XML节点
	public static final String KEY_LAST_STATE = "last_state";			// 之前安装状态：非XML节点
	public static final String KEY_APPINSTALLER =  "appInstaller";		// AppInstaller对象：非XML节点
		// 链接列表 exp_upper XML 节点
	public static final String KEY_ITEM = "item"; // XML父节点
	public static final String KEY_LINK_LOGO = "link_logo";	// LOGO地址
	public static final String KEY_LINK_URL = "link_url";	// 链接地址
		// APP 信息
	public static final String KEY_ICON = "icon";			// 图标
	public static final String KEY_CUR_VERNAME = "cur_vername"; // 应用程序当前版本名
	
	private static ArrayList<HashMap<String, Object>> netAppsList;      //应用列表
	private static ArrayList<HashMap<String, String>> netLinksList;		//链接列表
	private static ArrayList<HashMap<String, Object>> matchedAppList;	//匹配之后的应用列表（所有）
	private static ArrayList<HashMap<String, Object>> netAppsList_cache;    //应用列表缓存
	private static ArrayList<HashMap<String, String>> netLinksList_cache;	//链接列表缓存
	private static ArrayList<HashMap<String, Object>> matchedAppList_cache;	//匹配之后的应用列表缓存
	private static boolean[] updateFragmentFlags = {false, false, false}; //分区是否更新标签
	private static ProgressDialog updatePD;
	private static GetXmlThread updateth;        //更新XML文件下载线程
	private static GetXmlThread appth;			//应用列表XML文件下载线程
	private static GetXmlThread expth;			//链接列表XML文件下载线程
	private static boolean sliderInitiated;
	private static MainViewPagerAdapter mainViewPagerAdapter;
	private static int currIndex;// 当前页卡编号
	
	//所有的非静态变量
	public GetUpdateXmlHandler update_getXmlHandler;	//更新XML文件下载完成处理器
	public GetAppXmlHandler app_getXmlHandler;			//应用列表XML文件下载完成处理器
	public GetExpXmlHandler exp_getXmlHandler;			//链接列表XML文件下载完成处理器
	
//	private LinearLayout top_container;
	private BroadcastReceiver mConnReceiver;
	private ViewPager viewPager;//页卡内容
	private ImageView slider;//动画图片
	private ImageButton menu_left_button, menu_right_button;//头标
	private int offset, shift;// 动画图片偏移量
	private int bmpW;// 动画图片宽度
	private FragmentManager fragmentManager;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
//		top_container = (LinearLayout) findViewById(R.id.top_container);
		
		//初始化变量
//		DATADIR = "./data/data/" + getString(R.string.package_name) + "/";  //存放xml,shell文件
		DATADIR = getCacheDir().getPath() + "/"; //存放xml,shell文件
		netAppsList = new ArrayList<HashMap<String, Object>>();
		netLinksList = new ArrayList<HashMap<String, String>>();
		netAppsList_cache = new ArrayList<HashMap<String, Object>>();
		netLinksList_cache = new ArrayList<HashMap<String, String>>();
		fragmentManager = getSupportFragmentManager();
		isConnected = false;
		isBusyboxInstalled = false;
		useCacheAppList = false;
		useCacheExpList = false;
		appCacheInited = false;
		expCacheInited = false;
		updateth = null;
		appth = null;
		expth = null;
		loadingData = false;
		UpdateXmlDownloaded = false; 
		AppXmlDownloaded = false; 
		ExpXmlDownloaded = false;
		newAppListReady = false;
		newExpListReady = false;
		sliderInitiated = false;
		currIndex = ACC_INDEX;
		offset = 0;
		shift = 0;
			
		InitImageView();
		InitImageButton();
		InitViewPager();
			
		//Init and register connection broadcast receiver
		mConnReceiver = new MyConnReceiver();
		try {
			registerReceiver(mConnReceiver, 
					new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    	} catch (IllegalArgumentException e) {
    		Log.e("IllegalArgumentException", "Receiver has been registered.");
    	}
		
		update_getXmlHandler = new GetUpdateXmlHandler();
		app_getXmlHandler = new GetAppXmlHandler();
		exp_getXmlHandler = new GetExpXmlHandler();
		
		if (Toolbox.isConnectionAvailable(getApplicationContext())) {
//				if (Toolbox.isServerAvailable()) {
			isConnected = true;
			beginDownloadUpdateXml();
			beginDownloadAppXml();
			beginDownloadExpXml();
		}
		else {
			isConnected = false;
		}
		
        EasyTracker.getInstance(this).activityStart(this); //Start tracker
	}

	private void InitViewPager() {
		viewPager=(ViewPager) findViewById(R.id.viewPagerMain);
		
//		ViewPager viewPager_orig=(ViewPager) findViewById(R.id.viewPagerMain);
//		viewPager_orig.setVisibility(View.GONE);
//		viewPager = new ViewPager(getApplicationContext()) {
//			@Override
//			protected boolean canScroll(View v, boolean checkV, int dx, int x, int y) {
//			   if(v != this && v instanceof ViewPager) {
//			      return true;
//			   }
//			   return super.canScroll(v, checkV, dx, x, y);
//			}
//		};
//		viewPager.setId(R.id.viewPager);
//		top_container.addView(viewPager);
		 
		mainViewPagerAdapter = new MainViewPagerAdapter(fragmentManager);
		
		viewPager.setOffscreenPageLimit(2);
        viewPager.setAdapter(mainViewPagerAdapter);
		viewPager.setCurrentItem(currIndex);
		viewPager.setOnPageChangeListener(new MyOnPageChangeListener());
	}
	
	/**
	 *  初始化头标
	 */
	private void InitImageButton() {
		menu_left_button = (ImageButton) findViewById(R.id.menu_left_button);
		menu_right_button = (ImageButton) findViewById(R.id.menu_right_button);

		menu_left_button.setOnClickListener(new MyOnClickListener(MainActivity.EXP_INDEX));
		menu_right_button.setOnClickListener(new MyOnClickListener(MainActivity.DIAG_INDEX));
	}

	/**
	 * 初始化动画
	 */
	private void InitImageView() {
		slider= (ImageView) findViewById(R.id.cursor);
		bmpW = BitmapFactory.decodeResource(getResources(), R.drawable.menu_tab).getWidth();// 获取图片宽度
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		int screenW = dm.widthPixels;// 获取分辨率宽度
		shift = screenW / 3; 
		offset = (shift - bmpW) / 2;// 计算偏移量
		int initial = shift * currIndex + offset; 
		Matrix matrix = new Matrix();
		matrix.postTranslate(initial, 0);
		slider.setImageMatrix(matrix);// 设置动画初始位置
	}

	/** 
	 * 头标点击监听
	 */
	private class MyOnClickListener implements OnClickListener{
        private int index=0;
        public MyOnClickListener(int i){
        	index=i;
        }
		public void onClick(View v) {
			viewPager.setCurrentItem(index);			
		}
		
	}

	/**
	 * 页面滑动监听
	 * @author Jingwen Gao
	 *
	 */
    public class MyOnPageChangeListener implements OnPageChangeListener{
		public void onPageScrollStateChanged(int arg0) {
		}

		public void onPageScrolled(int arg0, float arg1, int arg2) {
		}

		public void onPageSelected(int arg0) {
			if (!sliderInitiated) {
				Matrix matrix = new Matrix();
				matrix.postTranslate(offset, 0);
				slider.setImageMatrix(matrix);
				sliderInitiated = true;
			}
			
			Animation animation = new TranslateAnimation(shift*currIndex, shift*arg0, 0, 0);
			animation.setFillAfter(true);// True:图片停在动画结束位置
			animation.setDuration(300);
			slider.startAnimation(animation);
			
			if ((EXP_INDEX <= arg0) && (arg0 <= DIAG_INDEX)) {
				currIndex = arg0;
				if (getUpdateFragmentFlag(arg0) == true) {
					mainViewPagerAdapter.refresh(arg0);
				}
			}
			else {
				Log.e("Debug-onPageSelected", "Page index out of bounds: index = "+arg0);
			}	
		}
    }
    
    /**
     * 检查当前是否使用缓存，返回对应的链接列表。
     * @return 链接列表
     */
    public static ArrayList<HashMap<String, String>> getNetLinksList() {
		if (useCacheExpList) {
			return netLinksList_cache;
		}
		else {
			if (newExpListReady) {
				return netLinksList;
			}
			else {
				return null;
			}
		}
	}
    
    /**
     * 检查当前是否使用缓存，返回对应的匹配应用列表。
     * @return 匹配应用列表
     */
    public static ArrayList<HashMap<String, Object>> getMatchedAppList() {
    	if (useCacheAppList) {
			return matchedAppList_cache;
		}
		else {
			if (newAppListReady) {
				return matchedAppList;
			}
			else {
				return null;
			}
		}
	}
	
    /**
     * 检查当前是否使用缓存,设置对应的匹配应用列表。
     * @param list 匹配应用列表
     */
	public static void setMatchedAppList(ArrayList<HashMap<String, Object>> list) {
		if (useCacheAppList) {
			matchedAppList_cache = list;
		}
		else {
			matchedAppList = list;
		}
	}
	
	/**
	 * 取得当前正在使用的列表中指定项的应用包名。
	 * @param position 指定项位置
	 * @return 应用包名
	 */
	public static String getMatchedAppPackage(int position) {
		return (String) getMatchedAppList().get(position).get(KEY_PACKAGE);
	}
	
	/**
	 * 取得当前正在使用的列表中指定项的当前安装状态。
	 * @param position 指定项位置
	 * @return 当前安装状态
	 */
	public static int getMatchedAppsListCurState(int position) {
		return Integer.parseInt((String) getMatchedAppList().get(position).get(KEY_CUR_STATE));
	}
	
	/**
	 * 更新当前正在使用的列表中指定项的安装状态，并将之前的安装状态备份。
	 * @param position 指定项位置
	 * @param newstate 新的安装状态
	 */
	public static void updateMatchedAppsListState(int position, int newstate) {
		getMatchedAppList().get(position).put(KEY_LAST_STATE, getMatchedAppList().get(position).get(KEY_CUR_STATE));
		getMatchedAppList().get(position).put(KEY_CUR_STATE, ""+newstate);
	}
	
	/**
	 * 重置当前正在使用的列表中指定项的安装状态。
	 * @param position 指定项位置
	 */
	public static void restoreMatchedAppsListState(int position) {
		getMatchedAppList().get(position).put(KEY_CUR_STATE, getMatchedAppList().get(position).get(KEY_LAST_STATE));
	}
	
	/**
	 * 取得当前正在使用的列表中指定项的AppInstaller对象。
	 * @param position 指定项位置
	 * @return AppInstaller对象
	 */
	public static AppInstaller getAppInstaller(int position) {
		return (AppInstaller) getMatchedAppList().get(position).get(KEY_APPINSTALLER);
	}
	
	/**
	 * 设置当前正在使用的列表中指定项的AppInstaller对象。
	 * @param position 指定项位置
	 * @param installer AppInstaller对象
	 */
	public static void setAppInstaller(int position, AppInstaller installer) {
		getMatchedAppList().get(position).put(KEY_APPINSTALLER, installer);
	}
	
	public static void resetUpdateth() {
		updateth = null;
	}
	
	public static void resetAppth() {
		appth = null;
	}
	
	public static void resetExpth() {
		expth = null;
	}
	
	/**
	 * 取得指定页面的更新标签。
	 * @param number 页面编号
	 * @return 更新标签
	 */
	public static boolean getUpdateFragmentFlag(int number) {
		return updateFragmentFlags[number];
	}
	
	/**
	 * 设置指定页面的更新标签为true。
	 * @param number 页面编号
	 */
	public static void notifyUpdateFragment(int number) {
		updateFragmentFlags[number] = true;
	}
	
	/**
	 * 设置指定页面的更新标签为false。
	 * @param number 页面编号
	 */
	public static void cancelNotifyUpdateFragment(int number) {
		updateFragmentFlags[number] = false;
	}
	
	/**
	 * 隐藏版本更新时的“正在更新”对话框。
	 */
	public static void hideUpdateDialogue() {
		updatePD.hide();
	}
	
	/**
	 * 取得指定位置的页面Fragment对象。
	 * @param position 页面编号
	 * @return 页面Fragment对象
	 */
	public static Fragment getFragment(int position) {
		return mainViewPagerAdapter.getItem(position);
	}
	
	/**
	 * 取得当前页面编号。
	 * @return 当前页面编号
	 */
	public static int getCurrIndex() {
		return currIndex;
	}
	
	public void beginDownloadUpdateXml() {
		// 从网络获取更新文件ver.xml
		if (updateth == null) {
			Log.v("Debug", "--beginDownloadUpdateXml");
			updateth = new GetXmlThread(true, UPDATE_NOTIFY, update_getXmlHandler);
			update_getXmlHandler.setActivity(MainActivity.this);
			updatePD  = new ProgressDialog(MainActivity.this);
			update_getXmlHandler.setProgressDialog(updatePD);
			update_getXmlHandler.setUpdateTh(updateth);
			updateth.start();
		}
	}
	
	public void beginDownloadAppXml() {
		loadingData = true;
		// 从网络获取应用列表xml
		if (appth == null) {
			Log.v("Debug", "--beginDownloadAppXml");
			appth = new GetXmlThread(true, APP_NOTIFY, app_getXmlHandler);
			app_getXmlHandler.setAppTh(appth);
	 		app_getXmlHandler.setContext(getApplicationContext());
	 		appth.start();
		}
	}
	
	public void beginDownloadExpXml() {
		loadingData = true;
		// 从网络获取链接列表xml
 		if (expth == null) {
 			Log.v("Debug", "--beginDownloadExpXml");
 			expth = new GetXmlThread(true, EXP_NOTIFY, exp_getXmlHandler);
 	 		exp_getXmlHandler.setExpTh(expth);
 	        expth.start();
 		}
	}
	
	/**
	 * 获取缓存的XML文件，解析，初始化缓存的匹配应用列表。
	 * @param context Context对象
	 * @return 缓存的匹配应用列表
	 */
	public static ArrayList<HashMap<String, Object>> initMatchedAppListCache(Context context) {
		if (!appCacheInited) {
			appCacheInited = true;
			File appfile = new File(MainActivity.DATADIR + MainActivity.APPLIST_CACHE);
			//解析XML文件，将数据存入数据结构netAppsList_cache
			parseAppXml(appfile, netAppsList_cache);
			ArrayList<HashMap<String, Object>> mList = generateMatchedAppList(context, netAppsList_cache);
			setMatchedAppList(mList);
		}
		
		return matchedAppList_cache;
	}
	
	/**
	 * 获取缓存的XML文件，解析，初始化缓存的链接列表。
	 * @return 缓存的链接列表
	 */
	public static ArrayList<HashMap<String, String>> initNetLinksListCache() {
		if (!expCacheInited) {
			expCacheInited = true;
			File exppfile = new File(MainActivity.DATADIR + MainActivity.EXPLIST_CACHE);
			//解析XML文件，将数据存入数据结构netLinksList_cache
			parseExpXml(exppfile, netLinksList_cache);
		}
		
		return netLinksList_cache;
	}
	
	public static class GetAppXmlHandler extends Handler {
		File appfile;
		GetXmlThread appth;
		Context context; 
		
		public void setAppTh(GetXmlThread th) {
			this.appth = th;
		}
		
		public void setContext(Context c) {
			this.context = c;
		}
		
		public void handleMessage(Message msg) {  
			switch (msg.what) {    
				case APP_NOTIFY:  
					appth.setIsFirst(false);	
					appfile = ((GetXmlThread) appth).getXmlFile();
					//解析XML文件，将数据存入数据结构netAppsList
					parseAppXml(appfile, netAppsList);
					MainActivity.AppXmlDownloaded = true;
					
					if (! (useCacheAppList && 
							netAppsList.equals(netAppsList_cache))) {
						useCacheAppList = false;
	    				ArrayList<HashMap<String, Object>> mList = generateMatchedAppList(context, netAppsList);
	    				setMatchedAppList(mList);
	    				newAppListReady = true;
	    				ExperienceFragment.right_inited = false;
	    				notifyUpdateFragment(MainActivity.EXP_INDEX); 
					}
					
					break;  
				
				default:
					super.handleMessage(msg); 
			}    
		} 
	}
	
	/**
	 * 解析XML配置文件信息，遍历所有应用信息节点，并将相关信息存入结构applist。
	 * @param xmlfile XML配置文件
	 * @param applist 目的应用列表结构地址
	 */
	private static void parseAppXml(File xmlfile, ArrayList<HashMap<String, Object>> applist) {
		if (xmlfile != null) {
			Document doc = Toolbox.getDomElement(xmlfile); // 获取 DOM 节点
			if (doc != null) {
				NodeList nl = doc.getElementsByTagName(KEY_APP);
				// 循环遍历所有的应用信息节点 <app>
				for (int i = 0; i < nl.getLength(); i++) {
					// 新建一个 HashMap
					HashMap<String, Object> app = new HashMap<String, Object>();
					Element e = (Element) nl.item(i);
					// 每个子节点添加到HashMap关键= >值
					app.put(KEY_ID, Toolbox.getValue(e, KEY_ID));
					app.put(KEY_NAME, Toolbox.getValue(e, KEY_NAME));
					app.put(KEY_SPECIFICATION, Toolbox.getValue(e, KEY_SPECIFICATION));
					app.put(KEY_VERNAME, Toolbox.getValue(e, KEY_VERNAME));
					app.put(KEY_PACKAGE, Toolbox.getValue(e, KEY_PACKAGE));
					app.put(KEY_VERCODE, Toolbox.getValue(e, KEY_VERCODE));
					app.put(KEY_THUMB_URL, Toolbox.getValue(e, KEY_THUMB_URL));
					app.put(KEY_INSTALL_URL, Toolbox.getValue(e, KEY_INSTALL_URL));
					app.put(KEY_SCREENSHOT1, Toolbox.getValue(e, KEY_SCREENSHOT1));
					app.put(KEY_SCREENSHOT2, Toolbox.getValue(e, KEY_SCREENSHOT2));
					app.put(KEY_SCREENSHOT3, Toolbox.getValue(e, KEY_SCREENSHOT3));
					app.put(KEY_SCREENSHOT4, Toolbox.getValue(e, KEY_SCREENSHOT4));
					app.put(KEY_SCREENSHOT5, Toolbox.getValue(e, KEY_SCREENSHOT5));
					// 初始状态设为“uninstalled”
					app.put(KEY_CUR_STATE, "" + NOT_INSTALLED);
					app.put(KEY_LAST_STATE, "" + NOT_INSTALLED);

					// HashList添加到数组列表
					applist.add(app); 
				}	
			}
		}
	}
	
	public static class GetExpXmlHandler extends Handler {
		File expfile;
		GetXmlThread expth;		
		
		public void setExpTh(GetXmlThread th) {
			this.expth = th;
		}
		
		public void handleMessage(Message msg) {  
			switch (msg.what) {    
				case EXP_NOTIFY:  
					expth.setIsFirst(false);	
					expfile = ((GetXmlThread) expth).getXmlFile();	
					//解析XML文件，将数据存入数据结构netLinksList
					parseExpXml(expfile, netLinksList);
					MainActivity.ExpXmlDownloaded = true;
					
					if (! (useCacheExpList && 
							netLinksList.equals(netLinksList_cache))) {
						useCacheExpList = false;
	    				newExpListReady = true;
	    				ExperienceFragment.left_inited = false;
	    				notifyUpdateFragment(MainActivity.EXP_INDEX); 
					}
					
					break; 
					
				default:
					super.handleMessage(msg); 
			}    
		} 
	}
	
	/**
	 * 解析XML配置文件信息，遍历所有应用信息节点，并将相关信息存入结构linklist。
	 * @param xmlfile XML配置文件
	 * @param linklist 目的链接列表结构地址
	 */
	private static void parseExpXml(File xmlfile, ArrayList<HashMap<String, String>> linklist) {
		if (xmlfile != null) {	
			Document doc = Toolbox.getDomElement(xmlfile); // 获取 DOM 节点
			if (doc != null) {	
				NodeList nl = doc.getElementsByTagName(KEY_ITEM);
				// 循环遍历所有的应用信息节点 <item>
				for (int i = 0; i < nl.getLength(); i++) {
					// 新建一个 HashMap
					HashMap<String, String> item = new HashMap<String, String>();
					Element e = (Element) nl.item(i);
					// 每个子节点添加到HashMap关键= >值
					item.put(KEY_ID, Toolbox.getValue(e, KEY_ID));
					item.put(KEY_LINK_LOGO, Toolbox.getValue(e, KEY_LINK_LOGO));
					item.put(KEY_LINK_URL, Toolbox.getValue(e, KEY_LINK_URL));

					// HashList添加到数组列表
					linklist.add(item);		
				}
			}
		}
	}
	
	public static ArrayList<HashMap<String, Object>> generateMatchedAppList(Context context, ArrayList<HashMap<String, Object>> netapplist) {
		ArrayList<HashMap<String, Object>> localApps = new ArrayList<HashMap<String, Object>> ();
		//得到PackageManager对象  
        PackageManager pm = context.getPackageManager();  
        
        //得到系统安装的所有程序包的PackageInfo对象  
        List<PackageInfo> packs = pm.getInstalledPackages(0);  
          
        for(PackageInfo pi:packs){  
            HashMap<String, Object> app = new HashMap<String, Object>();              
            //这将会显示所有安装的应用程序，包括系统应用程序   
            app.put(MainActivity.KEY_PACKAGE, pi.applicationInfo.packageName);
            app.put(MainActivity.KEY_VERNAME, pi.versionName);
            app.put(MainActivity.KEY_VERCODE, ""+pi.versionCode);

            //循环读取并存到HashMap中，再增加到ArrayList上，一个HashMap就是一项  
            localApps.add(app);  
        } 
        
//		ArrayList<HashMap<String, Object>> netapplist = getNetAppsList();
		ArrayList<HashMap<String, Object>> list = null;
		if (netapplist != null) {  
			list = matchApp(localApps, netapplist);
		}	
		
		return list;
	}
	
	public static ArrayList<HashMap<String, Object>> matchApp(ArrayList<HashMap<String, Object>> local, 
			ArrayList<HashMap<String, Object>> net){
		if (net.size() == 0) {
			return null;
		}
		//因为arraylist是pass-by-value，所以应该使用clone()以免net参数的值被改变！
		@SuppressWarnings("unchecked")
		ArrayList<HashMap<String, Object>> netcopy = (ArrayList<HashMap<String, Object>>) net.clone();
		ArrayList<HashMap<String, Object>> matched = new ArrayList<HashMap<String, Object>>();
		String nappName, lappName;
		int nappVer, lappVer;
		boolean match = false;
		HashMap<String, Object> map = null;
		
		for (HashMap<String, Object> napp : netcopy) {
			nappName = (String) napp.get(MainActivity.KEY_PACKAGE);
			nappVer = Integer.parseInt((String) napp.get(MainActivity.KEY_VERCODE));
        	for(HashMap<String, Object> lapp: local) {
        		match = false;
        		lappName = (String) lapp.get(MainActivity.KEY_PACKAGE);
        		lappVer = Integer.parseInt((String) lapp.get(MainActivity.KEY_VERCODE));
        		if (nappName.equals(lappName)) {
        			match = true;
        			map = lapp;
        			if (lappVer >= nappVer) { //只有在本地版本不低于服务器版本时显示在体验区
        				//更新applist中的state值为installed
            			napp.put(KEY_CUR_STATE, ""+MainActivity.INSTALLED);
        			}
        			else {
        				//更新applist中的state值为need_update
        				napp.put(KEY_CUR_STATE, ""+ MainActivity.NEED_UPDATE);
        				//记住本地应用当前版本名
        				napp.put(KEY_CUR_VERNAME, lapp.get(KEY_VERNAME));
        			}
        			break;
        		}
        	}
        	
        	if (match) {
				matched.add(0, napp);
				local.remove(map);
        		map = null;
    		}
    		else {
    			matched.add(napp);
    		}
		}
        
		return matched;
	}
	
	protected class MyConnReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			ConnectivityManager mConnManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo currentNetworkInfo = (NetworkInfo) mConnManager.getActiveNetworkInfo();
            
            AccessFragment fragment1 = (AccessFragment) getFragment(ACC_INDEX);
            if (fragment1 != null) {
            	if(currentNetworkInfo != null) {
                	if((AccessFragment.hostipv4 == null) || 
                			!AccessFragment.hostipv4.equals(Toolbox.getLocalIpv4Address())) {
                		fragment1.initAccessFragment();
                	}
                }
                else{
                	if(AccessFragment.hostipv4 != null) {
                		fragment1.initAccessFragment();
                	}
                }
            }
            else {
            	Log.d("Debug", "AccessFragment not instantiated yet.");
            }
            
            if (Toolbox.isConnectionAvailable(getApplicationContext())) {
//            if (Toolbox.isServerAvailable()) {
    			if (!MainActivity.UpdateXmlDownloaded) {
    				beginDownloadUpdateXml();
    			}
    			if (!MainActivity.AppXmlDownloaded) {
    				beginDownloadAppXml();
    			}
    			if (!MainActivity.ExpXmlDownloaded) {
    				beginDownloadExpXml();
    			}
    		}
		}
		
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		if (currIndex == EXP_INDEX) {
			((ExperienceFragment) getFragment(EXP_INDEX)).checkUpdate();
		}
		else {
			notifyUpdateFragment(EXP_INDEX);
		}
	}
	
	@Override
	protected void onPause() {
		super.onPause();
	}
	
	public void onStop() {
    	super.onStop();
    	EasyTracker.getInstance(this).activityStop(this); //Stop tracker  
    }

	//设置回退
	@Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
		
        if(keyCode == KeyEvent.KEYCODE_BACK){
        	switch (currIndex) {
        	case MainActivity.EXP_INDEX:
        		ExperienceFragment expFrag = (ExperienceFragment) getFragment(currIndex);
        		//Whether it is in subpage of ExperienceFragment
        		if ((expFrag != null) && ExperienceFragment.isInSubpage) {
        			expFrag.backToAppList();
        		}
        		else {
        			confirmExit();
        		}
        		break;
        	case MainActivity.DIAG_INDEX:
        		DiagnosisFragment diagFrag = (DiagnosisFragment) getFragment(currIndex);
        		//Whether it is in subpage of ApplicationFragment
        		if ((diagFrag != null) && diagFrag.isInSubpage) {
        			diagFrag.backToDiagnosis();
        		}
        		else {
        			confirmExit();
        		}
        		break;
        	default:
        		confirmExit();
        	}
        	return true; 
        } 
        return super.onKeyDown(keyCode,event);
    }
    
    public void confirmExit(){//退出确认
    	AlertDialog.Builder ad=new AlertDialog.Builder(MainActivity.this);
    	ad.setTitle("退出");
    	ad.setMessage("是否退出" + getString(R.string.app_name) + "?");
    	ad.setPositiveButton("是", new DialogInterface.OnClickListener() {//退出按钮
			@Override
			public void onClick(DialogInterface dialog, int i) {
				//Unregister connection broadcast receiver
				try {
		    		unregisterReceiver(mConnReceiver);
		    		File appfrom = new File(MainActivity.DATADIR + MainActivity.APPLIST);
		    		File appto = new File(MainActivity.DATADIR + MainActivity.APPLIST_CACHE);
		    		if (appto.exists()) {
		    			appto.delete();
		    		}
		    		appto.createNewFile();
		    		Toolbox.copyFile(appfrom, appto);
		    		
		    		File linkfrom = new File(MainActivity.DATADIR + MainActivity.EXPLIST);
		    		File linkto = new File(MainActivity.DATADIR + MainActivity.EXPLIST_CACHE);
		    		if (linkto.exists()) {
		    			linkto.delete();
		    		}
		    		linkto.createNewFile();
		    		Toolbox.copyFile(linkfrom, linkto);
		    	} catch (IllegalArgumentException e) {
		    		Log.e("Debug-IllegalArgumentException", "Receiver not registered.");
		    	} catch (IOException e) {
		    		Log.e("Debug-IOException", e.getMessage());
				}
				MainActivity.this.finish();//关闭activity
			}
		});
    	ad.setNegativeButton("否",new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int i) {
				//不退出不用执行任何操作
			}
		});
    	ad.show();//显示对话框
    }
}


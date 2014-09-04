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
	//���еľ�̬����
	public static boolean isConnected;
	public static boolean isBusyboxInstalled;
	public static boolean loadingData;
	public static boolean UpdateXmlDownloaded, AppXmlDownloaded, ExpXmlDownloaded;
	public static boolean newAppListReady, newExpListReady;
	public static boolean useCacheAppList, useCacheExpList;
	public static boolean appCacheInited, expCacheInited;
	public static String DATADIR;  //�ڲ��洢������������ص�XML�ļ���shell�ļ���Ŀ¼
//		public static final String SERVERIP = "210.25.132.140";
	public static String ISATAP_PREFIX = "2001:da8:202:107:0:5efe"; 
	
	//����
	public static final String SERVER = "http://210.25.132.140/Smart6";  //�����������ļ���Ŀ¼��ַ(Don't forget the "http" part)
	public static final String UPDATEPATH = SERVER + "/update/";  	//�������˸����ļ���
	public static final String VERFILE = "ver.xml";			//�������˸����ļ�
	public static final int FLAG_UPDATE = -11;
	public static final int FLAG_NOT_STARTED = -22;
	public static final String UPDATEFILENAME = "ipv6genius";	//����APK�ļ���Ҫ��������ļ���
	public static final String XMLPATH = SERVER + "/xmls/";  		//�������������ļ���
	public static final String APPLIST1 = "applist1.xml";	//��������Ӧ���б�1
	public static final String APPLIST2 = "applist2.xml";	//��������Ӧ���б�2
	public static final String APPLIST = "applist.xml"; 	//Ӧ���б��ش洢�ļ���
	public static final String EXPLIST = "exp_upper.xml";	//�������������б�
	public static final String APPLIST_CACHE = "applist_cache.xml";	//��������Ӧ���б���
	public static final String EXPLIST_CACHE = "exp_upper_cache.xml";	//�������������б���
	public static final String TEST_DNS_SITE = "ipv4.test-ipv6.com";  //��IPv4���������ڲ����û��Ƿ�������DNS64
	public static final int APP_NOTIFY = 0x11, EXP_NOTIFY = 0x12, UPDATE_NOTIFY = 0x13,
			TIMER_NOTIFY = 0x21, ACC_NOTIFY = 0x22,
			TEST_START = 0x31, TEST_FINISH = 0x32,
			DIAGNOSIS_NOTIFY = 0x41;   //��ͬ�¼�����Ϣ��ʶ
	public static final int NOT_INSTALLED = 0, INSTALLING = 1, INSTALLED = 2, NEED_UPDATE = 3;  //Ӧ�ð�װ״̬
	public static final String BUSYBOX_ORIGIN = "busybox";  //busybox����
	public static final String BUSYBOX_T = "busybox_t";		//��Ӧ�ý����û��ն��ϰ�װ��busybox������
	public static final int EXP_INDEX = 0, ACC_INDEX = 1, DIAG_INDEX = 2; //��������Ӧ��ҳ�����
	
		// �����ļ� ver.xml �ڵ�
	public static final String KEY_VER = "ver"; // XML���ڵ�
	public static final String KEY_VERNAME = "verName";				// �汾��
	public static final String KEY_VERCODE = "verCode";				// �汾��
	public static final String KEY_URL = "url";						// apk�ļ���ַ
	public static final String KEY_DNS = "dns";						// DNS��ַ
	public static String dns; //DNS��ַ
		// Ӧ���б�applist XML �ڵ�
	public static final String KEY_APP = "app"; // XML���ڵ�
	public static final String KEY_ID = "id";						// ID
	public static final String KEY_NAME = "name";					// Ӧ����
//	public static final String KEY_APK = "apk";						// apk�ļ���
	public static final String KEY_PACKAGE = "package";				// Ӧ�ó������
	public static final String KEY_SPECIFICATION = "specification";	// ˵��
	public static final String KEY_THUMB_URL = "thumb_url";			// ����ͼ��ַ
	public static final String KEY_INSTALL_URL = "install_url";		// APK��ַ
	public static final String KEY_SCREENSHOT1 = "screenshot1";		// Ӧ�ý�ͼ1��ַ
	public static final String KEY_SCREENSHOT2 = "screenshot2";		// Ӧ�ý�ͼ2��ַ
	public static final String KEY_SCREENSHOT3 = "screenshot3";		// Ӧ�ý�ͼ3��ַ
	public static final String KEY_SCREENSHOT4 = "screenshot4";		// Ӧ�ý�ͼ4��ַ
	public static final String KEY_SCREENSHOT5 = "screenshot5";		// Ӧ�ý�ͼ5��ַ
	public static final String KEY_CUR_STATE = "current_state";			// ��ǰ��װ״̬����XML�ڵ�
	public static final String KEY_LAST_STATE = "last_state";			// ֮ǰ��װ״̬����XML�ڵ�
	public static final String KEY_APPINSTALLER =  "appInstaller";		// AppInstaller���󣺷�XML�ڵ�
		// �����б� exp_upper XML �ڵ�
	public static final String KEY_ITEM = "item"; // XML���ڵ�
	public static final String KEY_LINK_LOGO = "link_logo";	// LOGO��ַ
	public static final String KEY_LINK_URL = "link_url";	// ���ӵ�ַ
		// APP ��Ϣ
	public static final String KEY_ICON = "icon";			// ͼ��
	public static final String KEY_CUR_VERNAME = "cur_vername"; // Ӧ�ó���ǰ�汾��
	
	private static ArrayList<HashMap<String, Object>> netAppsList;      //Ӧ���б�
	private static ArrayList<HashMap<String, String>> netLinksList;		//�����б�
	private static ArrayList<HashMap<String, Object>> matchedAppList;	//ƥ��֮���Ӧ���б����У�
	private static ArrayList<HashMap<String, Object>> netAppsList_cache;    //Ӧ���б���
	private static ArrayList<HashMap<String, String>> netLinksList_cache;	//�����б���
	private static ArrayList<HashMap<String, Object>> matchedAppList_cache;	//ƥ��֮���Ӧ���б���
	private static boolean[] updateFragmentFlags = {false, false, false}; //�����Ƿ���±�ǩ
	private static ProgressDialog updatePD;
	private static GetXmlThread updateth;        //����XML�ļ������߳�
	private static GetXmlThread appth;			//Ӧ���б�XML�ļ������߳�
	private static GetXmlThread expth;			//�����б�XML�ļ������߳�
	private static boolean sliderInitiated;
	private static MainViewPagerAdapter mainViewPagerAdapter;
	private static int currIndex;// ��ǰҳ�����
	
	//���еķǾ�̬����
	public GetUpdateXmlHandler update_getXmlHandler;	//����XML�ļ�������ɴ�����
	public GetAppXmlHandler app_getXmlHandler;			//Ӧ���б�XML�ļ�������ɴ�����
	public GetExpXmlHandler exp_getXmlHandler;			//�����б�XML�ļ�������ɴ�����
	
//	private LinearLayout top_container;
	private BroadcastReceiver mConnReceiver;
	private ViewPager viewPager;//ҳ������
	private ImageView slider;//����ͼƬ
	private ImageButton menu_left_button, menu_right_button;//ͷ��
	private int offset, shift;// ����ͼƬƫ����
	private int bmpW;// ����ͼƬ���
	private FragmentManager fragmentManager;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
//		top_container = (LinearLayout) findViewById(R.id.top_container);
		
		//��ʼ������
//		DATADIR = "./data/data/" + getString(R.string.package_name) + "/";  //���xml,shell�ļ�
		DATADIR = getCacheDir().getPath() + "/"; //���xml,shell�ļ�
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
	 *  ��ʼ��ͷ��
	 */
	private void InitImageButton() {
		menu_left_button = (ImageButton) findViewById(R.id.menu_left_button);
		menu_right_button = (ImageButton) findViewById(R.id.menu_right_button);

		menu_left_button.setOnClickListener(new MyOnClickListener(MainActivity.EXP_INDEX));
		menu_right_button.setOnClickListener(new MyOnClickListener(MainActivity.DIAG_INDEX));
	}

	/**
	 * ��ʼ������
	 */
	private void InitImageView() {
		slider= (ImageView) findViewById(R.id.cursor);
		bmpW = BitmapFactory.decodeResource(getResources(), R.drawable.menu_tab).getWidth();// ��ȡͼƬ���
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		int screenW = dm.widthPixels;// ��ȡ�ֱ��ʿ��
		shift = screenW / 3; 
		offset = (shift - bmpW) / 2;// ����ƫ����
		int initial = shift * currIndex + offset; 
		Matrix matrix = new Matrix();
		matrix.postTranslate(initial, 0);
		slider.setImageMatrix(matrix);// ���ö�����ʼλ��
	}

	/** 
	 * ͷ��������
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
	 * ҳ�滬������
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
			animation.setFillAfter(true);// True:ͼƬͣ�ڶ�������λ��
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
     * ��鵱ǰ�Ƿ�ʹ�û��棬���ض�Ӧ�������б�
     * @return �����б�
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
     * ��鵱ǰ�Ƿ�ʹ�û��棬���ض�Ӧ��ƥ��Ӧ���б�
     * @return ƥ��Ӧ���б�
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
     * ��鵱ǰ�Ƿ�ʹ�û���,���ö�Ӧ��ƥ��Ӧ���б�
     * @param list ƥ��Ӧ���б�
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
	 * ȡ�õ�ǰ����ʹ�õ��б���ָ�����Ӧ�ð�����
	 * @param position ָ����λ��
	 * @return Ӧ�ð���
	 */
	public static String getMatchedAppPackage(int position) {
		return (String) getMatchedAppList().get(position).get(KEY_PACKAGE);
	}
	
	/**
	 * ȡ�õ�ǰ����ʹ�õ��б���ָ����ĵ�ǰ��װ״̬��
	 * @param position ָ����λ��
	 * @return ��ǰ��װ״̬
	 */
	public static int getMatchedAppsListCurState(int position) {
		return Integer.parseInt((String) getMatchedAppList().get(position).get(KEY_CUR_STATE));
	}
	
	/**
	 * ���µ�ǰ����ʹ�õ��б���ָ����İ�װ״̬������֮ǰ�İ�װ״̬���ݡ�
	 * @param position ָ����λ��
	 * @param newstate �µİ�װ״̬
	 */
	public static void updateMatchedAppsListState(int position, int newstate) {
		getMatchedAppList().get(position).put(KEY_LAST_STATE, getMatchedAppList().get(position).get(KEY_CUR_STATE));
		getMatchedAppList().get(position).put(KEY_CUR_STATE, ""+newstate);
	}
	
	/**
	 * ���õ�ǰ����ʹ�õ��б���ָ����İ�װ״̬��
	 * @param position ָ����λ��
	 */
	public static void restoreMatchedAppsListState(int position) {
		getMatchedAppList().get(position).put(KEY_CUR_STATE, getMatchedAppList().get(position).get(KEY_LAST_STATE));
	}
	
	/**
	 * ȡ�õ�ǰ����ʹ�õ��б���ָ�����AppInstaller����
	 * @param position ָ����λ��
	 * @return AppInstaller����
	 */
	public static AppInstaller getAppInstaller(int position) {
		return (AppInstaller) getMatchedAppList().get(position).get(KEY_APPINSTALLER);
	}
	
	/**
	 * ���õ�ǰ����ʹ�õ��б���ָ�����AppInstaller����
	 * @param position ָ����λ��
	 * @param installer AppInstaller����
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
	 * ȡ��ָ��ҳ��ĸ��±�ǩ��
	 * @param number ҳ����
	 * @return ���±�ǩ
	 */
	public static boolean getUpdateFragmentFlag(int number) {
		return updateFragmentFlags[number];
	}
	
	/**
	 * ����ָ��ҳ��ĸ��±�ǩΪtrue��
	 * @param number ҳ����
	 */
	public static void notifyUpdateFragment(int number) {
		updateFragmentFlags[number] = true;
	}
	
	/**
	 * ����ָ��ҳ��ĸ��±�ǩΪfalse��
	 * @param number ҳ����
	 */
	public static void cancelNotifyUpdateFragment(int number) {
		updateFragmentFlags[number] = false;
	}
	
	/**
	 * ���ذ汾����ʱ�ġ����ڸ��¡��Ի���
	 */
	public static void hideUpdateDialogue() {
		updatePD.hide();
	}
	
	/**
	 * ȡ��ָ��λ�õ�ҳ��Fragment����
	 * @param position ҳ����
	 * @return ҳ��Fragment����
	 */
	public static Fragment getFragment(int position) {
		return mainViewPagerAdapter.getItem(position);
	}
	
	/**
	 * ȡ�õ�ǰҳ���š�
	 * @return ��ǰҳ����
	 */
	public static int getCurrIndex() {
		return currIndex;
	}
	
	public void beginDownloadUpdateXml() {
		// �������ȡ�����ļ�ver.xml
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
		// �������ȡӦ���б�xml
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
		// �������ȡ�����б�xml
 		if (expth == null) {
 			Log.v("Debug", "--beginDownloadExpXml");
 			expth = new GetXmlThread(true, EXP_NOTIFY, exp_getXmlHandler);
 	 		exp_getXmlHandler.setExpTh(expth);
 	        expth.start();
 		}
	}
	
	/**
	 * ��ȡ�����XML�ļ�����������ʼ�������ƥ��Ӧ���б�
	 * @param context Context����
	 * @return �����ƥ��Ӧ���б�
	 */
	public static ArrayList<HashMap<String, Object>> initMatchedAppListCache(Context context) {
		if (!appCacheInited) {
			appCacheInited = true;
			File appfile = new File(MainActivity.DATADIR + MainActivity.APPLIST_CACHE);
			//����XML�ļ��������ݴ������ݽṹnetAppsList_cache
			parseAppXml(appfile, netAppsList_cache);
			ArrayList<HashMap<String, Object>> mList = generateMatchedAppList(context, netAppsList_cache);
			setMatchedAppList(mList);
		}
		
		return matchedAppList_cache;
	}
	
	/**
	 * ��ȡ�����XML�ļ�����������ʼ������������б�
	 * @return ����������б�
	 */
	public static ArrayList<HashMap<String, String>> initNetLinksListCache() {
		if (!expCacheInited) {
			expCacheInited = true;
			File exppfile = new File(MainActivity.DATADIR + MainActivity.EXPLIST_CACHE);
			//����XML�ļ��������ݴ������ݽṹnetLinksList_cache
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
					//����XML�ļ��������ݴ������ݽṹnetAppsList
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
	 * ����XML�����ļ���Ϣ����������Ӧ����Ϣ�ڵ㣬���������Ϣ����ṹapplist��
	 * @param xmlfile XML�����ļ�
	 * @param applist Ŀ��Ӧ���б�ṹ��ַ
	 */
	private static void parseAppXml(File xmlfile, ArrayList<HashMap<String, Object>> applist) {
		if (xmlfile != null) {
			Document doc = Toolbox.getDomElement(xmlfile); // ��ȡ DOM �ڵ�
			if (doc != null) {
				NodeList nl = doc.getElementsByTagName(KEY_APP);
				// ѭ���������е�Ӧ����Ϣ�ڵ� <app>
				for (int i = 0; i < nl.getLength(); i++) {
					// �½�һ�� HashMap
					HashMap<String, Object> app = new HashMap<String, Object>();
					Element e = (Element) nl.item(i);
					// ÿ���ӽڵ���ӵ�HashMap�ؼ�= >ֵ
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
					// ��ʼ״̬��Ϊ��uninstalled��
					app.put(KEY_CUR_STATE, "" + NOT_INSTALLED);
					app.put(KEY_LAST_STATE, "" + NOT_INSTALLED);

					// HashList��ӵ������б�
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
					//����XML�ļ��������ݴ������ݽṹnetLinksList
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
	 * ����XML�����ļ���Ϣ����������Ӧ����Ϣ�ڵ㣬���������Ϣ����ṹlinklist��
	 * @param xmlfile XML�����ļ�
	 * @param linklist Ŀ�������б�ṹ��ַ
	 */
	private static void parseExpXml(File xmlfile, ArrayList<HashMap<String, String>> linklist) {
		if (xmlfile != null) {	
			Document doc = Toolbox.getDomElement(xmlfile); // ��ȡ DOM �ڵ�
			if (doc != null) {	
				NodeList nl = doc.getElementsByTagName(KEY_ITEM);
				// ѭ���������е�Ӧ����Ϣ�ڵ� <item>
				for (int i = 0; i < nl.getLength(); i++) {
					// �½�һ�� HashMap
					HashMap<String, String> item = new HashMap<String, String>();
					Element e = (Element) nl.item(i);
					// ÿ���ӽڵ���ӵ�HashMap�ؼ�= >ֵ
					item.put(KEY_ID, Toolbox.getValue(e, KEY_ID));
					item.put(KEY_LINK_LOGO, Toolbox.getValue(e, KEY_LINK_LOGO));
					item.put(KEY_LINK_URL, Toolbox.getValue(e, KEY_LINK_URL));

					// HashList��ӵ������б�
					linklist.add(item);		
				}
			}
		}
	}
	
	public static ArrayList<HashMap<String, Object>> generateMatchedAppList(Context context, ArrayList<HashMap<String, Object>> netapplist) {
		ArrayList<HashMap<String, Object>> localApps = new ArrayList<HashMap<String, Object>> ();
		//�õ�PackageManager����  
        PackageManager pm = context.getPackageManager();  
        
        //�õ�ϵͳ��װ�����г������PackageInfo����  
        List<PackageInfo> packs = pm.getInstalledPackages(0);  
          
        for(PackageInfo pi:packs){  
            HashMap<String, Object> app = new HashMap<String, Object>();              
            //�⽫����ʾ���а�װ��Ӧ�ó��򣬰���ϵͳӦ�ó���   
            app.put(MainActivity.KEY_PACKAGE, pi.applicationInfo.packageName);
            app.put(MainActivity.KEY_VERNAME, pi.versionName);
            app.put(MainActivity.KEY_VERCODE, ""+pi.versionCode);

            //ѭ����ȡ���浽HashMap�У������ӵ�ArrayList�ϣ�һ��HashMap����һ��  
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
		//��Ϊarraylist��pass-by-value������Ӧ��ʹ��clone()����net������ֵ���ı䣡
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
        			if (lappVer >= nappVer) { //ֻ���ڱ��ذ汾�����ڷ������汾ʱ��ʾ��������
        				//����applist�е�stateֵΪinstalled
            			napp.put(KEY_CUR_STATE, ""+MainActivity.INSTALLED);
        			}
        			else {
        				//����applist�е�stateֵΪneed_update
        				napp.put(KEY_CUR_STATE, ""+ MainActivity.NEED_UPDATE);
        				//��ס����Ӧ�õ�ǰ�汾��
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

	//���û���
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
    
    public void confirmExit(){//�˳�ȷ��
    	AlertDialog.Builder ad=new AlertDialog.Builder(MainActivity.this);
    	ad.setTitle("�˳�");
    	ad.setMessage("�Ƿ��˳�" + getString(R.string.app_name) + "?");
    	ad.setPositiveButton("��", new DialogInterface.OnClickListener() {//�˳���ť
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
				MainActivity.this.finish();//�ر�activity
			}
		});
    	ad.setNegativeButton("��",new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int i) {
				//���˳�����ִ���κβ���
			}
		});
    	ad.show();//��ʾ�Ի���
    }
}


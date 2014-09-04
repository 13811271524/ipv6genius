package com.application.ipv6genius;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import com.application.ipv6genius.AppListViewAdapter.ViewHolder;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class ExperienceFragment extends Fragment{
	private FragmentActivity mActivity;
	private int currPage; //当前页面
	private LinearLayout layout;
	private LinearLayout main_view;
	private RelativeLayout appinfo_view;
	private Button left_button, right_button;
	private ListView app_list;
	private GridView link_grid;
	private TextView exp_prompt;
	private TitleButtonListener left_listener, right_listener;
	private AppListViewAdapter adapter;
	private MyListItemListener itemListener;
	private static int curr_position;
	public static boolean left_inited, right_inited;
	public static boolean isInSubpage;
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) { 
		Log.v("Debug", "0-onCreateView");
		View view = inflater.inflate(R.layout.fragment1, container, false);
		
		mActivity = getActivity();
		
		//初始化元件
		layout = (LinearLayout) view.findViewById(R.id.experience_layout);
		main_view = (LinearLayout) layout.findViewById(R.id.experience_main);
		appinfo_view = (RelativeLayout) layout.findViewById(R.id.experience_appinfo);
		left_button = (Button) layout.findViewById(R.id.experience_left);
		right_button = (Button) layout.findViewById(R.id.experience_right);
		app_list = (ListView) layout.findViewById(R.id.app_list);
		link_grid = (GridView) layout.findViewById(R.id.link_gridview);
		exp_prompt = (TextView) layout.findViewById(R.id.exp_prompt);
		
		//初始化变量
		currPage = 1;
		left_inited = false;
		right_inited = false;
		isInSubpage = false;
		curr_position = MainActivity.FLAG_NOT_STARTED;
		
		left_listener = new TitleButtonListener(1, R.drawable.experience_left_on, 
				right_button, R.drawable.experience_right_off, 
				(View) link_grid, (View) app_list, false);
		left_button.setOnClickListener(left_listener);

		right_listener = new TitleButtonListener(2, R.drawable.experience_right_on, 
				left_button, R.drawable.experience_left_off, 
				(View) app_list, (View) link_grid, false);
		right_button.setOnClickListener(right_listener);
		
		checkUpdate();
      
        return view;
	}
	
	/**
	 * 保存当前正在更新的应用列表项编号。与二级列表的显示有关。
	 * @param position 编号
	 */
	public static void setCurrPosition(int position) {
		curr_position = position;
	}

	/**
	 * 标题按钮侦听
	 * @author Jingwen Gao
	 */
	private class TitleButtonListener implements Button.OnClickListener {
		private View viewOn, viewOff;
		private Button peer;
		private int res1, res2;
		private int pageIndex;
		private boolean inited;
		
		public TitleButtonListener(int index, int resIDOwn, Button peer, int resIDPeer,  
				View on, View off, boolean inited) {
			this.viewOn = on;
			this.viewOff = off;
			this.peer = peer;
			this.res1 = resIDOwn;
			this.res2 = resIDPeer;
			this.pageIndex = index;
			this.inited = inited;
		}
		
		public void setInited(boolean inited) {
			this.inited = inited;
		}
		
		@Override
		public void onClick(View arg0) {
			currPage = pageIndex;
			initView(pageIndex);
			((Button)arg0).setBackgroundResource(res1);
			peer.setBackgroundResource(res2);
			if (inited) {
				viewOn.setVisibility(View.VISIBLE);
			}
			else {
				viewOn.setVisibility(View.GONE);
				if (isCacheExists(pageIndex)) {
					initViewFromCache(pageIndex);
				}
				else {
					expDataUnavailable();
				}
			}
			viewOff.setVisibility(View.GONE);
		}
		
	}
	
	/**
	 * 初始化指定页面。
	 * @param index 指定页面的编号
	 */
	private void initView(int index) {
		switch(index) {
		case 1:
			//Initiate link grid view
			if (left_inited == false) {
				ArrayList<HashMap<String, String>> netlinklist = MainActivity.getNetLinksList();
				if ((netlinklist != null) && (netlinklist.size() != 0)) { 
					freshLinkGrid(netlinklist);
					left_inited = true;
					left_listener.setInited(left_inited);
				} 
				else {
					if (isCacheExists(index)) {
						initViewFromCache(index);
					}
					else {
						expDataUnavailable();
					}
				}
			}
			break;
		case 2:
			//Initiate app list view
			if (right_inited == false) {
				ArrayList<HashMap<String, Object>> netapplist = MainActivity.getMatchedAppList();
				if ((netapplist != null) && (netapplist.size() != 0)) {		
					freshAppList(netapplist);
					right_inited = true; 
					right_listener.setInited(right_inited);
				}
				else {
					if (isCacheExists(index)) {
						initViewFromCache(index);
					}
					else {
						expDataUnavailable();
					}
				}
			}
			break;
		}
		
		if (left_inited && right_inited) {
			MainActivity.cancelNotifyUpdateFragment(MainActivity.EXP_INDEX);
		}
		
	}
	
	/**
	 * 用缓存文件初始化指定页面。
	 * @param index 页面编号
	 */
	private void initViewFromCache(int index) {
		switch(index) {
		case 1:
			//Initiate link grid view
			if (left_inited == false) {
				MainActivity.useCacheExpList = true;
				ArrayList<HashMap<String, String>> netlinklist_cache = MainActivity.initNetLinksListCache();
				if ((netlinklist_cache != null) && (netlinklist_cache.size() != 0)) { 
					freshLinkGrid(netlinklist_cache);
					
					left_inited = true;
					left_listener.setInited(left_inited);
				} 
				else {
					expDataUnavailable();
				}
			}
			break;
		case 2:
			//Initiate app list view
			if (right_inited == false) {
				MainActivity.useCacheAppList = true;
				ArrayList<HashMap<String, Object>> netapplist_cache = 
						MainActivity.initMatchedAppListCache(mActivity.getApplicationContext());
				if ((netapplist_cache != null) && (netapplist_cache.size() != 0)) {		
					freshAppList(netapplist_cache);

					right_inited = true; 
					right_listener.setInited(right_inited);
				}
				else {
					expDataUnavailable();
				}
			}
			break;
		}
		
		if (left_inited && right_inited) {
			MainActivity.cancelNotifyUpdateFragment(MainActivity.EXP_INDEX);
		}
		
	}
	
	/**
	 * Function to show upper grid view, called after the downloading of the xml file.
	 * @param linksList
	 */
	private void freshLinkGrid(ArrayList<HashMap<String, String>> linksList) {
		exp_prompt.setVisibility(View.GONE);
		link_grid.setVisibility(View.VISIBLE);
		//为GridView设置适配器 
		link_grid.setAdapter(new LinkGridViewAdapter(mActivity, linksList)); 
        //注册监听事件 
		link_grid.setOnItemClickListener(new GridItemListener(linksList)); 
	}
	
	private class GridItemListener implements OnItemClickListener {
		ArrayList<HashMap<String, String>> linksList;
		
		public GridItemListener(ArrayList<HashMap<String, String>> list) {
			linksList = list;
		}

		@Override
		public void onItemClick(AdapterView<?> parent, View v, int position, long id) 
        { 
            String link_url = linksList.get(position).get(MainActivity.KEY_LINK_URL);
            Uri uri = Uri.parse(link_url);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        } 
	}
	
	
	/**
	 * Function to show app list, called after the downloading of the xml file.
	 * @param appsList
	 */
	private void freshAppList(ArrayList<HashMap<String, Object>> appsList) {
		exp_prompt.setVisibility(View.GONE);
		app_list.setVisibility(View.VISIBLE);
		// 添加适配器
		if (adapter == null) {
			adapter = new AppListViewAdapter(mActivity, appsList);
		}
		else {
			adapter.refresh(appsList);
		}
		app_list.setAdapter(adapter);
		// 为单一列表行添加单击事件
		itemListener = new MyListItemListener(mActivity.getApplicationContext(), appsList);
		app_list.setOnItemClickListener(itemListener);
	}
	
	
	/**
     * List item listener for displaying the secondary contents of each item.
     * 
     * @author Jingwen Gao
     */
	private class MyListItemListener implements OnItemClickListener {
		Context context;
		ArrayList<HashMap<String, Object>> list;
		
		public MyListItemListener(Context c, ArrayList<HashMap<String, Object>> l) {
			context = c;
			list = l;
		}
		
		@Override
		public void onItemClick(AdapterView<?> parent, View view,
				int position, long id) {
			//ViewHolder must be static to be called from another static class!
			ViewHolder viewholder = new ViewHolder(position, view);  //Holds the info of the item clicked
			
			LinearLayout screenshots = (LinearLayout) appinfo_view.findViewById(R.id.screenshots);
			TextView item_specification = (TextView) appinfo_view.findViewById(R.id.appinfo_intro);
			ImageView item_image = (ImageView) appinfo_view.findViewById(R.id.appinfo_list_image);
//			TextView item_title_text = (TextView) appinfo_view.findViewById(R.id.appinfo_title_text);
			TextView item_name = (TextView) appinfo_view.findViewById(R.id.appinfo_name);
			TextView item_version = (TextView) appinfo_view.findViewById(R.id.appinfo_version);
			Button item_install_button = (Button) appinfo_view.findViewById(R.id.appinfo_install_button);
			
			AppListViewAdapter.clone(viewholder.list_image, item_image);
//			AppListViewAdapter.clone(viewholder.name, item_title_text);
			AppListViewAdapter.clone(viewholder.name, item_name);
			AppListViewAdapter.clone(viewholder.specification, item_specification);
			AppListViewAdapter.clone(viewholder.version, item_version);

			HashMap<String, Object> app = list.get(position);
	        
			AppListViewAdapter.installButtonState(item_install_button, false, 
					MainActivity.getMatchedAppsListCurState(position), 
					position, 
					(String) app.get(MainActivity.KEY_NAME), 
					(String) app.get(MainActivity.KEY_INSTALL_URL));
			
			ImageLoader imageLoader = new ImageLoader(context);
			imageLoader.displayImage((String) app.get(MainActivity.KEY_SCREENSHOT1), 
					(ImageView) screenshots.findViewById(R.id.img1));
			imageLoader.displayImage((String) app.get(MainActivity.KEY_SCREENSHOT2), 
					(ImageView) screenshots.findViewById(R.id.img2));
			imageLoader.displayImage((String) app.get(MainActivity.KEY_SCREENSHOT3), 
					(ImageView) screenshots.findViewById(R.id.img3));
			imageLoader.displayImage((String) app.get(MainActivity.KEY_SCREENSHOT4), 
					(ImageView) screenshots.findViewById(R.id.img4));
			imageLoader.displayImage((String) app.get(MainActivity.KEY_SCREENSHOT5), 
					(ImageView) screenshots.findViewById(R.id.img5));

			HorizontalScrollView screenShots = (HorizontalScrollView) appinfo_view.findViewById(R.id.screenshots_scrollview);
			screenShots.requestDisallowInterceptTouchEvent(true); 
			
			setCurrPosition(position);
			main_view.setVisibility(View.GONE);
			appinfo_view.setVisibility(View.VISIBLE);
			isInSubpage = true;
		}
	}
	
	/**
	 * Refresh subpage of app list.
	 */
	private void refreshSubpage() {
		if (curr_position >= 0) {
			Button subpage_install_button = (Button) 
					appinfo_view.findViewById(R.id.appinfo_install_button);
			
			HashMap<String, Object> curr_app = 
					MainActivity.getMatchedAppList().get(curr_position);
			
			AppListViewAdapter.installButtonState(
					subpage_install_button, false, 
					MainActivity.getMatchedAppsListCurState(curr_position), 
					curr_position, 
					(String) curr_app.get(MainActivity.KEY_NAME), 
					(String) curr_app.get(MainActivity.KEY_INSTALL_URL));
		}
	}
	
	/**
	 * If the required data to display view can not be obtained, prompt user.
	 */
	public void expDataUnavailable() {
		if (MainActivity.isConnected == false) {
			exp_prompt.setText(R.string.server_unconnectable);
		}
		else {
			if(MainActivity.loadingData) {
				exp_prompt.setText(R.string.loading_data);
			}
			else {
				exp_prompt.setText(R.string.default_prompt);
			}
		}
		
		exp_prompt.setVisibility(View.VISIBLE);
	}
	
	/**
	 * 检查指定的配置文件缓存是否存在。
	 * @param selector 选择器
	 * @return 存在返回true，否则返回false
	 */
	public boolean isCacheExists(int selector) {
		boolean result = false;
		File file;
		
		switch (selector) {
		case 1:
			file = new File(MainActivity.DATADIR + MainActivity.APPLIST_CACHE);
			result = file.exists();
			break;
		case 2:
			file = new File(MainActivity.DATADIR + MainActivity.EXPLIST_CACHE);
			result = file.exists();
			break;
		}
		
		return result;
	}
	
	/**
	 * 负责整个体验区界面的检查更新、获取数据更新界面等功能。
	 */
	public void checkUpdate() {
		initView(currPage);
		
		//检查是否有应用安装过程
		if (curr_position >= 0) {
			//检查是否安装成功
			String curr_pkg = MainActivity.getMatchedAppPackage(curr_position);
			if (Toolbox.isAppInstalled(mActivity.getApplicationContext(), curr_pkg)) {
				MainActivity.updateMatchedAppsListState(curr_position, MainActivity.INSTALLED);
	    	}
	    	else {
	    		MainActivity.restoreMatchedAppsListState(curr_position);
	    	}
			
			//更新view
			if (isInSubpage) {
				refreshSubpage();
				MainActivity.notifyUpdateFragment(MainActivity.EXP_INDEX);
			}
			else {
				ArrayList<HashMap<String, Object>> netapplist = MainActivity.getMatchedAppList();
				if ((netapplist != null) && (netapplist.size() != 0)) {		
					freshAppList(netapplist);
				}
			}
			
			curr_position = MainActivity.FLAG_NOT_STARTED;
		}
	}
	
	/**
	 * 处理体验区返回键事件。如果当前在二级页面，则返回应用列表，并检查列表更新。
	 */
	public void backToAppList() {
		isInSubpage = false;
		
		//检查列表更新
		if (MainActivity.getUpdateFragmentFlag(MainActivity.EXP_INDEX) == true) {
			ArrayList<HashMap<String, Object>> netapplist = MainActivity.getMatchedAppList();
			if ((netapplist != null) && (netapplist.size() != 0)) {		
				freshAppList(netapplist);
			}
			
			if (left_inited && right_inited) {
				MainActivity.cancelNotifyUpdateFragment(MainActivity.EXP_INDEX);
			}
		}
		
		appinfo_view.setVisibility(View.GONE);
		main_view.setVisibility(View.VISIBLE);
	}
}

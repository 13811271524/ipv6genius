package com.application.ipv6genius;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

/**
 * “体验”区上半部分GridView适配器
 * @author Jingwen Gao
 *
 */
public class LinkGridViewAdapter extends BaseAdapter{ 
	private static Activity activity; 
    //图片、链接数组 
    private static ArrayList<HashMap<String, String>> data;
    private static LayoutInflater inflater=null;
    public ImageLoader imageLoader; //用来下载图片的类
    
    public LinkGridViewAdapter(Activity a, ArrayList<HashMap<String, String>> d){ 
    	activity = a;
        data=d;
        inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        imageLoader=new ImageLoader(activity.getApplicationContext());
    } 
    public int getCount() { 
        return data.size(); 
    } 

    public Object getItem(int item) { 
        return item; 
    } 

    public long getItemId(int id) { 
        return id; 
    } 
     
    //创建View方法 
    public View getView(int position, View convertView, ViewGroup parent) { 
    	UpperGridViewHolder viewholder;
    	if (data!=null) {
		    if (convertView == null) { 
		    	convertView = inflater.inflate(R.layout.grid_item, null);
	    		viewholder = new UpperGridViewHolder(position, convertView);
	    		viewholder = new UpperGridViewHolder(position, convertView);
    	        convertView.setTag(viewholder);
		    }  
		    else { 
		    	viewholder = (UpperGridViewHolder) convertView.getTag(); 
		    } 
		    
		    HashMap<String, String> link = new HashMap<String, String>();
			link = data.get(position);
	        String link_logo = link.get(MainActivity.KEY_LINK_LOGO);
	        imageLoader.displayImage(link_logo, viewholder.link_logo);
	        viewholder.link_url = link.get(MainActivity.KEY_LINK_URL);
		}
    	
		return convertView;
    } 
    
    public static class UpperGridViewHolder   
    {  
    	public int position;  
    	public ImageView link_logo;
        public String link_url;  
        
        public UpperGridViewHolder (int position, View view) {
        	this.position = position;
        	this.link_logo = (ImageView) view.findViewById(R.id.link_logo);	
        }
    }  
} 
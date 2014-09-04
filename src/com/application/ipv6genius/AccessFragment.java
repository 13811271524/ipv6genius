package com.application.ipv6genius;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import android.annotation.SuppressLint;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

@SuppressLint("HandlerLeak")
public class AccessFragment extends Fragment{
	public static String hostipv4;  
	public static String hostipv6;
	private static String isConfigurableResult;
	private FragmentActivity mActivity;
	private RelativeLayout layout;
	private TextView information_tv;
	private ImageView connection_state;
	private Button open_button;
	private int tunnelState;	//隧道状态
	private final int NO_TUNNEL = 0, TUNNEL_UNCHANGED = 1, TUNNEL_CHANGED = 2; //隧道状态可能的值
	private String addrInfo;
	private InitAccessFragmentThread initTh;
	private MyToggleButtonListener listener;
	
	@SuppressLint("HandlerLeak")
	Handler mHandler = new Handler() {
    	@Override
    	public void handleMessage(Message msg) {
    		switch (msg.what) {
    		case MainActivity.ACC_NOTIFY:
    			//Check if the host had ipv4 address
    	        if(hostipv4 != null) {
    	        	//Check if the host had ipv6 address
    		        if(hostipv6 != null) {
    		        	switch (tunnelState) {
    		        	case NO_TUNNEL:
    		        		Log.v("Debug-handleMessage", "----本身具有IPv6地址");
    		        		connection_state.setImageResource(R.drawable.main_success);
    		        		information_tv.setText(addrInfo);
    		        		information_tv.setGravity(Gravity.LEFT);
    						//Show close button
    		        		listener.setOpenButtonState(true);
    		        		open_button.setEnabled(false);
    		        		break;
    		        	case TUNNEL_UNCHANGED:
    		        		Log.v("Debug-handleMessage", "----IPv4地址无变化");
    		        		connection_state.setImageResource(R.drawable.main_success);
    		        		information_tv.setText(addrInfo);
    		        		information_tv.setGravity(Gravity.LEFT);
    						//Show close button
    		        		listener.setOpenButtonState(true);
    		        		open_button.setEnabled(true);
    		        		break;
    		        	case TUNNEL_CHANGED:
    		        		Log.v("Debug-handleMessage", "----IPv4地址变化：需要重建");
    		        		//关闭隧道
			        		closeTunnel();
			        		listener.setOpenButtonState(false);
			        		//重建隧道
			        		listener.buttonDoSomething();
			        		open_button.setEnabled(true);
    		        		break;
    		        	}
    				}
    		        else {
    		        	connection_state.setImageResource(R.drawable.main_init);
    		        	information_tv.setText("试试点击按钮建立隧道吧 ^_^");
    		        	information_tv.setGravity(Gravity.CENTER);
    		        	listener.setOpenButtonState(false);
    		        	open_button.setEnabled(true);
    		        }
    	        }
    	        else {
    	        	connection_state.setImageResource(R.drawable.main_fail);
    	        	information_tv.setText("无法连接到网络！");
    	        	information_tv.setGravity(Gravity.CENTER);
    	        	//Close tunnel
    	        	closeTunnel();
    	        	open_button.setEnabled(true);
    	        	listener.setOpenButtonState(false);
    	        }
    			
    			break;
    		}
    		super.handleMessage(msg);
    	}
    };
	
	public View onCreateView(LayoutInflater inflater, ViewGroup c,
			Bundle savedInstanceState) { 
		Log.v("Debug", "1-onCreateView");
		View view = inflater.inflate(R.layout.fragment2, c, false);
		
		mActivity = getActivity();
		
		//初始化元件
		layout = (RelativeLayout) view.findViewById(R.id.access_layout);
		connection_state = (ImageView) layout.findViewById(R.id.connection_state);
        open_button = (Button) layout.findViewById(R.id.open_button);
        information_tv = (TextView) layout.findViewById(R.id.information_tv);
        
        //初始化变量
        hostipv4 = null;
        hostipv6 = null;
        isConfigurableResult = null;
        
        initAccessFragment();
        
        listener = new MyToggleButtonListener(open_button);
        //Open button listener
        open_button.setOnClickListener(listener);
        
        return view;
	}
	
	/**
     * Init AccessFragment.
     * 
     */
	public void initAccessFragment() {
		if (initTh != null) {
        	initTh.stopInit();
        }
        initTh = new InitAccessFragmentThread(mHandler);
        initTh.start();
        MainActivity.cancelNotifyUpdateFragment(MainActivity.ACC_INDEX);
	}
	
	private class MyToggleButtonListener implements Button.OnClickListener {
		private Button button;
		private boolean currentStateOn = false;
		
		public MyToggleButtonListener(Button button) {
			this.button = button;
		}

		@Override
		public void onClick(View v) {
			button = (Button) v;
			setOpenButtonState(currentStateOn);
			buttonDoSomething();
		}
		
		public void setOpenButtonState(boolean state) {
			currentStateOn = state;
			if (button != null) {
				if (currentStateOn) {
					button.setBackgroundResource(R.drawable.button_open_green);
				}
				else {
					button.setBackgroundResource(R.drawable.button_open_grey);
				}
			}
			else {
				Log.e("Debug-setOpenButtonState", "button = null");
			}
		}
		
		public void buttonDoSomething() {
			if (button != null) {
				if (!currentStateOn) {
					button.setBackgroundResource(R.drawable.button_open_green);
					openAction();
				}
				else {
					button.setBackgroundResource(R.drawable.button_open_grey);
					closeAction();
				}
			}
			else {
				Log.e("Debug-setOpenButtonState", "button = null");
			}
		}
		
		private void openAction() {
			//Check if the host is configurable
			isConfigurableResult = isConfigurable(hostipv4, isConfigurableResult); 
			
			//Check if the host is configurable to establish tunnel
	        if (isConfigurableResult.equals("1111")) {
        		if (openTunnel(hostipv4)) { //Tunnel opened
        			connection_state.setImageResource(R.drawable.main_success);
        			information_tv.setText(getAddrInfo());
        			information_tv.setGravity(Gravity.LEFT);
        			//Set button state
        			currentStateOn = true;
        		}
            	else { //Tunnel open error
            		connection_state.setImageResource(R.drawable.main_fail);
            		information_tv.setText(getReason(isConfigurableResult));
            		information_tv.setGravity(Gravity.LEFT);
            		setOpenButtonState(false);
            	}
	        }
	        else {
	        	connection_state.setImageResource(R.drawable.main_fail);
	        	information_tv.setText(getReason(isConfigurableResult));
	        	information_tv.setGravity(Gravity.LEFT);
	        	setOpenButtonState(false);
	        }
		}
		
		private void closeAction() {
    		//Close tunnel
    		closeTunnel();  
    		connection_state.setImageResource(R.drawable.main_init);
        	information_tv.setText("试试点击按钮建立隧道吧 ^_^");
        	information_tv.setGravity(Gravity.CENTER);
    		//Set button state
    		currentStateOn = false;
		}
	}
	
	/**
	 * Thread to perform time consuming tasks before update UI.
	 * @author Jingwen Gao
	 *
	 */
	private class InitAccessFragmentThread extends Thread{ 
		Handler handler;
		volatile boolean stop;
		
		public InitAccessFragmentThread(Handler h) {
			handler = h;
			stop = false;
		}
		
		public void stopInit() {
			stop = true;
		}
		
		public void run() {
			if (!stop) {
				//Get addr info
				addrInfo = getAddrInfo(); 
			}
			
			if (!stop) {
				if ((hostipv4 != null) && (hostipv6 != null)) {
					//Get tunnel state
					tunnelState = compareIPv6withIPv4(hostipv4, hostipv6, MainActivity.ISATAP_PREFIX); 
				}
			}
			
			if (!stop) {
				//Get configurable state
				isConfigurableResult = isConfigurable(hostipv4, isConfigurableResult); 
			}
			
			if (!stop) {
				Message m = new Message();
				m.what = MainActivity.ACC_NOTIFY;
				handler.sendMessage(m);
			}
		}
	}

	
	/**
     * Get the IPv4, IPv6, and DNS addresses information of the host
     * 
     * @return DNS addresses information as a string
     */
	public String getAddrInfo() {
		String line = "";
        StringBuilder sb = new StringBuilder(line);

		//Get host ipv6 address
        try {
			hostipv6 = Toolbox.getLocalIPv6Address();
		} catch (IOException e) {
			e.printStackTrace();
		}
        
        sb.append(getString(R.string.hostIPv6) + " ");
        if (hostipv6 != null) {
            sb.append(hostipv6);
        }
        else {
        	sb.append(getString(R.string.emptyaddr));
        }

		//Get host ipv4 address
        sb.append('\n' + getString(R.string.hostIPv4) + " ");
        hostipv4 = Toolbox.getLocalIpv4Address();
        if (hostipv4 != null) {
            sb.append(hostipv4);
        }
        else {  //If the host have got no IPv4 address
        	sb.append(getString(R.string.emptyaddr));
        }

        sb.append('\n' + getString(R.string.DNS) + " ");
        sb.append(Toolbox.getDNSAddr());

		return sb.toString();
	}
	
	 /**
     * Check if the host is configurable to establish tunnel.
     * 
     * @return "1111" if is configurable
     */
    public String isConfigurable(String ipv4, String result) {
    	StringBuilder isConfigurable = new StringBuilder("");
    	
    	if (ipv4 != null) {
    		isConfigurable.append("1");  //hostipv4 not null
    		
    		if (!Toolbox.isPrivate(ipv4)) 
    			isConfigurable.append("1");  //hostipv4 not private
    		else
    			isConfigurable.append("0");
    	}
    	else
    		isConfigurable.append("00");
    	
    	if (result == null) {  //If root and SIT have not been tested before
    		if (Toolbox.haveRoot()) 
        		isConfigurable.append("1");  //Host have root
        	else
        		isConfigurable.append("0");
        	
        	if (Toolbox.haveSIT())
        		isConfigurable.append("1");  //Host have SIT
        	else
        		isConfigurable.append("0");
    	}
    	else {
    		isConfigurable.append(result.substring(2, 4));
    	}
    	
    	return isConfigurable.toString();
    }
	
	/**
     * Edit the shell script to be executed for opening ISATAP tunnel and run it
     * 
     * @return isOpened 
     */
    public boolean openTunnel(String ipv4addr) {
    	String path0 = "tunnel0.sh";  
    	String path1 = MainActivity.DATADIR + "tunnel.sh"; 
    	File file1 = new File(path1);
    	BufferedReader reader = null;
    	BufferedWriter writer = null;
    	boolean isOpened = false;			
    	
    	//Get AssetManager
    	AssetManager asset = mActivity.getBaseContext().getAssets();
    	try {
    		reader = new BufferedReader(new InputStreamReader(asset.open(path0)));
			if (file1.exists()) {
				file1.delete(); 	
			}
			file1.createNewFile();		
			writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path1)));
			String tmp = null;
			
			//Read 1 line at a time, until 'null', the end of file
			while ((tmp = reader.readLine()) != null) {		
				if (tmp.equals("localIP=") && ipv4addr != null) {  //Hard coded
					tmp += ipv4addr;
				}
				
				writer.write(tmp);
				writer.newLine();
			}
			reader.close();
			writer.flush();
			writer.close();
		} 
    	catch (IOException e1) {
			e1.printStackTrace();
		}
    	finally {
    		if (reader != null) {
    			try {
    				reader.close();
    			}
    			catch (IOException e1){
    				e1.printStackTrace();
    			}
    		}
    	}
    	
    	//Delete existed tunnel
    	closeTunnel();
    	
    	/* Set ip tunnel: first, use "chmod" command to make sure the .sh file have got the 
    	 * permission to be executed; then, execute the .sh file.
    	 */
    	new ExecCommandWithHandler("chmod 755 " + path1, null, 0, false).execute();
    	ExecCommandWithHandler e = new ExecCommandWithHandler("sh " + path1, null, 0, false);
		e.execute();
    	if (e.getExitValue() == 0) {
			isOpened = true;
		}
		else {
			isOpened = false;
		}
		
		return isOpened;
    }
    
    /**
     * Close ISATAP tunnel
     */
    public void closeTunnel() {
		new ExecCommandWithHandler("ip tunnel del sit1", null, 0, false).execute();
    }
    
    /**
     * Decide if the ipv6 address matches the ipv6 prefix and is constructed from the ipv4 address.
     * 
     * @param ipv4
     * @param ipv6
     * @param ipv6Prefix
     * @return A constant represent the result.
     */
    public int compareIPv6withIPv4(String ipv4, String ipv6, String ipv6Prefix) {
    	int result = NO_TUNNEL;
    	
    	if (ipv6.startsWith(ipv6Prefix)) {
    		String ending = ipv6.substring(ipv6Prefix.length()+1);
    		String[] endingStrs = ending.split(":");
    		
    		if (endingStrs.length == 2) {
    			StringBuilder formerIpv4 = new StringBuilder("");
				formerIpv4.append(Integer.valueOf(endingStrs[0].substring(0, endingStrs[0].length()-2), 16));
    			formerIpv4.append("." + Integer.valueOf(endingStrs[0].substring(endingStrs[0].length()-2, endingStrs[0].length()), 16));
    			formerIpv4.append("." + Integer.valueOf(endingStrs[1].substring(0, endingStrs[1].length()-2), 16));
    			formerIpv4.append("." + Integer.valueOf(endingStrs[1].substring(endingStrs[1].length()-2, endingStrs[1].length()), 16));
    			if (ipv4.equals(formerIpv4.toString())) {
    				result = TUNNEL_UNCHANGED;
    			}
    			else {
    				result = TUNNEL_CHANGED;
    			}
    		}
    		else {
    			Log.e("Debug-compareIPv6withIPv4", "IPv6 address error: IPv6Addr should be (prefix*6 + ending*2)");
    		}
    	}
    	else {
    		//Current IPv6 address is not constructed from given IPv6 prefix
    		result = NO_TUNNEL;
    	}
    	
    	return result;
    }
    
    public String getReason(String isConfigurableResult) {
    	StringBuilder sb = new StringBuilder("");
    	int count = 0;
    	
    	//imageView1.setVisibility(View.INVISIBLE);
    	
    	sb.append(getString(R.string.unable_reason));
    	if(isConfigurableResult.charAt(0) == '0') {
    		count++;
    		sb.append(count + ". " + getString(R.string.unable_reason1) + "\n");
    	}
    	else {
    		if(isConfigurableResult.charAt(1) == '0') {
        		count++;
        		sb.append(count + ". " + getString(R.string.unable_reason2) + "\n");
        	}
    	}
    	
    	if(isConfigurableResult.charAt(2) == '0') {
    		count++;
    		sb.append(count + ". " + getString(R.string.unable_reason3) + "\n");
    	}
    	
    	if(isConfigurableResult.charAt(3) == '0') {
    		count++;
    		sb.append(count + ". " + getString(R.string.unable_reason4) + "\n");
    	}
    	
    	return sb.toString();
    }
}

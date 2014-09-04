package com.application.ipv6genius;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class DiagnosisFragment extends Fragment{
	private static String result;
	private FragmentActivity mActivity;
	private LinearLayout layout;
	private RelativeLayout tools_view;
	private ScrollView result_view;
	private Button ping_button;
	private Button traceroute_button;
	private EditText ping_text;
	private EditText traceroute_text;
	private TextView diagnosis_result;
	private ExecCommandWithHandler execCmdUtil;
	private DiagnosisHandler handler;
	private String utilName;
	public boolean isInSubpage;
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) { 
		Log.v("Debug", "2-onCreateView");
		View view = inflater.inflate(R.layout.fragment3, container, false);
		mActivity = getActivity();
		
		//初始化元件
		layout = (LinearLayout) view.findViewById(R.id.diagnosis_layout);
		tools_view = (RelativeLayout) layout.findViewById(R.id.tools);
		result_view = (ScrollView) layout.findViewById(R.id.result_view);
		ping_button = (Button) tools_view.findViewById(R.id.ping_button);
		traceroute_button = (Button) tools_view.findViewById(R.id.traceroute_button);
		ping_text = (EditText) tools_view.findViewById(R.id.ping_text);
		traceroute_text = (EditText) tools_view.findViewById(R.id.traceroute_text);
		diagnosis_result = (TextView) result_view.findViewById(R.id.diagnosis_result);
		
		//初始化变量
		handler = new DiagnosisHandler(diagnosis_result);
		utilName = null;
		isInSubpage = false;
		result = null;
		
		ping_button.setOnClickListener(new DiagnosisUtilOnClickListener(ping_text, 0, 
				diagnosis_result, handler));
		
		traceroute_button.setOnClickListener(new DiagnosisUtilOnClickListener(traceroute_text, 1, 
				diagnosis_result, handler));
        
		MainActivity.cancelNotifyUpdateFragment(MainActivity.DIAG_INDEX);
		
        return view;
	}
	
	private class DiagnosisUtilOnClickListener implements Button.OnClickListener {
		EditText editTx;
		int type;
		TextView resultTv;
		DiagnosisHandler handler;
		
		public DiagnosisUtilOnClickListener(EditText editTx, int type, 
				TextView resultTv, 
				DiagnosisHandler handler) {
			this.editTx = editTx;
			this.type = type;
			this.resultTv = resultTv;
			this.handler = handler;
		}
		
		@Override
		public void onClick(View v) {
			String input = editTx.getText().toString(); 
			if((input != null) && !input.equals("")) {
				//Show result
				tools_view.setVisibility(View.GONE);
				result_view.setVisibility(View.VISIBLE);
				isInSubpage = true;
				
				ExecCommandWithHandler util = getExecCmdUtil();
				if (util != null) {
					//Stop former command
					util.stopExecution();
					util = null;
				}
				
				setDiagnosisResult(null);
				resultTv.setText(R.string.diagnosing);
				
				new DiagnosisUtilThread(input, handler, type).start();
			}
		}
	}
	
	/**
	 * Thread to execute after an input has been entered. In this thread, the input is first validated, and then mapped into
	 * IP address. When a valid IP address is obtained, the selected command will be executed with the IP address as an input.
	 * @author Jingwen Gao
	 *
	 */
	private class DiagnosisUtilThread extends Thread {
		String input;
		DiagnosisHandler handler;
		int num;
		ExecCommandWithHandler util;
		
		public DiagnosisUtilThread(String i, DiagnosisHandler h, int n) {
			input = i;
			handler = h;
			num = n;
		}
		
		public void run(){
			setDiagnosisResult(null);
			String address = Toolbox.filterInputDiagnosis(input);
			
			if (address != null) {
				String utilName = getUtilName();
				switch(num) {
				case 0:
					util = new ExecCommandWithHandler(utilName + " ping6 -c 3 " + address, 
							handler, MainActivity.DIAGNOSIS_NOTIFY, true);
					handler.setUtil(util);
					setExecCmdUtil(util);
					util.execute();
					break;
				case 1:
					util = new ExecCommandWithHandler(utilName + " traceroute6 " + address, 
							handler, MainActivity.DIAGNOSIS_NOTIFY, true);
					handler.setUtil(util);
					setExecCmdUtil(util);
					util.execute();
					break;
				}
			}
			else {
				Message m = new Message();
				m.what = MainActivity.DIAGNOSIS_NOTIFY;
				handler.sendMessage(m);
			}
		}
	}
	
	/**
	 * Function to configure the "busybox" util: first, judge whether the host has installed the
	 * tool whose name is given by the parameter; then, if not, install the busybox util whose name 
	 * is given by String constant MainActivity.BUSYBOX_T.
	 * @param original The original name of the util.
	 * @return The name of the busybox util, either the original name, or MainActivity.BUSYBOX_T.
	 */
	private String configureUtils(String original) {
		String util;
		MainActivity.isBusyboxInstalled = Toolbox.haveBusyboxUtils(original); 
		if (MainActivity.isBusyboxInstalled) {
			util = original; 
		}
		else {
			Log.d("Debug-configureUtils", "----Installing busybox_t!");
			MainActivity.isBusyboxInstalled = Toolbox.installMBusybox(mActivity.getBaseContext());
			util = MainActivity.BUSYBOX_T;
		}
		
		return util;
	}
	
	public static void setDiagnosisResult(String input){
		result = input;
	}
	
	public void setExecCmdUtil(ExecCommandWithHandler util) {
		execCmdUtil = util;
	}
	
	public ExecCommandWithHandler getExecCmdUtil() {
		return execCmdUtil;
	}
	
	public void setUtilName(String name) {
		utilName = name;
	}
	
	public String getUtilName() {
		Log.e("Debug", "1-getUtilName = "+utilName);
		if (utilName == null) {
			//初始化busybox工具
			utilName = configureUtils(MainActivity.BUSYBOX_ORIGIN);
		}
		Log.e("Debug", "2-getUtilName = "+utilName);
		return utilName;
	}
	
	/**
	 * The handler to handle diagnosis messages.
	 * @author Jingwen Gao
	 *
	 */
	private static class DiagnosisHandler extends Handler {
		volatile ExecCommandWithHandler util;
		TextView tv;
		
		public DiagnosisHandler(TextView t) {
			tv = t;
			util = null;
		}
		
		public void setUtil(ExecCommandWithHandler e) {
			util = e;
		}
		
		public void handleMessage(Message msg) {
			switch(msg.what){
			case MainActivity.DIAGNOSIS_NOTIFY:
				if (util != null) {
					setDiagnosisResult(util.getResult());
					if(result != null) {
						tv.setText(result);
					}
				}
				else {
					if(result != null) {
						tv.setText(result);
					}
					else {
						tv.setText(R.string.diagnosing_error);
					}
				}
				break;
			}
			
			setDiagnosisResult(null);
			
			super.handleMessage(msg);
		}
	}
	
	public void backToDiagnosis() {
		isInSubpage = false;
		
		//Show result
		result_view.setVisibility(View.GONE);
		tools_view.setVisibility(View.VISIBLE);
	}
}

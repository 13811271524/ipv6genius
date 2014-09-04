package com.application.ipv6genius;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.Log;

public class Config {
        public static int getVerCode(Context context) {
                int verCode = -1;
                try {
                        verCode = context.getPackageManager().getPackageInfo(
                                        context.getPackageName(), 0).versionCode;  
                } catch (NameNotFoundException e) {
                        Log.e("Debug-Config", e.getMessage());
                }
                return verCode;
        }
        
        public static String getVerName(Context context) {
                String verName = "";
                try {
                        verName = context.getPackageManager().getPackageInfo(
                        				context.getPackageName(), 0).versionName;
                } catch (NameNotFoundException e) {
                        Log.e("Debug-Config", e.getMessage());
                }
                return verName; 

        }
        
        public static String getAppName(Context context) {
                String appName = context.getResources()
                .getText(R.string.app_name).toString();
                return appName;
        }
}

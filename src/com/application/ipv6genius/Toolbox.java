package com.application.ipv6genius;

import java.io.*;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.AssetManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import java.util.regex.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;


public class Toolbox {
	/**
     * Get local IPv4 address
     * 
     * @return result
     */
    public static String getLocalIpv4Address() {       
    	try {       
    	    for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); 
    	    		en.hasMoreElements();) {       
    	    	NetworkInterface intf = en.nextElement();       
    	        for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); 
    	        		enumIpAddr.hasMoreElements();) {       
    	        	InetAddress inetAddress = enumIpAddr.nextElement();   
    	        	// Check if it's ipv4 address and reserved address
    	            if (inetAddress instanceof Inet4Address && !isReservedAddr(inetAddress)) {       
    	            	return inetAddress.getHostAddress().toString();       
    	            }       
    	        }       
    	     }       
    	 } 
    	catch (SocketException ex) {       
    		Log.e("WifiPreference IpAddress", ex.toString());       
    	}       
    	return null;       
	}
    
	/**
     * Get local IPv6 address
     * 
     * @return result
     */
    public static String getLocalIPv6Address() throws IOException {
        InetAddress inetAddress = null;
        Enumeration<NetworkInterface> networkInterfaces = NetworkInterface
                .getNetworkInterfaces();
        outer: while (networkInterfaces.hasMoreElements()) {
            Enumeration<InetAddress> inetAds = networkInterfaces.nextElement()
                    .getInetAddresses();
            while (inetAds.hasMoreElements()) {
                inetAddress = inetAds.nextElement(); 
                // Check if it's ipv6 address and reserved address
                if (inetAddress instanceof Inet6Address && !isReservedAddr(inetAddress)) {
                    break outer;  
                }
                inetAddress = null; //In case when there is no IPv6 interface! 
            }
        }
 
        if (inetAddress != null) {
        	String ipAddr = inetAddress.getHostAddress();
        	// Filter network card No
        	int index = ipAddr.indexOf('%');
        	if (index > 0) {
        		ipAddr = ipAddr.substring(0, index);
        	}
 
        	return ipAddr;
        }
        else return null;
    }
 
    /**
     * Check if it's "local address" or "link local address" or
     * "loop back address"
     * 
     * @param ip address
     * @return result
     */
    private static boolean isReservedAddr(InetAddress inetAddr) {
        if (inetAddr.isAnyLocalAddress() || inetAddr.isLinkLocalAddress()
                || inetAddr.isLoopbackAddress()) {
            return true;
        }
 
        return false;
    }
    
    /**
     * Judge if the host can get AAAA record for a given IPv4-only site, thus determine whether
     * the DNS server of the host can perform IPv4/IPv6 translation.
     * 
     * @param site
     * @return result
     */
    public static boolean isUnderTranslation(String site){
    	boolean result = false;
    	
    	try {
			InetAddress[] addrs = InetAddress.getAllByName(site);
			for (InetAddress addr : addrs) {
				if (addr instanceof Inet6Address) {
					result = true;
				}
				Log.v("Debug-isUnderTranslation", "= " + result + ": "+addr.toString());
			}
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} 
    	
    	return result;
    }

    
    /** 
     * Determine whether the passed IP address is private or not
     * 
     * Private IP: Class A:  10.0.0.0-10.255.255.255  
     *			   Class B:  172.16.0.0-172.31.255.255  
     *			   Class C:  192.168.0.0-192.168.255.255 
     *
     * @param ip address to be determined, can't be null as an input!
     * @return isPrivate
     */
    public static boolean isPrivate(String ip) {
    	boolean isPrivate = false;
    	String pattern = "(\\b10\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\b)|" +
    	        "(\\b172\\.((1[6-9])|(2\\d)|(3[01]))\\.\\d{1,3}\\.\\d{1,3}\\b)|" +
    	        "(\\b192\\.168\\.\\d{1,3}\\.\\d{1,3}\\b)";
    	Pattern p = Pattern.compile(pattern);
    	Matcher m = p.matcher(ip);
    	isPrivate = m.matches();
    	
    	return isPrivate;
    }
    
    /**
     * Get local DNS server address: execute the "getprop | grep net.dns1" command, 
     * and get the kernel feedback, which includes the information of DNS.
     * 
     * @return dns
     */
    public static String getDNSAddr() {
    	String dns = "无";
    	
		ExecCommandWithHandler exec = new ExecCommandWithHandler("getprop net.dns1", null, 0, false);
		exec.execute();
		String result = exec.getResult();
		
		if(result != null) {
			dns = result;
			return dns;
		}
		
		return dns;
    }
    
    /**
     * Check if the host has got root privilege.
     * 
     * @return haveRoot
     */
    public static boolean haveRoot() {
    	boolean rooted = false;
		
		//通过执行测试命令来检测
		ExecCommandWithHandler exec = new ExecCommandWithHandler("echo test", null, 0, false);
		exec.execute();
		String str = exec.getResult();
		if (str.equals("test\n"))  { //Don't forget the ending "\n"
			rooted = true;
		}
    	else {
    		rooted = false;
    	}
		
    	return rooted;
    }
    
    /**
     * Check if the host has got SIT module enabled.
     * 
     * @return haveSIT
     */
    public static boolean haveSIT() {
    	boolean haveSIT = false;
    	
		ExecCommandWithHandler exec = new ExecCommandWithHandler("ip tunnel help", null, 0, false);
		exec.execute();
		String str = exec.getResult();
		if(str!=null) {
			String[] strarray1 = str.split("[|]");
			int size = strarray1.length;
			for(int i = 0; i < size; i++) {
				if (strarray1[i].equals(" sit ")) {
					haveSIT = true;
				}
			}
		}
    	
    	return haveSIT;
    }
    
    /**
     * Return the IPv6 address of given domain name.
     * 
     * @param domain name
     */
    public static String getIPv6Addr(String name){
    	String result = null;
    	
    	try {
			InetAddress addr = InetAddress.getByName(name);
			if (addr instanceof Inet6Address) {
				result = addr.getHostAddress();
				// Filter network card No
				int index = result.indexOf('%');
				if(index > 0) {
					result = result.substring(0, index);
				}
			}
		} catch (UnknownHostException e) {
			result = null;
			Log.e("getIPv6Addr", e.toString());
		} 
    	
    	return result;
    }
    
    /**
     * Diagnosis fragment: filter the input to ping6/traceroute6, and 
     * if the input is domain name, return its corresponding IPv6 address.
     * 
     * @param input
     * @return
     */
    public static String filterInputDiagnosis(String input) {
    	String result = null;
    	
		if(input.contains(":")) {
			result = input;
		}
		else {
			result = getIPv6Addr(input);
		}
    	
    	
    	return result;
    }
    
    /**
     * 获取XML DOM元素
     * @param xmlFile
     * @return
     */
    public static Document getDomElement(File xmlFile){
        Document doc = null; 
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance(); 
        try { 
  
            DocumentBuilder db = dbf.newDocumentBuilder(); 
        	doc = db.parse(xmlFile); 
            
            } catch (ParserConfigurationException e) { 
                Log.e("getDomElement-ParserConfigurationException", 
                		"Parsing file: "+xmlFile.toString()
                		+"\n"+e.getMessage()); 
                return null; 
            } catch (SAXParseException e) { 
                Log.e("getDomElement-SAXParseException", 
                		"Parsing file: "+xmlFile.toString()
                		+"\n Line: "+e.getLineNumber()+" Column: "+e.getColumnNumber()
                		+"\n"+e.getMessage()); 
                return null; 
            } catch (SAXException e) { 
                Log.e("getDomElement-SAXException", 
                		"Parsing file: "+xmlFile.toString()
                		+"\n"+e.getMessage()); 
                return null; 
            } catch (IOException e) { 
                Log.e("getDomElement-IOException", 
                		"Parsing file: "+xmlFile.toString()
                		+"\n"+e.getMessage()); 
                return null; 
            } 
  
            return doc; 
    } 
  
    /**
     * 获取节点值
     * @param elem
     * @return
     */
     public static final String getElementValue( Node elem ) { 
         Node child; 
         if( elem != null){ 
             if (elem.hasChildNodes()){ 
                 for( child = elem.getFirstChild(); child != null; child = child.getNextSibling() ){ 
                     if( child.getNodeType() == Node.TEXT_NODE  ){ 
                         return child.getNodeValue(); 
                     } 
                 } 
             } 
         } 
         return ""; 
     } 
  
     /**
      * 获取节点值
      * @param item
      * @param str
      * @return
      */
     public static String getValue(Element item, String str) { 
    	 NodeList n = item.getElementsByTagName(str); 
    	 return getElementValue(n.item(0)); 
     } 
     
     public static void CopyStream(InputStream is, OutputStream os) {
 		final int buffer_size = 1024;
 		try {
 			byte[] bytes = new byte[buffer_size];
 			for (;;) {
 				int count = is.read(bytes, 0, buffer_size);
 				if (count == -1)
 					break;
 				os.write(bytes, 0, count);
// 				is.close();  //在调用处已经close过了
// 				os.close();
 			}
 		} catch (Exception ex) {
 		}
 	}
    
    /**
     * Copy file from source to destination.
     * @param from Source
     * @param to Destination
     */
    public static void copyFile(File from, File to) {
 		try {
 			InputStream in = new FileInputStream(from);
 			OutputStream out = new FileOutputStream(to);
 			Toolbox.CopyStream(in, out);
 			out.close();
 			in.close();
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
     
     /**
      * Decide if there is available network connection for the host.
      * @param cotext
      * @return If there is available network connection for the host
      */
     public static boolean isConnectionAvailable(Context cotext)   
     {  
         boolean isAvailable = false;  
         ConnectivityManager connectivityManager = (ConnectivityManager)cotext.getSystemService(Context.CONNECTIVITY_SERVICE);  
         if (connectivityManager != null)   
         {  
             NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();                  
             if (activeNetworkInfo == null || !activeNetworkInfo.isConnected())   
             {                 
             	 isAvailable = false;  
             }   
             else   
             {  
             	 isAvailable = true;  
             }  
         }   
         else   
         {  
             Log.e("isConnectionAvailable", "Can't get connectivityManager");  
         }  
         Log.v("Debug-isConnectionAvailable", "= "+isAvailable);
         return isAvailable;  
     }  
     
//     /**
//      * Decide if the server is available with ping.
//      * @return If the server is available
//      */
//     public static boolean isServerAvailable() {
//    	 ExecCommandWithHandler exec = new ExecCommandWithHandler("ping -c 3 " + MainActivity.SERVERIP, null, 0, false);
//    	 exec.execute();
//    	 String result = exec.getResult();
//    	 if (result.contains("time=")) {
//    		 return true;
//    	 }
//    	 else {
//    		 return false;
//    	 }
//     }
     
     /**
      * Function to decide whether the host has the given busybox ping6/traceroute6 utils.
      * @param busybox The designated busybox command
      * @return
      */
     public static boolean haveBusyboxUtils(String busybox) {
    	 Log.v("Debug-haveBusyboxUtils","target = "+busybox);
    	 boolean result = false;
    	 ExecCommandWithHandler execCmdUtil = new ExecCommandWithHandler(busybox + " ping6", 
    			 null, 0, false);
    	 execCmdUtil.execute();
    	 String str1 = execCmdUtil.getResult();
    	 execCmdUtil = new ExecCommandWithHandler(busybox + " traceroute6", 
    			 null, 0, false);
    	 execCmdUtil.execute();
    	 String str2 = execCmdUtil.getResult();
    	 if(str1.contains("not found") || str2.contains("not found")) {
    		 result = false;
    		 return result;
    	 }
    	 else {
    		 if(str1.contains("Usage:") || str2.contains("Usage:")) {
    			 result = true;
    			 return result;
    		 }
    	 }
    	 
    	 return result;
     }
     
     /**
      * Copy busybox executable from app assets to designated system dir (/system/xbin/).
      * 
      * @param context Application context
      * @return The result of installation
      */
     public static boolean installMBusybox(Context context) {
    	 String busyboxName = MainActivity.BUSYBOX_T;
    	 String dir = "/system/xbin/";
    	 File des;
    	 
    	 if (!haveBusyboxUtils(busyboxName)) {
    		 FileCache cache = new FileCache(context);
        	 des = cache.getFile(busyboxName);
        	 if (!des.exists()) {
        		//Copy busybox executable from assets to app cache dir
            	 AssetManager manager = context.getAssets();
            	 try {
        			InputStream in = manager.open(busyboxName);
        			OutputStream out = new FileOutputStream(des);
        			CopyStream(in, out);
        			out.close();
        			in.close();
        		} catch (IOException e) {
        			Log.e("installMBusybox", e.toString());
        		}
        	 }
        	 
        	 //Copy busybox executable from app cache dir to designated system dir
        	 ExecCommandWithHandler execCmdUtil;
        	 execCmdUtil = new ExecCommandWithHandler("mount -o remount,rw -t yaffs2 /dev/block/mtdblock3 /system", 
 					null, 0, false);
			 execCmdUtil.execute();
			 if (!(new File(dir)).exists()) {
				 execCmdUtil = new ExecCommandWithHandler("mkdir " + dir, 
		 					null, 0, false);
				 execCmdUtil.execute();
			 }
 			 execCmdUtil = new ExecCommandWithHandler("cat " + des.getAbsolutePath() + " > " + dir + busyboxName, 
 					null, 0, false);
 			 execCmdUtil.execute();
 			 execCmdUtil = new ExecCommandWithHandler("chmod 4755 " + dir + busyboxName, 
 					null, 0, false);
 			 execCmdUtil.execute();
    	 }
    	 
    	 return haveBusyboxUtils(busyboxName);
     }
     
     /**
      * Check if a package has been installed.
      * @param context
      * @param packageName
      * @return
      */
     public static boolean isAppInstalled(Context context, String packageName) {  
    	 PackageManager packageManager = context.getPackageManager();  
    	 try {  
    		 PackageInfo pInfo = packageManager.getPackageInfo(packageName,  
    				 PackageManager.COMPONENT_ENABLED_STATE_DEFAULT); 
    		 //判断是否获取到了对应的包名信息 
    		 if(pInfo!=null){  
    			 Log.e("Debug-isAppInstalled", " installed!");
    			 return true;
    		 }  
    	 } catch (NameNotFoundException e) {  
    		 e.printStackTrace();  
    	 }  
    	 Log.e("Debug-isAppInstalled", " not installed!");
    	 return false;
	} 
}

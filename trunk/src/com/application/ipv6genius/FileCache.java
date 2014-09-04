package com.application.ipv6genius;

import java.io.File;
import android.content.Context;

public class FileCache {
    
    private File cacheDir;
    
    public FileCache(Context context){
    	//找一个用来缓存文件的路径
        if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED))
            cacheDir=new File(android.os.Environment.getExternalStorageDirectory(),"Smart6Cache");
        else
            cacheDir=context.getCacheDir();
        if(!cacheDir.exists())
            cacheDir.mkdirs();
    }
    
    public File getFile(String name){
        File f = new File(cacheDir, name);
        return f;
        
    }
    
    public void clear(){
        File[] files=cacheDir.listFiles();
        if(files==null)
            return;
        for(File f:files)
            f.delete();
    }

}

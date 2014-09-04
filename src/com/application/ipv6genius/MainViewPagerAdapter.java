package com.application.ipv6genius;

import java.util.ArrayList;
import java.util.List;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

public class MainViewPagerAdapter extends PagerAdapter {
    private final FragmentManager mFragmentManager;
    private FragmentTransaction mTransaction = null;
    private List<Fragment> mFragmentList;
    
    public MainViewPagerAdapter(FragmentManager fragmentManager) {
        mFragmentManager = fragmentManager;
        mFragmentList  = new ArrayList<Fragment>(3); 
        mFragmentList.add(new ExperienceFragment());  
        mFragmentList.add(new AccessFragment());  
        mFragmentList.add(new DiagnosisFragment());
    }
    @Override
    public int getCount() {
        return mFragmentList.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return ((Fragment) object).getView() == view;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        if (mTransaction == null) {
            mTransaction = mFragmentManager.beginTransaction();
        }
        String name = getTag(position);
        Fragment fragment = mFragmentManager.findFragmentByTag(name);
        if (fragment != null) {
            mTransaction.attach(fragment);
        } 
        else {
//            fragment = getItem(position);
        	fragment = mFragmentList.get(position);
            mTransaction.add(container.getId(), fragment,
                    getTag(position));
        }
        return fragment;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
    	if (mTransaction == null) {
            mTransaction = mFragmentManager.beginTransaction();
        }
        mTransaction.detach((Fragment) object);
    }
    
    @Override
    public void finishUpdate(ViewGroup container) {
    	if (mTransaction != null) {
            mTransaction.commitAllowingStateLoss();
            mTransaction = null;
            mFragmentManager.executePendingTransactions();
        }
    }
    
    public Fragment getItem(int position){
    	return  mFragmentManager.findFragmentByTag(getTag(position));
    }
    
    public long getItemId(int position) {
    	return position;
    }
    
    protected  String getTag(int position){
        return "Frag"+position;
    }

    public void refresh(int position) {
    	Log.v("Debug-refresh_page", "page = "+position);
    	if (mTransaction == null) {
            mTransaction = mFragmentManager.beginTransaction();
        }
        String name = getTag(position);
        Fragment fragment = mFragmentManager.findFragmentByTag(name);
        if (fragment != null) {
        	if (position == MainActivity.EXP_INDEX) {
    			((ExperienceFragment) getItem(MainActivity.EXP_INDEX)).checkUpdate();
    		}
        	else {
        		mTransaction.detach(fragment);
            	mTransaction.attach(fragment);
        	}
        } else {
            Log.e("Debug-refresh_page", "fragment"+position+" = null !");
        }
    }
}

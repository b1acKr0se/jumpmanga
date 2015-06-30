package io.wyrmise.jumpmanga;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

/**
 * Created by Thanh on 6/30/2015.
 */
public class ViewPagerAdapter extends FragmentStatePagerAdapter {
    CharSequence titles[];
    int noOfTabs;

    public ViewPagerAdapter(FragmentManager fm,CharSequence mTitles[], int mNumbOfTabsumb) {
        super(fm);
        titles = mTitles;
        noOfTabs = mNumbOfTabsumb;
    }

    @Override
    public Fragment getItem(int position) {

        if(position == 0) // if the position is 0 we are returning the First tab
        {
            InfoFragment infoFragment = new InfoFragment();
            return infoFragment;
        }
        else             // As we are having 2 tabs if the position is now 0 it must be 1 so we are returning second tab
        {
            ChapterFragment chapterFragment = new ChapterFragment();
            return chapterFragment;
        }

    }

    public Fragment newInstance(String url) {
        InfoFragment infoFragment = new InfoFragment();
        Bundle args = new Bundle();
        args.putString("image_url", url);
        infoFragment.setArguments(args);
        return infoFragment;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return titles[position];
    }

    @Override
    public int getCount() {
        return noOfTabs;
    }



}

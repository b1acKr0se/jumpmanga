package io.wyrmise.jumpmanga;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

/**
 * Created by Thanh on 6/30/2015.
 */
public class ViewPagerAdapter extends FragmentStatePagerAdapter {

    final int PAGE_COUNT = 2;
    private String titles[] = new String[]{"Information", "Chapter"};
    private Context context;


    public ViewPagerAdapter(FragmentManager fm, Context context) {
        super(fm);
        this.context = context;
    }

    @Override
    public Fragment getItem(int position) {

        if (position == 0)
        {
            InfoFragment infoFragment = new InfoFragment();
            return infoFragment;
        } else
        {
            ChapterFragment chapterFragment = new ChapterFragment();
            return chapterFragment;
        }

    }

    @Override
    public CharSequence getPageTitle(int position) {
        return titles[position];
    }

    @Override
    public int getCount() {
        return PAGE_COUNT;
    }


}

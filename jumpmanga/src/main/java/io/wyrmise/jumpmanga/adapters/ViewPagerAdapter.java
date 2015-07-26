package io.wyrmise.jumpmanga.adapters;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import io.wyrmise.jumpmanga.fragments.ChapterFragment;
import io.wyrmise.jumpmanga.fragments.InfoFragment;

/**
 * Created by Thanh on 6/30/2015.
 */
public class ViewPagerAdapter extends FragmentStatePagerAdapter {

    final int PAGE_COUNT = 2;
    private String titles[];
    private Context context;


    public ViewPagerAdapter(FragmentManager fm, Context context, String[] t) {
        super(fm);
        this.context = context;
        titles = t;
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

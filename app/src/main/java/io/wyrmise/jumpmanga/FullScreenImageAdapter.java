package io.wyrmise.jumpmanga;

import android.app.Activity;
import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by Thanh on 7/1/2015.
 */
public class FullScreenImageAdapter extends PagerAdapter {
    private Activity activity;
    private ArrayList<Page> pages;
    private LayoutInflater layoutInflater;

    public FullScreenImageAdapter(Activity a, ArrayList<Page> p) {
        activity = a;
        pages = p;
    }

    @Override
    public int getCount() {
        return pages.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == ((LinearLayout) object);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        ImageView imageView;
        layoutInflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = layoutInflater.inflate(R.layout.fullscreen_image_slider, container, false);
        imageView = (ImageView) layout.findViewById(R.id.img);

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((ReaderActivity)activity).onClick(view);
            }
        });

        Picasso.with(activity.getApplicationContext()).load(pages.get(position).getUrl()).into(imageView);

        ((ViewPager) container).addView(layout);

        return layout;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        ((ViewPager) container).removeView((LinearLayout) object);
    }
}

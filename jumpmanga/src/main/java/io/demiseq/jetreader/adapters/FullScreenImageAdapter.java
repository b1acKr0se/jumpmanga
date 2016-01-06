package io.demiseq.jetreader.adapters;

import android.app.Activity;
import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import io.demiseq.jetreader.R;
import io.demiseq.jetreader.activities.ReadActivity;
import io.demiseq.jetreader.model.Page;
import io.demiseq.jetreader.widget.TouchImageView;

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
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        TouchImageView imageView;
        layoutInflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = layoutInflater.inflate(R.layout.fullscreen_image_slider, container, false);
        imageView = (TouchImageView) layout.findViewById(R.id.img);

        layout.setTag(position);

        imageView.resetZoom();

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((ReadActivity) activity).onClick(view);
            }
        });

        Picasso.with(activity.getApplicationContext()).load(pages.get(position).getUrl()).placeholder(R.drawable.page_placeholder)
                .error(R.drawable.page_error).into(imageView, new Callback() {

            @Override
            public void onSuccess() {
                ((ReadActivity) activity).updateProgress();
            }

            @Override
            public void onError() {

            }
        });

        imageView.resetZoom();

        container.addView(layout);

        return layout;
    }


    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((LinearLayout) object);
    }
}

package io.demiseq.jetreader.adapters;

import android.app.Activity;
import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;

import io.demiseq.jetreader.R;
import io.demiseq.jetreader.activities.DownloadedReadActivity;
import io.demiseq.jetreader.activities.ReadActivity;
import io.demiseq.jetreader.widget.TouchImageView;

public class DownloadedImageAdapter extends PagerAdapter {
    private Activity activity;
    private ArrayList<String> imagePaths = new ArrayList<String>();
    private LayoutInflater layoutInflater;
    private boolean fromDownloaded;

    public DownloadedImageAdapter(Activity a, ArrayList<String> path, boolean fromDownloaded) {
        activity = a;
        imagePaths = path;
        this.fromDownloaded = fromDownloaded;
    }

    @Override
    public int getCount() {
        return imagePaths.size();
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

        if(!fromDownloaded)
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((ReadActivity) activity).onClick(view);
            }
        });
        else
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ((DownloadedReadActivity) activity).onClick(view);
                }
            });

        Picasso.with(activity.getApplicationContext()).load(new File(imagePaths.get(position))).placeholder(R.drawable.page_placeholder)
                .error(R.drawable.page_error).into(imageView);

        imageView.resetZoom();

        container.addView(layout);

        return layout;
    }


    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((LinearLayout) object);
    }
}
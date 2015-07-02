package io.wyrmise.jumpmanga;

/**
 * Created by Thanh on 6/29/2015.
 */

import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.List;

import io.wyrmise.jumpmanga.model.Manga;

public class MangaAdapter extends RecyclerView.Adapter<MangaAdapter.ViewHolder> implements View.OnClickListener {
    private List<Manga> items;
    private OnItemClickListener onItemClickListener;

    public MangaAdapter(List<Manga> items) {
        this.items = items;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    @Override public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.manga_recycler_items, parent, false);
        v.setOnClickListener(this);
        return new ViewHolder(v);
    }

    @Override public void onBindViewHolder(final ViewHolder holder, int position) {
        Manga item = items.get(position);
        holder.text.setText(item.getName());
        holder.latest.setText(item.getLatest());
        holder.text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(holder.latest.getVisibility()==TextView.GONE) holder.latest.setVisibility(TextView.VISIBLE);
                else holder.latest.setVisibility(TextView.GONE);
            }
        });
        holder.image.setImageBitmap(null);

        Picasso.with(holder.image.getContext()).load(item.getImage())
                .into(holder.image, new Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError() {

                    }
                });
        holder.itemView.setTag(item);
    }

    @Override public int getItemCount() {
        return items.size();
    }

    @Override public void onClick(final View v) {
        // Give some time to the ripple to finish the effect
        if (onItemClickListener != null) {
            new Handler().postDelayed(new Runnable() {
                @Override public void run() {
                    onItemClickListener.onItemClick(v, (Manga) v.getTag());
                }
            }, 200);
        }
    }

    protected static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView image;
        public TextView text, latest;

        public ViewHolder(View itemView) {
            super(itemView);
            image = (ImageView) itemView.findViewById(R.id.image);
            text = (TextView) itemView.findViewById(R.id.text);
            latest = (TextView) itemView.findViewById(R.id.latest);
        }
    }

    public interface OnItemClickListener {

        void onItemClick(View view, Manga manga);

    }
}

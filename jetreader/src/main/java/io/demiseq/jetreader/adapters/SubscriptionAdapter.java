package io.demiseq.jetreader.adapters;

import android.content.Context;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import io.demiseq.jetreader.R;
import io.demiseq.jetreader.model.Manga;

public class SubscriptionAdapter extends RecyclerView.Adapter<SubscriptionAdapter.ViewHolder> implements View.OnClickListener {
    private ArrayList<Manga> recent_list;
    private MangaAdapter.OnItemClickListener onItemClickListener;
    private Context context;

    public SubscriptionAdapter(Context c, ArrayList<Manga> list) {
        context = c;
        recent_list = list;
    }

    public void setOnItemClickListener(MangaAdapter.OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.subscription_items, parent, false);
        v.setOnClickListener(this);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Manga manga = recent_list.get(position);
        holder.manga_name.setText(manga.getName());
        holder.chapter_name.setText(manga.getChapter().getName());
        if (!manga.getImage().equals("")) {
            Glide.with(context).load(manga.getImage()).error(R.drawable.error).into(holder.image);
        }
        holder.itemView.setTag(manga);
    }

    @Override
    public int getItemCount() {
        return recent_list.size();
    }

    @Override
    public void onClick(final View v) {
        // Give some time to the ripple to finish the effect
        if (onItemClickListener != null) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    onItemClickListener.onItemClick(v, (Manga) v.getTag());
                }
            }, 200);
        }
    }

    protected class ViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.manga_thumbnail) ImageView image;
        @Bind(R.id.manga_name) TextView manga_name;
        @Bind(R.id.chapter_name) TextView chapter_name;

        public ViewHolder(View v) {
            super(v);
            ButterKnife.bind(this,v);
        }
    }
}

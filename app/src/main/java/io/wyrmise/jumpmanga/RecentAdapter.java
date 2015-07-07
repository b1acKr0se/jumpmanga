package io.wyrmise.jumpmanga;

import android.content.Context;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import io.wyrmise.jumpmanga.model.Manga;

/**
 * Created by Thanh on 7/5/2015.
 */
public class RecentAdapter extends RecyclerView.Adapter<RecentAdapter.ViewHolder> implements View.OnClickListener {
    private ArrayList<Manga> recent_list;
    private MangaAdapter.OnItemClickListener onItemClickListener;
    private Context context;

    public RecentAdapter(Context c, ArrayList<Manga> list) {
        context = c;
        recent_list = list;
    }

    public void setOnItemClickListener(MangaAdapter.OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.recent_items, parent, false);
        v.setOnClickListener(this);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Manga manga = recent_list.get(position);
        holder.manga_name.setText(manga.getName());
        holder.chapter_name.setText(manga.getRecent().getName());
        if (!manga.getImage().equals("")) {
            Picasso.with(context).load(manga.getImage()).error(R.drawable.error).into(holder.image);
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
        public ImageView image;
        public TextView manga_name, chapter_name;

        public ViewHolder(View v) {
            super(v);
            image = (ImageView) v.findViewById(R.id.manga_thumbnail);
            manga_name = (TextView) v.findViewById(R.id.manga_name);
            chapter_name = (TextView) v.findViewById(R.id.chapter_name);
        }
    }
}




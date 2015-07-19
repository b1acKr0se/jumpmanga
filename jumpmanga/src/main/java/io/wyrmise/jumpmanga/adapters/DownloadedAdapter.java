package io.wyrmise.jumpmanga.adapters;

import android.content.Context;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import io.wyrmise.jumpmanga.R;
import io.wyrmise.jumpmanga.model.Chapter;
import io.wyrmise.jumpmanga.model.Manga;

public class DownloadedAdapter extends RecyclerView.Adapter<DownloadedAdapter.ViewHolder> implements View.OnClickListener {

    private ArrayList<Chapter> chapters;
    private Context context;
    private OnItemClickListener onItemClickListener;

    public DownloadedAdapter(Context c, ArrayList<Chapter> list) {
        context = c;
        chapters = list;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.downloaded_items, parent, false);
        v.setOnClickListener(this);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Chapter chapter = chapters.get(position);
        holder.manga_name.setText(chapter.getMangaName());
        holder.chapter_name.setText(chapter.getName());
        holder.itemView.setTag(chapter);
    }

    @Override
    public int getItemCount() {
        return chapters.size();
    }

    @Override
    public void onClick(final View v) {
        // Give some time to the ripple to finish the effect
        if (onItemClickListener != null) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    onItemClickListener.onItemClick(v, (Chapter) v.getTag());
                }
            }, 200);
        }
    }

    protected class ViewHolder extends RecyclerView.ViewHolder {
        public TextView manga_name, chapter_name;

        public ViewHolder(View v) {
            super(v);
            manga_name = (TextView) v.findViewById(R.id.manga_name);
            chapter_name = (TextView) v.findViewById(R.id.chapter_name);
        }
    }


    public interface OnItemClickListener {

        void onItemClick(View view, Chapter chapter);

    }

}

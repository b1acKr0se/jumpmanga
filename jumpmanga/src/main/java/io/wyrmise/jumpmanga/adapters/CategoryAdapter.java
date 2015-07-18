package io.wyrmise.jumpmanga.adapters;

import android.content.Context;
import android.os.Handler;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.List;

import io.wyrmise.jumpmanga.utils.OnLoadMoreListener;
import io.wyrmise.jumpmanga.R;
import io.wyrmise.jumpmanga.model.Manga;

/**
 * Created by Thanh on 7/13/2015.
 */
public class CategoryAdapter extends RecyclerView.Adapter implements View.OnClickListener {
    private final int VIEW_ITEM = 1;
    private final int VIEW_PROG = 0;
    private int visibleThreshold = 5;

    private int lastVisibleItem, totalItemCount;
    private boolean loading;
    private OnLoadMoreListener onLoadMoreListener;

    private List<Manga> items;
    private OnItemClickListener onItemClickListener;

    private Context context;

    public CategoryAdapter(Context c, List<Manga> items, RecyclerView recyclerView) {
        context = c;
        this.items = items;
        if (recyclerView.getLayoutManager() instanceof LinearLayoutManager) {
            final LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();

            recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    totalItemCount = linearLayoutManager.getItemCount();
                    lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition();
                    if (!loading && totalItemCount < (lastVisibleItem + visibleThreshold)) {
                        //reach the end
                        if (onLoadMoreListener != null) {
                            onLoadMoreListener.onLoadMore();
                        }
                        loading = true;
                    }
                }
            });
        }
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position) != null ? VIEW_ITEM : VIEW_PROG;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        RecyclerView.ViewHolder vh;
        if (viewType == VIEW_ITEM) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.category_items, parent, false);
            v.setOnClickListener(this);
            vh = new MangaViewHolder(v);
        } else {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.progress_view, parent, false);
            vh = new ProgressViewHolder(v);
        }
        return vh;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof MangaViewHolder) {
            Manga item = items.get(position);
            ((MangaViewHolder) holder).text.setText(item.getName());
            ((MangaViewHolder) holder).latest.setText(item.getLatest());
            ((MangaViewHolder) holder).image.setImageBitmap(null);

            if (!item.getImage().equals("")) {
                Picasso.with(((MangaViewHolder) holder).image.getContext()).load(item.getImage()).placeholder(R.drawable.placeholder).error(R.drawable.error)
                        .into(((MangaViewHolder) holder).image, new Callback() {
                            @Override
                            public void onSuccess() {

                            }

                            @Override
                            public void onError() {

                            }
                        });
            } else {
                ((MangaViewHolder) holder).image.setImageDrawable(context.getResources().getDrawable(R.drawable.error));
            }

            holder.itemView.setTag(item);
        } else {
            ((ProgressViewHolder) holder).progressBar.setIndeterminate(true);
        }

    }


    public void setLoaded() {
        loading = false;
    }

    public void setOnLoadMoreListener(OnLoadMoreListener onLoadMoreListener) {
        this.onLoadMoreListener = onLoadMoreListener;
    }


    @Override
    public int getItemCount() {
        return items.size();
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

    protected static class MangaViewHolder extends RecyclerView.ViewHolder {
        public ImageView image;
        public TextView text, latest;

        public MangaViewHolder(View itemView) {
            super(itemView);
            image = (ImageView) itemView.findViewById(R.id.manga_thumbnail);
            text = (TextView) itemView.findViewById(R.id.manga_name);
            latest = (TextView) itemView.findViewById(R.id.chapter_name);

        }
    }

    public static class ProgressViewHolder extends RecyclerView.ViewHolder {
        public ProgressBar progressBar;

        public ProgressViewHolder(View v) {
            super(v);
            progressBar = (ProgressBar) v.findViewById(R.id.progressBar);
        }
    }

    public interface OnItemClickListener {

        void onItemClick(View view, Manga manga);

    }
}

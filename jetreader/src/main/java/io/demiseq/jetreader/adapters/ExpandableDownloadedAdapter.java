package io.demiseq.jetreader.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bignerdranch.expandablerecyclerview.Adapter.ExpandableRecyclerAdapter;
import com.bignerdranch.expandablerecyclerview.Model.ParentObject;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import io.demiseq.jetreader.R;
import io.demiseq.jetreader.activities.DownloadedReadActivity;
import io.demiseq.jetreader.activities.MainActivity;
import io.demiseq.jetreader.model.Chapter;
import io.demiseq.jetreader.model.Wrapper;
import io.demiseq.jetreader.utils.FileUtils;

public class ExpandableDownloadedAdapter
        extends ExpandableRecyclerAdapter<ExpandableDownloadedAdapter.ParentViewHolder, ExpandableDownloadedAdapter.ChildViewHolder> {

    private LayoutInflater mInflater;
    private FileUtils fileUtils;
    private ArrayList<Chapter> chapters;

    public ExpandableDownloadedAdapter(Context context, List<ParentObject> itemList) {
        super(context, itemList);
        mInflater = LayoutInflater.from(context);
        fileUtils = new FileUtils();
    }

    @Override
    public ParentViewHolder onCreateParentViewHolder(ViewGroup viewGroup) {
        View view = mInflater.inflate(R.layout.list_item_parent, viewGroup, false);
        return new ParentViewHolder(view);
    }

    @Override
    public ChildViewHolder onCreateChildViewHolder(ViewGroup viewGroup) {
        View view = mInflater.inflate(R.layout.list_item_child, viewGroup, false);
        return new ChildViewHolder(view);
    }

    @Override
    public void onBindParentViewHolder(ParentViewHolder viewHolder, final int i, final Object o) {
        final Wrapper wrapper = (Wrapper) o;
        viewHolder.mangaName.setText(wrapper.getName());
        Picasso.with(mContext).load(wrapper.getImagePath()).error(R.drawable.error).into(viewHolder.mangaThumbnail);
        viewHolder.deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog alertDialog = new AlertDialog.Builder(mContext).create();
                alertDialog.setMessage(mContext.getResources().getString(R.string.confirm_delete));
                alertDialog.setCanceledOnTouchOutside(true);
                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        File sdcard = Environment.getExternalStorageDirectory();
                        File directory = new File(sdcard.getAbsolutePath() + "/.Jump Manga/" + wrapper.getName());
                        fileUtils.delete(directory);
                        if (mContext instanceof MainActivity) {
                            Toast.makeText(mContext, "Deleted", Toast.LENGTH_SHORT).show();
                            ((MainActivity) mContext).GetDownloaded();
                        }
                    }
                });
                alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Just kidding", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                alertDialog.show();
            }
        });
    }

    @Override
    public void onBindChildViewHolder(ChildViewHolder viewHolder, int i, Object o) {
        final Chapter chapter = (Chapter) o;
        viewHolder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, DownloadedReadActivity.class);
                intent.putExtra("manga_name", chapter.getMangaName());
                intent.putExtra("chapter_name", chapter.getName());
                intent.putStringArrayListExtra("image_path", chapter.getPath());
                mContext.startActivity(intent);
            }
        });
        viewHolder.chapterName.setText(chapter.getName());
        if(chapter.isRead())
            viewHolder.readStatus.setVisibility(View.VISIBLE);
        else viewHolder.readStatus.setVisibility(View.INVISIBLE);
    }



    public class ParentViewHolder extends com.bignerdranch.expandablerecyclerview.ViewHolder.ParentViewHolder {

        @Bind(R.id.mangaName) TextView mangaName;
        @Bind(R.id.mangaThumbnail) ImageView mangaThumbnail;
        @Bind(R.id.delete) ImageView deleteBtn;

        public ParentViewHolder(View view) {
            super(view);
            ButterKnife.bind(this,view);
        }
    }

    public class ChildViewHolder extends com.bignerdranch.expandablerecyclerview.ViewHolder.ChildViewHolder {

        @Bind(R.id.chapterName) TextView chapterName;
        @Bind(R.id.readStatus) ImageView readStatus;
        public View view;

        public ChildViewHolder(View view) {
            super(view);
            ButterKnife.bind(this,view);
            this.view = view;
        }
    }
}

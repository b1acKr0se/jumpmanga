package io.wyrmise.jumpmanga;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.ArrayList;
import io.wyrmise.jumpmanga.database.JumpDatabaseHelper;
import io.wyrmise.jumpmanga.model.Chapter;

/**
 * Created by Thanh on 6/30/2015.
 */
public class ChapterAdapter extends ArrayAdapter<Chapter> implements Filterable {
    private ArrayList<Chapter> chapters;
    private ArrayList<Chapter> temp;
    private JumpDatabaseHelper db;
    private Context context;
    Holder holder;

    public ChapterAdapter(Context c, int textViewResourceId, ArrayList<Chapter> list) {
        super(c, textViewResourceId);
        chapters = list;
        context = c;
        temp = new ArrayList<>(chapters);
        db = new JumpDatabaseHelper(context);
    }

    @Override
    public int getCount() {
        return chapters.size();
    }

    @Override
    public Chapter getItem(int position) {
        return chapters.get(position);
    }


    @Override
    public View getView(final int position, View convertView, ViewGroup container) {
        View convertView1 = convertView;
        LayoutInflater vi = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (convertView1 == null) {
            holder = new Holder();
            convertView1 = vi.inflate(R.layout.chapter_list_item, container,
                    false);
            holder.chapter_name = (TextView) convertView1.findViewById(R.id.name);
            holder.read_status = (ImageView) convertView1.findViewById(R.id.status_read);
            holder.fav_status = (CheckBox) convertView1.findViewById(R.id.favorite_box);

            convertView1.setTag(holder);

        } else {
            holder = (Holder) convertView1.getTag();
        }

        final Chapter chapter = getItem(position);

        holder.chapter_name.setText(chapter.getName());

        holder.fav_status.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CheckBox c = (CheckBox) view;
                if(!c.isChecked()) {
                    chapter.setIsFav(false);
                    System.out.println("Chapter name unfav: " +chapter.getName());
                    db.unfavChapter(chapter, chapter.getMangaName().replaceAll("'", "''"));
                } else {
                    chapter.setIsFav(true);
                    System.out.println("Chapter name fav: " +chapter.getName());
                    db.favChapter(chapter,chapter.getMangaName().replaceAll("'", "''"));
                }
            }
        });

        if(chapter.isRead())
            holder.read_status.setVisibility(ImageView.VISIBLE);
        else holder.read_status.setVisibility(ImageView.INVISIBLE);

        if(chapter.isFav())
            holder.fav_status.setChecked(true);
        else holder.fav_status.setChecked(false);

        return convertView1;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                if (results.count == 0) {
                    notifyDataSetInvalidated();
                } else {
                    chapters = (ArrayList<Chapter>) results.values;
                    notifyDataSetChanged();
                }
            }

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();
                if (constraint == null || constraint.length() == 0) {
                    results.values = temp;
                    results.count = temp.size();
                } else {
                    ArrayList<Chapter> filteredRowItems = new ArrayList<>();
                    chapters = temp;
                    for (Chapter c : chapters) {
                        if (c.getName().trim().toLowerCase().contains(constraint.toString().trim().toLowerCase())) {
                            filteredRowItems.add(c);
                        }
                    }
                    results.values = filteredRowItems;
                    results.count = filteredRowItems.size();
                }
                return results;
            }
        };
    }

    private class Holder {
        public TextView chapter_name;
        public ImageView read_status;
        public CheckBox fav_status;
    }

}

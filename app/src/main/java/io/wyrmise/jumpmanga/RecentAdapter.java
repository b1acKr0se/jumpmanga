package io.wyrmise.jumpmanga;

import android.content.Context;
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
public class RecentAdapter extends ArrayAdapter<Manga> {
    private ArrayList<Manga> mangas;
    private Context context;
    private Holder holder;

    public RecentAdapter(Context c, int textViewResourceId, ArrayList<Manga> arrayList) {
        super(c, textViewResourceId);
        context = c;
        mangas = arrayList;
    }

    @Override
    public int getCount() {
        return mangas.size();
    }

    @Override
    public Manga getItem(int position) {
        return mangas.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup container) {
        View convertView1 = convertView;
        LayoutInflater vi = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (convertView1 == null) {
            holder = new Holder();
            convertView1 = vi.inflate(R.layout.recent_items, container,
                    false);
            holder.image = (ImageView) convertView1.findViewById(R.id.manga_thumbnail);
            holder.manga_name = (TextView) convertView1.findViewById(R.id.manga_name);
            holder.chapter_name = (TextView) convertView1.findViewById(R.id.chapter_name);
            convertView1.setTag(holder);
        } else {
            holder = (Holder) convertView1.getTag();
        }

        Manga manga = getItem(position);

        if(!manga.getImage().equals("")){
            Picasso.with(context).load(manga.getImage()).error(R.drawable.error).into(holder.image);
        }

        holder.manga_name.setText(manga.getName());
        holder.chapter_name.setText(manga.getRecent().getName());

        return convertView1;
    }

    private class Holder {
        public ImageView image;
        public TextView manga_name, chapter_name;
    }
}

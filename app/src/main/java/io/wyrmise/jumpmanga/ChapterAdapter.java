package io.wyrmise.jumpmanga;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Thanh on 6/30/2015.
 */
public class ChapterAdapter extends ArrayAdapter<Chapter> {
    private ArrayList<Chapter> arrayList;
    private Context context;
    Holder holder;


    public ChapterAdapter(Context c, int textViewResourceId,ArrayList<Chapter> list) {
        super(c, textViewResourceId);
        arrayList = list;
        context = c;

    }

    @Override
    public int getCount() {
        return arrayList.size();
    }

    @Override
    public Chapter getItem(int position) {
        return arrayList.get(position);
    }


    @Override
    public View getView(int position, View convertView, ViewGroup container) {
        View convertView1 = convertView;
        LayoutInflater vi = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (convertView1 == null) {
            holder = new Holder();
            convertView1 = vi.inflate(R.layout.chapter_list_item, container,
                    false);
            holder.chapter_name = (TextView) convertView1.findViewById(R.id.name);
            convertView1.setTag(holder);
        } else {
            holder = (Holder) convertView1.getTag();
        }

        Chapter chapter = getItem(position);

        holder.chapter_name.setText(chapter.getName());

        return convertView1;
    }

    private class Holder {
        public TextView chapter_name;
    }


}

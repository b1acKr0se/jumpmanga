package io.wyrmise.jumpmanga.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import java.util.ArrayList;

import io.wyrmise.jumpmanga.R;
import io.wyrmise.jumpmanga.model.Manga;

/**
 * Created by Thanh on 7/12/2015.
 */
public class SearchAdapter extends ArrayAdapter<Manga> implements Filterable {
    private ArrayList<Manga> mangas;
    private ArrayList<Manga> temp;
    private Context context;

    public SearchAdapter(Context c, int textViewResourceId, ArrayList<Manga> list) {
        super(c, textViewResourceId);
        context = c;
        mangas = list;
        temp = new ArrayList<>(mangas);
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
    public View getView(final int position, View convertView, ViewGroup parent) {
        LayoutInflater vi = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (convertView == null) {
            convertView = vi.inflate(R.layout.search_dropdown_item, parent, false);
        }

        TextView manga_name = (TextView) convertView.findViewById(R.id.search_manga_name);

        Manga manga = getItem(position);

        convertView.setTag(manga);

        manga_name.setText(manga.getName());

        return convertView;
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
                    mangas = (ArrayList<Manga>) results.values;
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
                    ArrayList<Manga> filteredRowItems = new ArrayList<>();
                    mangas = temp;
                    for (Manga m : mangas) {
                        if (m.getName().trim().toLowerCase().contains(constraint.toString().trim().toLowerCase())) {
                            filteredRowItems.add(m);
                        }
                    }
                    results.values = filteredRowItems;
                    results.count = filteredRowItems.size();
                }
                return results;
            }
        };
    }


}

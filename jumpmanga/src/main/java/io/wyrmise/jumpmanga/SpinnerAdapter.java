package io.wyrmise.jumpmanga;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import io.wyrmise.jumpmanga.model.Category;

public class SpinnerAdapter extends BaseAdapter {
    private ArrayList<Category> categories = new ArrayList<>();
    private Context context;

    public SpinnerAdapter(Context c) {
        context = c;
    }

    public void addItem(Category c) {
        categories.add(c);
    }

    public void addItems(ArrayList<Category> list) {
        categories.addAll(list);
    }

    public void clear() {
        categories.clear();
    }

    @Override
    public int getCount() {
        return categories.size();
    }

    @Override
    public Object getItem(int position) {
        return categories.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    private String getTitle(int position) {
        return position >= 0 && position < categories.size() ? categories.get(position).getName() : "";
    }

    @Override
    public View getDropDownView(int position, View view, ViewGroup parent) {
        LayoutInflater vi = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (view == null || !view.getTag().toString().equals("DROPDOWN")) {
            view = vi.inflate(R.layout.spinner_item_dropdown, parent, false);
            view.setTag("DROPDOWN");
        }

        TextView textView = (TextView) view.findViewById(android.R.id.text1);
        textView.setText(getTitle(position));

        return view;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater vi = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (view == null || !view.getTag().toString().equals("NON_DROPDOWN")) {
            view = vi.inflate(R.layout.
                    spinner_item, parent, false);
            view.setTag("NON_DROPDOWN");
        }
        TextView textView = (TextView) view.findViewById(android.R.id.text1);
        textView.setText(getTitle(position));
        return view;
    }

}

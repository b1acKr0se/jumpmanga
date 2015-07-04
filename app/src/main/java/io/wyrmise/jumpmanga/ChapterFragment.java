package io.wyrmise.jumpmanga;


import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.ArrayList;

import io.wyrmise.jumpmanga.database.DatabaseHelper;
import io.wyrmise.jumpmanga.manga24hbaseapi.DownloadUtils;
import io.wyrmise.jumpmanga.model.Chapter;


/**
 * A simple {@link Fragment} subclass.
 */
public class ChapterFragment extends Fragment {

    private DatabaseHelper db;
    private ListView listView;
    private ArrayList<Chapter> chapters;
    private ProgressBar progressBar;
    public static ChapterAdapter adapter;

    private Context context;

    private String name;

    public ChapterFragment() {
        // Required empty public constructor
    }

    public static ChapterAdapter getAdapter() {
        return adapter;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_chapter, container, false);

        context = getActivity().getApplicationContext();

        db = new DatabaseHelper(context);

        setHasOptionsMenu(true);

        listView = (ListView) view.findViewById(R.id.listView);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(context, ReadActivity.class);
                if (!adapter.getItem(i).isRead()) {
                    db.insertChapter(adapter.getItem(i), name);
                    adapter.getItem(i).setIsRead(true);
                    adapter.notifyDataSetChanged();
                }
                intent.putExtra("manga", name);
                intent.putExtra("name", adapter.getItem(i).getName());
                intent.putExtra("url", adapter.getItem(i).getUrl());
                int position = chapters.indexOf(adapter.getItem(i));
                intent.putExtra("position", position);
                intent.putParcelableArrayListExtra("list", chapters);
                startActivity(intent);

            }
        });

        name = ((DetailActivity) getActivity()).getManga().getName();

        String url = ((DetailActivity) getActivity()).getManga().getUrl();

        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);

        new GetMangaDetails().execute(url);

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_detail, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView actionSearchView = (SearchView) searchItem.getActionView();
        actionSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextChange(String newText) {
                Log.d("OnQuerySubmit", "onQueryTextChange");
                ChapterFragment.getAdapter().getFilter().filter(newText);
                return true;
            }

            @Override
            public boolean onQueryTextSubmit(String query) {
                Log.d("OnQuerySubmit", "onQueryTextSubmit");
                //ListViewFragment.adapter.getFilter().filter(query);
                return true;
            }
        });

        actionSearchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                ChapterFragment.getAdapter().getFilter().filter("");
                return false;
            }
        });
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onResume(){
        super.onResume();
        if(adapter!=null){
            System.out.println("notifyDataSetChanged");
            adapter.notifyDataSetChanged();
        }
    }

    public class GetMangaDetails extends AsyncTask<String, Void, ArrayList<Chapter>> {
        public void onPreExecute() {
            progressBar.setVisibility(ProgressBar.VISIBLE);
            listView.setVisibility(ListView.GONE);
        }

        public ArrayList<Chapter> doInBackground(String... params) {

            DownloadUtils download = new DownloadUtils(params[0]);
            ArrayList<Chapter> arr = download.GetChapters();
            for (Chapter c : arr) {
                if (db.isChapterRead(c, name.replaceAll("'", "''"))) c.setIsRead(true);
            }
            return arr;
        }

        public void onPostExecute(ArrayList<Chapter> arr) {
            if (arr != null) {

                chapters = arr;

                adapter = new ChapterAdapter(context, R.layout.chapter_list_item, chapters);

                listView.setAdapter(adapter);

                progressBar.setVisibility(ProgressBar.GONE);

                listView.setVisibility(ListView.VISIBLE);

                listView.setTextFilterEnabled(true);

            } else {
                progressBar.setVisibility(ProgressBar.GONE);
                Toast.makeText(context, "Cannot retrieve the chapters, please check your network", Toast.LENGTH_LONG).show();
            }
        }
    }
}

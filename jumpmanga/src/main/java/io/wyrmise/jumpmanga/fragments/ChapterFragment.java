package io.wyrmise.jumpmanga.fragments;


import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.util.ArrayList;

import io.wyrmise.jumpmanga.R;
import io.wyrmise.jumpmanga.activities.DetailActivity;
import io.wyrmise.jumpmanga.activities.ReadActivity;
import io.wyrmise.jumpmanga.adapters.ChapterAdapter;
import io.wyrmise.jumpmanga.database.JumpDatabaseHelper;
import io.wyrmise.jumpmanga.manga24hbaseapi.DownloadUtils;
import io.wyrmise.jumpmanga.model.Chapter;
import io.wyrmise.jumpmanga.model.Manga;


/**
 * A simple {@link Fragment} subclass.
 */
public class ChapterFragment extends Fragment {

    private JumpDatabaseHelper db;
    private ListView listView;
    private ArrayList<Chapter> chapters;
    private ProgressBar progressBar;
    private static ChapterAdapter adapter;

    private Context context;

    private Manga manga;

    private String url;

    private SwipeRefreshLayout refreshLayout;

    private boolean isChecked = false;

    private ArrayList<Chapter> temp;

    private SwitchCompat switchCompat;

    private SearchView actionSearchView;

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

        db = new JumpDatabaseHelper(context);

        setHasOptionsMenu(true);

        listView = (ListView) view.findViewById(R.id.listView);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(context, ReadActivity.class);
                if (!adapter.getItem(i).isRead()) {
                    db.insertChapter(adapter.getItem(i), manga.getName());
                    adapter.getItem(i).setIsRead(true);
                }
                db.insertRecentChapter(manga, adapter.getItem(i));
                intent.putExtra("manga", manga);
                intent.putExtra("name", adapter.getItem(i).getName());
                intent.putExtra("url", adapter.getItem(i).getUrl());
                int position = chapters.indexOf(adapter.getItem(i));
                intent.putExtra("position", position);
                intent.putParcelableArrayListExtra("list", chapters);
                startActivity(intent);

            }
        });


        manga = ((DetailActivity) getActivity()).getManga();

        url = ((DetailActivity) getActivity()).getManga().getUrl();

        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);

        refreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.refresh_list);

        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Toast.makeText(context, "Refreshing chapter list...", Toast.LENGTH_SHORT).show();
                listView.setVisibility(ListView.GONE);
                new GetMangaDetails().execute(url);
            }
        });

//        if (savedInstanceState != null && savedInstanceState.containsKey("list")) {
//            chapters = savedInstanceState.getParcelableArrayList("list");
//            adapter = new ChapterAdapter(context, R.layout.chapter_list_item, chapters);
//            listView.setAdapter(adapter);
//            progressBar.setVisibility(ProgressBar.GONE);
//
//            listView.setVisibility(ListView.VISIBLE);
//
//            listView.setTextFilterEnabled(true);
//        } else {
        progressBar.setVisibility(ProgressBar.VISIBLE);
        listView.setVisibility(ListView.GONE);
        new GetMangaDetails().execute(url);

//        }
        return view;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_detail, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        actionSearchView = (SearchView) searchItem.getActionView();

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

        RelativeLayout layout = (RelativeLayout) menu.findItem(R.id.action_switch).getActionView();

        switchCompat = (SwitchCompat) layout.findViewById(R.id.favorite_switch);

        switchCompat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                isChecked = b;
                if (isChecked && chapters != null) {
                    ArrayList<Chapter> arr = new ArrayList<Chapter>();
                    for (int i = chapters.size() - 1; i >= 0; i--) {
                        if (chapters.get(i).isFav() == true)
                            arr.add(chapters.get(i));
                    }

                    if (arr.size() > 0) {
                        chapters = new ArrayList<Chapter>(arr);
                        adapter = new ChapterAdapter(context, R.layout.chapter_list_item, chapters);
                        listView.setAdapter(adapter);
                    } else {
                        Toast.makeText(context, "You have no favorite chapter for this manga", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    chapters = new ArrayList<Chapter>(temp);
                    adapter = new ChapterAdapter(context, R.layout.chapter_list_item, chapters);
                    listView.setAdapter(adapter);
                }
            }
        });

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (switchCompat != null && switchCompat.getVisibility() == View.GONE)
            switchCompat.setVisibility(View.VISIBLE);
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }


//    @Override
//    public void onSaveInstanceState(Bundle bundle) {
//        if (chapters != null)
//            bundle.putParcelableArrayList("list", chapters);
//        super.onSaveInstanceState(bundle);
//
//    }

    public class GetMangaDetails extends AsyncTask<String, Void, ArrayList<Chapter>> {
        public void onPreExecute() {
            if (switchCompat != null)
                switchCompat.setVisibility(View.GONE);
        }

        public ArrayList<Chapter> doInBackground(String... params) {
            DownloadUtils download = new DownloadUtils(params[0]);
            ArrayList<Chapter> arr = download.GetChapters();
            if (arr != null)
                for (Chapter c : arr) {
                    c.setMangaName(manga.getName());
                    if (db.isChapterRead(c, manga.getName().replaceAll("'", "''")))
                        c.setIsRead(true);
                    if (db.isChapterFav(c, manga.getName().replaceAll("'", "''")))
                        c.setIsFav(true);
                }
            return arr;
        }

        public void onPostExecute(ArrayList<Chapter> arr) {
            refreshLayout.setRefreshing(false);

            if (arr != null) {

                chapters = arr;

                temp = new ArrayList<>(chapters);

                adapter = new ChapterAdapter(context, R.layout.chapter_list_item, chapters);

                listView.setAdapter(adapter);

                progressBar.setVisibility(ProgressBar.GONE);

                listView.setVisibility(ListView.VISIBLE);

                listView.setTextFilterEnabled(true);

                if (switchCompat != null)
                    switchCompat.setVisibility(View.VISIBLE);

            } else {
                progressBar.setVisibility(ProgressBar.GONE);
                Toast.makeText(context, "Cannot retrieve the chapters, please check your network", Toast.LENGTH_LONG).show();
            }
        }
    }
}
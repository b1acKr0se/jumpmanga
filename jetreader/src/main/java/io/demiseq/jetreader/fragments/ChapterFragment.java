package io.demiseq.jetreader.fragments;


import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.SwitchCompat;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.BindString;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.demiseq.jetreader.R;
import io.demiseq.jetreader.activities.DetailActivity;
import io.demiseq.jetreader.activities.ReadActivity;
import io.demiseq.jetreader.adapters.ChapterAdapter;
import io.demiseq.jetreader.database.JumpDatabaseHelper;
import io.demiseq.jetreader.api.MangaLibrary;
import io.demiseq.jetreader.model.Chapter;
import io.demiseq.jetreader.model.Manga;
import io.demiseq.jetreader.service.DownloadService;


/**
 * A simple {@link Fragment} subclass.
 */
public class ChapterFragment extends Fragment {

    private JumpDatabaseHelper db;
    private ArrayList<Chapter> chapters;
    private static ChapterAdapter adapter;
    private Context context;
    private Manga manga;
    private String url;
    private boolean isChecked = false;
    private ArrayList<Chapter> temp;
    private SwitchCompat switchCompat;
    private SearchView actionSearchView;

    @Bind(R.id.empty)
    TextView empty;
    @Bind(R.id.listView)
    ListView listView;
    @Bind(R.id.refresh_list)
    SwipeRefreshLayout refreshLayout;
    @Bind(R.id.reload)
    Button reload;
    @Bind(R.id.progress_bar)ProgressBar progressBar;

    @BindString(R.string.chapter_retrieve_failed)
    String chapter_retrieve_failed;
    @BindString(R.string.no_fav_chapter)
    String no_fav_chapter;
    @BindString(R.string.refresh_list)
    String refresh_list;

    @OnClick(R.id.reload)
    public void reload() {
        new GetMangaDetails().execute(url);
    }


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

        ButterKnife.bind(this, view);

        context = getActivity();

        db = new JumpDatabaseHelper(context);

        progressBar.getIndeterminateDrawable().setColorFilter(Color.GRAY, PorterDuff.Mode.SRC_ATOP);

        setHasOptionsMenu(true);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    Intent intent = new Intent(context, ReadActivity.class);
                    if (!adapter.getItem(i).isRead()) {
                        db.markChapterAsRead(adapter.getItem(i), manga.getName());
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

        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);

        listView.setFastScrollEnabled(true);

        manga = ((DetailActivity) getActivity()).getManga();

        url = ((DetailActivity) getActivity()).getManga().getUrl();

        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Toast.makeText(context, refresh_list, Toast.LENGTH_SHORT).show();
                listView.setVisibility(ListView.GONE);
                new GetMangaDetails().execute(url);
            }
        });

        listView.setVisibility(ListView.GONE);
        new GetMangaDetails().execute(url);

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

    }

    private void setChoiceModeListener() {
        listView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
            @Override
            public void onItemCheckedStateChanged(ActionMode actionMode, int i, long l, boolean b) {
                final int checkedCount = listView.getCheckedItemCount();
                actionMode.setTitle(checkedCount + "/" + adapter.getCount());
                adapter.toggleSelection(i);
            }

            @Override
            public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
                actionMode.getMenuInflater().inflate(R.menu.menu_cab_download, menu);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.cab_download:
                        SparseBooleanArray selected = adapter.getSelectedItems();
                        ArrayList<Chapter> chapters = new ArrayList<>();

                        for (int i = selected.size(); i >= 0; i--) {
                            if (selected.valueAt(i)) {
                                System.out.println(i);
                                Chapter c = adapter.getItem(selected.keyAt(i));
                                chapters.add(c);
                            }
                        }
                        if (chapters.size() > 0) {
                            String image = ((DetailActivity) context).getManga().getImage();
                            Intent intent = new Intent(context, DownloadService.class);
                            intent.putExtra("image", image);
                            intent.putParcelableArrayListExtra("list", chapters);
                            context.startService(intent);
                        }
                        actionMode.finish();
                        return true;

                    case R.id.cab_select_all:
                        for (int i = 0; i < adapter.getCount(); i++) {
                            listView.setItemChecked(i, true);
                        }
                        adapter.selectAll();
                        System.out.println("Count: " + adapter.getCount());
                        System.out.println("selected: " + adapter.getSelectedCount());
                        return true;

                    case R.id.cab_deselect_all:
                        for (int i = 0; i < adapter.getCount(); i++) {
                            if (listView.isItemChecked(i))
                                listView.setItemChecked(i, false);
                        }
                        return true;
                }
                return false;
            }

            @Override
            public void onDestroyActionMode(ActionMode actionMode) {
                adapter.removeSelection();
            }
        });
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_detail, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        actionSearchView = (SearchView) searchItem.getActionView();

        actionSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextChange(String newText) {
                ChapterFragment.getAdapter().getFilter().filter(newText);
                return true;
            }

            @Override
            public boolean onQueryTextSubmit(String query) {
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
                    ArrayList<Chapter> arr = new ArrayList<>();
                    for (int i = chapters.size() - 1; i >= 0; i--) {
                        if (chapters.get(i).isFav())
                            arr.add(chapters.get(i));
                    }

                    if (arr.size() > 0) {
                        chapters = new ArrayList<>(arr);
                        adapter = new ChapterAdapter(context, R.layout.chapter_list_item, chapters);
                        listView.setAdapter(adapter);
                    } else {
                        compoundButton.setChecked(false);
                        Toast.makeText(context, no_fav_chapter, Toast.LENGTH_SHORT).show();
                    }
                } else if (chapters != null) {
                    compoundButton.setChecked(false);
                    chapters = new ArrayList<>(temp);
                    adapter = new ChapterAdapter(context, R.layout.chapter_list_item, chapters);
                    listView.setAdapter(adapter);
                } else compoundButton.setChecked(false);
            }
        });

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onResume() {
        super.onResume();
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
            progressBar.setVisibility(View.VISIBLE);
            reload.setVisibility(View.GONE);
        }

        public ArrayList<Chapter> doInBackground(String... params) {
            MangaLibrary download = new MangaLibrary(params[0]);
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

            progressBar.setVisibility(View.GONE);

            if (arr != null) {
                if (arr.size() > 0) {
                    chapters = arr;

                    temp = new ArrayList<>(chapters);

                    adapter = new ChapterAdapter(context, R.layout.chapter_list_item, chapters);

                    listView.setAdapter(adapter);

                    setChoiceModeListener();

                    listView.setVisibility(ListView.VISIBLE);

                    listView.setTextFilterEnabled(true);
                } else if (arr.size() == 0) {
                    empty.setVisibility(View.VISIBLE);
                    listView.setVisibility(View.GONE);
                }
            } else {
                Toast.makeText(context, chapter_retrieve_failed, Toast.LENGTH_LONG).show();
                reload.setVisibility(View.VISIBLE);
            }
        }
    }
}

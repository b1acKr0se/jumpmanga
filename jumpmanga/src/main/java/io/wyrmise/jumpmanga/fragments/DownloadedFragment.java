package io.wyrmise.jumpmanga.fragments;


import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Environment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bignerdranch.expandablerecyclerview.Model.ParentObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import io.wyrmise.jumpmanga.R;
import io.wyrmise.jumpmanga.adapters.DownloadedAdapter;
import io.wyrmise.jumpmanga.adapters.ExpandableDownloadedAdapter;
import io.wyrmise.jumpmanga.model.Chapter;
import io.wyrmise.jumpmanga.model.Manga;
import io.wyrmise.jumpmanga.model.Wrapper;
import io.wyrmise.jumpmanga.utils.FileUtils;
import io.wyrmise.jumpmanga.widget.SimpleDividerItemDecoration;

/**
 * A simple {@link Fragment} subclass.
 */
public class DownloadedFragment extends Fragment {

    private List<Wrapper> wrappers;
    private ExpandableDownloadedAdapter adapter;
    private RecyclerView recyclerView;
    private TextView empty;
    private Context context;

    public DownloadedFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.context = activity;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        context = null;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_downloaded, container, false);


        recyclerView = (RecyclerView) view.findViewById(R.id.list);
        empty = (TextView) view.findViewById(R.id.empty);


        wrappers = new ArrayList<>();

        ArrayList<ParentObject> parentObjects = new ArrayList<>();

        File sdCard = Environment.getExternalStorageDirectory();
        File dir = new File(sdCard.getAbsolutePath() + "/.Jump Manga/");
        if (dir.exists() && dir.isDirectory() && dir.listFiles().length > 0) {
            File[] mangaNames = dir.listFiles();

            for (int i = mangaNames.length - 1; i >= 0; i--) {
                if (mangaNames[i].isDirectory() && mangaNames[i].listFiles().length > 0) {
                    File[] chapterNames = mangaNames[i].listFiles();
                    Wrapper w = new Wrapper();
                    w.setName(mangaNames[i].getName());
                    ArrayList<Chapter> chapters = new ArrayList<>();
                    for (File f : chapterNames) {
                        if(f.isDirectory()) {
                            Chapter c = new Chapter();
                            c.setMangaName(mangaNames[i].getName());
                            c.setName(f.getName());
                            FileUtils fileUtils = new FileUtils();
                            if (fileUtils.isChapterDownloaded(c.getMangaName(), c.getName())) {
                                c.setPath(fileUtils.getFilePaths());
                                chapters.add(c);
                            }
                        } else if (f.isFile()) {
                            w.setImagePath(f);
                            System.out.println(f.getName());
                        }
                    }
                    Collections.sort(chapters);
                    ArrayList<Object> list = new ArrayList<>();
                    for(Chapter c: chapters) list.add(c);
                    w.setChildObjectList(list);
                    parentObjects.add(w);
                }
            }

            if (parentObjects.size() > 0 && parentObjects != null) {
                adapter = new ExpandableDownloadedAdapter(getActivity(), parentObjects);
                recyclerView.setVisibility(View.VISIBLE);
                recyclerView.setLayoutManager(new LinearLayoutManager(getActivity().getApplicationContext()));
                recyclerView.setAdapter(adapter);
                empty.setVisibility(View.GONE);
            } else {
                recyclerView.setVisibility(View.GONE);
                empty.setVisibility(View.VISIBLE);
            }
        } else {
            recyclerView.setVisibility(View.GONE);
            empty.setVisibility(View.VISIBLE);
        }

        if (savedInstanceState != null)
            adapter.onRestoreInstanceState(savedInstanceState);

        return view;
    }



    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState = adapter.onSaveInstanceState(outState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_downloaded_fragment, menu);
        super.onCreateOptionsMenu(menu,inflater);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

}

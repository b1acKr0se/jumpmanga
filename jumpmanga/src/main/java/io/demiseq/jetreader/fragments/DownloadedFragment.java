package io.demiseq.jetreader.fragments;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
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
import java.util.Collections;
import java.util.List;

import butterknife.Bind;
import butterknife.BindString;
import butterknife.ButterKnife;
import io.demiseq.jetreader.R;
import io.demiseq.jetreader.adapters.ExpandableDownloadedAdapter;
import io.demiseq.jetreader.database.JumpDatabaseHelper;
import io.demiseq.jetreader.model.Chapter;
import io.demiseq.jetreader.model.Wrapper;
import io.demiseq.jetreader.utils.FileUtils;

/**
 * A simple {@link Fragment} subclass.
 * Containing downloaded manga and their respective chapters.
 */
public class DownloadedFragment extends Fragment {

    private List<Wrapper> wrappers;
    private ExpandableDownloadedAdapter adapter;
    private Context context;


    @Bind(R.id.list)
    RecyclerView recyclerView;
    @Bind(R.id.empty)
    TextView empty;
    @BindString(R.string.size_info) String size_info;
    @BindString(R.string.info) String info;


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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_downloaded, container, false);

        ButterKnife.bind(this, view);

        JumpDatabaseHelper db = new JumpDatabaseHelper(getActivity());

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
                        if (f.isDirectory()) {
                            Chapter c = new Chapter();
                            c.setMangaName(mangaNames[i].getName());
                            c.setName(f.getName());
                            FileUtils fileUtils = new FileUtils();
                            if (fileUtils.isChapterDownloaded(c.getMangaName(), c.getName())) {
                                c.setPath(fileUtils.getFilePaths());
                                c.setIsRead(db.isChapterRead(c, c.getMangaName()));
                                chapters.add(c);
                            }
                        } else if (f.isFile()) {
                            w.setImagePath(f);
                        }
                    }
                    Collections.sort(chapters);
                    ArrayList<Object> list = new ArrayList<>();
                    for (Chapter c : chapters) list.add(c);
                    w.setChildObjectList(list);
                    parentObjects.add(w);
                }
            }

            if (parentObjects.size()>0) {
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
    public void onResume() {
        super.onResume();
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState = adapter.onSaveInstanceState(outState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_downloaded_fragment, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_info:
                showInfo();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showInfo() {
        FileUtils fileUtils = new FileUtils();
        long bytes = fileUtils.getTotalSize();
        String message = size_info+bytes+"MB.";
        AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
        alertDialog.setTitle(info);
        alertDialog.setMessage(message);
        alertDialog.setCanceledOnTouchOutside(true);
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }

}

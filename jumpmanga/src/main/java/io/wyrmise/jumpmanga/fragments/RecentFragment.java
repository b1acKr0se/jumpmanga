package io.wyrmise.jumpmanga.fragments;


import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import io.wyrmise.jumpmanga.R;
import io.wyrmise.jumpmanga.activities.ReadActivity;
import io.wyrmise.jumpmanga.adapters.MangaAdapter;
import io.wyrmise.jumpmanga.adapters.RecentAdapter;
import io.wyrmise.jumpmanga.database.JumpDatabaseHelper;
import io.wyrmise.jumpmanga.manga24hbaseapi.FetchingMachine;
import io.wyrmise.jumpmanga.model.Chapter;
import io.wyrmise.jumpmanga.model.Manga;
import io.wyrmise.jumpmanga.utils.AsyncTaskCallback;


/**
 * A simple {@link Fragment} subclass.
 */
public class RecentFragment extends Fragment implements MangaAdapter.OnItemClickListener, AsyncTaskCallback {

    private JumpDatabaseHelper db;
    private Context context;
    private ArrayList<Manga> mangas;
    private RecentAdapter adapter;
    private ProgressDialog progressDialog;
    private boolean isTaskRunning = false;

    @Bind(R.id.list)
    RecyclerView recyclerView;
    @Bind(R.id.empty)
    TextView empty;


    public RecentFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.context = activity;
    }

    @Override
    public void onDetach() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        super.onDetach();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        context = null;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (isTaskRunning) {
            progressDialog = ProgressDialog.show(getActivity(), null, "Preparing...");
        }
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_recent, container, false);

        ButterKnife.bind(this,view);

        db = new JumpDatabaseHelper(context);

        return view;
    }

    @Override
    public void onItemClick(View view, Manga manga) {
        if (!isTaskRunning) {
            GetChapterList task = new GetChapterList(manga,this);
            task.execute(manga.getUrl());
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mangas = db.getRecentChapters();
        if (mangas != null && context != null) {
            adapter = new RecentAdapter(context, mangas);
            adapter.setOnItemClickListener(RecentFragment.this);
            recyclerView.setVisibility(View.VISIBLE);
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
            recyclerView.setAdapter(adapter);
            empty.setVisibility(View.GONE);
        } else {
            recyclerView.setVisibility(View.GONE);
            empty.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onTaskStarted() {
        isTaskRunning = true;
        progressDialog = ProgressDialog.show(getActivity(), null, "Picking up where you left off...");
    }

    @Override
    public void onTaskFinished() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
        isTaskRunning = false;
    }


    private class GetChapterList extends AsyncTask<String, Void, ArrayList<Chapter>> {

        private Manga manga;
        private AsyncTaskCallback callback;

        public GetChapterList(Manga m, AsyncTaskCallback c) {
            this.manga = m;
            callback = c;
        }

        @Override
        public void onPreExecute() {
            callback.onTaskStarted();
        }

        @Override
        public ArrayList<Chapter> doInBackground(String... params) {
            FetchingMachine download = new FetchingMachine(params[0]);
            ArrayList<Chapter> arr = download.GetChapters();
            return arr;
        }

        @Override
        public void onPostExecute(ArrayList<Chapter> arr) {
            callback.onTaskFinished();
            if (arr != null) {
                Intent intent = new Intent(context, ReadActivity.class);
                intent.putExtra("manga", manga);
                intent.putExtra("name", manga.getChapter().getName());
                intent.putExtra("url", manga.getChapter().getUrl());
                int position = -1;
                for (Chapter c : arr) {
                    if (c.getName().equals(manga.getChapter().getName()))
                        position = arr.indexOf(c);
                }
                intent.putExtra("position", position);
                intent.putParcelableArrayListExtra("list", arr);
                startActivity(intent);
            } else {
                Toast.makeText(context, "There's an error with your network, please check", Toast.LENGTH_SHORT).show();
            }

        }
    }

}

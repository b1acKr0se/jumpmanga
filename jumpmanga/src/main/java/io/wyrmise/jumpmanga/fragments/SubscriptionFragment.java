package io.wyrmise.jumpmanga.fragments;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import io.wyrmise.jumpmanga.R;
import io.wyrmise.jumpmanga.activities.ReadActivity;
import io.wyrmise.jumpmanga.adapters.MangaAdapter;
import io.wyrmise.jumpmanga.adapters.SubscriptionAdapter;
import io.wyrmise.jumpmanga.database.JumpDatabaseHelper;
import io.wyrmise.jumpmanga.manga24hbaseapi.DownloadUtils;
import io.wyrmise.jumpmanga.model.Chapter;
import io.wyrmise.jumpmanga.model.Manga;
import io.wyrmise.jumpmanga.utils.OrientationLocker;
import io.wyrmise.jumpmanga.widget.SimpleDividerItemDecoration;


/**
 * A simple {@link Fragment} subclass.
 */
public class SubscriptionFragment extends Fragment implements MangaAdapter.OnItemClickListener{

    private JumpDatabaseHelper db;

    private Context context;

    private ArrayList<Manga> mangas;

    private RecyclerView recyclerView;

    private SubscriptionAdapter adapter;

    private TextView empty;

    private ProgressDialog progressDialog;


    public SubscriptionFragment() {
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
    public void onItemClick(View view, Manga manga) {
        GetChapterList task = new GetChapterList(manga, progressDialog);
        task.execute(manga.getUrl());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_subscription, container, false);

        db = new JumpDatabaseHelper(context);

        recyclerView = (RecyclerView) view.findViewById(R.id.list);

        empty = (TextView) view.findViewById(R.id.empty);

        progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Preparing...");
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(true);

        return view;

    }

    @Override
    public void onResume() {
        super.onResume();
        mangas = db.getAllSubscription();
        if (mangas != null && context != null) {
            adapter = new SubscriptionAdapter(context, mangas);
            adapter.setOnItemClickListener(SubscriptionFragment.this);
            recyclerView.setVisibility(View.VISIBLE);
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
            recyclerView.addItemDecoration(new SimpleDividerItemDecoration(context));
            recyclerView.setAdapter(adapter);
            empty.setVisibility(View.GONE);
        } else {
            recyclerView.setVisibility(View.GONE);
            empty.setVisibility(View.VISIBLE);
        }
    }

    private class GetChapterList extends AsyncTask<String, Void, ArrayList<Chapter>> {

        private Manga manga;

        private ProgressDialog progressDialog;

        public GetChapterList(Manga m, ProgressDialog progressDialog) {
            this.manga = m;
            this.progressDialog = progressDialog;
        }

        @Override
        public void onPreExecute() {
            OrientationLocker.lock(getActivity());
            progressDialog.show();
        }

        @Override
        public ArrayList<Chapter> doInBackground(String... params) {
            DownloadUtils download = new DownloadUtils(params[0]);
            ArrayList<Chapter> arr = download.GetChapters();
            return arr;
        }

        @Override
        public void onPostExecute(ArrayList<Chapter> arr) {
            try {
                if (progressDialog != null && progressDialog.isShowing())
                    progressDialog.dismiss();
            } catch (Exception e) {

            }
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

                db.deleteFromSubscription(manga.getName(),manga.getChapter().getName());

                startActivity(intent);
            } else {
                Toast.makeText(context, "There's an error with your network, please check", Toast.LENGTH_SHORT).show();
            }

            OrientationLocker.unlock(getActivity());
        }
    }

}

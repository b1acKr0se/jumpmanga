package io.wyrmise.jumpmanga.fragments;


import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import io.wyrmise.jumpmanga.listener.OnLoadMoreListener;
import io.wyrmise.jumpmanga.R;
import io.wyrmise.jumpmanga.activities.DetailActivity;
import io.wyrmise.jumpmanga.adapters.MangaAdapter;
import io.wyrmise.jumpmanga.database.JumpDatabaseHelper;
import io.wyrmise.jumpmanga.manga24hbaseapi.DownloadUtils;
import io.wyrmise.jumpmanga.model.Manga;


/**
 * A simple {@link Fragment} subclass.
 */
public class NewFragment extends Fragment implements MangaAdapter.OnItemClickListener {

    private Context context;
    private JumpDatabaseHelper db;
    private ProgressBar progressBar;
    private RecyclerView recyclerView;
    private ArrayList<Manga> mangas = new ArrayList<>();
    private ArrayList<Manga> moreManga;
    private TextView empty;
    private MangaAdapter adapter;
    private int page = 2;

    public NewFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_main, container, false);

        context = getActivity().getApplicationContext();

        db = new JumpDatabaseHelper(context);

        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);

        recyclerView = (RecyclerView) view.findViewById(R.id.recycler);

        empty = (TextView) view.findViewById(R.id.empty);

        if (savedInstanceState == null)
            new GetMangas().execute("http://manga24h.com/status/new.html");
        else {
            setUpAdapter(savedInstanceState);
        }

        return view;
    }

    public void setUpAdapter(Bundle savedInstanceState) {
        progressBar.setVisibility(ProgressBar.GONE);
        recyclerView.setVisibility(RecyclerView.VISIBLE);
        mangas = savedInstanceState.getParcelableArrayList("list");
        page = savedInstanceState.getInt("page");
        int orientation = context.getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_PORTRAIT)
            recyclerView.setLayoutManager(new GridLayoutManager(context, 2));
        else
            recyclerView.setLayoutManager(new GridLayoutManager(context, 4));
        adapter = new MangaAdapter(context, mangas, recyclerView);
        adapter.setOnItemClickListener(NewFragment.this);
        adapter.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore() {
                mangas.add(null);
                adapter.notifyItemInserted(mangas.size() - 1);
                if (page <= 146)
                    new LoadMoreManga().execute("http://manga24h.com/status/new.html/" + page);
                else
                    Toast.makeText(context, "Reach the end of page!", Toast.LENGTH_LONG).show();

            }
        });
        recyclerView.setAdapter(adapter);

    }

    @Override
    public void onSaveInstanceState(Bundle bundle) {
        bundle.putParcelableArrayList("list", mangas);
        bundle.putInt("page", page);
        super.onSaveInstanceState(bundle);

    }

//    @Override
//    public void onConfigurationChanged(Configuration newConfig) {
//        super.onConfigurationChanged(newConfig);
//        // Checks the orientation of the screen
//        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
//            recyclerView.setLayoutManager(new GridLayoutManager(context, 4));
//            recyclerView.setAdapter(adapter);
//        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
//            recyclerView.setLayoutManager(new GridLayoutManager(context, 2));
//            recyclerView.setAdapter(adapter);
//        }
//    }

    @Override
    public void onItemClick(View view, Manga manga) {
        Intent intent = new Intent(context, DetailActivity.class);
        intent.putExtra("manga", manga);
        startActivity(intent);
    }

    public class LoadMoreManga extends AsyncTask<String, Void, ArrayList<Manga>> {
        @Override
        public ArrayList<Manga> doInBackground(String... params) {
            DownloadUtils download = new DownloadUtils(params[0]);
            ArrayList<Manga> result = download.GetMangas(10);
            return result;
        }

        public void onPostExecute(ArrayList<Manga> result) {
            mangas.remove(mangas.size() - 1);
            adapter.notifyItemRemoved(mangas.size());

            if (result != null) {
                moreManga = result;
                page++;
                for (int i = 0; i < moreManga.size(); i++) {
                    mangas.add(moreManga.get(i));
                    adapter.notifyItemInserted(mangas.size());
                }
            } else {
                Toast.makeText(context, "There's something wrong with your network, please check", Toast.LENGTH_LONG).show();
            }
            adapter.setLoaded();
        }
    }


    public class GetMangas extends AsyncTask<String, Void, ArrayList<Manga>> {

        @Override
        public void onPreExecute() {
            recyclerView.setVisibility(RecyclerView.GONE);
            empty.setVisibility(TextView.GONE);
            progressBar.setVisibility(ProgressBar.VISIBLE);
        }

        @Override
        public ArrayList<Manga> doInBackground(String... params) {
            DownloadUtils download = new DownloadUtils(params[0]);
            ArrayList<Manga> arrayList = download.GetMangas(10);
            if (arrayList != null) {
                for (Manga m : arrayList) {
                    if (db.isMangaFavorited(m.getName().replaceAll("'", "''")))
                        m.setIsFav(true);
                }
            }
            return arrayList;
        }

        @Override
        public void onPostExecute(ArrayList<Manga> result) {
            progressBar.setVisibility(ProgressBar.GONE);

            if (result != null) {
                recyclerView.setVisibility(RecyclerView.VISIBLE);
                mangas = result;
                int orientation = context.getResources().getConfiguration().orientation;
                if (orientation == Configuration.ORIENTATION_PORTRAIT)
                    recyclerView.setLayoutManager(new GridLayoutManager(context, 2));
                else
                    recyclerView.setLayoutManager(new GridLayoutManager(context, 4));
                adapter = new MangaAdapter(context, mangas, recyclerView);
                adapter.setOnItemClickListener(NewFragment.this);
                adapter.setOnLoadMoreListener(new OnLoadMoreListener() {
                    @Override
                    public void onLoadMore() {
                        mangas.add(null);
                        adapter.notifyItemInserted(mangas.size() - 1);
                        new LoadMoreManga().execute("http://manga24h.com/status/new.html/" + page);

                    }
                });
                recyclerView.setAdapter(adapter);
            } else {
                recyclerView.setVisibility(RecyclerView.GONE);
                empty.setVisibility(TextView.VISIBLE);
                Toast.makeText(context, "There's something wrong with your network, please check", Toast.LENGTH_LONG).show();
            }
        }
    }

}

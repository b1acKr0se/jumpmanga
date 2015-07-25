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

import com.jpardogo.android.googleprogressbar.library.GoogleProgressBar;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.BindString;
import butterknife.ButterKnife;
import io.wyrmise.jumpmanga.utils.OnLoadMoreListener;
import io.wyrmise.jumpmanga.R;
import io.wyrmise.jumpmanga.activities.DetailActivity;
import io.wyrmise.jumpmanga.adapters.MangaAdapter;
import io.wyrmise.jumpmanga.database.JumpDatabaseHelper;
import io.wyrmise.jumpmanga.manga24hbaseapi.FetchingMachine;
import io.wyrmise.jumpmanga.model.Manga;


/**
 * A simple {@link Fragment} subclass.
 */
public class NewFragment extends Fragment implements MangaAdapter.OnItemClickListener {

    private Context context;
    private JumpDatabaseHelper db;
    private ArrayList<Manga> mangas = new ArrayList<>();
    private ArrayList<Manga> moreManga;
    private MangaAdapter adapter;
    private int page = 2;

    @Bind(R.id.progressBar)
    GoogleProgressBar progressBar;
    @Bind(R.id.recycler) RecyclerView recyclerView;
    @Bind(R.id.empty) TextView empty;
    @BindString(R.string.load_error) String load_error;
    @BindString(R.string.last_page) String last_page;

    public NewFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_main, container, false);

        ButterKnife.bind(this, view);

        context = getActivity().getApplicationContext();

        db = new JumpDatabaseHelper(context);

        if (savedInstanceState == null)
            new GetMangas().execute("http://manga24h.com/status/new.html");
        else {
            setUpAdapter(savedInstanceState);
        }

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
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
                    Toast.makeText(context, last_page, Toast.LENGTH_LONG).show();

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
            FetchingMachine download = new FetchingMachine(params[0]);
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
                Toast.makeText(context, load_error, Toast.LENGTH_LONG).show();
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
            FetchingMachine download = new FetchingMachine(params[0]);
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
                Toast.makeText(context, load_error, Toast.LENGTH_LONG).show();
            }

        }
    }

}

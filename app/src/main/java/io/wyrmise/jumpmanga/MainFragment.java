package io.wyrmise.jumpmanga;


import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import io.wyrmise.jumpmanga.database.DatabaseHelper;
import io.wyrmise.jumpmanga.manga24hbaseapi.DownloadUtils;
import io.wyrmise.jumpmanga.model.Manga;


/**
 * A simple {@link Fragment} subclass.
 */
public class MainFragment extends Fragment implements MangaAdapter.OnItemClickListener {

    private static final String TYPE = "manga_type";
    private static final int RETRIEVE_HOT_MANGA = 1;
    private static final int RETRIEVE_FAVORITE_MANGA = 2;

    private Context context;
    private DatabaseHelper db;
    private ProgressBar progressBar;
    private RecyclerView recyclerView;
    private ArrayList<Manga> mangas = new ArrayList<>();
    private ArrayList<Manga> moreManga;
    private TextView empty;
    private MangaAdapter adapter;
    private int page = 2;


    public MainFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        int i = getArguments().getInt(TYPE);

        View view = inflater.inflate(R.layout.fragment_main, container, false);

        context = getActivity().getApplicationContext();

        db = new DatabaseHelper(context);

        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);

        recyclerView = (RecyclerView) view.findViewById(R.id.recycler);

        empty = (TextView) view.findViewById(R.id.empty);

        switch (i){
            case RETRIEVE_HOT_MANGA:
                new GetMangas().execute("http://manga24h.com/status/hot.html");
                break;
            case RETRIEVE_FAVORITE_MANGA:
                new GetFavoriteManga().execute();
                break;
        }

        return view;
    }

    @Override
    public void onItemClick(View view, Manga manga) {
        Intent intent = new Intent(context, DetailedActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra("manga",manga);
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

            for(Manga m : arrayList) {
                if(db.isMangaFavorited(m.getName().replaceAll("'","''")))
                    m.setIsFav(true);
            }

            return arrayList;
        }

        @Override
        public void onPostExecute(ArrayList<Manga> result) {
            progressBar.setVisibility(ProgressBar.GONE);
            recyclerView.setVisibility(RecyclerView.VISIBLE);

            if (result != null) {
                mangas = result;
                recyclerView.setLayoutManager(new GridLayoutManager(context, 2));
                adapter = new MangaAdapter(context, mangas, recyclerView);
                adapter.setOnItemClickListener(MainFragment.this);
                adapter.setOnLoadMoreListener(new OnLoadMoreListener() {
                    @Override
                    public void onLoadMore() {
                        mangas.add(null);
                        adapter.notifyItemInserted(mangas.size() - 1);
                        new LoadMoreManga().execute("http://manga24h.com/status/hot.html/" + page);

                    }
                });
                recyclerView.setAdapter(adapter);
            } else {
                empty.setVisibility(TextView.VISIBLE);
                Toast.makeText(context, "There's something wrong with your network, please check", Toast.LENGTH_LONG).show();
            }
        }
    }

    public class GetFavoriteManga extends AsyncTask<Void, Void, ArrayList<Manga>> {

        @Override
        public void onPreExecute() {
            recyclerView.setVisibility(RecyclerView.GONE);
            empty.setVisibility(TextView.GONE);
            progressBar.setVisibility(ProgressBar.VISIBLE);
        }

        @Override
        public ArrayList<Manga> doInBackground(Void... params) {
            return db.getAllFavoritedMangas();
        }

        @Override
        public void onPostExecute(ArrayList<Manga> result) {
            progressBar.setVisibility(ProgressBar.GONE);
            recyclerView.setVisibility(RecyclerView.VISIBLE);

            if (result != null) {
                mangas = result;
                recyclerView.setLayoutManager(new GridLayoutManager(context, 2));
                adapter = new MangaAdapter(context, mangas, recyclerView);
                adapter.setOnItemClickListener(MainFragment.this);
                recyclerView.setAdapter(adapter);
            } else {
                empty.setVisibility(TextView.VISIBLE);
                Toast.makeText(context, "No favorite manga", Toast.LENGTH_LONG).show();
            }
        }
    }

}

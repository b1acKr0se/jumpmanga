package io.wyrmise.jumpmanga.fragments;


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
import io.wyrmise.jumpmanga.activities.MainActivity;
import io.wyrmise.jumpmanga.adapters.CategoryAdapter;
import io.wyrmise.jumpmanga.database.JumpDatabaseHelper;
import io.wyrmise.jumpmanga.manga24hbaseapi.FetchingMachine;
import io.wyrmise.jumpmanga.model.Manga;
import io.wyrmise.jumpmanga.widget.SimpleDividerItemDecoration;


/**
 * A simple {@link Fragment} subclass.
 */
public class CategoryFragment extends Fragment implements CategoryAdapter.OnItemClickListener {

    private JumpDatabaseHelper db;
    private Context context;
    private ArrayList<Manga> mangas;
    private ArrayList<Manga> moreManga;
    private CategoryAdapter adapter;
    private String url = "";
    private int page = 2;
    private int max_page = 1;
    private int position = -1;

    @Bind(R.id.progressBar) GoogleProgressBar progressBar;
    @Bind(R.id.list) RecyclerView recyclerView;
    @Bind(R.id.empty) TextView empty;

    @BindString(R.string.network_error) String network_error;

    public CategoryFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_category, container, false);

        ButterKnife.bind(this,view);

        context = getActivity().getApplicationContext();

        db = new JumpDatabaseHelper(context);

        max_page = getArguments().getInt("max_page");
        url = getArguments().getString("url");
        position = getArguments().getInt("position");

        if (savedInstanceState == null) {
            new GetManga().execute(url);
        } else {
            setUpAdapter(savedInstanceState);
        }

        return view;
    }

    @Override
    public void onItemClick(View view, Manga manga) {
        Intent intent = new Intent(context, DetailActivity.class);
        intent.putExtra("manga", manga);
        startActivity(intent);
    }

    @Override
    public void onSaveInstanceState(Bundle bundle) {
        bundle.putParcelableArrayList("list", mangas);
        bundle.putInt("page", page);
        bundle.putString("url", url);
        bundle.putInt("max_page", max_page);
        bundle.putInt("position", position);
        super.onSaveInstanceState(bundle);

    }

    public void setUpAdapter(Bundle savedInstanceState) {
        progressBar.setVisibility(ProgressBar.GONE);
        recyclerView.setVisibility(RecyclerView.VISIBLE);
        mangas = savedInstanceState.getParcelableArrayList("list");
        page = savedInstanceState.getInt("page");
        max_page = savedInstanceState.getInt("max_page");
        url = savedInstanceState.getString("url");
        position = savedInstanceState.getInt("position");

        ((MainActivity) getActivity()).setUpSpinner();
        ((MainActivity) getActivity()).setSpinnerPosition(position);

        recyclerView.setLayoutManager(new LinearLayoutManager(context));

        adapter = new CategoryAdapter(context, mangas, recyclerView);
        adapter.setOnItemClickListener(CategoryFragment.this);
        adapter.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore() {
                if (max_page >= 2 && page <= max_page && !url.equals("")) {
                    mangas.add(null);
                    adapter.notifyItemInserted(mangas.size() - 1);
                    new LoadMoreManga().execute(url + "" + page);
                }
            }
        });
        recyclerView.addItemDecoration(new SimpleDividerItemDecoration(context));
        recyclerView.setAdapter(adapter);
    }


    private class GetManga extends AsyncTask<String, Void, ArrayList<Manga>> {
        @Override
        public void onPreExecute() {
            recyclerView.setVisibility(RecyclerView.GONE);
            empty.setVisibility(TextView.GONE);
            progressBar.setVisibility(ProgressBar.VISIBLE);
        }

        @Override
        public ArrayList<Manga> doInBackground(String... params) {
            FetchingMachine download = new FetchingMachine(params[0]);
            ArrayList<Manga> arrayList = download.GetMangasFromCategory();
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
                empty.setVisibility(View.GONE);
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
                mangas = result;
                adapter = new CategoryAdapter(context, mangas, recyclerView);
                adapter.setOnItemClickListener(CategoryFragment.this);

                adapter.setOnLoadMoreListener(new OnLoadMoreListener() {
                    @Override
                    public void onLoadMore() {
                        if (max_page >= 2 && page <= max_page && !url.equals("")) {
                            mangas.add(null);
                            adapter.notifyItemInserted(mangas.size() - 1);
                            new LoadMoreManga().execute(url + "" + page);
                        }
                    }
                });
                recyclerView.addItemDecoration(new SimpleDividerItemDecoration(context));
                recyclerView.setAdapter(adapter);
            } else {
                recyclerView.setVisibility(RecyclerView.GONE);
                empty.setVisibility(TextView.VISIBLE);
                Toast.makeText(context, network_error, Toast.LENGTH_LONG).show();
            }

        }
    }

    public class LoadMoreManga extends AsyncTask<String, Void, ArrayList<Manga>> {


        @Override
        public ArrayList<Manga> doInBackground(String... params) {
            FetchingMachine download = new FetchingMachine(params[0]);
            ArrayList<Manga> result = download.GetMangasFromCategory();
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
                Toast.makeText(context, network_error, Toast.LENGTH_LONG).show();
            }
            adapter.setLoaded();
        }
    }
}

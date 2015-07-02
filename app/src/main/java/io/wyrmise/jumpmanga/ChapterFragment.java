package io.wyrmise.jumpmanga;


import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.ArrayList;

import io.wyrmise.jumpmanga.manga24hbaseapi.DownloadUtils;
import io.wyrmise.jumpmanga.model.Chapter;


/**
 * A simple {@link Fragment} subclass.
 */
public class ChapterFragment extends Fragment {

    private ListView listView;
    private ArrayList<Chapter> chapters;
    private ProgressBar progressBar;
    private ChapterAdapter adapter;

    public ChapterFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_chapter, container, false);

        listView = (ListView) view.findViewById(R.id.listView);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(getActivity().getApplicationContext(),ReaderActivity.class);
                intent.putExtra("name",adapter.getItem(i).getName());
                intent.putExtra("url",adapter.getItem(i).getUrl());
                intent.putExtra("position",i);
                intent.putParcelableArrayListExtra("list",chapters);
                startActivity(intent);
            }
        });

        progressBar = (ProgressBar) view.findViewById(R.id.progressBar);

        new GetMangaDetails().execute("http://manga24h.com/66/Fairy-Tail.html");

        return view;
    }

    public class GetMangaDetails extends AsyncTask<String, Void, ArrayList<Chapter>> {
        public void onPreExecute(){
            progressBar.setVisibility(ProgressBar.VISIBLE);
            listView.setVisibility(ListView.GONE);
        }
        public ArrayList<Chapter> doInBackground(String... params){
            DownloadUtils download = new DownloadUtils(params[0]);
            return download.GetChapters();
        }
        public void onPostExecute(ArrayList<Chapter> arr) {
            if(arr!=null) {
                chapters = arr;

                adapter = new ChapterAdapter(getActivity().getApplicationContext(),R.layout.chapter_list_item,chapters);

                listView.setAdapter(adapter);

                progressBar.setVisibility(ProgressBar.GONE);

                listView.setVisibility(ListView.VISIBLE);

            } else {
                progressBar.setVisibility(ProgressBar.GONE);
                Toast.makeText(getActivity().getApplicationContext(),"Cannot retrieve the chapters, please check your network",Toast.LENGTH_LONG).show();
            }
        }
    }
}

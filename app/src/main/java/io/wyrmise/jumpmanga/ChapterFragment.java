package io.wyrmise.jumpmanga;


import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.ArrayList;

import io.wyrmise.jumpmanga.manga24hbaseapi.DownloadUtils;


/**
 * A simple {@link Fragment} subclass.
 */
public class ChapterFragment extends Fragment {

    private ListView listView;
    private ArrayList<Chapter> chapters;
    private ProgressBar progressBar;


    public ChapterFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_chapter, container, false);

        listView = (ListView) view.findViewById(R.id.listView);

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
            DownloadUtils download = new DownloadUtils("http://manga24h.com/59/One-Piece-Dao-Hai-Tac.html");
            return download.GetChapters();
        }
        public void onPostExecute(ArrayList<Chapter> arr) {
            if(arr!=null) {
                chapters = arr;

                ChapterAdapter adapter = new ChapterAdapter(getActivity().getApplicationContext(),R.layout.chapter_list_item,chapters);

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

package io.wyrmise.jumpmanga;


import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import io.wyrmise.jumpmanga.manga24hbaseapi.DownloadUtils;


/**
 * A simple {@link Fragment} subclass.
 */
public class InfoFragment extends Fragment {

    public static final String ARG_IMAGE_URL = "image_url";
    private String[] str;
    private TextView detail, summary;

    public InfoFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        String image = ((DetailedActivity)getActivity()).getImage();

        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_info, container, false);

        new GetMangaDetails().execute("http://manga24h.com/66/Fairy-Tail.html");

        ImageView img = (ImageView) view.findViewById(R.id.image);

        Picasso.with(getActivity().getApplicationContext()).load(image).into(img);

        detail = (TextView) view.findViewById(R.id.detail);


        summary = (TextView) view.findViewById(R.id.description);



        return view;
    }


    public class GetMangaDetails extends AsyncTask<String, Void, String[]> {
        public String[] doInBackground(String... params){
            str = new String[2];
            DownloadUtils download = new DownloadUtils("http://manga24h.com/59/One-Piece-Dao-Hai-Tac.html");
            str[0] = download.GetMangaDetail();
            str[1] = download.GetMangaSummary();


            return str;
        }
        public void onPostExecute(String[] str) {
            detail.setText(str[0]);
            summary.setText(str[1]);
        }
    }
}

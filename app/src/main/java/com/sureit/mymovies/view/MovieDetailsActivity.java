package com.sureit.mymovies.view;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.squareup.picasso.Picasso;
import com.sureit.mymovies.data.Constants;
import com.sureit.mymovies.data.MovieList;
import com.sureit.mymovies.R;
import com.sureit.mymovies.data.TrailerList;
import com.sureit.mymovies.adapter.TrailerAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import am.appwise.components.ni.NoInternetDialog;

import static com.sureit.mymovies.data.Constants.API_KEY;

public class MovieDetailsActivity extends AppCompatActivity {
    private static final String LOG_TAG = "MovieDetailsActivity";
    NoInternetDialog noInternetDialog;
    private List<TrailerList> trailerLists;
    private TrailerAdapter adapter;
    private RecyclerView recyclerViewTr;
    private long idVal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_details);

        setupUI();

    }

    public void setupUI(){
        noInternetDialog = new NoInternetDialog.Builder(this).build();
        ImageView posterImageView = (ImageView) findViewById(R.id.posterImageView);
        TextView titleTextView = (TextView) findViewById(R.id.titleTextView);
        TextView description = (TextView) findViewById(R.id.tVdescription);
        TextView releaseTV= (TextView) findViewById(R.id.tVreleaseDate);
        TextView ratingTV= (TextView) findViewById(R.id.tVRatVal);

        ImageView trailerVV = (ImageView) findViewById(R.id.videoViewTrailer);
        recyclerViewTr = (RecyclerView) findViewById(R.id.trailerRV);
        recyclerViewTr.setHasFixedSize(false);
        recyclerViewTr.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false));
        recyclerViewTr.setItemAnimator(new DefaultItemAnimator());
        trailerLists = new ArrayList<>();

//        Intent intent = getIntent();
//        final String titleText = intent.getStringExtra(MovieAdapter.KEY_NAME);
//        String image = intent.getStringExtra(MovieAdapter.KEY_IMAGE);
//        final String descriptionText = intent.getStringExtra(MovieAdapter.KEY_DESCRIPTION);
//        final String ratings =intent.getStringExtra(MovieAdapter.KEY_VOTE_AVERAGE)+" / 10";
//        final String releaseDate=intent.getStringExtra(MovieAdapter.KEY_RELEASE_DATE);

        Bundle data=getIntent().getExtras();
        assert data != null;
        MovieList movieList=(MovieList) data.getParcelable(Constants.PARCEL_KEY);
        assert movieList != null;

        idVal = movieList.getId();
        final String titleText = movieList.getTitle();
        String image = "https://image.tmdb.org/t/p/w185/"+ movieList.getPosterUrl();
        final String descriptionText = movieList.getDescription();
        final String ratings =movieList.getVote_average()+" / 10";
        final String releaseDate= movieList.getReleaseDate();

        loadTrailers();

        Picasso.with(this)
                .load(image)
                .into(posterImageView);

        titleTextView.setText(titleText);
        ratingTV.setText(ratings);
        releaseTV.setText(releaseDate);
        description.setText(descriptionText);


    }

    private void loadTrailers() {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.show();

        StringRequest stringRequest = new StringRequest(Request.Method.GET,
                Constants.TRAILERS_MOVIES_URL+idVal+"/videos?api_key="+API_KEY , new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {



                try {

                    JSONObject jsonObject = new JSONObject(response);

                    JSONArray array = jsonObject.getJSONArray("results");

                    for (int i = 0; i < array.length(); i++){

                        JSONObject jo = array.getJSONObject(i);

                        TrailerList trailerListData = new TrailerList(jo.getString("id"),jo.getString("key"));
                        trailerLists.add(trailerListData);

                    }

                    adapter = new TrailerAdapter(trailerLists, getApplicationContext());
                    recyclerViewTr.setAdapter(adapter);
                    adapter.notifyDataSetChanged();
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.e(LOG_TAG, e.getMessage(), e);
                }
                progressDialog.dismiss();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                Toast.makeText(MovieDetailsActivity.this, "Error" + error.toString(), Toast.LENGTH_SHORT).show();

            }
        });

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        noInternetDialog.onDestroy();
    }
}

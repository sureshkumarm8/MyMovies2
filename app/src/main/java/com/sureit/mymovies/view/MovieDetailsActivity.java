package com.sureit.mymovies.view;

import android.app.ProgressDialog;
import android.arch.persistence.room.Room;
import android.database.sqlite.SQLiteConstraintException;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
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
import com.sureit.mymovies.R;
import com.sureit.mymovies.adapter.ReviewsAdapter;
import com.sureit.mymovies.adapter.TrailerAdapter;
import com.sureit.mymovies.data.Constants;
import com.sureit.mymovies.data.MovieList;
import com.sureit.mymovies.data.ReviewsList;
import com.sureit.mymovies.data.TrailerList;
import com.sureit.mymovies.db.Movie;
import com.sureit.mymovies.db.MovieDao;
import com.sureit.mymovies.db.MovieDatabase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import am.appwise.components.ni.NoInternetDialog;
import butterknife.BindView;
import butterknife.ButterKnife;

import static com.sureit.mymovies.data.Constants.API_KEY;

public class MovieDetailsActivity extends AppCompatActivity {
    private static final String LOG_TAG = "MovieDetailsActivity";
    NoInternetDialog noInternetDialog;
    private List<TrailerList> trailerLists;
    private TrailerAdapter adapter;
    private long idVal;

    private List<ReviewsList> reviewsLists;
    private ReviewsAdapter adapter2;
    @BindView(R.id.posterBanner) ImageView posterBannerIV;
    @BindView(R.id.posterImageView) ImageView posterImageView ;
    @BindView(R.id.titleTextView)TextView titleTextView ;
    @BindView(R.id.tVdescription)TextView description ;
    @BindView(R.id.tVreleaseDate)TextView releaseTV;
    @BindView(R.id.tVRatVal) TextView ratingTV;
    @BindView(R.id.tVfav) TextView favTV ;
    @BindView(R.id.reviewsRV) RecyclerView recyclerViewRV ;
    @BindView(R.id.videoViewTrailer) ImageView trailerVV;
    @BindView(R.id.trailerRV) RecyclerView recyclerViewTr;

    private ImageView favView;
    private boolean isFavorite = false;

    private MovieDatabase movieDatabase;
    private MovieDao mMovieDao;
    private Movie movie;
    private boolean update;
    private MovieList movieList;
    public static String stringT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_details);

        ButterKnife.bind(this);

        mMovieDao = Room.databaseBuilder(this, MovieDatabase.class, "db-movies")
                .allowMainThreadQueries()   //Allows room to do operation on main thread
                .build()
                .getMovieDao();

        setupUI();

    }

    public void setupUI(){
        noInternetDialog = new NoInternetDialog.Builder(this).build();

        recyclerViewTr.setHasFixedSize(false);
        recyclerViewTr.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false));
        recyclerViewTr.setItemAnimator(new DefaultItemAnimator());
        trailerLists = new ArrayList<>();


        recyclerViewRV.setHasFixedSize(false);
        recyclerViewRV.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recyclerViewRV.setItemAnimator(new DefaultItemAnimator());
        reviewsLists = new ArrayList<>();

//        Intent intent = getIntent();
//        final String titleText = intent.getStringExtra(MovieAdapter.KEY_NAME);
//        String image = intent.getStringExtra(MovieAdapter.KEY_IMAGE);
//        final String descriptionText = intent.getStringExtra(MovieAdapter.KEY_DESCRIPTION);
//        final String ratings =intent.getStringExtra(MovieAdapter.KEY_VOTE_AVERAGE)+" / 10";
//        final String releaseDate=intent.getStringExtra(MovieAdapter.KEY_RELEASE_DATE);

        Bundle data=getIntent().getExtras();
        assert data != null;
        movieList= data.getParcelable(Constants.PARCEL_KEY);
        assert movieList != null;

        idVal = movieList.getId();
        final String titleText = movieList.getTitle();
        String image = "https://image.tmdb.org/t/p/w185/"+ movieList.getPosterUrl();
        String imageB = "https://image.tmdb.org/t/p/w342/"+ movieList.getPosterUrl();
        final String descriptionText = movieList.getDescription();
        final String ratings =movieList.getVote_average()+" / 10";
        final String releaseDate= movieList.getReleaseDate();

        loadTrailers();
        loadReviews();

        Picasso.with(this)
                .load(imageB)
                .placeholder(R.drawable.picasso)
                .into(posterBannerIV);

        Picasso.with(this)
                .load(image)
                .placeholder(R.drawable.picasso)
                .into(posterImageView);

        titleTextView.setText(titleText);
        ratingTV.setText(ratings);
        releaseTV.setText(releaseDate);
        description.setText(descriptionText);

        favView = findViewById(R.id.favIcon);
        favView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleFavorite(v);
            }
        });
        if(mMovieDao.getMovieWithId(idVal)){
            favView.setImageResource(R.drawable.fav_fill);
            favTV.setVisibility(View.GONE);
            isFavorite = true;
        }

    }

    private void loadReviews() {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.show();

        StringRequest stringRequest = new StringRequest(Request.Method.GET,
                Constants.TRAILERS_MOVIES_URL+idVal+"/reviews?api_key="+API_KEY , new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {



                try {

                    JSONObject jsonObject = new JSONObject(response);

                    JSONArray array = jsonObject.getJSONArray("results");

                    for (int i = 0; i < array.length(); i++){

                        JSONObject jo = array.getJSONObject(i);

                        ReviewsList reviewsListData = new ReviewsList(jo.getString("author"),jo.getString("content"));
                        reviewsLists.add(reviewsListData);

                    }

                    adapter2 = new ReviewsAdapter(reviewsLists, getApplicationContext());
                    recyclerViewRV.setAdapter(adapter2);
                    adapter2.notifyDataSetChanged();
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

    private void toggleFavorite(View v) {

        if (!isFavorite) {
            Movie movie = new Movie();
            movie.setTitle(movieList.getTitle());
            movie.setContent(movieList.getDescription());
            movie.setMovie_id(movieList.getId());
            movie.setPosterUrl(movieList.getPosterUrl());
            movie.setReleasedate(movieList.getReleaseDate());
            movie.setVote_average(movieList.getVote_average());
            //Insert to database
            try {
                mMovieDao.insert(movie);
                setResult(RESULT_OK);
            } catch (SQLiteConstraintException e) {
                Snackbar.make(v.getRootView(),"A movie with same details already exists.",Snackbar.LENGTH_SHORT).show();
            }
            favView.setImageResource(R.drawable.fav_fill);
            favTV.setVisibility(View.GONE);
            Snackbar.make(v.getRootView(),"Added to favorites", Snackbar.LENGTH_SHORT).show();
            isFavorite = true;
        }else {
            Movie movie = new Movie();
            movie.setTitle(movieList.getTitle());
            movie.setContent(movieList.getDescription());
            movie.setMovie_id(movieList.getId());
            movie.setPosterUrl(movieList.getPosterUrl());
            movie.setReleasedate(movieList.getReleaseDate());
            movie.setVote_average(movieList.getVote_average());
            mMovieDao.delete(movie);
            String id= String.valueOf(movie.getMovie_id());
            favTV.setVisibility(View.VISIBLE);
            favView.setImageResource(R.drawable.fav_empty);

            setResult(RESULT_OK);
            Snackbar.make(v.getRootView(),"Removed from favorites", Snackbar.LENGTH_SHORT).show();
            isFavorite = false;
        }
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        noInternetDialog.onDestroy();
    }
}

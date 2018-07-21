package com.sureit.mymovies.view;

import android.app.ProgressDialog;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.arch.persistence.room.Room;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.sureit.mymovies.R;
import com.sureit.mymovies.adapter.MovieAdapter;
import com.sureit.mymovies.data.MovieList;
import com.sureit.mymovies.db.MovieDao;
import com.sureit.mymovies.db.MovieDatabase;
import com.sureit.mymovies.db.MovieViewModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import am.appwise.components.ni.NoInternetDialog;

import static com.sureit.mymovies.Util.Constants.API_KEY;
import static com.sureit.mymovies.Util.Constants.BASE_URL_MOVIE;
import static com.sureit.mymovies.Util.Constants.DB_NAME;
import static com.sureit.mymovies.Util.Constants.FAV_ROT;
import static com.sureit.mymovies.Util.Constants.PARCEL_KEY;
import static com.sureit.mymovies.Util.Constants.POPULAR_MOVIES_URL;
import static com.sureit.mymovies.Util.Constants.TOP_RATED_MOVIES_URL;

public class MainActivity extends AppCompatActivity {

    private static final String LOG_TAG = "MainActivity";
    NoInternetDialog noInternetDialog;

    private RecyclerView recyclerView;
    private MovieAdapter adapter;
    private List<MovieList> movieLists;

    private MovieDao mMovieDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recyclerViewMovie);
        recyclerView.setHasFixedSize(true);
        if(this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        }else {
            recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        }
        movieLists = new ArrayList<>();
        noInternetDialog = new NoInternetDialog.Builder(this).build();

        mMovieDao = Room.databaseBuilder(this, MovieDatabase.class, DB_NAME)
                .allowMainThreadQueries()   //Allows room to do operation on main thread
                .build()
                .getMovieDao();

        if(savedInstanceState!=null){
            if(savedInstanceState.containsKey(PARCEL_KEY)) {
                movieLists = savedInstanceState.getParcelableArrayList(PARCEL_KEY);
                adapter = new MovieAdapter(movieLists, getApplicationContext());
                recyclerView.setAdapter(adapter);
                adapter.notifyDataSetChanged();
            }
        }
        if(FAV_ROT){
            loadFavMovies();
        }else {
            loadUrlData(BASE_URL_MOVIE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.sort_settings,menu);
    return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id){

            case R.id.popular:
                item.setChecked(true);
                noInternetDialog = new NoInternetDialog.Builder(this).build();
                movieLists.clear();
                FAV_ROT = false;
                loadUrlData(POPULAR_MOVIES_URL);
                return true;

            case R.id.rated:
                item.setChecked(true);
                noInternetDialog = new NoInternetDialog.Builder(this).build();
                movieLists.clear();
                FAV_ROT = false;
                loadUrlData(TOP_RATED_MOVIES_URL);
                return true;

            case R.id.myfav:
                item.setChecked(true);
                movieLists.clear();
                FAV_ROT = true;
                loadFavMovies();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void loadFavMovies() {
        LiveData<List<MovieList>> movieListsL = mMovieDao.getMovies();
        movieListsL.observe(this, new Observer<List<MovieList>>() {
            @Override
            public void onChanged(@Nullable List<MovieList> movieLists) {
                adapter.setMoviesLive(movieLists);
                adapter = new MovieAdapter(adapter.getMoviesLive(), getApplicationContext());
                recyclerView.setAdapter(adapter);
                adapter.notifyDataSetChanged();
            }
        });

    }

    private void loadUrlData(String movieurl) {

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.show();

        StringRequest stringRequest = new StringRequest(Request.Method.GET,
                movieurl+API_KEY , new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                try {

                    JSONObject jsonObject = new JSONObject(response);
                    JSONArray array = jsonObject.getJSONArray("results");

                    for (int i = 0; i < array.length(); i++){

                        JSONObject jo = array.getJSONObject(i);

                        MovieList movieList = new MovieList(jo.getLong("id"),jo.getString("title"), jo.getString("overview"),
                                jo.getString("poster_path"),jo.getString("vote_average"),jo.getString("release_date"));
                        movieLists.add(movieList);

                    }

                    adapter = new MovieAdapter(movieLists, getApplicationContext());
                    recyclerView.setAdapter(adapter);
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

                Toast.makeText(MainActivity.this, "Error" + error.toString(), Toast.LENGTH_SHORT).show();

            }
        });

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelableArrayList(PARCEL_KEY, (ArrayList<? extends
                Parcelable>) movieLists);
    }

    private void setupViewModel() {
        MovieViewModel viewModel = ViewModelProviders.of(this).get(MovieViewModel.class);
        viewModel.getTasks().observe(this, new Observer<List<MovieList>>() {
            @Override
            public void onChanged(@Nullable List<MovieList> taskEntries) {
                adapter.setMoviesLive(taskEntries);
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if(FAV_ROT){
            loadFavMovies();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        noInternetDialog.onDestroy();
    }
}

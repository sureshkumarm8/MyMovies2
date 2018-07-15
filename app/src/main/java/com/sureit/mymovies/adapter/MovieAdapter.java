package com.sureit.mymovies.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.squareup.picasso.Picasso;
import com.sureit.mymovies.data.Constants;
import com.sureit.mymovies.view.MovieDetailsActivity;
import com.sureit.mymovies.data.MovieList;
import com.sureit.mymovies.R;

import java.util.List;

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.ViewHolder> {


    public static final String KEY_NAME = "name";
    public static final String KEY_IMAGE = "image";
    public static final String KEY_DESCRIPTION = "description";
    public static final String KEY_VOTE_AVERAGE= "vote_average";
    public static final String KEY_RELEASE_DATE= "release_date";
    // we define a list from the DevelopersList java class

    private List<MovieList> movieLists;
    private Context context;

    public MovieAdapter(List<MovieList> movieLists, Context context) {

        // generate constructors to initialise the List and Context objects

        this.movieLists = movieLists;
        this.context = context;

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // this method will be called whenever our ViewHolder is created
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.movie_item_card, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {

        // this method will bind the data to the ViewHolder from whence it'll be shown to other Views

        final MovieList developersList = movieLists.get(position);

        Picasso.with(context)
                .load("https://image.tmdb.org/t/p/w185/"+(developersList.getPosterUrl()))
                .into(holder.poster_url);

        holder.relativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MovieList movieList1 = movieLists.get(position);
                Intent skipIntent = new Intent(v.getContext(), MovieDetailsActivity.class);
                skipIntent.putExtra(Constants.PARCEL_KEY,
                        new MovieList(movieList1.getId() ,movieList1.getTitle(),movieList1.getDescription(),movieList1.getPosterUrl(),
                                movieList1.getVote_average(),movieList1.getReleaseDate()));
                v.getContext().startActivity(skipIntent);
            }
        });

    }

    public static String makeRequestUrlForPoster(String posterPath) {
        Uri.Builder uriBuilder = Uri.parse("https://image.tmdb.org/t/p/w185")
                .buildUpon()
                .appendPath(posterPath);
        return uriBuilder.toString();
    }

    @Override

    //return the size of the listItems (developersList)

    public int getItemCount() {
        return movieLists.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder  {

        // define the View objects

        public ImageView poster_url;
        public RelativeLayout relativeLayout;

        public ViewHolder(View itemView) {
            super(itemView);

            // initialize the View objects

            poster_url = (ImageView) itemView.findViewById(R.id.imageView);
            relativeLayout = (RelativeLayout) itemView.findViewById(R.id.relativeLayoutRV);
        }

    }
}

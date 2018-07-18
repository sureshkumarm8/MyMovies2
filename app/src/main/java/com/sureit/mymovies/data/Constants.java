package com.sureit.mymovies.data;

import com.sureit.mymovies.BuildConfig;

public class Constants {
    public static final String BASE_URL_MOVIE = "https://api.themoviedb.org/3/discover/movie?api_key=";
    public static final String POPULAR_MOVIES_URL = "https://api.themoviedb.org/3/movie/popular?api_key=";
    public static final String TOP_RATED_MOVIES_URL = "https://api.themoviedb.org/3/movie/top_rated?api_key=";
    public static final String TRAILERS_MOVIES_URL = "https://api.themoviedb.org/3/movie/";

    public static final String API_KEY = BuildConfig.MovieSecAPIKEY;

    public static final String PARCEL_KEY="MovieParcel";

    public static final String TABLE_NAME_NOTE ="favmovies";
    public static final String DB_NAME ="favmoviesdb.db";
}

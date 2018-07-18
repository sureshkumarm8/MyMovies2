package com.sureit.mymovies.db;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

/**
 * Created by Pavneet_Singh on 12/31/17.
 */

@Dao
public interface MovieDao {

    @Insert
    void insert(Movie movie);

    @Update
    void update(Movie... repos);

    @Delete
    void delete(Movie movie);

    @Query("SELECT * FROM  moviesfav")
    List<Movie> getMovies();

    @Query("SELECT * FROM moviesfav WHERE movie_id = :number")
    boolean getMovieWithId(long number);

}

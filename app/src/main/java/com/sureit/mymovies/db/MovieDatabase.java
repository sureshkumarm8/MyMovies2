package com.sureit.mymovies.db;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

/**
 * Created by Pavneet_Singh on 12/31/17.
 */

@Database(entities = { Movie.class }, version = 1)
//@TypeConverters({DateTypeConverter.class})
public abstract class MovieDatabase extends RoomDatabase {
    public abstract MovieDao getMovieDao();

}
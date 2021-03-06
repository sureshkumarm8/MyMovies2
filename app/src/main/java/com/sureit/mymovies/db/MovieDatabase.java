package com.sureit.mymovies.db;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

import com.sureit.mymovies.Util.Constants;
import com.sureit.mymovies.data.MovieList;

/**
 * Created by Pavneet_Singh on 12/31/17.
 */

@Database(entities = { MovieList.class }, version = 3)
//@TypeConverters({DateTypeConverter.class})
public abstract class MovieDatabase extends RoomDatabase {
    public abstract MovieDao getMovieDao();

    private static MovieDatabase INSTANCE;

    public static MovieDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(
                    context.getApplicationContext(),
                    MovieDatabase.class, Constants.DB_NAME)
                    .build();
        }

        return INSTANCE;
    }

    public static void destroyInstance() {
        INSTANCE = null;
    }


}
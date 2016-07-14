package ajpatel72.popularmovies.utils;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import ajpatel72.popularmovies.R;
import ajpatel72.popularmovies.model.Movie;
import ajpatel72.popularmovies.provider.DatabaseColumns;
import ajpatel72.popularmovies.provider.DatabaseProvider;
import rx.Subscription;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by Amit on 6/14/2016
 */

public class Utils {

    private static final String TAG = Utils.class.getSimpleName(); // For Debugging

    /*
    * The current movie is passed in as a parameter and a check is done whether
    * it is already in the database. If it is in the database, it is deleted, if
    * it is not in the database, it is inserted.
     */
    public static boolean handleFavoriteMovies(Context context, Movie movie) {
        boolean isFavorite;
        // If this movie is favorited, delete it from the database.
        if (isFavoritedMovie(context, movie)) {
            isFavorite = true;
            context.getContentResolver().delete(
                    DatabaseProvider.Movies.withId(Integer.parseInt(movie.getId())),
                    String.valueOf(movie.getId()),
                    null);
            // Otherwise, insert the movie into the database.
        } else {
            isFavorite = false;
            ContentValues contentValues = new ContentValues();
            contentValues.put(DatabaseColumns.MOVIE_ID, movie.getId());
            contentValues.put(DatabaseColumns.TITLE, movie.getOriginal_title());
            contentValues.put(DatabaseColumns.POSTER_PATH, movie.getPoster_path());
            contentValues.put(DatabaseColumns.BACKDROP_PATH, movie.getBackdrop_path());
            contentValues.put(DatabaseColumns.OVERVIEW, movie.getOverview());
            contentValues.put(DatabaseColumns.VOTE_AVERAGE, Double.toString(movie.getVote_average()));
            contentValues.put(DatabaseColumns.RELEASE_DATE, movie.getRelease_date());
            context.getContentResolver().insert(DatabaseProvider.Movies.CONTENT_URI, contentValues);
        }
        return isFavorite;
    }

    /*
    * This method returns true if the passed in movie is a favorite, and returns
    * false if the passed in movie is not in the database.
     */
    public static boolean isFavoritedMovie(Context context, Movie movie) {

        Uri movies = DatabaseProvider.Movies.CONTENT_URI;

        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query
                    (movies, null, DatabaseColumns.MOVIE_ID + " = " + movie.getId(), null, DatabaseColumns._ID);
            if (cursor != null && cursor.moveToNext()) {
                return true;
            } else {
                return false;
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
    }

    /*
    * This method returns a list of all the movies in the database.
     */
    public static List<Movie> getFavoritedMovies(Context context) {

        List<Movie> movies = new ArrayList<>();

        Uri movie = DatabaseProvider.Movies.CONTENT_URI;
        Cursor cursor = context.getContentResolver().query(movie, null, null, null, DatabaseColumns._ID);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                Movie movie1 = new Movie();
                movie1.setId(String.valueOf(cursor.getInt(cursor.getColumnIndex(DatabaseColumns.MOVIE_ID))));
                movie1.setOriginal_title(cursor.getString(cursor.getColumnIndex(DatabaseColumns.TITLE)));
                movie1.setPoster_path(cursor.getString(cursor.getColumnIndex(DatabaseColumns.POSTER_PATH)));
                movie1.setBackdrop_path(cursor.getString(cursor.getColumnIndex(DatabaseColumns.BACKDROP_PATH)));
                movie1.setRelease_date(cursor.getString(cursor.getColumnIndex(DatabaseColumns.RELEASE_DATE)));
                movie1.setOverview(cursor.getString(cursor.getColumnIndex(DatabaseColumns.OVERVIEW)));
                movie1.setVote_average((float)
                        Double.parseDouble(cursor.getString(cursor.getColumnIndex(DatabaseColumns.VOTE_AVERAGE))));
                movies.add(movie1);
            }
//            cursor.close();
        }
        cursor.close();
        return movies;
    }


    // http://stackoverflow.com/questions/33929760/detect-if-android-device-is-connected-to-the-internet
    /*
    * Returns true if there is network connectivity and false otherwise.
     */
    public static boolean isNetworkAvailable(Context context) {

        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    // Uses the isNetworkAvailable method to show a snackbar if there is no internet.
    public static boolean handleNetworkError(Context context, View rootView, String text) {

        if (Utils.isNetworkAvailable(context)) {
            return true;
        } else {
            showSnackbar(context, rootView, text + " Not Available Offline.");
            return false;
        }
    }

    //http://stackoverflow.com/questions/34020891/how-to-change-background-color-of-the-snackbar
    /*
     * Accepts text and displays the text in a Snackbar with a custom background color.
     */
    public static void showSnackbar(Context context, View rootView, String text) {

        Snackbar sb = Snackbar.make(rootView, text, Snackbar.LENGTH_LONG);
        View sbView = sb.getView();

        TextView mTextView = (TextView) rootView.findViewById(android.support.design.R.id.snackbar_text);
        // set text to center
        if (mTextView != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
                mTextView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            else {
                mTextView.setGravity(Gravity.CENTER_HORIZONTAL);
                mTextView.setGravity(Gravity.CENTER_VERTICAL);
            }
        }
        sbView.setBackgroundColor(ContextCompat.getColor(context, R.color.primary));
        sb.show();
    }


    public static void shareAppIntent(Context context, String subject, String text) {

        Intent sharingIntent = new Intent();
        sharingIntent.setAction(Intent.ACTION_SEND);
        sharingIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
        sharingIntent.putExtra(Intent.EXTRA_TEXT, text);
        sharingIntent.setType("text/plain");
        context.startActivity(Intent.createChooser(sharingIntent, "Share Movie Using"));

    }


    //https://github.com/THEONE10211024/RxJavaSamples/blob/master/app/src/main/java/pers/example/xiayong/rxjavasamples/fragments/RetrofitFragment.java
    public static void unsubscribeIfNotNull(Subscription subscription) {

        if (subscription != null) {
            subscription.unsubscribe();
        }
    }

    public static CompositeSubscription getNewCompositeSubIfUnsubscribed(CompositeSubscription subscription) {

        if (subscription == null || subscription.isUnsubscribed()) {
            return new CompositeSubscription();
        }
        return subscription;
    }
}

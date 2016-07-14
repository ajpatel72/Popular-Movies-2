/*
 * Created By Amit Patel
 * Project 1: Popular Movies
 * For the Udacity Nanodegree
 */
package ajpatel72.popularmovies.utils;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;

import ajpatel72.popularmovies.model.Movie;

/**
 * Class that gets a bitmap that is attached to a drawable
 */
public class ImageUtils {

    private static final String url = "http://image.tmdb.org/t/p/";

    /**
     * returns a bitmap that is attached to a  given drawable
     */
//    public static Bitmap getBitmap(ImageView imageView) {
//        return  ((BitmapDrawable)imageView.getDrawable()).getBitmap();
//    }

    public static Bitmap getBitmap(ScalingImageView imageView) {
        return  ((BitmapDrawable)imageView.getDrawable()).getBitmap();
    }


    /**
     * Utility method to construct the poster path from the url and
     * two parameters (poster size and poster path)
     */
    public static String getPosterPath(Movie movie, String posterSize) {
        return url + posterSize + "/" + movie.getPoster_path();
    }

    /**
     * Utility method to construct the backdrop path from the url and
     * two parameters (backdrop size and backdrop path)
     */
    public static String getBackdropPath(Movie movie, String backdropSize) {
        return url + backdropSize + "/" + movie.getBackdrop_path();
    }
}

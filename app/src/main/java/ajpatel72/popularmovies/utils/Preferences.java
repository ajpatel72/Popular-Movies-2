/*
 * Created By Amit Patel
 * Project 1: Popular Movies
 * For the Udacity Nanodegree
 */
package ajpatel72.popularmovies.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import ajpatel72.popularmovies.R;

/**
 * A Utility class that encapsulates all of the Shared Preferences Logic
 */
public class Preferences {

    public static String getSortType(Context context) {
        SharedPreferences prefs = getDefaultSharedPreferences(context);
        String defaultSort = context.getString(R.string.default_pref);
        return prefs.getString(context.getString(R.string.pref_key_sort), defaultSort);
    }

    public static void setSortType(Context context, String sortType){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(context.getString(R.string.pref_key_sort), sortType);
        editor.apply();
    }

    private static SharedPreferences getDefaultSharedPreferences(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }
}

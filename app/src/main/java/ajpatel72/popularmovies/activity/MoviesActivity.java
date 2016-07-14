/*
 * Created By Amit Patel
 * Project 1: Popular Movies
 * For the Udacity Nanodegree
 */
package ajpatel72.popularmovies.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import java.util.List;

import ajpatel72.popularmovies.R;
import ajpatel72.popularmovies.adapter.MoviesAdapter;
import ajpatel72.popularmovies.fragment.MovieDetailsFragment;
import ajpatel72.popularmovies.fragment.MoviesFragment;
import ajpatel72.popularmovies.model.Movie;
import ajpatel72.popularmovies.utils.Utils;
import butterknife.Bind;
import butterknife.ButterKnife;

import static ajpatel72.popularmovies.activity.DetailsActivity.buildIntent;

/*
* Master Activity that sets up the toolbar, calculated whether single or dual pane, implements
* a movie click interface to handle the major functionality of the app by opening the appropriate
* window depending on whether we are in single or dual pane mode.
 */
public class MoviesActivity extends AppCompatActivity
                            implements MoviesAdapter.MovieClickInterface,
                                       MovieDetailsFragment.FavoritesActionListener {

    private static final String TAG = MoviesActivity.class.getSimpleName(); // For Debugging

    private static final String STATE_MOVIES = "state_movies";

    boolean mTwoPane;
    private Movie mSavedInstanceMovies = null;
    private MoviesFragment mMoviesFragment;

    @Bind(R.id.toolbar)     Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movies);

        ButterKnife.bind(this);

        setUpToolbar();
        // Detect whether we are in single pane or dual (tablet) mode
        mTwoPane = findViewById(R.id.details_fragment_container) != null;
        // If dual pane and if this is not the first pass
        if (mTwoPane && savedInstanceState != null) {
            replaceDetailsFragment((Movie) savedInstanceState.getParcelable(STATE_MOVIES));
        }
        mMoviesFragment = (MoviesFragment) getSupportFragmentManager().findFragmentById(R.id.movies_fragment);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(STATE_MOVIES, mSavedInstanceMovies);
    }

    @Override
    public void onMovieClick(View movieView, Movie movie) {
        // Save movie details loaded from RecyclerView Adapter into mSavedInstanceMovies
        mSavedInstanceMovies = movie;

        if (!mTwoPane) {
            startActivity(buildIntent(this, movie));    // Simply start a new activity
        } else {
            replaceDetailsFragment(movie);       // Add new fragment to the right pane
        }
    }

    public void replaceDetailsFragment(@Nullable Movie movie) {
        if (!mTwoPane)     return;
        if (movie == null) return;

        MovieDetailsFragment mDetailsFragment = MovieDetailsFragment.newInstance(movie);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.details_fragment_container,
                         mDetailsFragment,
                         MovieDetailsFragment.class.getSimpleName())
                .commit();
    }

    private void setUpToolbar() {
        setSupportActionBar(mToolbar);
    }

    @Override
    public void onFavoriteAction() {
        List<Movie> movies = Utils.getFavoritedMovies(this);
        mMoviesFragment.loadFavoritesFromMoviesActivity(movies);
    }
}

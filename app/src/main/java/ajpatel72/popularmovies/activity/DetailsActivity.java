/*
 * Created By Amit Patel
 * Project 1: Popular Movies
 * For the Udacity Nanodegree
 */
package ajpatel72.popularmovies.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import ajpatel72.popularmovies.R;
import ajpatel72.popularmovies.fragment.MovieDetailsFragment;
import ajpatel72.popularmovies.model.Movie;
import ajpatel72.popularmovies.utils.ImageUtils;
import butterknife.Bind;
import butterknife.ButterKnife;

/*
* Details Master activity that simply sets up the toolbar, loads a backdrop
* and loads the data from the previous activity via an intent. We only
* get here in single pane mode, so a fragment transaction is applied that
* holds an instance of a movie.
 */
public class DetailsActivity extends AppCompatActivity
                             implements  MovieDetailsFragment.FavoritesActionListener {

    private static final String TAG = DetailsActivity.class.getSimpleName(); // For Debugging

    private static final String MOVIE_FROM_PARCEL = "movie";

    private Movie mMovie;

    @Bind(R.id.collapsing_toolbar)  CollapsingToolbarLayout mCollapsingToolbar;
    @Bind(R.id.backdrop)            ImageView mImageBackDrop;
    @Bind(R.id.toolbar)             Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_details);

        ButterKnife.bind(this);

        loadFromIntent();

        setupToolbar();

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.main_frame, MovieDetailsFragment.newInstance(mMovie))
                .commit();

        loadBackdrop();

    }

    public final Toolbar getToolbar() {
        return mToolbar;
    }

    /*
    * Set up toolbar, collapsing tool bar's title
    */
    private void setupToolbar() {
        setSupportActionBar(mToolbar);

        if (getSupportActionBar() == null) {
            throw new IllegalStateException("Action bar not set! Toolbar view was: " + mToolbar);
        } else {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        mCollapsingToolbar.setTitle(mMovie.getOriginal_title());
    }


    /**
     * Get the intent from the previous activity.
     * Load the movie object
     */
    private void loadFromIntent() {
        Intent newIntent = getIntent();
        if (newIntent == null) {
            finish();
        } else {
            mMovie = newIntent.getParcelableExtra(MOVIE_FROM_PARCEL);
            if (mMovie == null) {
                finish();
            }
        }
    }

    /**
     * Preferred way of building an intent to start a new activity
     * and pass in information via intent extras.
     */
    public static Intent buildIntent(Activity activity, Movie movie) {
        Intent intent = new Intent(activity, DetailsActivity.class);
        intent.putExtra(MOVIE_FROM_PARCEL, movie);
        return intent;
    }

    /*
    * Use the Picasso Library to laod the backdrop.
     */
    private void loadBackdrop() {
        if(mMovie.getBackdrop_path() != null) {
            Picasso.with(this)
                    .load(ImageUtils.getBackdropPath(mMovie, getString(R.string.param_backdrop_size)))
                    .into(mImageBackDrop);
        }
    }

    @Override
    public void onFavoriteAction() {

    }
}

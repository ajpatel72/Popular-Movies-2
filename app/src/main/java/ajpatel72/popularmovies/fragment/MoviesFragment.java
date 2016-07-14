package ajpatel72.popularmovies.fragment;

import android.graphics.Rect;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import ajpatel72.popularmovies.R;
import ajpatel72.popularmovies.adapter.MoviesAdapter;
import ajpatel72.popularmovies.model.Movie;
import ajpatel72.popularmovies.model.MovieResponse;
import ajpatel72.popularmovies.service.RestClient;
import ajpatel72.popularmovies.service.RestClientService;
import ajpatel72.popularmovies.utils.Preferences;
import ajpatel72.popularmovies.utils.Utils;
import butterknife.Bind;
import butterknife.BindDimen;
import butterknife.BindInt;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/*
* Master fragment whose major function is to load the movies into the recycler view. The sort
* menu functionality is largely implemented here. There is a network call each time the Sort Order
* is changed unless we are lookng at favorites, in whcih case,we have an Array List.
 */
public class MoviesFragment extends BaseFragment
                            implements MoviesAdapter.MovieClickInterface {

    private static final String TAG = MoviesFragment.class.getSimpleName(); // For Debugging

    // Strings to display in the Sort Menu
    private static final String MOST_POPULAR = "Most Popular";
    private static final String TOP_RATED = "Top Rated";
    private static final String FAVORITE = "Favorite";
    // Strings to keep track of the Sort Order
    private static final String POPULARITY_DESC = "popularity.desc";
    private static final String TOP_RATED_DESC = "vote_average.desc";
    private static final String FAVORITE_DESC = "favorite.desc";

    private static final String STATE_MOVIES = "state_movies";

    private MoviesAdapter mMoviesAdapter;

    ViewGroup rootView;

    private RestClient mRestClient = null;

    private String mSort;

    private List<Movie> mSavedInstanceMovies;

    private CompositeSubscription _subscriptions = new CompositeSubscription();

    // Adaptable Recycler View Settings that change according to device size
    @BindInt(R.integer.photo_grid_columns)      int mColumns;
    @BindDimen(R.dimen.grid_item_spacing)                             int mGridSpacing;
    @Bind(R.id.image_grid)                      RecyclerView mRecyclerViewGrid;

    public MoviesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(STATE_MOVIES, (ArrayList<? extends Parcelable>) mSavedInstanceMovies);
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = (ViewGroup) inflater.inflate(R.layout.fragment_movies, container, false);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mSort = Preferences.getSortType(getActivity());
        mRestClient = new RestClient();
        // If it's the first time, make a network call, if not use the existing.
        if (savedInstanceState != null) {
            mSavedInstanceMovies = savedInstanceState.getParcelableArrayList(STATE_MOVIES);
            mMoviesAdapter = new MoviesAdapter(getActivity(), mSavedInstanceMovies, this);
        } else {
            if (mSort.equals(FAVORITE_DESC)) {
                mSavedInstanceMovies = new ArrayList<>();
                mMoviesAdapter = new MoviesAdapter(getActivity(), mSavedInstanceMovies, this);

                List<Movie> movies = Utils.getFavoritedMovies(this.getActivity());
                loadFavorites(movies);
            } else {
                mSavedInstanceMovies = new ArrayList<>();
                mMoviesAdapter = new MoviesAdapter(getActivity(), mSavedInstanceMovies, this);

                getMoviesFromTMDB();
            }
        }
        setUpRecyclerViewGrid();
    }

    //https://github.com/THEONE10211024/RxJavaSamples/blob/master/app/src/main/java/pers/example/xiayong/rxjavasamples/fragments/RetrofitFragment.java

    /*
    * When another activity is in focus or maybe we turn off the
    * device, unsuscribe the RxJava subscription and update the
    * Sort Order in the Shared Preferences.
     */
    @Override
    public void onPause() {
        super.onPause();
        Utils.unsubscribeIfNotNull(_subscriptions);
        Preferences.setSortType(getActivity(), mSort);
    }

    /*
    * Upon Resume, make a new RxJava subscription if it's null.
    * Update the subtitle depending on the Sort Order
     */
    @Override
    public void onResume() {
        super.onResume();
        _subscriptions = Utils.getNewCompositeSubIfUnsubscribed(_subscriptions);
        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            if (mSort.equalsIgnoreCase(POPULARITY_DESC)) {
                actionBar.setSubtitle(MOST_POPULAR);
            } else if (mSort.equalsIgnoreCase(TOP_RATED_DESC)) {
                actionBar.setSubtitle(TOP_RATED);
            } else if (mSort.equalsIgnoreCase(FAVORITE_DESC)) {
                actionBar.setSubtitle(FAVORITE);
                List<Movie> m = Utils.getFavoritedMovies(this.getActivity());
                loadFavorites(m);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        _subscriptions.unsubscribe();
        Preferences.setSortType(getActivity(), mSort);
    }

    /*
    * Using Retrofit and RxJava to make the correct call by querying th
    * Sort Order. On Success, the Movies are loaded into the Grid.
     */
    private void getMoviesFromTMDB() {

        if (mSort.equals(FAVORITE_DESC)) {
            // do nothing
            return;
        }
        RestClientService restClientService = mRestClient.getRestClientService();

        rx.Observable movieObservable;
        // Make the correct call based on the Sort Order
        if (mSort.equals(POPULARITY_DESC)) {
            movieObservable = restClientService.getMoviesPopular(RestClient.API_KEY);
        } else {
            movieObservable = restClientService.getMoviesTopRated(RestClient.API_KEY);
        }
        _subscriptions.add(
                movieObservable
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Observer<MovieResponse>() {
                            @Override
                            public void onCompleted() {
                            }

                            @Override
                            public void onError(Throwable e) {
                                Utils.handleNetworkError(getActivity(), rootView, "Content");
                            }

                            @Override
                            public void onNext(MovieResponse movieResponse) {
                                loadMoviesIntoGrid(movieResponse.results);
                            }
                        })
        );
    }

    /**
     * Clear the RecyclerView and load the List of movies
     */
    private void loadMoviesIntoGrid(List<Movie> results) {
//        mEmpty.setVisibility(View.GONE);
        mMoviesAdapter.clear();
        mMoviesAdapter.add(results);
    }

    /**
     * Create GridLayoutManager and setup the RecyclerView
     * using mColumns and mGridSpacing whose value change
     * automatically depending on screen size. (Defined in
     * integers.xml and dimens.xml).
     */
    private void setUpRecyclerViewGrid() {
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), mColumns);
        mRecyclerViewGrid.setLayoutManager(gridLayoutManager);
        mRecyclerViewGrid.addItemDecoration(new GridMarginDecoration(mGridSpacing));
        mRecyclerViewGrid.setHasFixedSize(true);
        mRecyclerViewGrid.setAdapter(mMoviesAdapter);
    }


    private static class GridMarginDecoration extends RecyclerView.ItemDecoration {

        private final int space;

        public GridMarginDecoration(int space) {
            this.space = space;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view,
                                   RecyclerView parent, RecyclerView.State state) {
            outRect.left = space;
            outRect.top = space;
            outRect.right = space;
            outRect.bottom = space;
        }
    }

    /*
    * Implement the interface defined in MoviesAdapter
    * to handle the click event of each cell in the RecycleView
    */
    @Override
    public void onMovieClick(View itemView, Movie movie) {
        ((MoviesAdapter.MovieClickInterface) getActivity()).onMovieClick(itemView, movie);
    }

    /**
     * Inflate the menu
     * this adds items to the action bar if it is present.
     */
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        getActivity().getMenuInflater().inflate(R.menu.menu_movies, menu);
    }

    /**
     * Using the mSort variable that is filled using Shared
     * Preferences, we check the appropriate menu item.
     */
    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        switch (mSort) {
            case POPULARITY_DESC:
                menu.findItem(R.id.menu_sort_popularity).setChecked(true);
                break;
            case TOP_RATED_DESC:
                menu.findItem(R.id.menu_sort_rating).setChecked(true);
                break;
            case FAVORITE_DESC:
                menu.findItem(R.id.menu_sort_favorites).setChecked(true);
        }
    }

    /**
     * When a new menu item is clicked, the Sort variable
     * is updated and the handleActionBarSorting method
     * is called for the selected option.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // If not a favorite option, only perform action if internet is available.
        switch (item.getItemId()) {
            case R.id.menu_sort_popularity:
                if (Utils.handleNetworkError(getActivity(), rootView, "Most Popular")) {
                    mSort = POPULARITY_DESC;
                    handleActionBarSorting(POPULARITY_DESC, item, MOST_POPULAR);
                }
                break;
            case R.id.menu_sort_rating:
                if (Utils.handleNetworkError(getActivity(), rootView, " Top Rated")) {
                    mSort = TOP_RATED_DESC;
                    handleActionBarSorting(TOP_RATED_DESC, item, TOP_RATED);
                }
                break;
            case R.id.menu_sort_favorites:
                mSort = FAVORITE_DESC;
                handleActionBarSorting(FAVORITE_DESC, item, FAVORITE);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    /*
    * Called from onOptionsItemSelected method to modify the menu
    * and get new content if necessary
    *
    * When a new menu item is clicked, the Shared Preferences
    * are updated, the Action Bar's subtitle is updated and
    * onSortChange is called to retrieve the movies
    */
    private void handleActionBarSorting(String order, MenuItem item, String text) {
        if (item.isChecked()) {
            item.setChecked(false);
        } else {
            item.setChecked(!item.isChecked());
            Preferences.setSortType(getActivity(), order);
            if ( ((AppCompatActivity) getActivity()).getSupportActionBar() != null )
                ((AppCompatActivity) getActivity()).getSupportActionBar().setSubtitle(text);
            // If Favorites, load list, otherwise, make network call to service
            if (text.equals(FAVORITE)) {
                List<Movie> m = Utils.getFavoritedMovies(this.getActivity());
                loadFavorites(m);
            } else {
                onSortChanged();
            }
            Preferences.setSortType(getActivity(), mSort);
        }
    }

    /*
    * If the Sort Order is Favorites, and there are movies to
    * show, then display them, otherwise clear the right pane
    * and show snackbar.
     */
    public void loadFavorites(List<Movie> movies) {
        if (movies.size() > 0) {
            mMoviesAdapter.clear();
            mMoviesAdapter.add(movies);
        } else {
            mMoviesAdapter.clear();
            Utils.showSnackbar(getActivity(), rootView, getResources().getString(R.string.no_saved_favorites));
        }
    }

    /*
     * When the favorite button is clicked, we only want the right pane
     * to be cleared and reloaded if the Sort Order is favorite.
     */
    public void loadFavoritesFromMoviesActivity(List<Movie> movies) {
        if (mSort.equals(FAVORITE_DESC)) {
            loadFavorites(movies);
        }
    }

    private void onSortChanged() {
        // Make Network Call
        getMoviesFromTMDB();
    }
}

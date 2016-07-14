package ajpatel72.popularmovies.fragment;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.CardView;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

import ajpatel72.popularmovies.R;
import ajpatel72.popularmovies.model.Movie;
import ajpatel72.popularmovies.model.Review;
import ajpatel72.popularmovies.model.ReviewResponse;
import ajpatel72.popularmovies.model.Trailer;
import ajpatel72.popularmovies.model.TrailerResponse;
import ajpatel72.popularmovies.service.RestClient;
import ajpatel72.popularmovies.service.RestClientService;
import ajpatel72.popularmovies.utils.ImageUtils;
import ajpatel72.popularmovies.utils.ScalingImageView;
import ajpatel72.popularmovies.utils.Utils;
import butterknife.Bind;
import butterknife.OnClick;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/*
* In this class,
* The Share Trailer and Add Favorite functionalities are implemented.
* The movie object is obtained from a passed in argument and all of its
* details are extracted and displayed
* Network calls are made to obtain the Review and Trailer information
* which are extracted and displayed.
 */
public class MovieDetailsFragment extends BaseFragment
        implements View.OnClickListener {
    // For Debugging
    private static final String TAG = MovieDetailsFragment.class.getSimpleName();

    private Movie mMovie = null;

    ViewGroup rootView;

    private CompositeSubscription _subscriptions = new CompositeSubscription();

    private TrailerResponse mTrailers;
    private ReviewResponse mReviews;

    private RestClient mRestClient = null;

    private FavoritesActionListener mActionListener;

    private static final String MOVIE_FROM_PARCEL = "movie";

    private static final String STATE_REVIEWS = "state_reviews";
    private static final String STATE_TRAILERS = "state_trailers";

    @Nullable
    @Bind(R.id.backdrop2)                       ScalingImageView mScale;
    @Bind(R.id.title)                           TextView mTitle;
    @Bind(R.id.release_date)                    TextView mTvReleaseDate;
    @Bind(R.id.rating)                          TextView mTvRating;
    @Bind(R.id.imagePoster)                     ScalingImageView mImagePoster;
    @Bind(R.id.plot)                            TextView mTvPlot;
    @Bind(R.id.trailers)                        ViewGroup trailersView;
    @Bind(R.id.trailers_outer_container)        CardView trailersOuterContainer;
    @Bind(R.id.reviews)                         ViewGroup reviewsView;
    @Bind(R.id.reviews_outer_container)         CardView reviewsOuterContainer;
    @Bind(R.id.btnFav)                          ImageView buttonFav;

    @OnClick(R.id.btnFav)
    void btnFav_OnClick() {
        // If not a favorite, insert it. If it's a favorite, delete it.
        boolean isFavorite = Utils.handleFavoriteMovies(getActivity(), mMovie);
        // Show the appropriate button.
        // If isFavorite above is true, then it is now not a favorite, so
        // below, isFavorite means that now it's not a favorite.
        if (isFavorite) {
            buttonFav.setImageResource(R.drawable.ic_favorite_border_black);
//            String s = mMovie.getOriginal_title().toUpperCase() + " removed.";
            Utils.showSnackbar(getActivity(), rootView, mMovie.getOriginal_title().toUpperCase() + " removed.");
        } else {
            buttonFav.setImageResource(R.drawable.ic_favorite_black2);
//            String s = mMovie.getOriginal_title().toUpperCase() + " added.";
            Utils.showSnackbar(getActivity(), rootView, mMovie.getOriginal_title().toUpperCase() + " added.");

        }
        // To handle refreshing the right pane
        if (mActionListener != null) {
            mActionListener.onFavoriteAction();
        }
    }

    // Refers to an ImageButton defined in activity_detais_trailer.xml
    /*
     * Method that opens the share dialog.
     * A text string is constructed that contains the movie title and
     * fetches the YouTube trailer URL and combines them and use it
     * when popping up the share using dialog.
     */
    @OnClick(R.id.btnShare)
    void btnShare_OnClick() {
        // Construct String to be displayed in the Share dialog
        String movieTitle = mMovie.getOriginal_title();
        String content = String.format("%s %s %s %s",
                getResources().getString(R.string.share_text_begin),
                movieTitle,
                getResources().getString(R.string.share_text_end),
                makeYouTubeVideoUrl(mTrailers.results.get(0)));
        // Use helper method to launch the Share dialog.
        Utils.shareAppIntent(getActivity(),
                "Movie Recommendation: " + movieTitle,
                content);
    }

    public MovieDetailsFragment() {
        // Required empty public constructor
    }

    // Called from both MoviesActivity and Details Activity
    public static MovieDetailsFragment newInstance(Movie movie) {
        MovieDetailsFragment fragment = new MovieDetailsFragment();
        Bundle args = new Bundle();
        args.putParcelable(MOVIE_FROM_PARCEL, movie);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // Save the Review and Trailer objects to the Bundle.
        if (mReviews != null)
            outState.putParcelable(STATE_REVIEWS, mReviews);

        if (mTrailers != null)
            outState.putParcelable(STATE_TRAILERS, mTrailers);
    }



    /*
    * http://stackoverflow.com/a/33655722
    * onAttach(Activity activity) is deprecated, so this code is a
    * temporary fix.
    */

    // Method not available for API < 23.
    @TargetApi(23)
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        onAttachToContext(context);
    }

    // This method is depracated.
    @SuppressWarnings("deprecation")
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (Build.VERSION.SDK_INT < 23) {
            onAttachToContext(activity);
        }
    }

    // Either one of the above methods will run and call this method.
    protected void onAttachToContext(Context context) {
        try {
            mActionListener = (FavoritesActionListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(getActivity().toString() + " must implement FavoritesActionListener");
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = (ViewGroup) inflater.inflate(R.layout.fragment_movie_details, container, false);
        return rootView;
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mRestClient = new RestClient();

        // Retrieve the movie from MoviesActivity
        mMovie = getArguments().getParcelable(MOVIE_FROM_PARCEL);

        if (savedInstanceState != null) {
            mTrailers = savedInstanceState.getParcelable(STATE_TRAILERS);
            mReviews = savedInstanceState.getParcelable(STATE_REVIEWS);
        }
        // Fill Overview, Title, Date Released and Rating
        fillMovieDetails();

        // Either make a network call or use savedInstanceState to refill Reviews
        if (mReviews != null)
            fillReviewDetails(mReviews);
        else
            getReviewsFromTMDB();

        // Either make a network call or use savedInstanceState to refill Trailers
        if (mTrailers != null)
            fillTrailerDetails(mTrailers);
        else
            getTrailersFromTMDB();
        // Change Favorited Icon
        if (Utils.isFavoritedMovie(getActivity(), mMovie)) {
            buttonFav.setImageResource(R.drawable.ic_favorite_black2);
        } else {
            buttonFav.setImageResource(R.drawable.ic_favorite_border_black);
        }
    }

    //https://github.com/THEONE10211024/RxJavaSamples/blob/master/app/src/main/java/pers/example/xiayong/rxjavasamples/fragments/RetrofitFragment.java
    /*
   * When another activity is in focus or maybe we turn off the
   * device, unsuscribe the RxJava subscription
    */
    @Override
    public void onPause() {
        super.onPause();
        Utils.unsubscribeIfNotNull(_subscriptions);
    }

    /*
    * Upon Resume, make a new RxJava subsription if it's null.
     */
    @Override
    public void onResume() {
        super.onResume();
        _subscriptions = Utils.getNewCompositeSubIfUnsubscribed(_subscriptions);
    }

    /*
     * Clean up RxJava subscription.
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        _subscriptions.unsubscribe();
    }

    private void loadBackdrop() {
        if (mMovie.getBackdrop_path() != null) {
            Picasso.with(getActivity())
                    .load(ImageUtils.getBackdropPath(mMovie, getString(R.string.param_backdrop_size)))
                    .into(mScale);
        }
    }

    /*
    * Method to fill the details of the Movie.
    * This includes the Poster, Title, Rating, Overview, Release Date.
    */
    private void fillMovieDetails() {
        // Load backdrop
        if (rootView.findViewById(R.id.backdrop2) != null) {
            loadBackdrop();
        }
        // Display Poster Path and call method to make the background colors funky :)
        if (mMovie.getPoster_path() != null) {
            Picasso.with(getActivity())
                    .load(ImageUtils.getPosterPath(mMovie, getString(R.string.param_poster_size)))
                    .into(mImagePoster, new Callback() {
                        @Override
                        public void onSuccess() {
                            updateBackgroundColor();
                        }

                        @Override
                        public void onError() {
                        }
                    });
        }
        /* FILL DETAILS:
         * Fills details. If the detail is empty, we display, Not Available
         */
        //  Display movie title.
        if (mMovie.getOriginal_title().length() > 0) {
            mTitle.setText(mMovie.getOriginal_title().toUpperCase());
        } else {
            mTitle.setText(R.string.title_not_available);
        }
        // Text on backdrop


        // Display average rating.
        if (mMovie.getVote_average() < 0.0F) {
            mTvRating.setText(R.string.rating_not_available);
        } else {
            mTvRating.setText(getString(R.string.average_rating_new, mMovie.getVote_average()));
        }
        // Display release date.
        String getMovieString = mMovie.getRelease_date();
        if (getMovieString.length() > 0) {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            try {
                String dateToFormat = DateUtils.formatDateTime(getActivity(),
                        formatter.parse(getMovieString).getTime(), DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR);
                mTvReleaseDate.setText(dateToFormat);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        } else {
            mTvReleaseDate.setText(R.string.year_not_available);
        }
        // Display overview
        if (mMovie.getOverview().length() > 0) {
            mTvPlot.setText(mMovie.getOverview());
        } else {
            mTvPlot.setText(R.string.plot_not_available);
        }
    }

    /*
     * Using Retrofit and RxJava to get the reviews based on the movie id
      */
    private void getReviewsFromTMDB() {
        RestClientService restClientService = mRestClient.getRestClientService();

        _subscriptions.add(//
                restClientService.getReviews(Integer.parseInt(mMovie.getId()), RestClient.API_KEY)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Observer<ReviewResponse>() {
                            @Override
                            public void onCompleted() {
                            }

                            @Override
                            public void onError(Throwable e) {
                            }

                            @Override
                            public void onNext(ReviewResponse reviews) {
                                fillReviewDetails(reviews);
                            }
                        }));
    }

    /*
    * If there are reviews, extract the reviews and display them
     */
    private void fillReviewDetails(ReviewResponse reviews) {
        // If there are reviews, display them
        if (reviews != null && reviews.getReviews().size() > 0) {
            reviewsOuterContainer.setVisibility(View.VISIBLE);  // Make the view visible
            reviewsView.removeAllViews();
            LayoutInflater inflater = getActivity().getLayoutInflater();
            // For each review, extract the author and content and add them to the view
            for (Review review : reviews.getReviews()) {
                ViewGroup reviewContainer = (ViewGroup) inflater.inflate(R.layout.review, reviewsView, false);
                TextView reviewAuthor =  (TextView) reviewContainer.findViewById(R.id.review_author);
                TextView reviewContent = (TextView) reviewContainer.findViewById(R.id.review_content);
                ImageView reviewImage = (ImageView) reviewContainer.findViewById(R.id.review_image);
//                reviewImage.getLayoutParams().height = 100;
//                reviewImage.getLayoutParams().width  = 100;
                reviewAuthor.setText(review.author.toLowerCase());
                reviewContent.setText(review.content);
                Drawable drawable = ContextCompat.getDrawable(getActivity(), R.drawable.reel);

                reviewImage.setBackground(drawable);

                reviewsView.addView(reviewContainer);
            }
        }
    }

    /*
 * Using Retrofit and RxJava to get the trailers based on the movie id
  */
    private void getTrailersFromTMDB() {
        RestClientService restClientService = mRestClient.getRestClientService();

        _subscriptions.add(//
                restClientService.getTrailers(Integer.parseInt(mMovie.getId()), RestClient.API_KEY)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Observer<TrailerResponse>() {
                            @Override
                            public void onCompleted() {
                            }

                            @Override
                            public void onError(Throwable e) {
                            }

                            @Override
                            public void onNext(TrailerResponse trailers) {
                                fillTrailerDetails(trailers);
                            }
                        }));
    }


    /*
    * If there are mTrailers, extract the trailers and display them
    */
    private void fillTrailerDetails(TrailerResponse trailers) {

        if (trailers != null && trailers.getTrailers().size() > 0) {
            trailersOuterContainer.setVisibility(View.VISIBLE);
            trailersView.removeAllViews();
            LayoutInflater inflater = getActivity().getLayoutInflater();
            // For each trailer, extract thumbnail and url and load into thumbview
            for (Trailer trailer : trailers.getTrailers()) {
                final ViewGroup thumbnailContainer = (ViewGroup) inflater.inflate(R.layout.trailer, trailersView, false);
                // Target View
                final ImageView thumbView = (ImageView) thumbnailContainer.findViewById(R.id.trailer_thumbnail);
                // Use the trailer key to create the video url
                String url = makeYouTubeVideoUrl(trailer);
                thumbView.setTag(url);
                thumbView.setOnClickListener(this);
                // Use the trailer key to create the thumbnail url
                String thumbnail = makeYouTubeThumbnailUrl(trailer);

                Picasso.with(getActivity())
                        .load(thumbnail)
                        .into(thumbView);

                trailersView.addView(thumbnailContainer);
            }
            mTrailers = trailers;
        }
    }

    /*
    * Create the string by including the trailer key
     */
    public String makeYouTubeThumbnailUrl(Trailer trailer) {
        return String.format("http://img.youtube.com/vi/%1$s/0.jpg", trailer.key);

    }

    /*
    * Create the string by including the trailer key
    */
    public String makeYouTubeVideoUrl(Trailer trailer) {
        return String.format("http://www.youtube.com/watch?v=%1$s", trailer.key);
    }

    /**
     * Called when a view has been clicked.
     *
     * @param trailerView The view that was clicked.
     */
    @Override
    public void onClick(View trailerView) {
        if (trailerView.getId() == R.id.trailer_thumbnail) {
            String videoUrl = (String) trailerView.getTag();
            Intent playVideoIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(videoUrl));
            startActivity(playVideoIntent);
        }
    }

    // Color constants
    private static final int COLOR_START = R.color.primary;
    private static final int COLOR_END = R.color.accent;

    /**
     * Make use of the Pallete object to add funky backgrounds :)
     */
    private void updateBackgroundColor() {
        Bitmap bitmap = ImageUtils.getBitmap(mImagePoster);
        Palette palette = Palette.from(bitmap).generate();
        int colorStart = palette.getDarkMutedColor(ContextCompat.getColor(getActivity(), COLOR_START));
        int colorEnd = palette.getDarkVibrantColor(ContextCompat.getColor(getActivity(), COLOR_END));
        GradientDrawable gradientDrawable =
                new GradientDrawable(GradientDrawable.Orientation.BR_TL, new int[]{colorStart, colorEnd});
        gradientDrawable.setDither(true);
        if (rootView.getRootView().findViewById(R.id.details_fragment_container) != null) {
            rootView.getRootView().findViewById(R.id.details_fragment_container).setBackground(gradientDrawable);

        } else {
            rootView.getRootView().setBackground(gradientDrawable);

        }
    }

    // Interface to handle refreshing the right pane when the favorite button is selected
    public interface FavoritesActionListener {
        void onFavoriteAction();
    }

}

/*
 * Created By Amit Patel
 * Project 1: Popular Movies
 * For the Udacity Nanodegree
 */
package ajpatel72.popularmovies.service;

import ajpatel72.popularmovies.model.MovieResponse;
import ajpatel72.popularmovies.model.ReviewResponse;
import ajpatel72.popularmovies.model.TrailerResponse;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;
import rx.Observable;

/**
 * Retrofit turns a REST API into a Java Interface
 */
public interface RestClientService {

    @GET("movie/popular")
    Observable<MovieResponse> getMoviesPopular(
            @Query("api_key") String apiKey);

    @GET("movie/top_rated")
    Observable<MovieResponse> getMoviesTopRated(
            @Query("api_key") String apiKey);

    @GET("movie/{id}/videos")
    Observable<TrailerResponse> getTrailers(
            @Path("id") int id,
            @Query("api_key") String apiKey);

    @GET("movie/{id}/reviews")
    Observable<ReviewResponse> getReviews(
            @Path("id") int id,
            @Query("api_key") String apiKey);

}


/*
 * Created By Amit Patel
 * Project 1: Popular Movies
 * For the Udacity Nanodegree
 */
package ajpatel72.popularmovies.service;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Create a RestClient object with the RestClientService
 *
 */
public class RestClient {

    public static final String API_KEY  = "INSERT API KEY HERE";
    public static final String BASE_URL = "http://api.themoviedb.org/3/";

    private final RestClientService mRestClientService;

    /*
     * Create the Endpoint and Gson serializer/deserializer.
     * Create an implementation of the API defined by the specified service interface.
     */
    public RestClient() {

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        mRestClientService = retrofit.create(RestClientService.class);
    }

    /*
     * Getter method for the RestClientService
     */
    public RestClientService getRestClientService() {
        return mRestClientService;
    }
}

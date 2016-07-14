package ajpatel72.popularmovies.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

/**
 * Simple Java Class that will hold the individual Review Objects
 */
public class ReviewResponse implements Parcelable{

    public final List<Review> results;
//
//    public ReviewResponse(List<Review> results) {
//        this.results = results;
//    }

    public List<Review> getReviews() {
        return results;
    }

    protected ReviewResponse(Parcel in) {
        results = in.createTypedArrayList(Review.CREATOR);
    }

    /**
     * Describe the kinds of special objects contained in this Parcelable's
     * marshalled representation.
     *
     * @return a bitmask indicating the set of special object types marshalled
     * by the Parcelable.
     */
    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * Flatten this object in to a Parcel.
     *
     * @param dest  The Parcel in which the object should be written.
     * @param flags Additional flags about how the object should be written.
     *              May be 0 or {@link #PARCELABLE_WRITE_RETURN_VALUE}.
     */
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(results);
    }

    public static final Parcelable.Creator<ReviewResponse> CREATOR = new Parcelable.Creator<ReviewResponse>() {
        @Override
        public ReviewResponse createFromParcel(Parcel in) {
            return new ReviewResponse(in);
        }

        @Override
        public ReviewResponse[] newArray(int size) {
            return new ReviewResponse[size];
        }
    };
}

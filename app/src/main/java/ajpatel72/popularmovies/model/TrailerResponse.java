package ajpatel72.popularmovies.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

/**
 * Simple Java Class that will hold the individual Trailer Objects
 */
public class TrailerResponse implements Parcelable{

    public final List<Trailer> results;

//    public TrailerResponse(List<Trailer> results) {
//        this.results = results;
//    }

    public List<Trailer> getTrailers() {
        return results;
    }

    protected TrailerResponse(Parcel in) {
        results = in.createTypedArrayList(Trailer.CREATOR);
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

    public static final Parcelable.Creator<TrailerResponse> CREATOR = new Parcelable.Creator<TrailerResponse>() {
        @Override
        public TrailerResponse createFromParcel(Parcel in) {
            return new TrailerResponse(in);
        }

        @Override
        public TrailerResponse[] newArray(int size) {
            return new TrailerResponse[size];
        }
    };
}

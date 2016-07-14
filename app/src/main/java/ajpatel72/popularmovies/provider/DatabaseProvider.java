package ajpatel72.popularmovies.provider;

import android.net.Uri;

import net.simonvt.schematic.annotation.ContentProvider;
import net.simonvt.schematic.annotation.ContentUri;
import net.simonvt.schematic.annotation.InexactContentUri;
import net.simonvt.schematic.annotation.TableEndpoint;

/**
 *Database Provider required for the Schematic Content Provider generator.
 */
@ContentProvider(authority = DatabaseProvider.AUTHORITY, database = Database.class)
public class DatabaseProvider {

    public static final String AUTHORITY = "ajpatel72.popularmovies.provider.DatabaseProvider";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);

    private static Uri buildUri(String... paths) {
        Uri.Builder builder = BASE_CONTENT_URI.buildUpon();
        for (String path : paths) {
            builder.appendPath(path);
        }
        return builder.build();
    }

    interface Path {
        String MOVIES = "Movies";
    }

    @TableEndpoint(table = Database.Movies)
    public static class Movies {
        @ContentUri(
                path = Path.MOVIES,
                type = "vnd.android.cursor.dir/Movies"
        )
        public static final Uri CONTENT_URI = buildUri(Path.MOVIES);

        @InexactContentUri(
                name = "MOVIE_ID",
                path = Path.MOVIES + "/#",
                type = "vnd.android.cursor.item/Movie",
                whereColumn = DatabaseColumns.MOVIE_ID,
                pathSegment = 1
        )
        public static Uri withId(int id) {

            return buildUri(Path.MOVIES, String.valueOf(id));
        }
    }

}

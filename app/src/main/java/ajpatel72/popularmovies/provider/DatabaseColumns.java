package ajpatel72.popularmovies.provider;

import net.simonvt.schematic.annotation.AutoIncrement;
import net.simonvt.schematic.annotation.DataType;
import net.simonvt.schematic.annotation.NotNull;
import net.simonvt.schematic.annotation.PrimaryKey;

/**
 * Database Columns file required for the Schematic Content Provider generator.
 */
public class DatabaseColumns {

    @DataType(DataType.Type.INTEGER)
    @PrimaryKey
    @AutoIncrement
    public static final String _ID = "_id";

    @DataType(DataType.Type.TEXT)
    @NotNull
    public static final String MOVIE_ID = "movieId";

    @DataType(DataType.Type.TEXT)
    @NotNull
    public static final String TITLE = "title";

    @DataType(DataType.Type.TEXT)
    @NotNull
    public static final String POSTER_PATH = "posterPath";

    @DataType(DataType.Type.TEXT)
    @NotNull
    public static final String BACKDROP_PATH = "backdropPath";

    @DataType(DataType.Type.TEXT)
    @NotNull
    public static final String OVERVIEW = "overview";

    @DataType(DataType.Type.TEXT)
    @NotNull
    public static final String VOTE_AVERAGE = "voteAverage";

    @DataType(DataType.Type.TEXT)
    @NotNull
    public static final String RELEASE_DATE = "relaseDate";


}

package ajpatel72.popularmovies.provider;

import net.simonvt.schematic.annotation.Table;

/**
 * Database file required for the Schematic Content Provider generator.
 */
@net.simonvt.schematic.annotation.Database(version = Database.VERSION)
public class Database {
    public static final int VERSION = 1;

    @Table(DatabaseColumns.class)
    public static final String Movies = "Movies";

    private Database(){}
}

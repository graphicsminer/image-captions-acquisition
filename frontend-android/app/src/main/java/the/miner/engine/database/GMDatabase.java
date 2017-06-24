package the.miner.engine.database;

import android.database.sqlite.SQLiteDatabase;

import the.miner.engine.database.model.GMImage;

/**
 * Manage (create, alter, drop) table structure
 */
public class GMDatabase {

    private static GMDatabase instance = new GMDatabase();

    /**
     * Constructor
     */
    private GMDatabase() {
    }

    /* --------------------- GET-SET ------------------------- */

    /**
     * Get singleton GMDatabase instance album
     *
     * @return database album
     */
    public static GMDatabase instance() {
        return instance;
    }


    /* ---------------------- METHOD ------------------------- */

    /**
     * Create tables
     *
     * @param db SQlLite database
     */
    public void createAllTables(SQLiteDatabase db) {
        createImageTable(db);
    }

    /**
     * Drop all tables
     *
     * @param db SQlLite database
     */
    public void dropAllTables(SQLiteDatabase db) {
        dropTable(GMImage.class.getSimpleName(), db);
    }

    /**
     * Drop specific table
     *
     * @param tableName specific table name
     * @param db        SqlLite database
     */
    public void dropTable(String tableName, SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS " + tableName);
    }

     /* ---------------------- METHOD - IMAGE ------------------------- */

    /**
     * Create image table
     *
     * @param db SQLite database
     */
    private void createImageTable(SQLiteDatabase db) {
        String sql = "CREATE TABLE IF NOT EXISTS " + GMImage.class.getSimpleName() + " ("
                + GMImage.ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL UNIQUE, "
                + GMImage.SYNC + " NVARCHAR NOT NULL, "
                + GMImage.HASH + " NVARCHAR NOT NULL, "
                + GMImage.NAME + " NVARCHAR NOT NULL, "
                + GMImage.CAPTIONS + " NVARCHAR, "
                + GMImage.STATUS + " NVARCHAR NOT NULL, "
                + GMImage.LONGITUDE + " REAL, "
                + GMImage.LATITUDE + " REAL, "
                + GMImage.ALTITUDE + " REAL, "
                + GMImage.CATEGORIES + " NVARCHAR, "
                + GMImage.CONTRIBUTOR + " NVARCHAR, "
                + GMImage.CREATED_DATE + " NVARCHAR NOT NULL,"
                + GMImage.UPDATED_DATE + " NVARCHAR NOT NULL )";
        db.execSQL(sql);
    }

}

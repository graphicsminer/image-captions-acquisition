package the.miner.engine.database.migration;

import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

/**
 * Manage migration
 */
public class GMMigrationController {

    private static GMMigrationController instance = new GMMigrationController();
    private List<Class<? extends GMBuild>> builds = new ArrayList<>();

    private GMMigrationController() {
        // Add  new migration here. For example,
        builds.add(GMBuild251.class);
    }

    /* --------------------- GET-SET ------------------------- */

    public static GMMigrationController instance() {
        return instance;
    }

    /* ---------------------- METHOD ------------------------- */

    /**
     * Do migration
     *
     * @param db
     * @param oldVersion
     * @param newVersion
     */
    public void doMigration(SQLiteDatabase db, int oldVersion, int newVersion) {
        // run migration
        db.beginTransactionNonExclusive();
        try {
            for (Class<? extends GMBuild> clazz : builds) {
                GMBuild build = clazz.newInstance();
                if (build.getVersion() >= newVersion) {
                    build.handle(db);
                }
            }
            db.setTransactionSuccessful();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        db.endTransaction();
    }
}

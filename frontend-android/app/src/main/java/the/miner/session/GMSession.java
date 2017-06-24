package the.miner.session;

import android.content.Context;
import android.location.Location;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import the.miner.engine.database.GMDatabaseHelper;
import the.miner.utils.GMGlobal;

/**
 * Manage application session and resource such as database, storage, preference ...
 */
public class GMSession {

    // database helper
    private static GMDatabaseHelper mDatabaseHelper;

    // store
    private static GMStoreHelper mStore;

    // location
    private static Location mLocation;

    // categories
    private static Map<String, List<String>> mImageCategoriesMap;

    /* --------------------- GET-SET ------------------------- */

    /**
     * Get database helper
     *
     * @return database helper
     */
    public static GMDatabaseHelper getDatabaseHelper() {
        return mDatabaseHelper;
    }

    /**
     * Get store helper
     *
     * @return store
     */
    public static GMStoreHelper getStoreHelper() {
        return mStore;
    }

    /**
     * Get location info
     *
     * @return current location
     */
    public static Location getLocation() {
        return mLocation;
    }

    /**
     * Set location info
     *
     * @param location current location
     */
    public static void setLocation(Location location) {
        mLocation = location;
    }

    /**
     * Get image categories
     *
     * @return list of categories
     */
    public static List<String> getSampleImageCategories(Locale lang) {
        return mImageCategoriesMap.get(lang.toString());
    }

    /* ---------------------- METHOD --------------------------- */

    /**
     * Initialize session data for application.
     * This should be call after logging in
     *
     * @param context activity context
     */
    public static void init(Context context) {
        // initialize database helper
        if (mDatabaseHelper == null) {
            mDatabaseHelper = new GMDatabaseHelper(context, GMGlobal.DATABASE, GMGlobal.DATABASE_VERSION);
        }

        // initialize store helper
        if (mStore == null) {
            mStore = new GMStoreHelper(context, GMGlobal.APP_SHARED_PREFS);
            mStore.initApplicationDirs(); // create necessary directories
        }

        initTestData(context);
        loadConfigAndSampleData();
    }

    /**
     * Clear all session data
     */
    public static void destroy() {
        if (mDatabaseHelper != null) {
            mDatabaseHelper.close();
        }
        mStore = null;
    }

    private static void initTestData(Context context) {
    }

    /**
     * Load configuration and sample data
     */
    public static void loadConfigAndSampleData() {
        // Reload category
        mImageCategoriesMap = new HashMap<>();
        for (Locale lang : GMLanguage.getSupportedLanguage()) {
            mImageCategoriesMap.put(lang.toString(), mStore.getCategories(lang.toString()));
        }
    }
}

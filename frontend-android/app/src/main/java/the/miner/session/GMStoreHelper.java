package the.miner.session;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import the.miner.engine.database.model.GMImage;
import the.miner.utils.GMFileUtils;
import the.miner.utils.GMGlobal;
import the.miner.utils.GMStringUtils;

/**
 * Provide function for store data internally, externally for globally
 */
public class GMStoreHelper {

    private SharedPreferences mSharedPrefs;

    /**
     * Constructor
     *
     * @param context          activity context
     * @param sharedPreference shared preferences
     */
    public GMStoreHelper(Context context, String sharedPreference) {
        mSharedPrefs = context.getSharedPreferences(sharedPreference, Context.MODE_PRIVATE);
    }

    /* ---------------------- OVERRIDE ------------------------- */

    /* --------------------- GET-SET ------------------------- */

    /**
     * Get Android shared preferences
     *
     * @return shared preferences
     */
    public SharedPreferences getSharedPrefs() {
        return mSharedPrefs;
    }

    /**
     * Get relative path which is relative to application folder which is set at GMGlobal.APP_FOLDER.</p>
     * Example: <p/>
     * Absolute path: "/storage/emulated/0/{APP_FOLDER}/image/orchid.jpg" </p>
     * Relative path: "image/orchid.jpg"
     *
     * @param path path
     * @return relative path.
     */
    public String getRelativePath(String path) {
        File root = getRootDir();
        String rootPath = root.getAbsolutePath();

        if (path.length() > rootPath.length() && path.startsWith(rootPath)) {
            String relative = path.substring(rootPath.length() + 1);
            return relative.isEmpty() ? path : relative;
        }
        return path;
    }

    /**
     * Get absolute path which derive from root of android storage.</p>
     * Example: <p/>
     * Absolute path: "/storage/emulated/0/{APP_FOLDER}/image/orchid.jpg" </p>
     * Relative path: "image/orchid.jpg"
     *
     * @param path path
     * @return absolute path.
     */
    public String getAbsolutePath(String path) {
        File root = getRootDir();
        if (path.startsWith(root.getAbsolutePath())) {
            return path;
        }
        return GMFileUtils.joinFile(root, path).getAbsolutePath();
    }

    /**
     * Get root directory
     *
     * @return root directory. "${ANDROID_ROOT}/${APP_FOLDER}"
     */
    public File getRootDir() {
        return GMFileUtils.joinFile(Environment.getExternalStorageDirectory(), GMGlobal.APP_FOLDER);
    }

    /**
     * Get album directory
     *
     * @return directory
     */
    public File getImageDir() {
        return new File(getRootDir(), GMGlobal.APP_IMAGE_FOLDER);
    }

    /**
     * Get image file with absolute path
     *
     * @param image image object
     * @return image file
     */
    public File getImageFile(GMImage image) {
        return new File(getAbsolutePath(image.getFilePath()));
    }

    /**
     * Get temporary directory
     *
     * @return temporary directory
     */
    public File getTemporaryDir() {
        return GMFileUtils.joinFile(getRootDir(), GMGlobal.APP_TEMP_FOLDER);
    }

    /**
     * Get categories from setting file.
     * Filename is define at GMGlobal.CATEGORY_CONFIG_FILE
     *
     * @param lang Support Vietnamese and English [vi, en]
     * @return list of categories
     */
    public List<String> getCategories(String lang) {
        List<String> list = new ArrayList<>();

        String filename = GMFileUtils.getFileNameWithoutExtension(GMGlobal.CATEGORY_CONFIG_FILE)
                + "-" + lang
                + GMFileUtils.getFileExtension(GMGlobal.CATEGORY_CONFIG_FILE);
        File file = new File(getRootDir(), filename);
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            while ((line = br.readLine()) != null) {
                // Split category by '#' symbol
                for (String cat : line.split("#")) {
                    cat = cat.trim().toLowerCase();
                    if (!cat.isEmpty() && GMStringUtils.isValidString(GMImage.CSC, cat)) {
                        list.add("#" + cat);
                    }
                }

            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return list;
    }

    /* ---------------------- METHOD --------------------------- */

    /**
     * Initialize application directories
     * <p/>
     * <pre>
     * |- APP_FOLDER
     * |    |- TEMP_FOLDER
     * |    |- IMAGE_FOLDER
     * |            |- Image files
     * </pre>
     * <p/>
     * <ul>
     * <li>GMGlobal.APP_FOLDER: Root application directory</li>
     * <li>GMGlobal.APP_TEMP_FOLDER: Temporary Directory</li>
     * <li>GMGlobal.APP_IMAGE_FOLDER: Image Directory</li>
     * </ul>
     */
    public void initApplicationDirs() {
        // create app's root directory
        File rootDir = getRootDir();
        if (!rootDir.exists()) {
            rootDir.mkdirs();
        }

        // create image directory
        File imageDir = getImageDir();
        if (!imageDir.exists()) {
            imageDir.mkdirs();
        }

        // create temporary
        File tempDir = getTemporaryDir();
        if (!tempDir.exists()) {
            tempDir.mkdirs();
        }
    }
}

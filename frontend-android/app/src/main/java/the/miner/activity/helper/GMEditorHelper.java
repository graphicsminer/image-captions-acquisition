package the.miner.activity.helper;

import android.location.Location;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import the.miner.engine.database.GMDatabaseHelper;
import the.miner.engine.database.model.GMImage;
import the.miner.engine.database.model.GMTable;
import the.miner.session.GMSession;
import the.miner.session.GMStoreHelper;
import the.miner.utils.GMDateUtils;
import the.miner.utils.GMFileUtils;

public class GMEditorHelper {

    private static GMDatabaseHelper dbHelper = GMSession.getDatabaseHelper();
    private static GMStoreHelper storeHelper = GMSession.getStoreHelper();

    /* --------------------- GET-SET ------------------------- */

    /* ---------------------- METHOD ------------------------- */

    /**
     * Find image for main view
     */
    public static List<GMImage> findImageForDoneView() {
        List<GMImage> result = new ArrayList<>();
        String where = GMImage.STATUS + "= ?";
        String[] args = new String[]{GMImage.GMStatus.DONE.name()};
        String order = GMImage.UPDATED_DATE + " DESC";

        for (GMTable img : dbHelper.find(GMImage.class, null, where, args, order, null)) {
            result.add((GMImage) img);
        }

        return result;
    }

    /**
     * Find image for temporary view
     */
    public static List<GMImage> findImageForTaskView() {
        List<GMImage> result = new ArrayList<>();
        String where = GMImage.STATUS + "= ?";
        String[] args = new String[]{GMImage.GMStatus.TODO.name()};
        String order = GMImage.UPDATED_DATE + " DESC";

        for (GMTable img : dbHelper.find(GMImage.class, null, where, args, order, null)) {
            result.add((GMImage) img);
        }

        return result;
    }

    /**
     * Add new image
     *
     * @param photo image file
     * @return added image
     */
    public static GMImage addNewImage(File photo) {
        GMImage newImage = new GMImage(photo.getName()); // Create new image

        // add location info
        Location loc = GMSession.getLocation();
        if (loc != null) {
            newImage.setLongitude(loc.getLongitude());
            newImage.setAltitude(loc.getAltitude());
            newImage.setLatitude(loc.getLatitude());
        }

        // add to selected album
        if (dbHelper.insert(newImage) > 0) {
            // Move image file to image directory
            // if moving file fail -> keep photo in temp folder
            if (GMFileUtils.moveFile(photo, storeHelper.getImageFile(newImage))) {
                return newImage;

            } else {
                // revert change
                dbHelper.delete(newImage);
            }
        }

        return null;
    }

    /**
     * Delete an image
     *
     * @param image image
     * @return true if delete successfully
     */
    public static boolean deleteImage(GMImage image) {
        if (dbHelper.delete(image) > 0) /* remove from database */ {
            GMFileUtils.deleteFile(storeHelper.getImageFile(image)); // remove from storage
            return true;
        }
        return false;
    }

    /**
     * Complete editing image -> save and mark as done
     *
     * @param image image
     * @return true if saving is successful
     */
    public static boolean doneEditing(GMImage image) {
        image.setStatus(GMImage.GMStatus.DONE);
        if (!image.getSyncState().equals(GMTable.GMSyncState.NEW)) {
            image.setSyncState(GMTable.GMSyncState.MODIFIED);
        }
        image.setUpdatedDate(GMDateUtils.now());

        if (dbHelper.update(image) > 0) {
            return true;
        }
        return false;
    }

    /**
     * Move image to task list for later editing
     *
     * @param image image
     * @return true if moving is successful
     */
    public static boolean moveToTask(GMImage image) {
        image.setStatus(GMImage.GMStatus.TODO);
        if (!image.getSyncState().equals(GMTable.GMSyncState.NEW)) {
            image.setSyncState(GMTable.GMSyncState.MODIFIED);
        }
        image.setUpdatedDate(GMDateUtils.now());

        if (dbHelper.update(image) > 0) {
            return true;
        }
        return false;
    }

    /**
     * Get all categories
     *
     * @return list pof categories
     */
    public static List<String> getAllCategories(Locale lang) {
        Set<String> set = new HashSet<>();
        List<String> list = new ArrayList<>();

        // Get categories from local database
        for (String cat : dbHelper.getAllImageCategories(lang)) {
            set.add(cat);
        }

        // Get categories from setting file
        for (String cat : GMSession.getSampleImageCategories(lang)) {
            set.add(cat);
        }

        for (String cat : set) {
            list.add(cat);
        }

        return list;
    }
}

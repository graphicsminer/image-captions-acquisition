package the.miner.engine.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import the.miner.engine.database.migration.GMMigrationController;
import the.miner.engine.database.model.GMImage;
import the.miner.engine.database.model.GMTable;
import the.miner.utils.GMFileUtils;

public class GMDatabaseHelper extends SQLiteOpenHelper {

    /**
     * Constructor
     *
     * @param context android context
     */
    public GMDatabaseHelper(Context context, String database, int version) {
        super(context, database, null, version);
    }

    /* ---------------------- OVERRIDE ----------------------- */

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        GMDatabase.instance().createAllTables(sqLiteDatabase);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        GMMigrationController.instance().doMigration(sqLiteDatabase, oldVersion, newVersion);
    }

    /* ------------------- STATIC-METHOD --------------------- */

    /**
     * Convert row value to model
     *
     * @param cur    cursor which holds values
     * @param entity model object
     */
    public static void cursorToModel(Cursor cur, GMTable entity) {
        for (String key : cur.getColumnNames()) {
            int idx = cur.getColumnIndex(key);
            Object value = null;

            // check type of column to get appropriate value
            switch (cur.getType(idx)) {
                case Cursor.FIELD_TYPE_STRING:
                    value = cur.getString(idx);
                    break;
                case Cursor.FIELD_TYPE_FLOAT:
                    value = cur.getFloat(idx);
                    break;
                case Cursor.FIELD_TYPE_INTEGER:
                    value = cur.getLong(idx);
                    break;
                case Cursor.FIELD_TYPE_BLOB:
                    value = cur.getBlob(idx);
                    break;
                default: // null
                    break;
            }
            entity.setValue(key, value);
        }
    }

    /* ---------------------- METHOD --------------------------- */

    /**
     * Query database
     *
     * @param type    model class
     * @param columns selected columns
     * @param where   where statement. For example: "gender = ? and userName like ?"
     * @param args    where arguments
     * @param groupBy group clause. For example: "gender"
     * @param having  having clause. For example: "length(category) > 10"
     * @param orderBy order clause. For example: "userName DESC"
     * @param limit   Limits the number of rows. For example: "5"
     * @return list of entity model object
     */
    public List<GMTable> find(Class<? extends GMTable> type, String[] columns, String where, String[] args,
                              String groupBy, String having, String orderBy, String limit) {

        List<GMTable> result = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        try {
            Cursor cur = db.query(type.getSimpleName(),
                                  columns, where, args,
                                  groupBy, having, orderBy, limit);

            if (cur.moveToFirst()) {
                do {
                    // create model object
                    GMTable obj = type.newInstance();
                    this.cursorToModel(cur, obj); // convert row to entity model
                    result.add(obj);
                } while (cur.moveToNext());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        // According to issue #27, no need to close the database connection.
        // must close database to release connection
        // if (db != null) db.close();

        return result;
    }

    /**
     * Query database
     *
     * @param type    model class
     * @param columns selected columns
     * @param where   where statement. For example: "gender = ? and userName like ?"
     * @param args    where arguments
     * @param orderBy order clause. For example: "userName DESC"
     * @param limit   Limits the number of rows. For example: "5"
     * @return list of entity model object
     */
    public List<GMTable> find(Class<? extends GMTable> type, String[] columns, String where, String[] args,
                              String orderBy, String limit) {
        return find(type, columns, where, args, null, null, orderBy, limit);
    }

    /**
     * Query database
     *
     * @param type    model class
     * @param columns selected columns
     * @param where   where statement. For example: "gender = ? and userName like ?"
     * @param args    where arguments
     * @return list of entity model object
     */
    public List<GMTable> find(Class<? extends GMTable> type, String[] columns, String where, String[] args) {
        return find(type, columns, where, args, null, null);
    }

    /**
     * Find all record
     *
     * @param type model class
     * @return
     */
    public List<GMTable> findAll(Class<? extends GMTable> type) {
        return find(type, null, null, null);
    }

    /**
     * Find record by id and table
     *
     * @param type model class
     * @param id   record's id
     * @return
     */
    public GMTable findByID(Class<? extends GMTable> type, int id) {
        List<GMTable> result = find(type, null, GMTable.ID + " = ?", new String[]{Integer.toString(id)});
        return (result.isEmpty()) ? null : result.get(0);
    }

    /**
     * Insert data to database
     *
     * @param entity model object
     * @return id of inserted row
     */
    public long insert(GMTable entity) {
        SQLiteDatabase db = this.getWritableDatabase();
        long result = 0;

        // use transaction to ensure the integrity of data
        db.beginTransactionNonExclusive();
        try {
            ContentValues values = entity.toContentValue();
            result = db.insert(entity.getTableName(), null, values);
            entity.setValue(GMTable.ID, result);
            db.setTransactionSuccessful();

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        db.endTransaction();

        // According to issue #27, no need to close the database connection.
        // if (db != null) db.close();

        return result;
    }

    /**
     * Update database
     *
     * @param entity model object
     * @return the number of row affected
     */
    public int update(GMTable entity) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = 0;

        // use transaction to ensure the integrity of data
        db.beginTransactionNonExclusive();
        try {
            ContentValues values = entity.toContentValue();
            String id = Long.toString(entity.getId());
            result = db.update(entity.getTableName(), values, GMTable.ID + "=?", new String[]{id});
            db.setTransactionSuccessful();

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        db.endTransaction();

        // According to issue #27, no need to close the database connection.
        // if (db != null) db.close();

        return result;
    }

    /**
     * Remove data from table
     *
     * @param entity model object
     * @return the number of row affected
     */
    public int delete(GMTable entity) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = 0;

        // use transaction to ensure the integrity of data
        db.beginTransactionNonExclusive();
        try {
            String id = Long.toString(entity.getId());
            result = db.delete(entity.getTableName(), GMTable.ID + "=?", new String[]{id});
            db.setTransactionSuccessful();

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        db.endTransaction();

        // According to issue #27, no need to close the database connection.
        // if (db != null) db.close();

        return result;
    }

    /**
     * Find image by hash string
     *
     * @param hash hash string
     * @return image if exist and null if not
     */
    public GMImage findImageByHash(String hash) {
        List<GMTable> result = find(GMImage.class, null, GMImage.HASH + " = ?", new String[]{hash});
        return (result.isEmpty()) ? null : (GMImage) result.get(0);
    }

    /**
     * Get all available categories with prefix "#"
     *
     * @return array of categories
     */
    public String[] getAllImageCategories() {
        Set<String> set = new HashSet<>();
        for (GMTable data : findAll(GMImage.class)) {
            GMImage img = (GMImage) data;
            for (String lang : img.getCategoryLanguages()) {
                for (String cat : img.getCategories(lang)) {
                    set.add("#" + cat);
                }
            }
        }

        return set.toArray(new String[set.size()]);
    }

    /**
     * Get all available categories by language
     */
    public String[] getAllImageCategories(Locale lang) {
        Set<String> set = new HashSet<>();
        for (GMTable data : findAll(GMImage.class)) {
            GMImage img = (GMImage) data;
            for (String cat : img.getCategories(lang.toString())) {
                set.add("#" + cat);
            }
        }

        return set.toArray(new String[set.size()]);
    }

    /**
     * Export database to JSON object
     *
     * @return JSON object
     */
    public JSONObject exportDataToJson() throws JSONException {
        JSONObject json = new JSONObject();

        // export image data to json
        JSONArray imagesJArray = new JSONArray();
        for (GMTable data : findAll(GMImage.class)) {
            imagesJArray.put(data.getJson());
        }
        json.putOpt("images", imagesJArray);

        return json;
    }

    /**
     * Export database to JSON file
     *
     * @param file JSON file
     */
    public void exportDataToJsonFile(File file) throws IOException, JSONException {
        JSONObject json = this.exportDataToJson();
        GMFileUtils.byteToFile(json.toString().getBytes("UTF-8"), file);
    }

    /**
     * Import data from JSON file
     *
     * @param file JSON file
     */
    public void importDataFromJsonFile(File file) throws IOException, JSONException {
        JSONObject jsonObj = new JSONObject(GMFileUtils.fileToJson(file));

        // import image data
        JSONArray imagesJArray = jsonObj.getJSONArray("images");
        for (int i = 0; i < imagesJArray.length(); i++) {
            JSONObject imgJObj = imagesJArray.getJSONObject(i);

            GMImage img = new GMImage();
            img.setValue(GMImage.HASH, imgJObj.getString(GMImage.HASH));
            img.setValue(GMImage.NAME, imgJObj.getString(GMImage.NAME));
            if (!imgJObj.isNull(GMImage.CAPTIONS)) {
                img.setValue(GMImage.CAPTIONS, imgJObj.getString(GMImage.CAPTIONS));
            }
            if (!imgJObj.isNull(GMImage.CATEGORIES)) {
                img.setValue(GMImage.CATEGORIES, imgJObj.getString(GMImage.CATEGORIES));
            }
            img.setValue(GMImage.STATUS, imgJObj.getString(GMImage.STATUS));
            img.setValue(GMImage.SYNC, imgJObj.getString(GMImage.SYNC));
            img.setSyncState(GMTable.GMSyncState.NEW);
            if (!imgJObj.isNull(GMImage.LONGITUDE)) {
                img.setValue(GMImage.LONGITUDE, imgJObj.getDouble(GMImage.LONGITUDE));
            }
            if (!imgJObj.isNull(GMImage.LATITUDE)) {
                img.setValue(GMImage.LATITUDE, imgJObj.getDouble(GMImage.LATITUDE));
            }
            if (!imgJObj.isNull(GMImage.ALTITUDE)) {
                img.setValue(GMImage.ALTITUDE, imgJObj.getDouble(GMImage.ALTITUDE));
            }
            img.setValue(GMImage.CREATED_DATE, imgJObj.getString(GMImage.CREATED_DATE));
            img.setValue(GMImage.UPDATED_DATE, imgJObj.getString(GMImage.UPDATED_DATE));

            this.insert(img);
        }
    }
}

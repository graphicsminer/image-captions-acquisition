package the.miner.engine.database.model;

import android.content.ContentValues;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Define structure of table
 */
public class GMTable implements Serializable, Cloneable {

    public static final String ID = "id";
    public static final String SYNC = "sync";

    // model which hold object key (column's name) and value
    private Map<String, Object> mModel;

    // tagged object
    private Map<String, Object> mTag;

    public enum GMSyncState {
        NEW, // data have not been uploaded to server
        MODIFIED, // data have been uploaded to server but not latest version
        SYNC,  // data have been uploaded to server and is latest version
    }

    /**
     * Constructor
     */
    public GMTable() {
        mModel = new HashMap<>();
        mTag = new HashMap<>();
    }

    /* ---------------------- OVERRIDE ----------------------- */

    @Override
    public GMTable clone() throws CloneNotSupportedException {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            // serialize and pass the object
            oos.writeObject(this);
            oos.flush();
            ByteArrayInputStream bin = new ByteArrayInputStream(bos.toByteArray()); // E
            ObjectInputStream ois = new ObjectInputStream(bin);

            GMTable table = (GMTable) ois.readObject();
            ois.close();

            // return the new object
            return table;
        } catch (Exception e) {
            throw new CloneNotSupportedException();
        }
    }

    /* --------------------- GET-SET ------------------------- */

    /**
     * Get table's name.
     * Database table which object represent
     *
     * @return table's name which is corresponding to class.getSimpleName()
     */
    public String getTableName() {
        return getClass().getSimpleName();
    }

    /**
     * Get table id
     * Each table has a unique id
     *
     * @return table id
     */
    public long getId() {
        return (Long) getValue(ID);
    }

    /**
     * Get synchronization state of data
     *
     * @return state of data
     */
    public GMSyncState getSyncState() {
        return GMSyncState.valueOf((String) getValue(SYNC));
    }

    /**
     * Set sync state
     *
     * @param state state of data
     */
    public void setSyncState(GMSyncState state) {
        setValue(SYNC, state.name());
    }

    /**
     * Get model
     *
     * @return model
     */
    public Map<String, Object> getModel() {
        return mModel;
    }

    /**
     * Map of tagged objects
     *
     * @return all tagged object
     */
    public Map<String, Object> getTag() {
        return mTag;
    }

    /**
     * Get tagged object by key
     *
     * @param key tagged key
     * @return tagged object
     */
    public Object getTag(String key) {
        return getTag().get(key);
    }

    /**
     * Add tagged object
     *
     * @param key       tagged key
     * @param taggedObj tagged object
     */
    public void addTag(String key, Object taggedObj) {
        mTag.put(key, taggedObj);
    }

    /* ---------------------- METHOD ------------------------- */

    /**
     * Get value by column (key in JSON)
     *
     * @param key column's name
     * @return value
     */
    protected Object getValue(String key) {
        return mModel.get(key);
    }

    /**
     * Add value to table object
     *
     * @param key   column's name
     * @param value value
     * @return model object
     */
    public GMTable setValue(String key, Object value) {
        mModel.put(key, value);
        return this;
    }

    /**
     * Convert model to row value
     */
    public ContentValues toContentValue() {
        ContentValues values = new ContentValues();
        for (Map.Entry<String, Object> entry : getModel().entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            // ID is unique and is created automatically -> don't insert or update id
            if (key.equals(GMTable.ID)) {
                continue;
            } else if (value instanceof String) {
                values.put(key, (String) entry.getValue());
            } else if (value instanceof Double) {
                values.put(key, (Double) entry.getValue());
            } else if (value instanceof Float) {
                values.put(key, (Float) entry.getValue());
            } else if (value instanceof Integer) {
                values.put(key, (Integer) entry.getValue());
            } else if (value instanceof Long) {
                values.put(key, (Long) entry.getValue());
            } else if (value instanceof byte[]) {
                values.put(key, (byte[]) entry.getValue());
            }
        }
        return values;
    }

    /**
     * Convert table to json data
     *
     * @return json object
     */
    public JSONObject getJson() throws JSONException {
        JSONObject json = new JSONObject();

        for (Map.Entry<String, Object> entry : mModel.entrySet()) {
            if (!entry.getKey().equals(ID)) {
                json.putOpt(entry.getKey(), entry.getValue());
            }
        }

        return json;
    }
}

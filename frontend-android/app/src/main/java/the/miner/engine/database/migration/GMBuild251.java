package the.miner.engine.database.migration;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

import the.miner.engine.database.model.GMImage;

public class GMBuild251 extends GMBuild {
    @Override
    public int getVersion() {
        return 251;
    }

    @Override
    public void handle(SQLiteDatabase db) {
        db.beginTransactionNonExclusive();

        try {
            ContentValues values = new ContentValues();
            values.put(GMImage.STATUS, GMImage.GMStatus.DONE.name());
            db.update(GMImage.class.getSimpleName(), values, GMImage.STATUS + "=?", new String[]{"FEEDBACK"});
            db.setTransactionSuccessful();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        db.endTransaction();
    }
}

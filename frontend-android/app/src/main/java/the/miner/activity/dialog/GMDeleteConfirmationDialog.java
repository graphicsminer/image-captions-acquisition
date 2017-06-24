package the.miner.activity.dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;

import the.miner.R;

public abstract class GMDeleteConfirmationDialog {

    // title of dialog
    private String title;

    // android activity
    private Activity activity;

    /**
     * Constructor
     *
     * @param activity android activity
     */
    public GMDeleteConfirmationDialog(Activity activity) {
        this.activity = activity;
    }

    /**
     * Constructor
     *
     * @param activity android activity
     * @param title    dialog's title
     */
    public GMDeleteConfirmationDialog(Activity activity, String title) {
        this(activity);
        setTitle(title);
    }

    /* ---------------------- OVERRIDE ----------------------- */

    /**
     * On cancel click event
     *
     * @param dialog dialog
     * @param id     id of button
     */
    public abstract void onCancelClick(DialogInterface dialog, int id);

    /**
     * On OK click event
     *
     * @param dialog dialog
     * @param id     id of button
     */
    public abstract void onOkClick(DialogInterface dialog, int id);

    /* --------------------- GET-SET ------------------------- */

    /**
     * Get dialog's title
     *
     * @return title
     */
    public String getTitle() {
        return this.title;
    }

    /**
     * Set dialog title
     *
     * @param title title
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /* ---------------------- METHOD ------------------------- */

    /**
     * Create dialog
     *
     * @return alert dialog
     */
    public Dialog create() {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setMessage(title)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        onOkClick(dialog, id);
                    }
                })
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        onCancelClick(dialog, id);
                    }
                });
        // Create the AlertDialog object and return it
        return builder.create();
    }

}

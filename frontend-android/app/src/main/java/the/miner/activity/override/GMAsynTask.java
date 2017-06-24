package the.miner.activity.override;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import the.miner.R;

/**
 * Handle task asynchronously
 */
public class GMAsynTask extends AsyncTask<String, String, String> {

    private Context context;
    private ProgressDialog pDialog;

    /**
     * Constructor
     *
     * @param context activity context
     */
    public GMAsynTask(Context context) {
        this(context, R.string.loading);
    }

    /**
     * Activity context
     *
     * @param context               activity context
     * @param dialogMessageResource message getting from resource
     */
    public GMAsynTask(Context context, int dialogMessageResource) {
        this.context = context;
        pDialog = new ProgressDialog(context);
        pDialog.setMessage(context.getString(dialogMessageResource));
        pDialog.setCancelable(false); // dialog can't be canceled
        pDialog.setIndeterminate(false);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        // show dialog
        pDialog.show();
    }

    @Override
    protected String doInBackground(String... strings) {
        return null;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        pDialog.dismiss();
    }
}

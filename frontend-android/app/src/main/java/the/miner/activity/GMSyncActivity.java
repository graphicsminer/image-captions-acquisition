package the.miner.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import the.miner.R;
import the.miner.engine.database.GMDatabaseHelper;
import the.miner.engine.sync.GMSyncService;
import the.miner.engine.sync.GMSyncStatus;
import the.miner.session.GMKey;
import the.miner.session.GMSession;
import the.miner.session.GMStoreHelper;

public class GMSyncActivity extends GMBaseActivity {

    private GMDatabaseHelper dbHelper;
    private GMStoreHelper storeHelper;

    // components
    private EditText etServer;
    private EditText etUsername;
    private EditText etPassword;
    private Button btStart;

    // Receive synchronizing status
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            String msg = bundle.getString(GMKey.RESULT_DATA);

            switch (bundle.getInt(GMKey.RESULT_CODE)) {
                case GMSyncStatus.LOGIN_FAIL:
                    Toast.makeText(getApplicationContext(), "Login failed!", Toast.LENGTH_LONG).show();
                    Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
                    enableInput(true);
                    break;

                case GMSyncStatus.ALL_SYNCED:
                    Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
                    enableInput(true);
                    break;

                default:
                    break;
            }
        }
    };

    /* ---------------------- OVERRIDE ----------------------- */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sync);

        // Get helper
        storeHelper = GMSession.getStoreHelper();
        dbHelper = GMSession.getDatabaseHelper();

        // set back button and title
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.back);
        getSupportActionBar().setTitle(R.string.async_title);

        // Get component
        etServer = (EditText) findViewById(R.id.etServer);
        etServer.setText(storeHelper.getSharedPrefs().getString(GMKey.SYNC_SERVER, ""));

        // Temporary fix username and password
        etUsername = (EditText) findViewById(R.id.etUsername);
        etUsername.setText(storeHelper.getSharedPrefs().getString(GMKey.SYNC_USERNAME, ""));

        etPassword = (EditText) findViewById(R.id.etPassword);
        etPassword.setText(storeHelper.getSharedPrefs().getString(GMKey.SYNC_PASSWORD, ""));

        btStart = (Button) findViewById(R.id.btStart);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // register broadcast receiver
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, new IntentFilter(GMSyncService.NOTIFICATION));
    }

    @Override
    protected void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /* ---------------------- EVENT -------------------------- */

    /**
     * On start synchronization
     *
     * @param v button
     */
    public void onStartSyncClick(View v) {
        // save setting to preferences
        savePreference();

        // disable input and show bar progress
        this.enableInput(false);

        // start background service to run sync task
        Intent i = new Intent(this, GMSyncService.class);
        startService(i);
    }

    /* ---------------------- METHOD ------------------------- */

    /**
     * Save input to shared preferences
     */
    private void savePreference() {
        SharedPreferences.Editor editor = storeHelper.getSharedPrefs().edit();
        editor.putString(GMKey.SYNC_SERVER, etServer.getText().toString());
        editor.putString(GMKey.SYNC_USERNAME, etUsername.getText().toString());
        editor.putString(GMKey.SYNC_PASSWORD, etPassword.getText().toString());
        editor.commit();
    }

    /**
     * Enable or disable input
     *
     * @param enable
     */
    private void enableInput(boolean enable) {
        etServer.setEnabled(enable);
        etUsername.setEnabled(enable);
        etPassword.setEnabled(enable);
        btStart.setEnabled(enable);
    }

}

package the.miner.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import the.miner.R;
import the.miner.session.GMKey;
import the.miner.session.GMSession;
import the.miner.session.GMStoreHelper;

public class GMLoginActivity extends GMBaseActivity {

    private GMStoreHelper storeHelper;

    private EditText etUsername;

    /* ---------------------- OVERRIDE ----------------------- */

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // init session
        storeHelper = GMSession.getStoreHelper();

        etUsername = (EditText) findViewById(R.id.etUsername);
        etUsername.setText(storeHelper.getSharedPrefs().getString(GMKey.LOGIN_USERNAME, ""));
    }

    /* ---------------------- EVENT -------------------------- */

    /**
     * On login click event
     *
     * @param v button
     */
    public void onLoginClick(View v) {
        // check if username is empty or not
        String username = etUsername.getText().toString();
        if (username.isEmpty()) {
            Toast.makeText(this, R.string.al_input_name_message, Toast.LENGTH_SHORT).show();
            return;
        }

        // store username to preferences
        SharedPreferences.Editor edit = storeHelper.getSharedPrefs().edit();
        edit.putString(GMKey.LOGIN_USERNAME, username);
        edit.commit();

        // navigate to main activity
        Intent i = new Intent(this, GMMainActivity.class);
        startActivity(i);
    }

    /* ---------------------- METHOD ------------------------- */

}

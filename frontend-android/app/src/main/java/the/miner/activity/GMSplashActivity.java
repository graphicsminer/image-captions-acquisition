package the.miner.activity;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import the.miner.R;
import the.miner.session.GMSession;

public class GMSplashActivity extends GMBaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        navigateToMainActivity();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        this.navigateToMainActivity();
    }

    /**
     * Tap on screen
     * @param view
     */
    public void onScreeTap(View view) {
        this.navigateToMainActivity();
    }

    public void navigateToMainActivity() {
        if (hasPermission(Manifest.permission.READ_EXTERNAL_STORAGE,
                          Manifest.permission.WRITE_EXTERNAL_STORAGE,
                          Manifest.permission.ACCESS_FINE_LOCATION)) {
            GMSession.init(this);
            Intent i = new Intent(this, GMMainActivity.class);
            startActivity(i);
        }
    }
}

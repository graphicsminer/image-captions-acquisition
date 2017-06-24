package the.miner.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.speech.RecognizerIntent;
import android.support.v4.BuildConfig;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

import the.miner.R;
import the.miner.activity.helper.GMCode;
import the.miner.engine.database.GMDatabaseHelper;
import the.miner.engine.database.model.GMImage;
import the.miner.session.GMSession;
import the.miner.session.GMStoreHelper;
import the.miner.utils.GMDateUtils;
import the.miner.utils.GMGlobal;
import the.miner.utils.GMImageUtils;
import the.miner.utils.GMUtils;

/**
 * Override life cycle event.
 * <ul>Common life cycle
 * <li>Start new activity: onCreate -> OnStart -> OnResume</li>
 * <li>Hilde activity to background or another activity come into foreground:
 * onSaveInstanceState -> OnPause -> OnStop</li>
 * <li>Restart activity from background to foreground: OnRestart -> OnStart -> OnResume</li>
 * <li>Destroy activity: OnPause -> OnStop -> OnDestroy</li>
 * </ul>
 */
public class GMBaseActivity extends AppCompatActivity {


    private GMDatabaseHelper dbHelper;
    private GMStoreHelper storeHelper;
    private String photoPath = "";

    private LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            GMSession.setLocation(location);
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onProviderDisabled(String s) {

        }
    };

    /* ---------------------- OVERRIDE ----------------------- */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // hide default title bar
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        // hide keyboard when activity start
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        // request permission to access storage
        this.requestAllPermission();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        dbHelper = GMSession.getDatabaseHelper();
        storeHelper = GMSession.getStoreHelper();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onRestart() {
        super.onRestart();

        // request location update
        this.requestLocationUpdate();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case GMCode.REQUEST_ALL_PERMISSIONS:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    this.requestAllPermission();
                }
                break;
            default:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // On after taking a photo
        if (resultCode == RESULT_OK && requestCode == GMCode.REQUEST_IMAGE_CAPTURE) {
            this.onHandleCameraResult(photoPath);
            return;
        }

        if (resultCode == RESULT_OK && requestCode == GMCode.REQUEST_VOICE_RECOGNIZER) {
            ArrayList<String> words = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            onHandleVoiceRecognition(words.get(0));
            return;
        }

        if (resultCode == RESULT_OK && requestCode == GMCode.REQUEST_FILE_BROWSER) {
            Uri photoUri = data.getData();
            try {
                // get selected file name
                Cursor cur = getContentResolver().query(photoUri, null, null, null, null);
                int nameIdx = cur.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                cur.moveToFirst();
                String now = GMDateUtils.nowString(GMImage.DATE_TIME_FORMAT);
                String imageName = now + "_" + cur.getString(nameIdx);

                // save bitmap to file and compress to file standard size
                File photoFile = new File(storeHelper.getTemporaryDir(), imageName);
                photoPath = photoFile.getAbsolutePath();

                Bitmap bmp = MediaStore.Images.Media.getBitmap(this.getContentResolver(), photoUri);
                GMImageUtils.bitmapToFile(bmp, photoPath);

                // sub-class will override this method to handle photo
                this.onHandleFileBrowserResult(photoPath);

            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, getString(R.string.error_pick_photo), Toast.LENGTH_LONG);
            }
            return;
        }

        if (resultCode == RESULT_OK && requestCode == GMCode.REQUEST_DIRECTORY_BROWSER) {
            Uri uri = data.getData();
            photoPath = getRealPathFromURI(uri);
            onHandleDirectoryBrowser((new File(photoPath)).getParent());
        }
    }

    /* ---------------------- EVENT -------------------------- */

    /* --------------------- GET-SET ------------------------- */

    /* ---------------------- Method ------------------------- */

    /**
     * Check where app has specific permissions or not
     *
     * @param permissions permissions
     * @return true if has permissions
     */
    protected boolean hasPermission(String... permissions) {
        for (String permit : permissions) {
            if (ContextCompat.checkSelfPermission(this, permit) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    /**
     * Request all permission
     */
    protected void requestAllPermission() {
        if (!hasPermission(Manifest.permission.READ_EXTERNAL_STORAGE,
                           Manifest.permission.WRITE_EXTERNAL_STORAGE,
                           Manifest.permission.ACCESS_FINE_LOCATION)) {

            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.ACCESS_FINE_LOCATION}, GMCode.REQUEST_ALL_PERMISSIONS);
        }
    }

    /**
     * Request permission for storage accessing
     */
    public void requestStorageAccess() {
        if (!hasPermission(Manifest.permission.READ_EXTERNAL_STORAGE,
                           Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE}, GMCode.REQUEST_STORAGE_ACCESS);
        }
    }

    /**
     * Request permission for location accessing
     */
    public void requestLocationAccess() {
        if (!hasPermission(Manifest.permission.LOCATION_HARDWARE,
                           Manifest.permission.ACCESS_FINE_LOCATION)) {

            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.LOCATION_HARDWARE,
                    Manifest.permission.ACCESS_FINE_LOCATION}, GMCode.REQUEST_LOCATION_ACCESS);
        }
    }

    /**
     * Request launching camera using third-party camera application
     */
    protected void requestCamera() {
        if (GMUtils.isCameraAvailable(this)) {
            Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (i.resolveActivity(getPackageManager()) != null) {
                String imageName = GMUtils.getDeviceId(this) + "_" + GMDateUtils.nowString(GMImage.DATE_TIME_FORMAT) + ".jpg";
                File photoFile = new File(storeHelper.getTemporaryDir(), imageName);
                photoPath = photoFile.getAbsolutePath();

                // Save photo using URI
                Uri photoURI;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    photoURI = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".provider", photoFile);
                } else {
                    photoURI = Uri.fromFile(photoFile);
                }

                i.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(i, GMCode.REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    /**
     * Handle camera result
     */
    protected void onHandleCameraResult(String photoPath) {
    }


    /**
     * Request launching voice recognizer
     */
    protected void requestVoiceRecognizer(Locale locale) {
        Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, locale.toString());
        try {
            startActivityForResult(i, GMCode.REQUEST_VOICE_RECOGNIZER);
        } catch (Exception e) {
            Toast.makeText(this, R.string.error_launch_voice_recognizer, Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Handle speech result
     */
    protected void onHandleVoiceRecognition(String result) {
    }

    /**
     * Request launching file browser using third-party application
     */
    protected void requestFileBrowser() {
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.setType("image/*");
        if (i.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(i, GMCode.REQUEST_FILE_BROWSER);
        }
    }

    /**
     * Handle file browser result
     */
    protected void onHandleFileBrowserResult(String photoPath) {
    }

    /**
     * Request launching directory browser.
     * But, currently we only can launch file browser and get folder path as parent of selected file.
     * TODO:* Will fix it when possible
     */
    protected void requestDirectoryChooser() {
        Intent chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
        chooseFile.setType("file/*");
        Intent intent = Intent.createChooser(chooseFile, "Choose a file");
        startActivityForResult(intent, GMCode.REQUEST_DIRECTORY_BROWSER);
    }

    /**
     * Handle folder result
     *
     * @param dirPath
     */
    protected void onHandleDirectoryBrowser(String dirPath) {
    }

    /**
     * Load image asynchronously
     *
     * @param imageView image file
     * @param photo     photo file
     * @param size      size of image view: [small = 0, medium = 1, large = 2]
     */
    protected void loadPhotoIntoView(final ImageView imageView, final File photo, int size) {
        final int thumbSize;
        switch (size) {
            case 0:
                thumbSize = GMGlobal.THUMB_SMALL_SIZE;
                break;
            case 1:
                thumbSize = GMGlobal.THUMB_MEDIUM_SIZE;
                break;
            default:
                thumbSize = GMGlobal.THUMB_LARGE_SIZE;
                break;
        }
        new AsyncTask<String, String, String>() {
            private Bitmap bmp;

            @Override
            protected String doInBackground(String... strings) {
                // load preview photo
                if (photo == null || !photo.isFile()) {
                    bmp = BitmapFactory.decodeResource(getResources(), R.drawable.not_found);
                } else {
                    bmp = GMImageUtils.fileToBitmap(photo, thumbSize, thumbSize);
                }
                return null;
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                imageView.setImageBitmap(bmp);
            }
        }.execute();
    }

    /**
     * Request location
     */
    protected void requestLocationUpdate() {
        LocationManager lm = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        if (lm != null && lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestLocationAccess();
            }
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1f, locationListener);
        }
    }

    /**
     * Ger real file path from URI
     *
     * @param contentUri uri
     * @return absolute file path
     */
    public String getRealPathFromURI(Uri contentUri) {
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(contentUri, proj, null, null, null);
        if (cursor == null) return null;
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }
}


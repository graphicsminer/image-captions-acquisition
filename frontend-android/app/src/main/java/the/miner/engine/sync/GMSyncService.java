package the.miner.engine.sync;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import the.miner.R;
import the.miner.engine.database.GMDatabaseHelper;
import the.miner.engine.database.model.GMImage;
import the.miner.engine.database.model.GMTable;
import the.miner.session.GMKey;
import the.miner.session.GMLanguage;
import the.miner.session.GMSession;
import the.miner.session.GMStoreHelper;
import the.miner.utils.GMFileUtils;
import the.miner.utils.GMGlobal;

public class GMSyncService extends IntentService {

    public static final String NOTIFICATION = "the.miner.engine.sync.GMSyncService";
    private static final String TAG_RETRY = "retry";

    // helper class
    private GMStoreHelper storeHelper;
    private GMDatabaseHelper dbHelper;
    private GMHttpRequest httpRequest;

    // Notification
    private NotificationManager notificationManger;
    private NotificationCompat.Builder notificationBuilder;

    // sync progress info
    private int numberOfData;
    private int numberOfDone;
    private int numberOfFail;


    public GMSyncService() {
        super(GMSyncService.class.getSimpleName());
    }

    /* ---------------------- OVERRIDE ----------------------- */

    @Override
    public void onCreate() {
        super.onCreate();

        // init helper class
        storeHelper = GMSession.getStoreHelper();
        dbHelper = GMSession.getDatabaseHelper();

        // create notification
        notificationManger = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.refresh)
                .setContentTitle(getString(R.string.async_notification_title));
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            // init http request object
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(GMGlobal.HOST_NAME)
                    .build();
            httpRequest = retrofit.create(GMHttpRequest.class);
        } catch (Exception ex) {
            throwMessage(GMSyncStatus.LOGIN_FAIL, ex.toString());
            return;
        }

        // Login to get token key
        final String username = storeHelper.getSharedPrefs().getString(GMKey.SYNC_USERNAME, "");
        String password = storeHelper.getSharedPrefs().getString(GMKey.SYNC_PASSWORD, "");
        httpRequest.login(username, password).enqueue(new Callback<ResponseBody>() {

            @Override
            public void onResponse(Call<ResponseBody> call, final Response<ResponseBody> res) {
                String token;
                try {
                    JSONObject result = new JSONObject(res.body().string());
                    token = result.getString("token");

                } catch (Exception e) {
                    throwMessage(GMSyncStatus.LOGIN_FAIL, e.toString());
                    return;
                }

                if (token == null || token.isEmpty()) {
                    throwMessage(GMSyncStatus.LOGIN_FAIL, "Token is invalid");
                    return;
                }

                // download resources
                downloadResource(token);

                // Get data which have not synchronized
                // Synchronize image which have done caption only
                String where = GMImage.SYNC + "!= ? and " + GMImage.STATUS + " = ?";
                String[] args = new String[]{GMTable.GMSyncState.SYNC.name(), GMImage.GMStatus.DONE.name()};
                final List<GMTable> syncList = dbHelper.find(GMImage.class, null, where, args);
                if (syncList.isEmpty()) {
                    throwMessage(GMSyncStatus.ALL_SYNCED, "All data has been synchronized");
                    return;
                }

                // Start synchronizing image to server.
                for (GMTable data : syncList) {
                    numberOfData++;
                    data.addTag(TAG_RETRY, 0);

                    // set contributor
                    ((GMImage) data).setContributor(username);

                    try {
                        syncData((GMImage) data, token);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        notifyUploadFail();
                    }
                }

            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                t.printStackTrace();
                throwMessage(GMSyncStatus.LOGIN_FAIL, t.toString());
            }
        });

    }

    /* ---------------------- METHOD ------------------------- */

    /**
     * Send image and it's information to server
     *
     * @param image image data
     */
    private void syncData(final GMImage image, final String token) throws JSONException {
        // Sync data and upload image to server-database.
        // With new image data, we try to upload image file first
        //      -> OK -> upload image information -> OK -> set state to SYNC
        //      -> Fail -> retry
        // With modified image, we just upload image information
        //      -> OK -> set state to SYNC
        //      -> Fail -> retry

        if (image.getSyncState().equals(GMTable.GMSyncState.NEW)) {
            uploadImageFile(token, image);

        } else if (image.getSyncState().equals(GMTable.GMSyncState.MODIFIED)) {
            updateImage(token, image);
        }
    }

    /**
     * Add new image data to server
     *
     * @param token token key
     * @param image image object
     * @throws JSONException
     */
    private void createNewImage(final String token, final GMImage image) throws JSONException {
        httpRequest.createImage(token, image.getJson().toString()).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> res) {
                if (res.code() == 200) /* OK */ {
                    // change sync state to modified
                    image.setSyncState(GMTable.GMSyncState.SYNC);
                    dbHelper.update(image);

                    // notify
                    notifyUploadSuccess();

                } else {
                    notifyUploadFail();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                t.printStackTrace();

                // notify fail after retry reach limit
                if (((Integer) image.getTag(TAG_RETRY)) >= GMGlobal.RETRY_LIMIT) {
                    notifyUploadFail();
                }

                // retry upload image information
                new Retry() {
                    @Override
                    protected void handle(Object... params) {
                        try {
                            createNewImage((String) params[0], (GMImage) params[1]);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            notifyUploadFail();
                        }
                    }
                }.execute(token, image);
            }
        });
    }

    /**
     * Update image data to server
     *
     * @param token token key
     * @param image image data
     * @throws JSONException
     */
    private void updateImage(final String token, final GMImage image) throws JSONException {
        httpRequest.updateImage(image.getHash(), token, image.getJson().toString()).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> res) {
                if (res.code() == 200) {
                    // change sync state to modified
                    image.setSyncState(GMTable.GMSyncState.SYNC);
                    dbHelper.update(image);

                    // Notify
                    notifyUploadSuccess();
                } else {
                    notifyUploadFail();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                t.printStackTrace();

                // notify fail after retry reach limit
                if (((Integer) image.getTag(TAG_RETRY)) >= GMGlobal.RETRY_LIMIT) {
                    notifyUploadFail();
                }

                // retry upload image information
                new Retry() {
                    @Override
                    protected void handle(Object... params) {
                        try {
                            updateImage((String) params[0], (GMImage) params[1]);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            notifyUploadFail();
                        }
                    }
                }.execute(token, image);
            }
        });
    }

    /**
     * Upload image file to server
     *
     * @param token token key
     * @param image image object
     */
    private void uploadImageFile(final String token, final GMImage image) {
        // create RequestBody instance from file
        File imgFile = storeHelper.getImageFile(image);
        final RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), imgFile);

        // MultipartBody.Part is used to send also the actual file name
        MultipartBody.Part body = MultipartBody.Part.createFormData("file", imgFile.getName(), requestFile);

        // Add file name within the multipart request
        RequestBody requestFileName = RequestBody.create(MediaType.parse("multipart/form-data"), imgFile.getName());

        httpRequest.upload(token, requestFileName, body).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> res) {
                if (res.code() == 200) {
                    try {
                        // upload image information
                        image.addTag(TAG_RETRY, 0);
                        createNewImage(token, image);

                    } catch (JSONException e) {
                        e.printStackTrace();
                        notifyUploadFail();
                    }
                } else {
                    notifyUploadFail();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                t.printStackTrace();

                // notify fail after retry reach limit
                if (((Integer) image.getTag(TAG_RETRY)) >= GMGlobal.RETRY_LIMIT) {
                    notifyUploadFail();
                }

                // retry upload image
                new Retry() {
                    @Override
                    protected void handle(Object... params) {
                        uploadImageFile((String) params[0], (GMImage) params[1]);
                    }
                }.execute(token, image);
            }
        });
    }

    /**
     * Send message to UI
     *
     * @param resultCode code. See GMSyncCode
     * @param message    message
     */
    private void throwMessage(int resultCode, String message) {
        Intent i = new Intent(NOTIFICATION);
        i.putExtra(GMKey.RESULT_CODE, resultCode);
        i.putExtra(GMKey.RESULT_DATA, message);
        LocalBroadcastManager.getInstance(GMSyncService.this).sendBroadcast(i);
    }

    /**
     * Notify a failed uploading data
     */
    private void notifyUploadFail() {
        numberOfFail++;
        notificationBuilder.setContentText(getNotificationContent());
        notificationManger.notify(0, notificationBuilder.build());
    }

    /**
     * Notify a successful uploading data
     */
    private void notifyUploadSuccess() {
        numberOfDone++;
        notificationBuilder.setContentText(getNotificationContent());
        notificationManger.notify(0, notificationBuilder.build());
    }

    /**
     * Generate content for notification
     *
     * @return content
     */
    private String getNotificationContent() {
        String format = "Done %.2f%% - Success: %d - Fail: %d";
        float percentage = (numberOfDone / numberOfData) * 100;
        return String.format(format, percentage, numberOfDone, numberOfFail);
    }

    /**
     * Download resources
     *
     * @param token token key
     */
    private void downloadResource(String token) {
        // get category configuration. Support Vietnamese and English only
        for (Locale lang : GMLanguage.getSupportedLanguage()) {
            final String filename = GMFileUtils.getFileNameWithoutExtension(GMGlobal.CATEGORY_CONFIG_FILE)
                    + "-" + lang.toString()
                    + GMFileUtils.getFileExtension(GMGlobal.CATEGORY_CONFIG_FILE);

            httpRequest.getConfigFile(filename, token).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> res) {
                    if (res.code() == 200) {
                        try {
                            File file = new File(storeHelper.getRootDir(), filename);
                            GMFileUtils.byteToFile(res.body().bytes(), file);

                            // reload data
                            GMSession.loadConfigAndSampleData();

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    t.printStackTrace();
                }
            });
        }
    }

    /* -------------------- INNER CLASS ---------------------- */

    /**
     * Create class for retry action
     */
    private abstract class Retry {

        /**
         * Override this function to handle retry task
         *
         * @param params parameters
         */
        protected abstract void handle(Object... params);

        /**
         * Start retry
         *
         * @param token token key
         * @param image image data
         */
        public void execute(String token, GMImage image) {
            int retryNo = (Integer) image.getTag(TAG_RETRY);
            if (retryNo < GMGlobal.RETRY_LIMIT) {
                image.addTag(TAG_RETRY, retryNo + 1);
                handle(token, image);
            }
        }
    }
}

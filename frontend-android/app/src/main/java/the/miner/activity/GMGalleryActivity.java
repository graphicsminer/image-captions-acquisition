package the.miner.activity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.veinhorn.scrollgalleryview.MediaInfo;
import com.veinhorn.scrollgalleryview.ScrollGalleryView;
import com.veinhorn.scrollgalleryview.loader.DefaultImageLoader;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import the.miner.R;
import the.miner.engine.database.model.GMImage;
import the.miner.session.GMKey;
import the.miner.session.GMSession;
import the.miner.session.GMStoreHelper;
import the.miner.utils.GMImageUtils;
import the.miner.utils.GMUtils;

import static android.view.WindowManager.LayoutParams;

/**
 * Thank VEINHORN. </p>
 * https://github.com/VEINHORN/ScrollGalleryView
 */
public class GMGalleryActivity extends FragmentActivity {

    private ScrollGalleryView gallery;
    private GMStoreHelper storeHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_gallery);
        getWindow().setFlags(LayoutParams.FLAG_FULLSCREEN, LayoutParams.FLAG_FULLSCREEN);

        storeHelper = GMSession.getStoreHelper();

        // get data
        Bundle bundle = this.getIntent().getExtras();
        final List<GMImage> images = (List<GMImage>) bundle.getSerializable(GMKey.BUNDLE_DATA);
        final int pos = bundle.getInt(GMKey.BUNDLE_POSITION);

        gallery = (ScrollGalleryView) findViewById(R.id.gallery);
        gallery.setThumbnailSize(100)
                .setZoom(true)
                .setFragmentManager(this.getSupportFragmentManager());

        // get image by album
        final Point screenSize = GMUtils.getScreenSize(this);
        new AsyncTask<String, String, String>() {

            private List<MediaInfo> mediasInfo;

            @Override
            protected String doInBackground(String... args) {
                mediasInfo = new ArrayList<>(images.size());
                for (GMImage img : images) {
                    mediasInfo.add(loadMedia(img, screenSize.x, screenSize.y));
                }
                return null;
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                gallery.addMedia(mediasInfo).setCurrentItem(pos);
            }
        }.execute();
    }

    /**
     * Load image from file
     *
     * @param image  image
     * @param width  target width
     * @param height target height
     * @return
     */
    private MediaInfo loadMedia(GMImage image, int width, int height) {
        File photo = storeHelper.getImageFile(image);
        Bitmap bmp;
        if (photo.exists()) {
            bmp = GMImageUtils.fileToBitmap(photo, width, height);
        } else {
            bmp = BitmapFactory.decodeResource(this.getResources(), R.drawable.not_found);
        }

        return MediaInfo.mediaLoader(new DefaultImageLoader(bmp));
    }
}

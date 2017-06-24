package the.miner.activity.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.util.List;

import the.miner.R;
import the.miner.engine.database.model.GMImage;
import the.miner.engine.database.model.GMTable;
import the.miner.session.GMSession;
import the.miner.session.GMStoreHelper;
import the.miner.utils.GMDateUtils;
import the.miner.utils.GMGlobal;
import the.miner.utils.GMImageUtils;

/**
 * Provide access to data and making view for each list item
 */
public class GMImageViewAdapter extends GMBaseAdapter<GMImage> {

    private GMStoreHelper storeHelper;

    /**
     * Constructor
     *
     * @param context android activity
     * @param images  list of images
     */
    public GMImageViewAdapter(Context context, List<GMImage> images) {
        super(context, -1, images);
        storeHelper = GMSession.getStoreHelper();
    }

    /* ---------------------- OVERRIDE ------------------------- */

    @Override
    public View getView(int position, View convertView, final ViewGroup parent) {
        final ViewHolder viewHolder;
        final GMImage imageObj = getItem(position);

        // Use view holder to improve performance
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            // If there's no view to re-use, inflate a brand new view for row
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.custom_image_list_view, parent, false);

            viewHolder.PreviewPhoto = (ImageView) convertView.findViewById(R.id.ivPreviewPhoto);
            viewHolder.SyncStatus = (ImageView) convertView.findViewById(R.id.ivSyncStatus);
            viewHolder.Name = (TextView) convertView.findViewById(R.id.tvName);
            viewHolder.Date = (TextView) convertView.findViewById(R.id.tvDate);
            viewHolder.Detail = (ImageButton) convertView.findViewById(R.id.ibShowImageDetail);
            viewHolder.Caption = (TextView) convertView.findViewById(R.id.tvCaption);

            // cache the ViewHolder inside the fresh view
            convertView.setTag(viewHolder);

        } else {
            // View is being recycled, retrieve the ViewHolder from tag
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.Name.setText(imageObj.getHash());
        viewHolder.Date.setText(GMDateUtils.dateToString(imageObj.getUpdatedDate(), GMGlobal.DATE_TIME_FORMATTER));

        // Display caption line by line and order by language
        String cap = "";
        for (String lang : imageObj.getCaptionLanguages()) {
            for (String val : imageObj.getCaptions(lang)) {
                cap += val + "\r\n";
            }
            cap += "\r\n";
        }
        cap = cap.isEmpty() ? getContext().getString(R.string.no_caption) : cap.trim();
        viewHolder.Caption.setText(cap);
        viewHolder.Caption.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                // Do nothing. Just overlap list view item select event
                return false;
            }
        });

        viewHolder.Detail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TextView capView = viewHolder.Caption;
                if (capView.isShown()) {
                    capView.setVisibility(View.GONE);
                } else {
                    capView.setVisibility(View.VISIBLE);
                }
            }
        });

        if (imageObj.getSyncState().equals(GMTable.GMSyncState.SYNC)) {
            viewHolder.SyncStatus.setBackgroundResource(R.drawable.sync_done);
        } else {
            viewHolder.SyncStatus.setBackgroundResource(R.drawable.sync_wait);
        }

        // load bitmap image
        new AsyncTask<String, String, String>() {
            private Bitmap bmp;

            @Override
            protected String doInBackground(String... strings) {
                File photo = storeHelper.getImageFile(imageObj);
                if (photo.exists()) {
                    bmp = GMImageUtils.fileToBitmap(photo, GMGlobal.THUMB_SMALL_SIZE, GMGlobal.THUMB_SMALL_SIZE);
                } else {
                    bmp = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.not_found);
                }
                return null;
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                viewHolder.PreviewPhoto.setImageBitmap(bmp);
            }
        }.execute();

        return convertView;
    }

    /* ---------------------- Method ------------------------- */

    /* ---------------------- Inner Class ------------------------- */

    private static class ViewHolder {
        ImageView PreviewPhoto;
        ImageView SyncStatus;
        TextView Name;
        TextView Date;
        ImageButton Detail;
        TextView Caption;
    }
}

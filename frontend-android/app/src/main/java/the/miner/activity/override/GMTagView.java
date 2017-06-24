package the.miner.activity.override;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import the.miner.R;

public class GMTagView extends LinearLayout {

    private TextView tvTitle;

    private String mLanguage;

    /**
     * Constructor
     *
     * @param context android context
     */
    public GMTagView(Context context) {
        super(context);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.custom_tag_view, this, true);

        tvTitle = (TextView) findViewById(R.id.tvTagTitle);
        tvTitle.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                onSelect(GMTagView.this, view);
            }
        });

        ImageButton btRemove = (ImageButton) findViewById(R.id.ibRemove);
        btRemove.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                onRemoveClick(GMTagView.this, view);
            }
        });
    }

    /**
     * Constructor
     *
     * @param context android context
     * @param title   tag's title
     */
    public GMTagView(Context context, String title) {
        this(context);
        setTitle(title);
    }

    /* ---------------------- OVERRIDE ----------------------- */

    /* ---------------------- EVENT -------------------------- */

    /**
     * Remove current tag from from it parent
     *
     * @param tagView tag view
     * @param view    delete icon view
     */
    protected void onRemoveClick(GMTagView tagView, View view) {
        // nothing to do
    }

    /**
     * On select tag
     *
     * @param tagView tag view
     * @param view    text element
     */
    protected void onSelect(GMTagView tagView, View view) {
        // nothing to do
    }

    /* --------------------- GET-SET ------------------------- */

    /**
     * Get title of tag
     *
     * @return title
     */
    public String getTitle() {
        return tvTitle.getText().toString();
    }

    /**
     * Set tag title
     *
     * @param title tag's title
     */
    public void setTitle(String title) {
        tvTitle.setText(title);
    }

    /**
     * Get language
     *
     * @return language
     */
    public String getLanguage() {
        return mLanguage;
    }

    /**
     * Set language
     *
     * @param language language
     */
    public void setLanguage(String language) {
        mLanguage = language;
    }

    /* ---------------------- METHOD ------------------------- */


}

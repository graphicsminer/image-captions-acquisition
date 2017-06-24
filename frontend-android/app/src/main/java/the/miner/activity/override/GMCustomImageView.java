package the.miner.activity.override;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

public class GMCustomImageView extends ImageView {

    public GMCustomImageView(Context context) {
        super(context);
    }

    public GMCustomImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public GMCustomImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    /* ---------------------- OVERRIDE ----------------------- */

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // set heightMeasureSpec = widthMeasureSpec
        super.onMeasure(widthMeasureSpec, widthMeasureSpec);
    }

    /* --------------------- GET-SET ------------------------- */

}

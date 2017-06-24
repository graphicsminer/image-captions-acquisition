package the.miner.activity.override;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;
import android.widget.TextView;

import the.miner.R;

public class GMViewGroup extends RelativeLayout {

    public GMViewGroup(Context context, String title) {
        super(context);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.view_group, this, true);

        TextView tvTitle = (TextView) findViewById(R.id.tvGroupTitle);
        tvTitle.setText(title);
    }
}

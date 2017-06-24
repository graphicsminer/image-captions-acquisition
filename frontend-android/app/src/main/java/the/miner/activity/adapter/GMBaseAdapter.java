package the.miner.activity.adapter;


import android.content.Context;
import android.widget.ArrayAdapter;

import java.util.List;

public class GMBaseAdapter<T> extends ArrayAdapter<T> {

    public GMBaseAdapter(Context context, int resource, List<T> objects) {
        super(context, resource, objects);
    }
}

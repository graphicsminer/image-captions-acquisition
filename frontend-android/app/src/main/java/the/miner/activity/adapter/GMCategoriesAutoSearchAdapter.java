package the.miner.activity.adapter;

import android.content.Context;
import android.widget.Filter;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;

/**
 * Provide data for auto complete text edit
 */
public class GMCategoriesAutoSearchAdapter extends GMBaseAdapter<String> {
    private List<String> categories;

    /**
     * Constructor
     *
     * @param context activity context
     */
    public GMCategoriesAutoSearchAdapter(Context context, int resource, List<String> data) {
        super(context, resource, new ArrayList<String>());
        categories = data;
    }

    /* ---------------------- OVERRIDE ------------------------- */

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                FilterResults filter = new FilterResults();
                List<String> suggestions = new ArrayList<>();

                // char sequence is empty
                if (charSequence == null) return filter;

                // normalize string to avoid Unicode codepoint
                String input = Normalizer.normalize(charSequence, Normalizer.Form.NFC);
                if (input.startsWith("#")) {
                    // get appropriate suggestions
                    for (String cat : categories) {
                        if (match(cat.substring(1), input.substring(1))) {
                            suggestions.add(cat);
                        }
                    }
                }

                filter.values = suggestions;
                filter.count = suggestions.size();

                return filter;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                if (filterResults.count > 0) {
                    clear();  // clear old suggestion
                    addAll((List<String>) filterResults.values); // update suggestion
                }
            }

            @Override
            public CharSequence convertResultToString(Object resultValue) {
                return super.convertResultToString(resultValue);
            }
        };
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }

    /**
     * Check if searching text is matched with specific text content
     *
     * @param text   content
     * @param search searching text
     * @return true if match
     */
    public static boolean match(String text, String search) {
        if (text.toLowerCase().contains(search.toLowerCase())) return true;
        return false;
    }

    /* ---------------------- Method ------------------------- */

    public void setData(List<String> data) {
        categories = data;
    }
}

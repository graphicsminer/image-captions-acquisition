package the.miner.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.apmem.tools.layouts.FlowLayout;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;

import the.miner.R;
import the.miner.activity.adapter.GMCategoriesAutoSearchAdapter;
import the.miner.activity.helper.GMEditorHelper;
import the.miner.activity.override.GMTagView;
import the.miner.activity.override.GMViewGroup;
import the.miner.engine.database.GMDatabaseHelper;
import the.miner.engine.database.model.GMImage;
import the.miner.session.GMKey;
import the.miner.session.GMLanguage;
import the.miner.session.GMSession;
import the.miner.session.GMStoreHelper;
import the.miner.utils.GMImageUtils;
import the.miner.utils.GMUtils;

public class GMImageEditorActivity extends GMBaseActivity {

    private GMStoreHelper storeHelper;
    private GMDatabaseHelper dbHelper;

    private ImageView ivPreviewPhoto;
    private AutoCompleteTextView tvInfoInput;
    private GMCategoriesAutoSearchAdapter suggestionAdapter;

    private LinearLayout categoryArea;
    private LinearLayout captionArea;

    // Image dialog
    private Dialog imageDialog;
    private View imageDialogView;

    // Images or multiple images transferred via intent
    private GMImage data;
    private Boolean isMultipleData;
    private List<GMImage> dataList;
    private int editIdx = 0;

    // determine which tag view is in edit
    private GMTagView editTagView = null;

    // language
    private Locale selectedLang;

    /* ---------------------- OVERRIDE ----------------------- */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_editor);

        // Get helper
        storeHelper = GMSession.getStoreHelper();
        dbHelper = GMSession.getDatabaseHelper();

        // get bundle data.
        Bundle bundle = this.getIntent().getExtras();
        isMultipleData = bundle.getBoolean(GMKey.BUNDLE_IS_MULTIPLE);
        if (isMultipleData) {
            dataList = (List<GMImage>) bundle.getSerializable(GMKey.BUNDLE_DATA);
            data = dataList.get(editIdx);
        } else {
            data = (GMImage) bundle.get(GMKey.BUNDLE_DATA);
        }

        selectedLang = getSavedLanguage();

        // set back button and title
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.back);
        getSupportActionBar().setTitle(R.string.aie_title);

        // Get component
        ivPreviewPhoto = (ImageView) findViewById(R.id.ivPreviewPhoto);

        tvInfoInput = (AutoCompleteTextView) findViewById(R.id.tvInfoInput);
        tvInfoInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                return onAutoCompleteTextEditorAction(textView, i, keyEvent);
            }
        });

        suggestionAdapter = new GMCategoriesAutoSearchAdapter(
                this,
                android.R.layout.simple_list_item_1,
                GMEditorHelper.getAllCategories(selectedLang));
        tvInfoInput.setThreshold(1);
        tvInfoInput.setAdapter(suggestionAdapter);

        captionArea = (LinearLayout) findViewById(R.id.captionArea);
        categoryArea = (LinearLayout) findViewById(R.id.categoryArea);

        // create image dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        imageDialogView = this.getLayoutInflater().inflate(R.layout.custom_image_dialog, null);
        builder.setView(imageDialogView);
        imageDialog = builder.create();

        // load data onto layout
        this.loadData();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.image_editor_action_menu, menu);

        if (selectedLang.equals(GMLanguage.US)) {
            menu.findItem(R.id.action_change_language).setTitle("EN");
        } else {
            menu.findItem(R.id.action_change_language).setTitle("VI");
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;

            case R.id.action_change_language:
                // Support english and vietnamese
                if (item.getTitle().equals("EN")) {
                    item.setTitle("VI");
                    selectedLang = GMLanguage.VI;
                } else {
                    item.setTitle("EN");
                    selectedLang = GMLanguage.US;
                }
                saveSettingLanguage();

                // update category suggestion
                suggestionAdapter.setData(GMEditorHelper.getAllCategories(selectedLang));

                return true;

            case R.id.action_voice:
                this.requestVoiceRecognizer(selectedLang);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onHandleVoiceRecognition(String result) {
        super.onHandleVoiceRecognition(result);
        if (data.addCaption(selectedLang.toString(), result).length > 0) {
            loadCaption();
        }
    }

    /* ---------------------- EVENT -------------------------- */

    /**
     * On save button click event
     *
     * @param v button
     */
    public void onSaveClick(View v) {
        GMEditorHelper.doneEditing(data);
        this.nextEdit();
    }

    /**
     * On move image to task list
     *
     * @param v button
     */
    public void onMoveToTaskClick(View v) {
        // For the purpose of loading image's information into editor,
        // Image already create and insert to database as temporary
        GMEditorHelper.moveToTask(data);
        this.nextEdit();
    }

    /**
     * On delete image click event
     *
     * @param v button
     */
    public void onDeleteImageClick(View v) {
        GMEditorHelper.deleteImage(data);
        this.nextEdit();
    }

    /**
     * On clear input click event
     *
     * @param v image button
     */
    public void onClearInputClick(View v) {
        tvInfoInput.getText().clear();
        editTagView = null;
    }

    /**
     * On add category|caption click event
     *
     * @param v image button
     */
    public void onAddInfoClick(View v) {
        String input = tvInfoInput.getText().toString().toLowerCase();
        // normalize string to avoid Unicode codepoint
        input = Normalizer.normalize(input, Normalizer.Form.NFC);
        addImageInfo(input);
        tvInfoInput.getText().clear();
    }

    /**
     * Handle input and keyboard event
     *
     * @param textView auto complete text view
     * @param actionId action id
     * @param keyEvent key event
     * @return
     */
    private boolean onAutoCompleteTextEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
        if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_GO
                || actionId == EditorInfo.IME_ACTION_NEXT) {

            String input = tvInfoInput.getText().toString().toLowerCase();
            // normalize string to avoid Unicode codepoint
            input = Normalizer.normalize(input, Normalizer.Form.NFC);
            addImageInfo(input);
            ((AutoCompleteTextView) textView).dismissDropDown();
            tvInfoInput.getText().clear();
        }
        return false;
    }

    /**
     * On thumbnail image click event
     *
     * @param v thumbnail image view
     */
    public void onThumbnailClick(View v) {
        ImageView iv = (ImageView) imageDialogView.findViewById(R.id.ivFullImage);
        Point screenSize = GMUtils.getScreenSize(this);
        iv.setImageBitmap(GMImageUtils.fileToBitmap(storeHelper.getImageFile(data), screenSize.x, screenSize.y));
        imageDialog.show();
    }

    /* ---------------------- METHOD ------------------------- */

    /**
     * Split caption, category and other information from input
     * then add respective views to layout
     *
     * @param input input
     */
    private void addImageInfo(String input) {
        String lang = (editTagView != null) ? editTagView.getLanguage() : selectedLang.toString();
        // Remove old edited tag view before add new edited content
        if (editTagView != null) {
            String content = editTagView.getTitle();
            if (content.startsWith("#")) {
                data.removeCategory(lang, content);
            } else {
                data.removeCaption(lang, content);
            }
            editTagView = null;
        }

        // Add new edited content
        if (input.startsWith("#")) {
            // add category
            data.addCategory(lang, input.split("#"));
            loadCategory();
        } else {
            // add caption
            data.addCaption(lang, input.split("\\."));
            loadCaption();
        }
    }

    /**
     * Load data onto layout
     */
    private void loadData() {
        editTagView = null;

        // load data
        loadPhotoIntoView(ivPreviewPhoto, storeHelper.getImageFile(data), 1);
        loadCategory();
        loadCaption();
    }

    /**
     * Edit next image in list
     */
    private void nextEdit() {
        if (isMultipleData && editIdx < dataList.size() - 1) {
            editIdx++;
            data = dataList.get(editIdx);
            this.loadData();

        } else {
            // back to previous activity
            finish();
        }
    }

    /**
     * Load caption into layout.
     */
    private void loadCaption() {
        captionArea.removeAllViews();

        // load all captions.
        // Group captions by language
        for (String lang : data.getCaptionLanguages()) {
            String[] captions = data.getCaptions(lang);
            if (captions.length > 0) {
                GMViewGroup groupView = new GMViewGroup(this, getString(R.string.aie_group_caption) + " - " + lang);
                captionArea.addView(groupView);

                for (String cap : captions) {
                    addCaptionView(groupView, cap, lang);
                }
            }
        }
    }

    /**
     * Add caption view to layout
     *
     * @param caption caption
     * @return added view
     */
    private GMTagView addCaptionView(GMViewGroup groupView, final String caption, final String lang) {
        FlowLayout flowLayout = (FlowLayout) groupView.findViewById(R.id.flowLayout);
        GMTagView capView = new GMTagView(this, caption) {
            @Override
            protected void onRemoveClick(GMTagView tagView, View view) {
                super.onRemoveClick(tagView, view);
                data.removeCaption(tagView.getLanguage(), tagView.getTitle());
                loadCaption();
            }

            @Override
            protected void onSelect(GMTagView tagView, View view) {
                super.onSelect(tagView, view);
                editTagView = tagView;
                tvInfoInput.setText(tagView.getTitle());
            }
        };
        capView.setLanguage(lang);
        flowLayout.addView(capView); // add caption view to layout
        return capView;
    }

    /**
     * Load categories into layout.
     */
    private void loadCategory() {
        categoryArea.removeAllViews();

        // load all categories.
        // Group category by language
        for (String lang : data.getCategoryLanguages()) {
            String[] categories = data.getCategories(lang);
            if (categories.length > 0) {
                GMViewGroup groupView = new GMViewGroup(this, getString(R.string.aie_group_category) + " - " + lang);
                categoryArea.addView(groupView);

                for (String cat : categories) {
                    addCategoryView(groupView, cat, lang);
                }
            }
        }
    }

    /**
     * Add category view to layout
     *
     * @param category category
     * @return added view
     */
    private GMTagView addCategoryView(GMViewGroup groupView, final String category, final String lang) {
        FlowLayout flowLayout = (FlowLayout) groupView.findViewById(R.id.flowLayout);
        GMTagView catView = new GMTagView(this, "#" + category.replace("#", "")) {
            @Override
            protected void onRemoveClick(GMTagView tagView, View view) {
                super.onRemoveClick(tagView, view);
                data.removeCategory(tagView.getLanguage(), tagView.getTitle());
                loadCategory();
            }

            @Override
            protected void onSelect(GMTagView tagView, View view) {
                super.onSelect(tagView, view);
                editTagView = tagView;
                tvInfoInput.setText(tagView.getTitle());
            }
        };
        catView.setLanguage(lang);
        flowLayout.addView(catView); // add category view to layout
        return catView;
    }

    /**
     * Save setting language
     */
    private void saveSettingLanguage() {
        SharedPreferences.Editor editor = storeHelper.getSharedPrefs().edit();
        editor.putString(GMKey.IMAGE_LANGUAGE, selectedLang.getLanguage());
        editor.putString(GMKey.IMAGE_LANGUAGE_COUNTRY, selectedLang.getCountry());
        editor.commit();
    }

    /**
     * Get saved language from preferences
     *
     * @return setting language
     */
    private Locale getSavedLanguage() {
        String lang = storeHelper.getSharedPrefs().getString(GMKey.IMAGE_LANGUAGE, "");
        String lang_country = storeHelper.getSharedPrefs().getString(GMKey.IMAGE_LANGUAGE_COUNTRY, "");
        return lang.isEmpty() ? Locale.US : new Locale(lang, lang_country);
    }
}

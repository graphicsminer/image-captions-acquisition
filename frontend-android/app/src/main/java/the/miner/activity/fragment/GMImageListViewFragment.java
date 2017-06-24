package the.miner.activity.fragment;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import the.miner.R;
import the.miner.activity.GMGalleryActivity;
import the.miner.activity.GMImageEditorActivity;
import the.miner.activity.adapter.GMImageViewAdapter;
import the.miner.activity.dialog.GMDeleteConfirmationDialog;
import the.miner.activity.helper.GMEditorHelper;
import the.miner.engine.database.GMDatabaseHelper;
import the.miner.engine.database.model.GMImage;
import the.miner.session.GMKey;
import the.miner.session.GMSession;
import the.miner.session.GMStoreHelper;

public class GMImageListViewFragment extends GMBaseFragment<GMImageViewAdapter> {

    private GMStoreHelper storeHelper;
    private GMDatabaseHelper dbHelper;

    private Set<Integer> hideItemList = new HashSet<>();

    /* ---------------------- OVERRIDE ----------------------- */

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        storeHelper = GMSession.getStoreHelper();
        dbHelper = GMSession.getDatabaseHelper();

        return inflater.inflate(R.layout.fragment_image_list_view, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        /* Setup any handles to view albums here */

        // Attach adapter to list view
        ListView listView = (ListView) view.findViewById(R.id.lvImage);
        listView.setAdapter(getAdapter());
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Intent intent = new Intent(getActivity(), GMGalleryActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable(GMKey.BUNDLE_DATA, (Serializable) getImageForGallery(position));
                bundle.putInt(GMKey.BUNDLE_POSITION, 0);
                intent.putExtras(bundle);
                getActivity().startActivity(intent);
            }
        });
        registerForContextMenu(listView);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.image_context_menu, menu);

        // hide menu item
        for (int id : hideItemList) {
            menu.findItem(id).setVisible(false);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            case R.id.edit:
                this.edit(info);
                return true;
            case R.id.delete:
                this.remove(info);
                return true;
            case R.id.moveToTask:
                this.moveToTask(info);
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    @Override
    public void setData(final Serializable data) {
        super.setData(data);
        if (getAdapter() != null) {
            getAdapter().clear();
            getAdapter().addAll((List) data);
            getAdapter().notifyDataSetChanged();
        }
    }

    @Override
    protected GMImageViewAdapter initAdapter(Context context) {
        // Initialize adapter
        List<GMImage> data = new ArrayList<>();
        return new GMImageViewAdapter(context, data);
    }

    /* ---------------------- METHOD ------------------------- */

    /**
     * Remove image
     *
     * @param info menu info
     */
    private void remove(AdapterView.AdapterContextMenuInfo info) {
        final GMImage img = getAdapter().getItem(info.position);
        Dialog dialog = new GMDeleteConfirmationDialog(getActivity(), getString(R.string.confirmation_message)) {
            @Override
            public void onOkClick(DialogInterface dialog, int id) {
                if (GMEditorHelper.deleteImage(img)) {
                    getAdapter().remove(img); // remove from view
                } else {
                    Toast.makeText(getActivity(), getString(R.string.error_delete_image), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        }.create();
        dialog.show();
    }

    /**
     * Edit image -> move to photo editor
     *
     * @param info menu info
     */
    private void edit(AdapterView.AdapterContextMenuInfo info) {
        // Navigate to image editor
        GMImage img = getAdapter().getItem(info.position);
        Intent i = new Intent(getActivity(), GMImageEditorActivity.class);
        i.putExtra(GMKey.BUNDLE_DATA, img);
        startActivity(i);
    }

    /**
     * Move image to task
     *
     * @param info menu info
     */
    private void moveToTask(AdapterView.AdapterContextMenuInfo info) {
        GMImage img = getAdapter().getItem(info.position);
        img.setStatus(GMImage.GMStatus.TODO);
        if (dbHelper.update(img) > 0) {
            getAdapter().remove(img);
        } else {
            Toast.makeText(getActivity(), getString(R.string.error_update_image), Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Hide specified menu item
     *
     * @param id id of item
     */
    public void hideMenuItem(int id) {
        hideItemList.add(id);
    }

    /**
     * Show all menu item
     */
    public void showAllMenuItem() {
        hideItemList.clear();
    }

    /**
     * Show specified menu item.
     * By default all menu item will be display
     *
     * @param id id of item
     */
    public void showMenuItem(int id) {
        hideItemList.remove(id);
    }

    /**
     * Get image for displaying in gallery
     * Allow load maximum 10 pictures around selected image to avoid OutOfMemory problem
     * TODO: temporary fix error of OutOfMemory. See ticket #40
     * @param position selected image
     * @return list of image
     */
    private List<GMImage> getImageForGallery(int position) {
        List<GMImage> list = new ArrayList<>();
        final List<GMImage> images = (List<GMImage>) getData();
        list.add(images.get(position));

        for (int i = position - 1; i >= position - 5; i--) {
            if (i >= 0) {
                list.add(images.get(i));
            }
        }

        for (int i = position + 1; i <= position + 5; i++) {
            if (i <= images.size() - 1) {
                list.add(images.get(i));
            }
        }

        return list;
    }
}

package the.miner.activity;

import android.app.Dialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

import the.miner.R;
import the.miner.activity.dialog.GMDeleteConfirmationDialog;
import the.miner.activity.fragment.GMImageListViewFragment;
import the.miner.activity.helper.GMEditorHelper;
import the.miner.activity.override.GMAsynTask;
import the.miner.engine.database.GMDatabaseHelper;
import the.miner.engine.database.model.GMImage;
import the.miner.engine.database.model.GMTable;
import the.miner.session.GMKey;
import the.miner.session.GMSession;
import the.miner.session.GMStoreHelper;
import the.miner.utils.GMFileUtils;

public class GMMainActivity extends GMBaseActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private FragmentManager fragManager;
    private GMImageListViewFragment lvImageFragment;

    private GMDatabaseHelper dbHelper;
    private GMStoreHelper storeHelper;

    private NavigationView navigationView;
    private DrawerLayout drawer;
    private Toolbar toolbar;

    private List<GMImage> imageList;
    private int activeNavigationItem;

    /* ---------------------- OVERRIDE ----------------------- */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // init helper class
        fragManager = this.getFragmentManager();
        dbHelper = GMSession.getDatabaseHelper();
        storeHelper = GMSession.getStoreHelper();

        // init fragment
        FragmentTransaction trans = fragManager.beginTransaction();
        lvImageFragment = new GMImageListViewFragment();
        trans.replace(R.id.drawerBody, lvImageFragment);
        loadImages(GMImage.GMStatus.DONE);
        trans.commit();
        activeNavigationItem = R.id.navDoneView;

        // init android component
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.drawer_done);
        setSupportActionBar(toolbar);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    protected void onRestart() {
        super.onRestart();

        if (activeNavigationItem == R.id.navTodoView) {
            loadImages(GMImage.GMStatus.TODO);
        } else {
            loadImages(GMImage.GMStatus.DONE);
            navigationView.setCheckedItem(R.id.navDoneView);
        }
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_action_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
        // filter action button here
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.action_take_photo:
                this.requestCamera();
                return true;

            case R.id.action_file_browser:
                this.requestFileBrowser();
                return true;

            case R.id.action_folder_browser:
                requestDirectoryChooser();
                return true;

            case R.id.action_delete_all:
                this.deleteAllImages();
                return true;

            case R.id.action_edit_all:
                this.editAllImage();
                return true;

            case R.id.action_info:
                this.showInfo();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        activeNavigationItem = id;
        FragmentTransaction trans = fragManager.beginTransaction();

        // control menu item of list view fragment
        if (lvImageFragment != null) {
            lvImageFragment.showAllMenuItem();
        }

        if (id == R.id.navDoneView) {
            toolbar.setTitle(R.string.drawer_done);
            loadImages(GMImage.GMStatus.DONE);
            trans.replace(R.id.drawerBody, lvImageFragment);

        } else if (id == R.id.navTodoView) {
            lvImageFragment.hideMenuItem(R.id.moveToTask);
            toolbar.setTitle(R.string.drawer_todo);
            loadImages(GMImage.GMStatus.TODO);
            trans.replace(R.id.drawerBody, lvImageFragment);

        } else if (id == R.id.navSync) {
            Intent i = new Intent(this, GMSyncActivity.class);
            startActivity(i);

        } else if (id == R.id.navSetting) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        trans.commit();
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onHandleCameraResult(String photoPath) {
        super.onHandleCameraResult(photoPath);

        // Create new image
        GMImage img = GMEditorHelper.addNewImage(new File(photoPath));

        if (img != null) {
            // Navigate to photo editor
            Intent i = new Intent(this, GMImageEditorActivity.class);
            i.putExtra(GMKey.BUNDLE_DATA, img);
            startActivity(i);
        } else {
            Toast.makeText(this, R.string.error_add_image, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onHandleFileBrowserResult(String photoPath) {
        super.onHandleFileBrowserResult(photoPath);

        // Create new image
        GMImage img = GMEditorHelper.addNewImage(new File(photoPath));

        if (img != null) {
            // Navigate to photo editor
            Intent i = new Intent(this, GMImageEditorActivity.class);
            i.putExtra(GMKey.BUNDLE_DATA, img);
            startActivity(i);
        } else {
            Toast.makeText(this, R.string.error_add_image, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onHandleDirectoryBrowser(final String dirPath) {
        super.onHandleDirectoryBrowser(dirPath);

        GMAsynTask task = new GMAsynTask(this) {
            @Override
            protected String doInBackground(String... strings) {
                File[] imageFiles = new File(dirPath).listFiles(new FileFilter() {
                    @Override
                    public boolean accept(File file) {
                        return GMFileUtils.isImage(file, GMImage.SUPPORTED_IMAGE_TYPE);
                    }
                });

                for (File f : imageFiles) {
                    // copy file to app's folder
                    try {
                        File dest = new File(storeHelper.getTemporaryDir(), f.getName());
                        GMFileUtils.copy(f, dest);
                        GMEditorHelper.addNewImage(dest);
                    } catch (IOException e) {
                        Toast.makeText(GMMainActivity.this, R.string.error_add_image, Toast.LENGTH_SHORT).show();
                    }
                }
                return null;
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                Toast.makeText(GMMainActivity.this, R.string.success_load_all_images, Toast.LENGTH_SHORT).show();
            }
        };
        task.execute();
    }

    /* ---------------------- EVENT -------------------------- */

    /**
     * On refresh location click event
     *
     * @param v floating buttons
     */
    public void onRefreshLocationClick(View v) {
        this.requestLocationUpdate();
        if (GMSession.getLocation() != null) {
            Toast.makeText(this, R.string.gps_work_well, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, R.string.gps_not_work, Toast.LENGTH_SHORT).show();
        }
    }

    /* --------------------- GET-SET ------------------------- */

    /* ---------------------- METHOD ------------------------- */

    /**
     * Notify data changed
     */
    private void notifyDataChanged() {
        if (imageList != null) {
            // data changed -> notify to adapters
            lvImageFragment.setData((Serializable) imageList);
        }
    }

    /**
     * Load albums from database
     */
    private void loadImages(final GMImage.GMStatus status) {
        GMAsynTask task = new GMAsynTask(this) {
            @Override
            protected String doInBackground(String... args) {
                switch (status) {
                    case DONE:
                        imageList = GMEditorHelper.findImageForDoneView();
                        break;
                    case TODO:
                        imageList = GMEditorHelper.findImageForTaskView();
                        break;
                }
                return null;
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                notifyDataChanged();
            }
        };
        task.execute();
    }

    /**
     * Delete all images based on selected type of displayed layout (TODO or DONE)
     */
    private void deleteAllImages() {

        Dialog dialog = new GMDeleteConfirmationDialog(this, getString(R.string.confirmation_message)) {
            @Override
            public void onOkClick(DialogInterface dialog, int id) {
                Iterator<GMImage> it = imageList.iterator();
                while (it.hasNext()) {
                    GMImage img = it.next();
                    if (img.getSyncState().equals(GMTable.GMSyncState.SYNC)) {
                        GMEditorHelper.deleteImage(img);
                        it.remove();
                    }
                }

                lvImageFragment.setData((Serializable) imageList);
                Toast.makeText(GMMainActivity.this, R.string.success_delete_all_syn_photos, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onCancelClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        }.create();
        dialog.show();
    }

    /**
     * Edit all images based on selected type of displayed layout (TODO or DONE)
     */
    private void editAllImage() {
        // Navigate to image editor
        if (imageList.isEmpty()) {
            Toast.makeText(this, R.string.error_not_photo_to_edit, Toast.LENGTH_LONG).show();
        } else {
            Intent i = new Intent(this, GMImageEditorActivity.class);
            i.putExtra(GMKey.BUNDLE_DATA, (Serializable) imageList);
            i.putExtra(GMKey.BUNDLE_IS_MULTIPLE, true);
            startActivity(i);
        }
    }

    /**
     * Show extra information
     */
    private void showInfo() {
        String info = imageList.size() + " photos";
        Toast.makeText(this, info, Toast.LENGTH_LONG).show();
    }
}

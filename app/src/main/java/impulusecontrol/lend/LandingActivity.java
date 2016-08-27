package impulusecontrol.lend;

import android.Manifest;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.beardedhen.androidbootstrap.TypefaceProvider;
import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.OnMenuTabClickListener;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import impulusecontrol.lend.model.User;
import layout.AccountFragment;
import layout.HistoryFragment;
import layout.HomeFragment;
import layout.NewRequestFragment;

/**
 * Created by kerrk on 7/17/16.
 */
public class LandingActivity extends AppCompatActivity
        implements AccountFragment.OnFragmentInteractionListener,
        HomeFragment.OnFragmentInteractionListener,
        NewRequestFragment.OnFragmentInteractionListener,
        HistoryFragment.OnFragmentInteractionListener {

    private User user;
    private Toolbar toolbar;
    private BottomBar mBottomBar;
    private Integer currentMenuItem;
    FragmentManager fragmentManager = getFragmentManager();

    /**
     * Root of the layout of this Activity.
     */
    private View mLayout;

    public static final String TAG = "LandingActivity";

    /**
     * Id to identify the fine location permission request.
     */
    private static final int REQUEST_FINE_LOCATION = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TypefaceProvider.registerDefaultIconSets();
        setContentView(R.layout.activity_landing);
        mLayout = findViewById(R.id.landing_layout);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Fine location permission has not been granted.
            requestFineLocationPermission();
        } else {
            // Fine location is already available, show the camera preview.
            Log.i(TAG, "FINE LOCATION permission has already been granted.");
        }

        toolbar = (Toolbar) findViewById(R.id.tool_bar); // Attaching the layout to the toolbar object

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        toolbar.setTitle("");
        toolbar.setSubtitle("");

        mBottomBar = BottomBar.attach(this, savedInstanceState);
        mBottomBar.setItems(R.menu.bottom_bar);
        setmBottomBarListener();

        user = PrefUtils.getCurrentUser(LandingActivity.this);
        Log.e("user access token: ", user.getAccessToken());
        Log.e("user name: ", user.getName());

        // this isn't necessary ....just testing the server setup
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    URL url = new URL(Constants.NEARBY_API_PATH + "/users/me");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setReadTimeout(10000);
                    conn.setConnectTimeout(30000);
                    conn.setRequestMethod("GET");
                    conn.setRequestProperty("x-auth-token", user.getAccessToken());
                    int responseCode = conn.getResponseCode();
                    Log.e("response: ", "GET Response Code :: " + responseCode);
                } catch (IOException e) {
                    Log.e("ERROR!!!!! ", e.getMessage());
                }
                return null;
            }

        }.execute();
    }

    public void setmBottomBarListener() {
        mBottomBar.setOnMenuTabClickListener(new OnMenuTabClickListener() {
            @Override
            public void onMenuTabSelected(@IdRes int menuItemId) {
                if (menuItemId == R.id.bottomBarHomeItem) {
                    if(fragmentManager.findFragmentByTag(Constants.HOME_FRAGMENT_TAG) != null) {
                        fragmentManager.beginTransaction()
                                .setCustomAnimations(R.animator.enter_from_right, R.animator.exit_to_left)
                                .show(fragmentManager.findFragmentByTag(Constants.HOME_FRAGMENT_TAG))
                                .commit();
                    } else {
                        fragmentManager.beginTransaction()
                                .setCustomAnimations(R.animator.enter_from_right, R.animator.exit_to_left)
                                .add(R.id.content_frame, HomeFragment.newInstance(), Constants.HOME_FRAGMENT_TAG)
                                .commit();
                    }
                    hideOtherFragments(Constants.HOME_FRAGMENT_TAG, R.animator.exit_to_left);
                } else if (menuItemId == R.id.bottomBarAccountItem) {
                    if(fragmentManager.findFragmentByTag(Constants.ACCOUNT_FRAGMENT_TAG) != null) {
                        fragmentManager.beginTransaction()
                                .setCustomAnimations(R.animator.enter_from_left, R.animator.exit_to_right)
                                .show(fragmentManager.findFragmentByTag(Constants.ACCOUNT_FRAGMENT_TAG))
                                .commit();
                    } else {
                        fragmentManager.beginTransaction()
                                .setCustomAnimations(R.animator.enter_from_left, R.animator.exit_to_right)
                                .add(R.id.content_frame, AccountFragment.newInstance(), Constants.ACCOUNT_FRAGMENT_TAG)
                                .commit();
                    }
                    hideOtherFragments(Constants.ACCOUNT_FRAGMENT_TAG, R.animator.exit_to_right);
                } else {
                    int firstAnim = currentMenuItem != null && currentMenuItem < menuItemId ? R.animator.enter_from_left : R.animator.enter_from_right;
                    int secondAnim = currentMenuItem != null && currentMenuItem < menuItemId ? R.animator.exit_to_right : R.animator.exit_to_left;
                    if(fragmentManager.findFragmentByTag(Constants.HISTORY_FRAGMENT_TAG) != null) {
                        fragmentManager.beginTransaction()
                                .setCustomAnimations(firstAnim, secondAnim)
                                .show(fragmentManager.findFragmentByTag(Constants.HISTORY_FRAGMENT_TAG))
                                .commit();
                    } else {
                        fragmentManager.beginTransaction()
                                .setCustomAnimations(firstAnim, secondAnim)
                                .add(R.id.content_frame, HistoryFragment.newInstance(), Constants.HISTORY_FRAGMENT_TAG)
                                .commit();
                    }
                    hideOtherFragments(Constants.HISTORY_FRAGMENT_TAG, secondAnim);
                }
                currentMenuItem = menuItemId;
            }

            public void hideOtherFragments(String current, int leaveAnimation) {
                if (!current.equals(Constants.HOME_FRAGMENT_TAG)) {
                    if(fragmentManager.findFragmentByTag(Constants.HOME_FRAGMENT_TAG) != null){
                        fragmentManager.beginTransaction()
                                .setCustomAnimations(0, leaveAnimation)
                                .hide(fragmentManager.findFragmentByTag(Constants.HOME_FRAGMENT_TAG))
                                .commit();
                    }
                }
                if (!current.equals(Constants.HISTORY_FRAGMENT_TAG)) {
                    if(fragmentManager.findFragmentByTag(Constants.HISTORY_FRAGMENT_TAG) != null){
                        fragmentManager.beginTransaction()
                                .setCustomAnimations(0, leaveAnimation)
                                .hide(fragmentManager.findFragmentByTag(Constants.HISTORY_FRAGMENT_TAG))
                                .commit();
                    }
                }
                if (!current.equals(Constants.ACCOUNT_FRAGMENT_TAG)) {
                    if(fragmentManager.findFragmentByTag(Constants.ACCOUNT_FRAGMENT_TAG) != null){
                        fragmentManager.beginTransaction()
                                .setCustomAnimations(0, leaveAnimation)
                                .hide(fragmentManager.findFragmentByTag(Constants.ACCOUNT_FRAGMENT_TAG))
                                .commit();
                    }
                }
            }

            @Override
            public void onMenuTabReSelected(@IdRes int menuItemId) {
            }
        });
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // Necessary to restore the BottomBar's state, otherwise we would
        // lose the current tab on orientation change.
        mBottomBar.onSaveInstanceState(outState);
    }

    public void onFragmentInteraction(Uri uri) {
        //nothing
    }

    /**
     * Requests the Camera permission.
     * If the permission has been denied previously, a SnackBar will prompt the user to grant the
     * permission, otherwise it is requested directly.
     */
    private void requestFineLocationPermission() {
        Log.e(TAG, "FINE LOCATION permission has NOT been granted. Requesting permission.");
        Log.e(TAG, ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.ACCESS_FINE_LOCATION) + " *should show rationale");
        // BEGIN_INCLUDE(fine_location_permission_request)
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Provide an additional rationale to the user if the permission was not granted
            // and the user would benefit from additional context for the use of the permission.
            // For example if the user has previously denied the permission.
            Log.i(TAG,
                    "Displaying fine location permission rationale to provide additional context.");
            Snackbar.make(mLayout, R.string.permission_fine_location_rationale,
                    Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.ok, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            ActivityCompat.requestPermissions(LandingActivity.this,
                                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                    REQUEST_FINE_LOCATION);
                        }
                    })
                    .show();
        } else {
            // Fine location permission has not been granted yet. Request it directly.
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_FINE_LOCATION);
        }
        // END_INCLUDE(fine_location_permission_request)
    }

    public void goToAccount() {
        AccountFragment fragment = (AccountFragment)fragmentManager.findFragmentByTag(Constants.ACCOUNT_FRAGMENT_TAG);
        if (fragment != null) {
            fragment.parentScroll.scrollTo(0, 0);

        }
        mBottomBar.selectTabAtPosition(2, true);
    }

}

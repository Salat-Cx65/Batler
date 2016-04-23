package com.s_komlach.batler;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import com.daimajia.numberprogressbar.NumberProgressBar;
import com.dataconnectiontoggler.DeamonService;
import com.s_komlach.batler.fragments.*;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, Constants {

    private static final String STATE_SELECTED_DRAWER_INDEX = "selected_drawer_index";

    private Toolbar mToolbar;
    private NavigationView mDrawer;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private int mSelectedId = -1;
    private SwitchCompat batlerSwitch;
    public NumberProgressBar numericBar;
    private View mRootView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        numericBar = (NumberProgressBar) findViewById(R.id.number_progress_bar);
        setSupportActionBar(mToolbar);
        batlerSwitch = (SwitchCompat) mToolbar.findViewById(R.id.batlerSwitch);


        SharedPreferences pref = PreferenceManager
                .getDefaultSharedPreferences(this);

        batlerSwitch.setChecked(pref.getBoolean(STATE_BATLER, STATE_BATLER_DEFVALUE));
        batlerSwitch.setOnCheckedChangeListener(mListener);

        mDrawer = (NavigationView) findViewById(R.id.navigation_drawer);
        mDrawer.setNavigationItemSelectedListener(this);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        if (mDrawerLayout != null) {
            mRootView = mDrawerLayout;
            mDrawerToggle = new ActionBarDrawerToggle(this,
                    mDrawerLayout,
                    mToolbar,
                    R.string.drawer_open,
                    R.string.drawer_close);
            mDrawerLayout.setDrawerListener(mDrawerToggle);
            mDrawerToggle.syncState();
        }
        else
            mRootView = findViewById(R.id.root);

        mSelectedId = R.id.switchers_item;
        if (savedInstanceState != null) {
            mSelectedId = savedInstanceState.getInt(STATE_SELECTED_DRAWER_INDEX);
        }
        selectMenuItem(mSelectedId);


        if(!DeamonService.running && pref.getBoolean(STATE_BATLER, STATE_BATLER_DEFVALUE))
            startService(new Intent(MainActivity.this, DeamonService.class));

    }

        @Override
    public void onResume(){
        super.onResume();
            if(mSelectedId==R.id.switchers_item)
                selectMenuItem(mSelectedId);

    }

    private final CompoundButton.OnCheckedChangeListener mListener
            = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean state) {

            SharedPreferences pref = PreferenceManager
                    .getDefaultSharedPreferences(MainActivity.this);
            SharedPreferences.Editor edit = pref.edit();
            edit.putBoolean(STATE_BATLER, state);
            edit.commit();
            if(state){

                if(!DeamonService.running)
                    startService(new Intent(MainActivity.this, DeamonService.class));


            }
            else
            {
                if(DeamonService.running)
                    stopService(new Intent(MainActivity.this, DeamonService.class));

            }

            final String message = getString(R.string.you_switched,
                    compoundButton.getText(),
                    state ? "on" : "off");
            Snackbar.make(mToolbar, message, Snackbar.LENGTH_SHORT)
                        .show();

        }
    };

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_SELECTED_DRAWER_INDEX, mSelectedId);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (mDrawerToggle != null) {
            mDrawerToggle.onConfigurationChanged(newConfig);
        }
    }

    @Override
    public void onBackPressed() {
        if ( (mDrawerLayout != null) && (mDrawerLayout.isDrawerOpen(GravityCompat.START)) ) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem menuItem) {
        final int selectedId = menuItem.getItemId();
        if (mSelectedId == selectedId)
            return false;

        mSelectedId = selectedId;
        menuItem.setChecked(true);
        Snackbar.make(mRootView, menuItem.getTitle(), Snackbar.LENGTH_SHORT)
                .show();

        boolean result = selectMenuItem(mSelectedId);
        if (result) {
            setTitle(menuItem.getTitle());
            if (mDrawerLayout != null) {
                mDrawerLayout.closeDrawer(GravityCompat.START);
            }
        }
        return result;
    }

    AlertDialog dialog;
    
    private void showAlertDialog(String title, String text, DialogInterface.OnClickListener listenerOk){
        if(dialog!=null && dialog.isShowing())
            dialog.dismiss();
        dialog = new AlertDialog.Builder(this).setTitle(title).setMessage(text).setPositiveButton(getString(android.R.string.ok), listenerOk).show();

    }
    private boolean selectMenuItem(final int index) {
        Fragment fragment = null;
        final SharedPreferences pref = PreferenceManager
                .getDefaultSharedPreferences(this);
        final SharedPreferences.Editor edit = pref.edit();

        switch (index) {
            case R.id.switchers_item:
                fragment = new SwitchersFragment();

                if(pref.getBoolean("firstlaunch_"+index, true))
                showAlertDialog(getString(R.string.switchers),
                        "In this section you can choose what functions app should disable, when device is locked",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                edit.putBoolean("firstlaunch_"+index, false);
                                edit.commit();
                            }
                        });

                break;
            case R.id.rules_item:
                fragment = new RulesFragment();
                if(pref.getBoolean("firstlaunch_"+index, true))
                    showAlertDialog(getString(R.string.rules),
                            "In this section you can choose which rules to use in work",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    edit.putBoolean("firstlaunch_"+index, false);
                                    edit.commit();
                                }
                            });
                break;
            case R.id.follow_twitter:
                final String twitterName = getString(R.string.twitter_name);
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("twitter://user?screen_name=" + twitterName)));
                } catch (Exception e) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://twitter.com/intent/follow?screen_name=" + twitterName)));
                }

                break;
            case R.id.info_item:
                fragment = new InfoFragment();
                if(pref.getBoolean("firstlaunch_"+index, true))
                    showAlertDialog(getString(R.string.info),
                            "Here you can see information about battery usage",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    edit.putBoolean("firstlaunch_"+index, false);
                                    edit.commit();
                                }
                            });
                break;
            case R.id.exit_item:
                finish();
                break;
        }



        if (fragment == null) return false;
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();
        return true;
    }


}

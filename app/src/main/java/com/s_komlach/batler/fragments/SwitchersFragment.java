package com.s_komlach.batler.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import com.dataconnectiontoggler.Utils;
import com.s_komlach.batler.Constants;
import com.s_komlach.batler.MainActivity;
import com.s_komlach.batler.R;

public class SwitchersFragment extends Fragment implements Constants {


    private SwitchCompat mWifiSwitch, mBluetoothSwitch, mGpsSwitch, mMobDataSwitch, mNfcSwitch;
    private SwitchCompat brightnesSwitch, syncSwitch, powersaveSwitch,  orientattionSwitch, mAirplaneSwitch;

    int delta;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_switchers, container, false);

        mWifiSwitch = (SwitchCompat) view.findViewById(R.id.wifiSwitch);

        mBluetoothSwitch = (SwitchCompat) view.findViewById(R.id.bluetoothSwitch);
        mGpsSwitch = (SwitchCompat) view.findViewById(R.id.gpsSwitch);


            mMobDataSwitch = (SwitchCompat) view.findViewById(R.id.mobileDataSwitchNonRoot);
            view.findViewById(R.id.mobileDataSwitchWithRoot).setVisibility(View.GONE);


        mNfcSwitch = (SwitchCompat) view.findViewById(R.id.nfcSwitch);
        brightnesSwitch = (SwitchCompat) view.findViewById(R.id.brightnesSwitch);
        syncSwitch = (SwitchCompat) view.findViewById(R.id.syncSwitch);
        powersaveSwitch = (SwitchCompat) view.findViewById(R.id.powersaveSwitch);
        orientattionSwitch = (SwitchCompat) view.findViewById(R.id.orientattionSwitch);
        mAirplaneSwitch = (SwitchCompat) view.findViewById(R.id.airplanemodeSwitch);




        if(!getActivity().getPackageManager().hasSystemFeature(PackageManager.FEATURE_NFC))
            mNfcSwitch.setVisibility(View.GONE);


        if(!getActivity().getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH) && !getActivity().getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE))
            mBluetoothSwitch.setVisibility(View.GONE);


        if(!getActivity().getPackageManager().hasSystemFeature(PackageManager.FEATURE_WIFI))

            mWifiSwitch.setVisibility(View.GONE);

        if(!getActivity().getPackageManager().hasSystemFeature(PackageManager.FEATURE_LOCATION)&&
                !getActivity().getPackageManager().hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS) &&
                !getActivity().getPackageManager().hasSystemFeature(PackageManager.FEATURE_LOCATION_NETWORK))
            mGpsSwitch.setVisibility(View.GONE);


        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
            powersaveSwitch.setVisibility(View.GONE);


        ConnectivityManager connManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connManager==null || connManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE) == null)
            mMobDataSwitch.setVisibility(View.GONE);



        if(isKitKatPlus())
            view.findViewById(R.id.delim2).setVisibility(View.GONE);
        else
            view.findViewById(R.id.delim1).setVisibility(View.GONE);

        invalidateSwitchers();

        int counterChecked = 0;

        SharedPreferences pref = PreferenceManager
                .getDefaultSharedPreferences(getActivity());

        if(pref.getBoolean(STATE_WIFI, STATE_WIFI_DEFVALUE)) counterChecked++;
        if(pref.getBoolean(STATE_BLUETOOTH, STATE_BT_DEFVALUE)) counterChecked++;
        if(pref.getBoolean(STATE_GPS, STATE_GPS_DEFVALUE)) counterChecked++;
        if(pref.getBoolean(STATE_MOBILEDATA, STATE_MOBDATA_DEFVALUE)) counterChecked++;
        if(pref.getBoolean(STATE_NFC, STATE_NFC_DEFVALUE)) counterChecked++;
        if(pref.getBoolean(STATE_BRIGHTNESS, STATE_BRIGHTNESS_DEFVALUE)) counterChecked++;
        if(pref.getBoolean(STATE_SYNC, STATE_SYNC_DEFVALUE)) counterChecked++;
        if(pref.getBoolean(STATE_PSM, STATE_PSM_DEFVALUE)) counterChecked++;
        if(pref.getBoolean(STATE_ORIENTATION, STATE_ORIENT_DEFVALUE)) counterChecked++;
        if(pref.getBoolean(STATE_AIRPLANEMODE, STATE_APM_DEFVALUE)) counterChecked++;


        int counterVisible = 0;
        if(mWifiSwitch.getVisibility() !=View.GONE) counterVisible++;
        if(mBluetoothSwitch.getVisibility() !=View.GONE) counterVisible++;
        if(mGpsSwitch.getVisibility() !=View.GONE) counterVisible++;
        if(mMobDataSwitch.getVisibility() !=View.GONE) counterVisible++;
        if(mNfcSwitch.getVisibility() !=View.GONE) counterVisible++;
        if(brightnesSwitch.getVisibility() !=View.GONE) counterVisible++;
        if(syncSwitch.getVisibility() !=View.GONE) counterVisible++;
        if(powersaveSwitch.getVisibility() !=View.GONE) counterVisible++;
        if(orientattionSwitch.getVisibility() !=View.GONE) counterVisible++;
        if(mAirplaneSwitch.getVisibility() !=View.GONE) counterVisible++;

        delta = 100/counterVisible;

        ((MainActivity)getActivity()).numericBar.setProgress(delta * counterChecked);
        pref.edit().putString("currentLevel", String.valueOf(delta * counterChecked)).commit();


        mAirplaneSwitch.setOnCheckedChangeListener(mListener);
        orientattionSwitch.setOnCheckedChangeListener(mListener);
        powersaveSwitch.setOnCheckedChangeListener(mListener);
        syncSwitch.setOnCheckedChangeListener(mListener);
        brightnesSwitch.setOnCheckedChangeListener(mListener);
        mNfcSwitch.setOnCheckedChangeListener(mListener);
        mMobDataSwitch.setOnCheckedChangeListener(mListener);
        mGpsSwitch.setOnCheckedChangeListener(mListener);
        mWifiSwitch.setOnCheckedChangeListener(mListener);
        mBluetoothSwitch.setOnCheckedChangeListener(mListener);

        return view;
    }


    private void invalidateSwitchers(){



        SharedPreferences pref = PreferenceManager
                .getDefaultSharedPreferences(getActivity());
        if(isKitKatPlus()) {

            SharedPreferences.Editor edit = pref.edit();

            if(pref.getBoolean(STATE_GPS, true))
            edit.putBoolean(STATE_GPS, !Utils.isGpsEnabled(getActivity()));

            if(pref.getBoolean(STATE_MOBILEDATA, true))
            edit.putBoolean(STATE_MOBILEDATA, !Utils.isMobile(getActivity()));

            if(pref.getBoolean(STATE_AIRPLANEMODE, true))
            edit.putBoolean(STATE_AIRPLANEMODE, Utils.isAirplaneMode(getActivity()));

            if(pref.getBoolean(STATE_NFC, true))
            edit.putBoolean(STATE_NFC, !Utils.isNfcEnabled(getActivity()));

            if(pref.getBoolean(STATE_PSM, true))
            edit.putBoolean(STATE_PSM, Utils.isPowerMode(getActivity()));

            edit.commit();
        }



        mWifiSwitch.setChecked(pref.getBoolean(STATE_WIFI, STATE_WIFI_DEFVALUE));
        mBluetoothSwitch.setChecked(pref.getBoolean(STATE_BLUETOOTH, STATE_BT_DEFVALUE));

        mGpsSwitch.setChecked(pref.getBoolean(STATE_GPS, STATE_GPS_DEFVALUE));
        mMobDataSwitch.setChecked(pref.getBoolean(STATE_MOBILEDATA, STATE_MOBDATA_DEFVALUE));
        mNfcSwitch.setChecked(pref.getBoolean(STATE_NFC, STATE_NFC_DEFVALUE));

        brightnesSwitch.setChecked(pref.getBoolean(STATE_BRIGHTNESS, STATE_BRIGHTNESS_DEFVALUE));
        syncSwitch.setChecked(pref.getBoolean(STATE_SYNC, STATE_SYNC_DEFVALUE));
        powersaveSwitch.setChecked(pref.getBoolean(STATE_PSM, STATE_PSM_DEFVALUE));
        orientattionSwitch.setChecked(pref.getBoolean(STATE_ORIENTATION, STATE_ORIENT_DEFVALUE));

        mAirplaneSwitch.setChecked(pref.getBoolean(STATE_AIRPLANEMODE, STATE_APM_DEFVALUE));


    }
    private void showAlertDialog(String title, String text, int resid, DialogInterface.OnClickListener listenerOk,  DialogInterface.OnClickListener listenerCancel){
      new AlertDialog.Builder(getActivity()).setTitle(title).setMessage(text).setIcon(resid).setPositiveButton(getString(android.R.string.ok), listenerOk).setNegativeButton(getString(android.R.string.cancel), listenerCancel).show();

    }
    private boolean isKitKatPlus(){ return Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;}

    private final CompoundButton.OnCheckedChangeListener mListener
            = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(final CompoundButton compoundButton, final boolean state) {

           final SharedPreferences pref = PreferenceManager
                    .getDefaultSharedPreferences(getActivity());
            final SharedPreferences.Editor edit = pref.edit();

            if(compoundButton.equals(mWifiSwitch)){
                edit.putBoolean(STATE_WIFI, state);

            }
            else
            if(compoundButton.equals(mBluetoothSwitch)){
                edit.putBoolean(STATE_BLUETOOTH, state);

            }
            if(compoundButton.equals(brightnesSwitch)){
                edit.putBoolean(STATE_BRIGHTNESS, state);

            }
            else
            if(compoundButton.equals(syncSwitch)){
                edit.putBoolean(STATE_SYNC, state);


            }
            else
            if(compoundButton.equals(orientattionSwitch)){
                edit.putBoolean(STATE_ORIENTATION, state);
               }





            else
            if(compoundButton.equals(mGpsSwitch)){
                    if(state && isKitKatPlus())
                    showAlertDialog(compoundButton.getText().toString(),
                            getString(R.string.manual_only),
                            R.drawable.ic_crosshairs_gps_grey600_24dp,
                            new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));

                        }
                    },
                      new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    mGpsSwitch.setChecked(!state);
                                    ((MainActivity)getActivity()).numericBar.setProgress((int) (((MainActivity) getActivity()).numericBar.getProgress() - delta));
                                    edit.putString("currentLevel", String.valueOf((int) (((MainActivity) getActivity()).numericBar.getProgress() - delta)));
                                }
                            });
                    edit.putBoolean(STATE_GPS, state);


            }
            else
            if(compoundButton.equals(mMobDataSwitch)){

                if(state && (isKitKatPlus()))
                    showAlertDialog(compoundButton.getText().toString(),
                            getString(R.string.manual_only),
                            R.drawable.ic_swap_vertical_grey600_24dp,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent intent = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
                                    startActivity(intent);
                                }
                            },
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    mMobDataSwitch.setChecked(!state);
                                    ((MainActivity)getActivity()).numericBar.setProgress((int) (((MainActivity) getActivity()).numericBar.getProgress() - delta));
                                    edit.putString("currentLevel", String.valueOf((int) (((MainActivity) getActivity()).numericBar.getProgress() - delta)));
                                }
                            });


                edit.putBoolean(STATE_MOBILEDATA, state);

            }
            else
            if(compoundButton.equals( mAirplaneSwitch)) {

                if(state && isKitKatPlus())
                    showAlertDialog(compoundButton.getText().toString(), getString(R.string.manual_only), R.drawable.ic_airplane_grey600_24dp,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent intentAirplaneMode = new Intent(Settings.ACTION_AIRPLANE_MODE_SETTINGS);
                                    startActivity(intentAirplaneMode);
                                }
                            },
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    mAirplaneSwitch.setChecked(!state);
                                    ((MainActivity)getActivity()).numericBar.setProgress((int) (((MainActivity) getActivity()).numericBar.getProgress() - delta));
                                    edit.putString("currentLevel", String.valueOf((int) (((MainActivity) getActivity()).numericBar.getProgress() - delta)));

                                }
                            });

                edit.putBoolean(STATE_AIRPLANEMODE, state);
            }







            else
            if(compoundButton.equals(mNfcSwitch)){
                if(state)
                showAlertDialog(compoundButton.getText().toString(),
                        getString(R.string.manual_only),
                        R.drawable.ic_nfc_grey600_24dp,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                    Intent intent = new Intent(Settings.ACTION_NFC_SETTINGS);
                                    startActivity(intent);

                            }
                        },
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            mNfcSwitch.setChecked(!state);
                                ((MainActivity)getActivity()).numericBar.setProgress((int) (((MainActivity) getActivity()).numericBar.getProgress() - delta));
                                edit.putString("currentLevel", String.valueOf((int) (((MainActivity) getActivity()).numericBar.getProgress() - delta)));
                            }
                        });

               edit.putBoolean(STATE_NFC, state);
            }
            else
            if(compoundButton.equals(powersaveSwitch)){
                if(state)
                showAlertDialog(compoundButton.getText().toString(), getString(R.string.manual_only), R.drawable.ic_battery_60_grey600_24dp,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(Settings.ACTION_BATTERY_SAVER_SETTINGS);
                                startActivity(intent);
                            }
                        },
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                 powersaveSwitch.setChecked(false);
                                ((MainActivity)getActivity()).numericBar.setProgress((int) (((MainActivity) getActivity()).numericBar.getProgress() - delta));
                                edit.putString("currentLevel", String.valueOf((int) (((MainActivity) getActivity()).numericBar.getProgress() - delta)));

                            }
                        });

                edit.putBoolean(STATE_PSM, state);
            }





           if(state) {
               ((MainActivity) getActivity()).numericBar.setProgress((int) (((MainActivity) getActivity()).numericBar.getProgress() + delta));
                edit.putString("currentLevel", String.valueOf((int) (((MainActivity) getActivity()).numericBar.getProgress() + delta)));
           }
            else {
               ((MainActivity)getActivity()).numericBar.setProgress((int)(((MainActivity) getActivity()).numericBar.getProgress() - delta));
                edit.putString("currentLevel", String.valueOf((int) (((MainActivity) getActivity()).numericBar.getProgress() - delta)));
           }

            edit.commit();


            final String message = getString(R.string.you_switched,
                    compoundButton.getText(),
                    state ? "on" : "off");
            if (getView() != null) {
                Snackbar.make(getView(), message, Snackbar.LENGTH_SHORT)
                        .show();
            }
        }
    };
}

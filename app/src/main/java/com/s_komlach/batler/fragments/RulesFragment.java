package com.s_komlach.batler.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.NumberPicker;
import com.s_komlach.batler.Constants;
import com.s_komlach.batler.R;

public class RulesFragment extends Fragment implements Constants{

    private SwitchCompat chargingSwitch, smartModeSwitcher;
    Button btn1, btn2;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_rules, container, false);

        final SharedPreferences pref = PreferenceManager
                .getDefaultSharedPreferences(getActivity());

        chargingSwitch = (SwitchCompat) view.findViewById(R.id.chargingSwitch);
        chargingSwitch.setOnCheckedChangeListener(mListener);
        smartModeSwitcher = (SwitchCompat) view.findViewById(R.id.smartModeSwitch);
        smartModeSwitcher.setOnCheckedChangeListener(mListener);

        btn1 = (Button) view.findViewById(R.id.button1);
        btn1.setText(getString(R.string.wakeup_timeout, pref.getInt(STATE_WAKEUP, STATE_WAKEUP_DEFVALUE)));
        btn2 = (Button) view.findViewById(R.id.button2);
        btn2.setText(getString(R.string.standby_timeout, pref.getInt(STATE_STANDBY, STATE_STANBY_DEFVALUE)));

        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                View view  = LayoutInflater.from(getActivity()).inflate(R.layout.numberpicker, null);
                final NumberPicker np = (NumberPicker) view.findViewById(R.id.numberPicker1);
                np.setMaxValue(60);
                np.setMinValue(1);
                np.setValue(pref.getInt(STATE_WAKEUP, STATE_WAKEUP_DEFVALUE));
                np.setWrapSelectorWheel(false);
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(getString(R.string.minutes));
                builder.setView(view);
                builder.setPositiveButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which){
                        int value = np.getValue();
                        btn1.setText(getString(R.string.wakeup_timeout, value));
                        pref.edit().putInt(STATE_WAKEUP, value).commit();
                        dialog.dismiss();
                    }
                });
                builder.show();

            }
        });

        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                View view  = LayoutInflater.from(getActivity()).inflate(R.layout.numberpicker, null);
                final NumberPicker np = (NumberPicker) view.findViewById(R.id.numberPicker1);
                np.setMaxValue(5);
                np.setMinValue(1);
                np.setValue(pref.getInt(STATE_STANDBY, STATE_STANBY_DEFVALUE));
                np.setWrapSelectorWheel(false);
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(getString(R.string.minutes));
                builder.setView(view);
                builder.setPositiveButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int value = np.getValue();
                        btn2.setText(getString(R.string.standby_timeout, value));
                        pref.edit().putInt(STATE_STANDBY, value).commit();
                        dialog.dismiss();
                    }
                });
                builder.show();

            }
        });


        chargingSwitch.setChecked(pref.getBoolean(STATE_CHARGING, STATE_CHARG_DEFVALUE));

        smartModeSwitcher.setChecked(pref.getBoolean(STATE_SMART, STATE_SMART_DEFVALUE));
        return view;
    }

    private final CompoundButton.OnCheckedChangeListener mListener
            = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean state) {
            SharedPreferences pref = PreferenceManager
                    .getDefaultSharedPreferences(getActivity());
            SharedPreferences.Editor edit = pref.edit();

            if(compoundButton.equals(chargingSwitch)){
                edit.putBoolean(STATE_CHARGING, state);
            }
            if(compoundButton.equals(smartModeSwitcher)){
                edit.putBoolean(STATE_SMART, state);
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
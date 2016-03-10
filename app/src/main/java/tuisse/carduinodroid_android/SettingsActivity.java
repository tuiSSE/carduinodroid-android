package tuisse.carduinodroid_android;


import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.support.v7.app.ActionBar;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MenuItem;
import android.support.v4.app.NavUtils;

import java.util.List;

import tuisse.carduinodroid_android.data.BluetoothHandling;
import tuisse.carduinodroid_android.data.ControlMode;
import tuisse.carduinodroid_android.data.SerialType;

/**
 * <h1>Settings Activity</h1>
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p/>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 *
 * @author Till Max Schwikal
 * @version 1.0
 * @since 08.12.2015
 */
public class SettingsActivity extends AppCompatPreferenceActivity {
    private static final String TAG = "CarduinoSettings";

    private static CarduinodroidApplication carduino = null;

    private static Integer tryStringToInt(String value, Integer defaultValue){
        Integer intValue;
        try {
            intValue = Integer.valueOf(value);
        }
        catch (NumberFormatException e){
            Log.e(TAG, "NumberFormatExeption: "+ e.toString());
            intValue = defaultValue;
        }
        return intValue;
    }

    /**
     * Helper method to determine if the device has an extra-large screen. For
     * example, 10" tablets are extra-large.
     */
    private static boolean isXLargeTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }

    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */
    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            try {
                String stringValue = value.toString();
                if (carduino != null) {
                    Integer intValue;
                    switch (preference.getKey()) {
                        case "pref_key_control_mode":
                            intValue = tryStringToInt(stringValue, ControlMode.toInteger(ControlMode.TRANSCEIVER));
                            carduino.dataHandler.setControlMode(ControlMode.fromInteger(intValue));
                            Log.d(TAG, preference.getKey() + ": " + carduino.dataHandler.getControlMode().toString());
                            break;
                        case "pref_key_reset_battery":
                            intValue = tryStringToInt(stringValue, 0);
                            carduino.dataHandler.getData().setResetAccCur(intValue);
                            Log.d(TAG, preference.getKey() + ": " + intValue);
                            break;
                        case "pref_key_screensaver":
                            intValue = tryStringToInt(stringValue, 0);
                            carduino.dataHandler.setScreensaver(intValue);
                            Log.d(TAG, preference.getKey() + ": " + intValue);
                            break;
                        case "pref_key_serial_type":
                            intValue = tryStringToInt(stringValue, SerialType.toInteger(SerialType.BLUETOOTH));
                            carduino.dataHandler.setSerialPref(SerialType.fromInteger(intValue));
                            Log.d(TAG, preference.getKey() + ": " + carduino.dataHandler.getSerialPref().toString());
                            /*
                            if(carduino.dataHandler.getSerialPref().isBluetooth()){
                                findPreference("pref_key_bluetooth_device_name").setEnabled(true);
                            }
                            else{
                                findPreference("pref_key_bluetooth_device_name").setEnabled(false);
                            }
                            */

                            break;
                        case "pref_key_bluetooth_device_name":
                            carduino.dataHandler.setBluetoothDeviceName(stringValue);
                            Log.d(TAG, preference.getKey() + ": " + carduino.dataHandler.getBluetoothDeviceName());
                            break;
                        case "pref_key_bluetooth_handling":
                            intValue = tryStringToInt(stringValue, BluetoothHandling.toInteger(BluetoothHandling.AUTO));
                            carduino.dataHandler.setBluetoothHandling(BluetoothHandling.fromInteger(intValue));
                            Log.d(TAG, preference.getKey() + ": " + carduino.dataHandler.getBluetoothHandling());
                            break;
                        case "pref_key_failsafe_stop":
                            intValue = tryStringToInt(stringValue, 1);
                            if (intValue == 1) {
                                carduino.dataHandler.setFailSafeStopPref(true);
                            } else {
                                carduino.dataHandler.setFailSafeStopPref(false);
                            }
                            Log.d(TAG, preference.getKey() + ": " + carduino.dataHandler.getFailSafeStopPref());
                            break;
                        case "pref_key_debug_view":
                            intValue = tryStringToInt(stringValue, 1);
                            if (intValue == 1) {
                                carduino.dataHandler.setDebugView(true);
                            } else {
                                carduino.dataHandler.setDebugView(false);
                            }
                            Log.d(TAG, preference.getKey() + ": " + carduino.dataHandler.getDebugView());
                            break;

                        default:
                            Log.d(TAG, "key not known: " + preference.getKey());
                            break;

                    }
                } else {
                    Log.e(TAG, "no pointer to resource carduino");
                }
                if (preference instanceof ListPreference) {
                    // For list preferences, look up the correct display value in
                    // the preference's 'entries' list.
                    ListPreference listPreference = (ListPreference) preference;
                    int index = listPreference.findIndexOfValue(stringValue);

                    // Set the summary to reflect the new value.
                    preference.setSummary(
                            index >= 0
                                    ? listPreference.getEntries()[index]
                                    : null);

                }else{
                    // For all other preferences, set the summary to the value's
                    // simple string representation.
                    preference.setSummary(stringValue);
                }
            }catch (Exception e){
                Log.e(TAG, e.toString());
            }
            return true;
        }
    };

    /**
     * Binds a preference's summary to its value. More specifically, when the
     * preference's value is changed, its summary (line of text below the
     * preference title) is updated to reflect the value. The summary is also
     * immediately updated upon calling this method. The exact display format is
     * dependent on the type of preference.
     *
     * @see #sBindPreferenceSummaryToValueListener
     */
    private static void bindPreferenceSummaryToValue(Preference preference) {
        try {
            // Set the listener to watch for value changes.
            preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

            // Trigger the listener immediately with the preference's
            // current value.
            sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                    PreferenceManager
                            .getDefaultSharedPreferences(preference.getContext())
                            .getString(preference.getKey(), ""));
        }catch (Exception e){
            Log.e(TAG,e.toString());
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        carduino = (CarduinodroidApplication) getApplication();
        setupActionBar();
    }

    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        else{
            Log.d(TAG, "no Action bar");
        }
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            if (!super.onMenuItemSelected(featureId, item)) {
                NavUtils.navigateUpFromSameTask(this);
            }
            return true;
        }
        return super.onMenuItemSelected(featureId, item);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onIsMultiPane() {
        return isXLargeTablet(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.pref_headers, target);
    }

    /**
     * This method stops fragment injection in malicious applications.
     * Make sure to deny any unknown fragments here.
     */
    protected boolean isValidFragment(String fragmentName) {
        return PreferenceFragment.class.getName().equals(fragmentName)
                || GeneralPreferenceFragment.class.getName().equals(fragmentName)
         //       || DataSyncPreferenceFragment.class.getName().equals(fragmentName)
                || SerialPreferenceFragment.class.getName().equals(fragmentName);
    }

    /**
     * This fragment shows general preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class GeneralPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_general);
            setHasOptionsMenu(true);

            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.

            bindPreferenceSummaryToValue(findPreference("pref_key_control_mode"));
            bindPreferenceSummaryToValue(findPreference("pref_key_debug_view"));
            bindPreferenceSummaryToValue(findPreference("pref_key_reset_battery"));
            bindPreferenceSummaryToValue(findPreference("pref_key_screensaver"));
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }

    /**
     * This fragment shows notification preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class SerialPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_serial);
            setHasOptionsMenu(true);

            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.
            bindPreferenceSummaryToValue(findPreference("pref_key_serial_type"));
            bindPreferenceSummaryToValue(findPreference("pref_key_bluetooth_device_name"));
            bindPreferenceSummaryToValue(findPreference("pref_key_bluetooth_handling"));
            bindPreferenceSummaryToValue(findPreference("pref_key_failsafe_stop"));
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }
    /**
     * This fragment shows data and sync preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    /*
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class DataSyncPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_data_sync);
            setHasOptionsMenu(true);

            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.
            bindPreferenceSummaryToValue(findPreference("sync_frequency"));
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }
    */
}

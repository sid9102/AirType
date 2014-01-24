package co.sidhant.airtype;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.input.InputManager;
import android.os.Bundle;
import android.os.Handler;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.provider.Settings;
import android.util.Log;
import android.view.InputDevice;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import java.io.File;

/**
 * Created by pfista on 1/21/14.
 */
public class AirTypeIMESettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    public static final String AIR_PREF_INITIALIZED = "air_prefInitialized";
    private Preference stepOnePref;
    private Preference stepTwoPref;
    private Preference stepThreePref;
    private Preference stepFourPref;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        final Context context = getActivity().getApplicationContext();

        stepOnePref = findPreference("stepone");
        stepTwoPref = findPreference("steptwo");
        stepThreePref = findPreference("stepthree");
        stepFourPref = findPreference("stepfour");

        assert stepOnePref != null && stepTwoPref != null && stepThreePref != null && stepFourPref != null;

        // Allow the preferences to be grayed out if they aren't enabled
        stepOnePref.setShouldDisableView(true);
        stepTwoPref.setShouldDisableView(true);
        stepThreePref.setShouldDisableView(true);
        stepFourPref.setShouldDisableView(true);

        stepOnePref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                // TODO: launch an activity or something
                Intent intent = new Intent(context, AirTypeInitActivity.class);
                startActivity(intent);
                return true;
            }
        });
        stepTwoPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(Settings.ACTION_INPUT_METHOD_SETTINGS);
                startActivity(intent);
                return true;
            }
        });
        stepThreePref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                InputMethodManager imeManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imeManager != null) {
                    imeManager.showInputMethodPicker();
                } else {
                    Toast.makeText(context, "Error", Toast.LENGTH_LONG).show();
                }
                return true;
            }
        });
        stepFourPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                // TODO: launch an activity or something
                Intent intent = new Intent(context, CalibrateActivity.class);
                startActivity(intent);
                return true;
            }
        });

        // TODO: Set completion status of each step and adjust UI accordingly
    }

    @Override
    public void onResume() {
        super.onResume();

        Log.e("FRAGMENT", "resuming IME Frag");
        File wordbits = getActivity().getApplicationContext().getFileStreamPath("wordBits.ser");

        String currentKeyboard =  Settings.Secure.getString(getActivity().getContentResolver(), Settings.Secure.DEFAULT_INPUT_METHOD);
        String enabled = Settings.Secure.getString(getActivity().getContentResolver(), Settings.Secure.ENABLED_INPUT_METHODS);

        /* This is the logic to determine what steps should be enabled or disabled for AirType setup */
        if (!wordbits.exists()){
            // Doesn't have Wordbits, so we must initialize first. Steps 2-4 should be disabled until
            // this is done
            stepOnePref.setEnabled(true);
            stepTwoPref.setEnabled(false);
            stepThreePref.setEnabled(false);
            stepFourPref.setEnabled(false);

            Log.e("FRAGMENT", "wordbits doesn't exist");
        }
        else {
            // Already initialized, only step 1 should be disabled
            stepOnePref.setEnabled(false);
            stepOnePref.setSummary("AirType has been initialized successfully");

            // Allow the user to enable the AirType keyboard if it ins't already enabled
            if (enabled != null && enabled.contains(".AirTypeIME")){
                stepTwoPref.setEnabled(false);
                stepTwoPref.setSummary("AirType has been enabled");
            }
            else {
                stepTwoPref.setEnabled(true);
            }

            // Allow the user to select AirType if it isn't already selected
            if (currentKeyboard != null && currentKeyboard.equals(".AirTypeIME")){
                stepThreePref.setEnabled(false);
                stepThreePref.setSummary("AirType is your current keyboard");
                /* TODO: look into implementing InputManager.InputDeviceListener or using InputManager
                to see when the user changes the input method. I couldn't get this to work so far, it wasn't
                registering any changes...
                */
            }
            else {
                stepThreePref.setEnabled(true);
            }
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        // TODO: Listen here for updates to the preferences, update state of preferences accordingly

        if (key.equals(AIR_PREF_INITIALIZED)){
            Preference init_pref = findPreference(AIR_PREF_INITIALIZED);
            assert init_pref != null;
            init_pref.setSummary("Initialization complete");
            init_pref.setSelectable(false);
        }
    }
}

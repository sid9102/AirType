package co.sidhant.airtype;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.provider.Settings;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

/**
 * Created by pfista on 1/21/14.
 */
public class AirTypeIMESettingsFragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        final Context context = getActivity().getApplicationContext();

        Preference stepOne = findPreference("stepone");
        Preference stepTwo = findPreference("steptwo");
        Preference stepThree = findPreference("stepthree");
        Preference stepFour = findPreference("stepfour");

        assert stepOne != null;
        assert stepTwo != null;
        assert stepThree != null;
        assert stepFour != null;

        stepOne.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                // TODO: launch an activity or something
                Intent intent = new Intent(context, AirTypeInitActivity.class);
                startActivity(intent);
                return true;
            }
        });
        stepTwo.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = new Intent(Settings.ACTION_INPUT_METHOD_SETTINGS);
                startActivity(intent);
                return true;
            }
        });
        stepThree.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
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
        stepFour.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
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
}

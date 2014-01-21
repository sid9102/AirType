package co.sidhant.airtype;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;

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
        stepOne.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                // TODO: launch an activity or something
                Intent intent = new Intent(context, AirTypeInitActivity.class);
                startActivity(intent);
                return true;
            }
        });
    }
}

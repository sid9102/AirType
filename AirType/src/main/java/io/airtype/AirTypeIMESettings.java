package io.airtype;

import android.app.Activity;
import android.os.Bundle;


public class AirTypeIMESettings extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Display the settings fragment as the main content
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new AirTypeIMESettingsFragment()).commit();

        // TODO: add steps here for calibration, requirements for airtype to work like swype does
    }


}


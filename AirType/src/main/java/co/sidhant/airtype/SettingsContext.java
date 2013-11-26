package co.sidhant.airtype;

import android.content.Context;

/**
 * Created by sid9102 on 11/25/13.
 */
// This singleton allows the settings activity to pass a context back to the service
public class SettingsContext
{
    private static SettingsContext instance = null;
    public static Context mContext;

    protected SettingsContext() {
        // Exists only to defeat instantiation.
    }
    public static SettingsContext getInstance() {
        if(instance == null) {
            instance = new SettingsContext();
        }
        return instance;
    }
}
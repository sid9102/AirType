package io.airtype;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.Preference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by pfista on 1/21/14.
 */
public class AirTypeInitPreference extends Preference {

    private final boolean DEFAULT_VALUE = false;
    private boolean mInitialized;

    public AirTypeInitPreference (Context context, AttributeSet attributeSet){
        super(context, attributeSet);

    }

    @Override
    protected View onCreateView(ViewGroup parent) {
        return super.onCreateView(parent);
    }

    /* Saving the setting's value */
    @Override
    protected void onClick() {
        // TODO: run the airtype init procedure found in airtype init activity
        // update UI progress dialogue accordingly
        // and if successful: persistBoolean(true);
        Log.i("pref", "Clicked...");
        super.onClick();
    }

    /* Initializing the current value */
    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        super.onSetInitialValue(restorePersistedValue, defaultValue);

        if (restorePersistedValue){
            // Restore existing state
            mInitialized = this.getPersistedBoolean(DEFAULT_VALUE);
        }
        else {
            // Set default states from the XML attribute
            mInitialized = (Boolean)defaultValue;
            persistBoolean(mInitialized);
        }
    }

    /* Providing a default value */
    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getBoolean(index, DEFAULT_VALUE);
    }

    /* Saving and restoring the preference's state */
    @Override
    protected Parcelable onSaveInstanceState() {
        final Parcelable superState = super.onSaveInstanceState();

        if (isPersistent()){
            return superState;
        }

        final SavedState myState = new SavedState(superState);
        myState.value = mInitialized;
        return myState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        // Check whether we saved the state in onSaveInstanceState
        if (state == null || !state.getClass().equals(SavedState.class)) {
            // Didn't save the state, so call superclass
            super.onRestoreInstanceState(state);
            return;
        }

        // Cast state to custom BaseSavedState and pass to superclass
        SavedState myState = (SavedState) state;
        super.onRestoreInstanceState(myState.getSuperState());

        // TODO: Set this Preference's widget to reflect the restored state

    }

    private static class SavedState extends BaseSavedState {
        // Member that holds the setting's value
        // Change this data type to match the type saved by your Preference
        boolean value;

        public SavedState(Parcelable superState) {
            super(superState);
        }

        public SavedState(Parcel source) {
            super(source);
            // Get the current preference's value
            value = source.readByte() != 0; // Change this to read the appropriate data type
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            // Write the preference's value
            dest.writeByte((byte) (value ? 1: 0));  // Change this to write the appropriate data type
        }

        // Standard creator object using an instance of this class
        public static final Parcelable.Creator<SavedState> CREATOR =
                new Parcelable.Creator<SavedState>() {

                    public SavedState createFromParcel(Parcel in) {
                        return new SavedState(in);
                    }

                    public SavedState[] newArray(int size) {
                        return new SavedState[size];
                    }
                };
    }
}



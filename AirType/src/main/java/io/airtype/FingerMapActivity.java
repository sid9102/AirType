package io.airtype;

import android.app.Activity;
import android.app.Fragment;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

public class FingerMapActivity extends Activity {

    private static int mKeyPresses;
    private static TextView mTextView;
    private static EditText mEditText;
    private static SpannableStringBuilder mSpanText;

    @Override
    protected void onResume() {
        super.onResume();

        CharSequence t = mTextView.getText();
        if (t == null){
            Log.e("Fingermap", "text view is null");
        }
        else {
            mSpanText = new SpannableStringBuilder(t.toString());
            mSpanText.setSpan(new ForegroundColorSpan(Color.RED), 0, mKeyPresses, 0);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calibrate);

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }

        mKeyPresses = 0;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.calibrate, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_calibrate, container, false);

            mEditText = (EditText)rootView.findViewById(R.id.finger_input);
            mTextView = (TextView)rootView.findViewById(R.id.mapping_text);

            mEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    Log.i("Fingermap", "TextChanged: " + s + " keypresses " + mKeyPresses);
                    mKeyPresses++;
                    mSpanText.setSpan(new ForegroundColorSpan(Color.RED), 0, mKeyPresses, 0);
                    mTextView.setText(mSpanText, TextView.BufferType.SPANNABLE);

                    if (start != 0)
                        mEditText.setText("");
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });

            return rootView;
        }
    }

}

package io.airtype;

import android.content.Context;
import android.content.Intent;
import android.inputmethodservice.InputMethodService;
import android.os.IBinder;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


/**
 * Created by sid9102 on 11/19/13.
 */
public class AirTypeIME extends InputMethodService
{
    private final String TAG = "co.sidhant.airtype.AIRTYPE";

    // Soft-keyboard view for user input
    private AirTypeView mKeyboardView;

    // Shows word suggestions above the keyboard
    private CandidateView mCandidateView;

    // The suggested word the user is typing
    private StringBuilder mComposing;

    // List of words sorted by frequency that match the current word fragment
    private ArrayList<String> candidatesList;

    // Used for fast lookup of words given an integer which represents a finger
    private static EncodedTrie eTrie;

    // Used for capitalizing the first word of new sentences
    private boolean newSentence;

    @Override
    public void onCreate() {
        super.onCreate();

        candidatesList = new ArrayList<String>();
        newSentence = true;
        mComposing = new StringBuilder();
        initializeTrie();
    }

    private void initializeTrie() {
        Context context = getApplicationContext();

        File wordbits = getApplicationContext().getFileStreamPath("wordBits.ser");
        if (wordbits.exists()){
            // The file exists, so load the encoded trie into memory
            try {
                eTrie = new EncodedTrie(context);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        } else {
            Log.d(TAG, "Wordbits has not been initialized yet");

            // Reset to the old input method until we are done initializing. The user can change to
            // the new one in the settings activity
            try {
                InputMethodManager imm = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
                final IBinder token = this.getWindow().getWindow().getAttributes().token;
                imm.switchToLastInputMethod(token);
            } catch (Throwable t) { // java.lang.NoSuchMethodError if API_level<11
                Log.e(TAG, "cannot set the previous input method:");
                t.printStackTrace();
            }

            // Word bits didn't exist so we need to create it
            Intent intent = new Intent(this, AirTypeIMESettings.class);
            intent.putExtra("Wordbits", false);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }

    @Override
    public View onCreateInputView() {
        mKeyboardView = (AirTypeView) getLayoutInflater().inflate(R.layout.airtype, null);
        mKeyboardView.mOneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onKeyDown(KeyEvent.KEYCODE_1, new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_1));
            }
        });
        mKeyboardView.mTwoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onKeyDown(KeyEvent.KEYCODE_2,new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_2) );
            }
        });
        mKeyboardView.mThreeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onKeyDown(KeyEvent.KEYCODE_3,new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_3) );
            }
        });
        mKeyboardView.mFourButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onKeyDown(KeyEvent.KEYCODE_4,new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_4) );
            }
        });
        mKeyboardView.mFiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onKeyDown(KeyEvent.KEYCODE_5,new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_5) );
            }
        });
        mKeyboardView.mSixButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onKeyDown(KeyEvent.KEYCODE_6,new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_6) );
            }
        });
        mKeyboardView.mSevenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onKeyDown(KeyEvent.KEYCODE_7,new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_7) );
            }
        });
        mKeyboardView.mEightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onKeyDown(KeyEvent.KEYCODE_8,new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_8) );
            }
        });

        return mKeyboardView;
    }

    @Override
    public View onCreateCandidatesView() {
        mCandidateView  = new CandidateView(this);
        mCandidateView.setService(this);
        setCandidatesViewShown(true);
        return mCandidateView;
    }

    /**
     * Called to inform the input method that text input has started in an
     * editor.  You should use this callback to initialize the state of your
     * input to match the state of the editor given to it.
     *
     * @param attribute The attributes of the editor that input is starting
     * in.
     * @param restarting Set to true if input is restarting in the same
     * editor such as because the application has changed the text in
     * the editor.  Otherwise will be false, indicating this is a new
     * session with the editor.
     */
    @Override
    public void onStartInput(EditorInfo attribute, boolean restarting) {
        // TODO: use attribute and restarting correctly
        super.onStartInput(attribute, restarting);
        mComposing.setLength(0);
        if (eTrie != null) {
            eTrie.resetCurNode();
        } else {
            initializeTrie();
        }
    }
    /**
     * Update the list of available candidates from the current composing
     * text.  This will need to be filled in by however you are determining
     * candidates.
     */
    private void updateCandidates() {
        if (mComposing.length() > 0) {
            candidatesList = new ArrayList<String>();
            candidatesList.add(mComposing.toString());
            candidatesList.addAll(eTrie.getAlts());
            setSuggestions(candidatesList, true, true);
        } else {
            setSuggestions(null, false, false);
        }
    }

    public void setSuggestions(List<String> suggestions, boolean completions,
                               boolean typedWordValid) {
        if (suggestions != null && suggestions.size() > 0) {
            setCandidatesViewShown(true);
        } else if (isExtractViewShown()) {
            setCandidatesViewShown(true);
        }
        if (mCandidateView != null) {
            mCandidateView.setSuggestions(suggestions, completions, typedWordValid);
        }
    }

    /**
     * This is called when the user is done editing a field.  We can use
     * this to reset our state.
     */
    @Override public void onFinishInput() {
        super.onFinishInput();

        // Clear current composing text and candidates.
        mComposing.setLength(0);
        updateCandidates();
        if (eTrie != null) eTrie.resetCurNode();
        // We only hide the candidates window when finishing input on
        // a particular editor, to avoid popping the underlying application
        // up and down if the user is entering text into the bottom of
        // its window.
        setCandidatesViewShown(false);

    }

    public void pickSuggestion(int index) {
        // TODO: fix this
        String selection;
        selection = candidatesList.get(index);
        if(selection.equals("i") || newSentence)
        {
            selection = selection.substring(0, 1).toUpperCase() + selection.substring(1);
        }
        newSentence = false;
        InputConnection ic = getCurrentInputConnection();
        if(ic!= null)
            ic.commitText(selection + " ", selection.length() + 1);
        mComposing.setLength(0);
        updateCandidates();
        eTrie.resetCurNode();
        if (mCandidateView != null) {
            mCandidateView.clear();
        }

    }

    /**
     * Use this to monitor key events being delivered to the application.
     * We get first crack at them, and can either resume them or let them
     * continue to the app.
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        super.onKeyDown(keyCode, event);
        switch (keyCode) {
            case KeyEvent.KEYCODE_DEL:
                // Special handling of the delete key: if we currently are
                // composing text for the user, we want to modify that instead
                // of let the application to the delete itself.
                if (mComposing.length() > 0) {
                    handleBackspace();
                    return false;
                }

            case KeyEvent.KEYCODE_ENTER:
                // Let the underlying text editor always handle these.
                return false;

            // Take input from the fingers
            case KeyEvent.KEYCODE_1:
            case KeyEvent.KEYCODE_2:
            case KeyEvent.KEYCODE_3:
            case KeyEvent.KEYCODE_4:
            case KeyEvent.KEYCODE_5:
            case KeyEvent.KEYCODE_6:
            case KeyEvent.KEYCODE_7:
            case KeyEvent.KEYCODE_8:
                handleFinger(keyCode - 8);
                return true;

            // Don't do anything with built in buttons!
            case KeyEvent.KEYCODE_BACK:
            case KeyEvent.KEYCODE_MENU:
                return false;

            default:
                return true;
        }

    }

    private void handleBackspace() {
        final int length = mComposing.length();
        InputConnection ic = getCurrentInputConnection();
        if (length > 1) {
            mComposing.delete(length - 1, length);
            if(ic != null)
                ic.setComposingText(mComposing, 1);
            updateCandidates();
        } else if (length > 0) {
            mComposing.setLength(0);
            if(ic != null)
                ic.commitText("", 0);
            updateCandidates();
        }
    }

    @Override public boolean onKeyUp(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
            case KeyEvent.KEYCODE_MENU:
                return false;
            default:
                return true;
        }
    }

    public void handleFinger(int keyCode)
    {
        //Punctuation!
        if(eTrie.isEndOfWord())
        {
            if (!eTrie.hasChildren())
            {
                //user wants a comma!
                if(keyCode == 5)
                {
                    mComposing = new StringBuilder(eTrie.getWord() + ",");
                    pickSuggestion(0);
                    return;
                } // user wants a period
                else if(keyCode == 6)
                {
                    mComposing = new StringBuilder(eTrie.getWord() + ".");
                    newSentence = true;
                    pickSuggestion(0);
                    return;
                }
            }
        }

        if(eTrie.goToChild(keyCode))
        {
            mComposing = new StringBuilder(eTrie.getWord());
            updateCandidates();
            InputConnection ic = getCurrentInputConnection();
            if(ic != null)
                ic.setComposingText(mComposing, 1);
        }
        else
        {
            if (mComposing.length() > 0) {
                pickSuggestion(0);
            }
        }
    }

    public static class stringLengthComparator implements Comparator<String> {

        @Override
        public int compare(String s1, String s2) {
            if(s1.length() > s2.length())
            {
                return 1;
            }
            else if(s1.length() < s2.length())
            {
                return -1;
            }
            else
                return s1.compareTo(s2);
        }
    }
}
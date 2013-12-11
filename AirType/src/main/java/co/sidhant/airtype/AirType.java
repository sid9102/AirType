package co.sidhant.airtype;

import android.content.Context;
import android.content.Intent;
import android.inputmethodservice.InputMethodService;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.CompletionInfo;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Created by sid9102 on 11/19/13.
 */
public class AirType extends InputMethodService
{
    private AirTypeView mAirTypeView;

    private CandidateView mCandidateView;
    private CompletionInfo[] mCompletions;
    private String mWordSeparators;
    private StringBuilder mComposing = new StringBuilder();
    private ArrayList<String> candidatesList;
    public FingerMap fMap = new FingerMap();
    private static EncodedTrie eTrie;

    @Override
    public void onCreate() {
        super.onCreate();
        candidatesList = new ArrayList<String>();

        boolean firstRun = true;
        Context mContext = getApplicationContext();
        try
        {
            assert mContext != null;
            mContext.openFileInput("wordBits.ser");
            firstRun = false;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        if(firstRun)
        {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        }
        else
        {
            try {
                eTrie = new EncodedTrie(mContext);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public View onCreateInputView() {
        mAirTypeView = (AirTypeView) getLayoutInflater().inflate(R.layout.airtype, null);
        mAirTypeView.mOneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onKeyDown(KeyEvent.KEYCODE_1,new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_1) );
            }
        });
        mAirTypeView.mTwoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onKeyDown(KeyEvent.KEYCODE_2,new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_2) );
            }
        });
        mAirTypeView.mThreeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onKeyDown(KeyEvent.KEYCODE_3,new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_3) );
            }
        });
        mAirTypeView.mFourButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onKeyDown(KeyEvent.KEYCODE_4,new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_4) );
            }
        });
        mAirTypeView.mFiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onKeyDown(KeyEvent.KEYCODE_5,new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_5) );
            }
        });
        mAirTypeView.mSixButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onKeyDown(KeyEvent.KEYCODE_6,new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_6) );
            }
        });
        mAirTypeView.mSevenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onKeyDown(KeyEvent.KEYCODE_7,new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_7) );
            }
        });
        mAirTypeView.mEightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onKeyDown(KeyEvent.KEYCODE_8,new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_8) );
            }
        });

        mWordSeparators = getResources().getString(R.string.word_separators);
        return mAirTypeView;
    }

    @Override
    public View onCreateCandidatesView() {
        mCandidateView  = new CandidateView(this);
        mCandidateView.setService(this);
        setCandidatesViewShown(true);
        return mCandidateView;
    }

    @Override
    public void onStartInput(EditorInfo attribute, boolean restarting) {
        super.onStartInput(attribute, restarting);
        mComposing.setLength(0);
        mCompletions = null;
    }
    /**
     * Update the list of available candidates from the current composing
     * text.  This will need to be filled in by however you are determining
     * candidates.
     */
    private void updateCandidates() {

        // TODO: determine candidates here

        if (mComposing.length() > 0) {
            candidatesList = eTrie.getAlts();
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

        // We only hide the candidates window when finishing input on
        // a particular editor, to avoid popping the underlying application
        // up and down if the user is entering text into the bottom of
        // its window.
        setCandidatesViewShown(false);

    }

    public void pickSuggestion(int index) {
        // TODO: fix this
        String selection;
        if(index != 0)
        {
            selection = candidatesList.get(index);
        }
        else
        {
            selection = mComposing.toString();
        }
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
     * Helper function to commit any text being composed in to the editor.
     */
    private void commitTyped(InputConnection inputConnection) {
        if (mComposing.length() > 0) {
            inputConnection.commitText(mComposing, mComposing.length());
            mComposing.setLength(0);
            updateCandidates();
        }
    }

    private String getWordSeparators() {
        return mWordSeparators;
    }

    public boolean isWordSeparator(int code) {
        String separators = getWordSeparators();
        return separators.contains(String.valueOf((char)code));
    }


    /**
     * Use this to monitor key events being delivered to the application.
     * We get first crack at them, and can either resume them or let them
     * continue to the app.
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        super.onKeyDown(keyCode, event);
        char key = (char) event.getUnicodeChar();
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
                handleFinger(keyCode);
                return true;

            default:
                if (isWordSeparator(key)) {
                    // Handle separator
                    if (mComposing.length() > 0) {
                        mComposing.append(key);
                        // TODO: don't print number, print 0 position of candidates list!
                        pickSuggestion(0);
                    }
                    return true;
                }
                mComposing.append(key);
                //getCurrentInputConnection().setComposingText(mComposing, 1);
                updateCandidates();
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
            default:
                return true;
        }

    }

    public void handleFinger(int keyCode)
    {
        //Punctuation!
        if(eTrie.isEndOfWord())
        {
            if (!eTrie.goToChildPrecise(keyCode - 8))
            {
                //user wants a comma!
                if(keyCode - 8 == 6)
                {
                    mComposing = new StringBuilder(eTrie.getWord() + ",");
                    pickSuggestion(0);
                    return;
                } // user wants a period
                else if(keyCode - 8 == 7)
                {
                    mComposing = new StringBuilder(eTrie.getWord() + ".");
                    pickSuggestion(0);
                    return;
                }

            }
        }

        if(eTrie.goToChild(keyCode - 8))
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

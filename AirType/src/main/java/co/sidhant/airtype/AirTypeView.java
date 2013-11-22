package co.sidhant.airtype;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Button;
import android.widget.RelativeLayout;

import co.sidhant.airtype.R;

/**
 * Created by pfista on 11/15/13.
 */
public class AirTypeView extends RelativeLayout {

    private final Context mContext;
    protected Button mOneButton;
    protected Button mTwoButton;
    protected Button mThreeButton;
    protected Button mFourButton;
    protected Button mFiveButton;
    protected Button mSixButton;
    protected Button mSevenButton;
    protected Button mEightButton;
    public AirTypeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }

    @Override
    protected void onFinishInflate() {
        mOneButton = (Button) findViewById(R.id.one);
        mTwoButton = (Button) findViewById(R.id.two);
        mThreeButton = (Button) findViewById(R.id.three);
        mFourButton = (Button) findViewById(R.id.four);
        mFiveButton = (Button) findViewById(R.id.five);
        mSixButton = (Button) findViewById(R.id.six);
        mSevenButton = (Button) findViewById(R.id.seven);
        mEightButton = (Button) findViewById(R.id.eight);
        /*mSymbolsButton = (Button) findViewById(R.id.symbols_btn);
        mSymbolsButton.setOnClickListener(mButtonClickListener);
        mSymbolsButton.setOnLongClickListener(mButtonLongClickListener);
        mShiftButton = (Button) findViewById(R.id.shift_btn);
        mShiftButton.setOnClickListener(mButtonClickListener);
        mShiftButton.setOnLongClickListener(mButtonLongClickListener);
        mBackspaceButton = (Button) findViewById(R.id.backspace_btn);
        mBackspaceButton.setOnClickListener(mButtonClickListener);
        mBackspaceButton.setOnLongClickListener(mButtonLongClickListener);
        mSpaceButton = (Button) findViewById(R.id.space_btn);
        mSpaceButton.setOnClickListener(mButtonClickListener);
        mSpaceButton.setOnLongClickListener(mButtonLongClickListener);*/

    }

}

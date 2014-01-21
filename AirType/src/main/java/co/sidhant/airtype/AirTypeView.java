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
    }

}

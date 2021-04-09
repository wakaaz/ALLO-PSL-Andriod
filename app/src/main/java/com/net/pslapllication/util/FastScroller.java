package com.net.pslapllication.util;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import com.net.pslapllication.R;

/**
 * Created by Wencharm on 2/20/16.
 */
public class FastScroller extends View {
    public FastScroller(Context context) {
        super(context);
        init(context);
    }

    public FastScroller(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public FastScroller(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    // Touch event
    private OnTouchingLetterChangedListener onTouchingLetterChangedListener;
    public static String HEART = "HEART";
    public static String[] alphabetArray = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q",
            "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "#"};
    private int choose = -1;
    private Paint paint = new Paint();

    private TextView mTextDialog;
    private Context context;

    private float singleHeight;
    private Typeface regularFont;
    private Typeface iconFont;

    public void setTextView(TextView mTextDialog) {
        this.mTextDialog = mTextDialog;
    }

    public void init(Context context) {
        this.context = context;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            regularFont = getResources().getFont(R.font.lato_regular);
        }
        // iconFont = Typeface.createFromAsset(context.getApplicationContext().getAssets(), "icomoon.ttf");
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (getHeight() < context.getResources().getDisplayMetrics().heightPixels / 2) return;
        int height = getHeight();
        int width = getWidth();
        singleHeight = (height * 1f) / alphabetArray.length;
        singleHeight = (height * 1f - singleHeight / 2) / alphabetArray.length;
        paint.setColor(getResources().getColor(R.color.text_grey3));
         paint.setAntiAlias(true);
        paint.setTextSize(context.getResources().getDisplayMetrics().density * 10);
        for (int i = 0; i < alphabetArray.length; i++) {
            float xPos = width / 2 - paint.measureText(alphabetArray[i]) / 2;
            float yPos = singleHeight * i + singleHeight;
            if (alphabetArray[i].equals(HEART)) {
                //paint.setTypeface(iconFont);

              //  canvas.drawText(context.getString(R.string.icon_heart), width / 2 - paint.measureText(b[1]) / 2 - context.getResources().getDisplayMetrics().density * 2, singleHeight, paint);
            } else {
                paint.setTypeface(regularFont);
                canvas.drawText(alphabetArray[i], xPos, yPos, paint);
            }
        }

    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        final int action = event.getAction();
        final float y = event.getY();
        final int oldChoose = choose;
        final OnTouchingLetterChangedListener listener = onTouchingLetterChangedListener;
        final int c = (int) (y / getHeight() * alphabetArray.length);

        switch (action) {
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                choose = -1;
                if (mTextDialog != null) {
                    mTextDialog.setVisibility(View.INVISIBLE);
                    paint.setColor(getResources().getColor(R.color.text_grey3));

                }
                break;

            default:
                if (c >= 0 && c < alphabetArray.length) {
                    if (listener != null && oldChoose != c) {
                        listener.onTouchingLetterChanged(alphabetArray[c]);
                    }
                    if (mTextDialog != null) {
                        if (alphabetArray[c].equals(HEART)) {
                            //mTextDialog.setTypeface(iconFont);
                         //   mTextDialog.setText(context.getString(R.string.icon_heart));
                        } else {
                            mTextDialog.setTypeface(regularFont);
                            mTextDialog.setText(alphabetArray[c]);

                        }

                        mTextDialog.setVisibility(View.VISIBLE);
                        mTextDialog.setTranslationY(y < singleHeight * 2 ? 0 : y - singleHeight * 2);

                        paint.setColor(getResources().getColor(R.color.colorPrimaryDark));

                    }
                    choose = c;
                }
                break;
        }
        return true;
    }

    /**
     * @param onTouchingLetterChangedListener
     */
    public void setOnTouchingLetterChangedListener(OnTouchingLetterChangedListener onTouchingLetterChangedListener) {
        this.onTouchingLetterChangedListener = onTouchingLetterChangedListener;
    }

    public interface OnTouchingLetterChangedListener {
        void onTouchingLetterChanged(String s);
    }
}


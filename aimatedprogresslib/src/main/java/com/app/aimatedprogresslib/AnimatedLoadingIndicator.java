package com.app.aimatedprogresslib;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AnimationUtils;
import androidx.annotation.NonNull;
import com.app.aimatedprogresslib.Indicators.ProgressBallMultipleIndicator;

/*
 * Created by Yash on 06/08/19.
 */

/*
*   git tag -a "v1.0" -m "aimatedprogresslib version 1.0"

    git push origin v1.1
*/

public class AnimatedLoadingIndicator extends View
{
    private static final ProgressBallMultipleIndicator DEFAULT_INDICATOR = new ProgressBallMultipleIndicator();

    private static final int MIN_SHOW_TIME = 500; // ms
    private static final int MIN_DELAY = 500; // ms

    private long mStartTime = -1;

    private boolean mPostedHide = false;

    private boolean mPostedShow = false;

    private boolean mDismissed = false;

    private final Runnable mDelayedHide = new Runnable()
    {
        @Override
        public void run()
        {
            mPostedHide = false;
            mStartTime = -1;
            setVisibility(View.GONE);
        }
    };

    private final Runnable mDelayedShow = new Runnable()
    {
        @Override
        public void run()
        {
            mPostedShow = false;
            if (!mDismissed)
            {
                mStartTime = System.currentTimeMillis();
                setVisibility(View.VISIBLE);
            }
        }
    };

    int mMinWidth;
    int mMaxWidth;
    int mMinHeight;
    int mMaxHeight;

    private Indicator mIndicator;
    private int mIndicatorColor;

    private boolean mShouldStartAnimationDrawable;

    public AnimatedLoadingIndicator(Context context)
    {
        super(context);
        init(context, null,0,0);
    }

    public AnimatedLoadingIndicator(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        init(context, attrs,0, R.style.AnimatedLoadingIndicatorView);
    }

    public AnimatedLoadingIndicator(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
        init(context, attrs,defStyleAttr, R.style.AnimatedLoadingIndicatorView);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public AnimatedLoadingIndicator(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes)
    {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context,attrs,defStyleAttr,R.style.AnimatedLoadingIndicatorView);
    }

    private void init(Context context,AttributeSet attrs,int defStyleAttr, int defStyleRes)
    {
        mMinWidth = 24;
        mMaxWidth = 48;
        mMinHeight = 24;
        mMaxHeight = 48;

        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.AnimatedLoadingIndicatorView, defStyleAttr, defStyleRes);

        mMinWidth = a.getDimensionPixelSize(R.styleable.AnimatedLoadingIndicatorView_minWidth, mMinWidth);
        mMaxWidth = a.getDimensionPixelSize(R.styleable.AnimatedLoadingIndicatorView_maxWidth, mMaxWidth);
        mMinHeight = a.getDimensionPixelSize(R.styleable.AnimatedLoadingIndicatorView_minHeight, mMinHeight);
        mMaxHeight = a.getDimensionPixelSize(R.styleable.AnimatedLoadingIndicatorView_maxHeight, mMaxHeight);
        String indicatorName  =a.getString(R.styleable.AnimatedLoadingIndicatorView_indicatorName);
        mIndicatorColor = a.getColor(R.styleable.AnimatedLoadingIndicatorView_indicatorColor, Color.WHITE);
        setIndicator(indicatorName);
        if (mIndicator==null)
        {
            setIndicator(DEFAULT_INDICATOR);
        }
        a.recycle();
    }

    public Indicator getIndicator()
    {
        return mIndicator;
    }

    public void setIndicator(Indicator d)
    {
        if (mIndicator != d)
        {
            if (mIndicator != null)
            {
                mIndicator.setCallback(null);
                unscheduleDrawable(mIndicator);
            }

            mIndicator = d;

            setIndicatorColor(mIndicatorColor);
            if (d != null)
            {
                d.setCallback(this);
            }

            postInvalidate();
        }
    }


    /**
     * setIndicatorColor(0xFF00FF00)
     * or
     * setIndicatorColor(Color.BLUE)
     * or
     * setIndicatorColor(Color.parseColor("#FF4081"))
     * or
     * setIndicatorColor(0xFF00FF00)
     * or
     * setIndicatorColor(getResources().getColor(android.R.color.black))
     */
    public void setIndicatorColor(int color)
    {
        this.mIndicatorColor=color;
        mIndicator.setColor(color);
    }

    public void setIndicator(String indicatorName)
    {
        if (TextUtils.isEmpty(indicatorName))
        {
            return;
        }
        StringBuilder drawableClassName=new StringBuilder();
        if (!indicatorName.contains("."))
        {
            String defaultPackageName=getClass().getPackage().getName();
            drawableClassName.append(defaultPackageName)
                    .append(".indicators")
                    .append(".");
        }
        drawableClassName.append(indicatorName);
        try
        {
            Class<?> drawableClass = Class.forName(drawableClassName.toString());
            Indicator indicator = (Indicator) drawableClass.newInstance();
            setIndicator(indicator);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void smoothToShow()
    {
        startAnimation(AnimationUtils.loadAnimation(getContext(),android.R.anim.fade_in));
        setVisibility(VISIBLE);
    }

    public void smoothToHide()
    {
        startAnimation(AnimationUtils.loadAnimation(getContext(),android.R.anim.fade_out));
        setVisibility(GONE);
    }

    public void hide()
    {
        mDismissed = true;
        removeCallbacks(mDelayedShow);
        long diff = System.currentTimeMillis() - mStartTime;
        if (diff >= MIN_SHOW_TIME || mStartTime == -1)
        {
            setVisibility(View.GONE);
        }
        else
        {
            if (!mPostedHide)
            {
                postDelayed(mDelayedHide, MIN_SHOW_TIME - diff);
                mPostedHide = true;
            }
        }
    }

    public void show()
    {
        mStartTime = -1;
        mDismissed = false;
        removeCallbacks(mDelayedHide);
        if (!mPostedShow)
        {
            postDelayed(mDelayedShow, MIN_DELAY);
            mPostedShow = true;
        }
    }

    @Override
    protected boolean verifyDrawable(@NonNull Drawable who)
    {
        return who == mIndicator || super.verifyDrawable(who);
    }

    void startAnimation()
    {
        if (getVisibility() != VISIBLE)
        {
            return;
        }

        if (mIndicator != null)
        {
            mShouldStartAnimationDrawable = true;
        }
        postInvalidate();
    }

    void stopAnimation()
    {
        if (mIndicator != null)
        {
            mIndicator.stop();
            mShouldStartAnimationDrawable = false;
        }
        postInvalidate();
    }

    @Override
    public void setVisibility(int v)
    {
        if (getVisibility() != v)
        {
            super.setVisibility(v);
            if (v == GONE || v == INVISIBLE)
            {
                stopAnimation();
            }
            else
            {
                startAnimation();
            }
        }
    }

    @Override
    protected void onVisibilityChanged(@NonNull View changedView, int visibility)
    {
        super.onVisibilityChanged(changedView, visibility);
        if (visibility == GONE || visibility == INVISIBLE)
        {
            stopAnimation();
        }
        else
        {
            startAnimation();
        }
    }

    @Override
    public void invalidateDrawable(@NonNull Drawable dr)
    {
        if (verifyDrawable(dr))
        {
            final Rect dirty = dr.getBounds();
            final int scrollX = getScrollX() + getPaddingLeft();
            final int scrollY = getScrollY() + getPaddingTop();

            invalidate(dirty.left + scrollX, dirty.top + scrollY, dirty.right + scrollX, dirty.bottom + scrollY);
        }
        else
        {
            super.invalidateDrawable(dr);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh)
    {
        updateDrawableBounds(w, h);
    }

    private void updateDrawableBounds(int w, int h)
    {
        w -= getPaddingRight() + getPaddingLeft();
        h -= getPaddingTop() + getPaddingBottom();

        int right = w;
        int bottom = h;
        int top = 0;
        int left = 0;

        if (mIndicator != null)
        {
            final int intrinsicWidth = mIndicator.getIntrinsicWidth();
            final int intrinsicHeight = mIndicator.getIntrinsicHeight();
            final float intrinsicAspect = (float) intrinsicWidth / intrinsicHeight;
            final float boundAspect = (float) w / h;
            if (intrinsicAspect != boundAspect)
            {
                if (boundAspect > intrinsicAspect)
                {
                    final int width = (int) (h * intrinsicAspect);
                    left = (w - width) / 2;
                    right = left + width;
                }
                else
                {
                    final int height = (int) (w * (1 / intrinsicAspect));
                    top = (h - height) / 2;
                    bottom = top + height;
                }
            }
            mIndicator.setBounds(left, top, right, bottom);
        }
    }

    @Override
    protected synchronized void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);
        drawTrack(canvas);
    }

    void drawTrack(Canvas canvas)
    {
        final Drawable d = mIndicator;
        if (d != null)
        {
            final int saveCount = canvas.save();

            canvas.translate(getPaddingLeft(), getPaddingTop());

            d.draw(canvas);
            canvas.restoreToCount(saveCount);

            if (mShouldStartAnimationDrawable)
            {
                ((Animatable) d).start();
                mShouldStartAnimationDrawable = false;
            }
        }
    }

    @Override
    protected synchronized void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        int dw = 0;
        int dh = 0;

        final Drawable d = mIndicator;
        if (d != null)
        {
            dw = Math.max(mMinWidth, Math.min(mMaxWidth, d.getIntrinsicWidth()));
            dh = Math.max(mMinHeight, Math.min(mMaxHeight, d.getIntrinsicHeight()));
        }

        updateDrawableState();

        dw += getPaddingLeft() + getPaddingRight();
        dh += getPaddingTop() + getPaddingBottom();

        final int measuredWidth = resolveSizeAndState(dw, widthMeasureSpec, 0);
        final int measuredHeight = resolveSizeAndState(dh, heightMeasureSpec, 0);
        setMeasuredDimension(measuredWidth, measuredHeight);
    }

    @Override
    protected void drawableStateChanged()
    {
        super.drawableStateChanged();
        updateDrawableState();
    }

    private void updateDrawableState()
    {
        final int[] state = getDrawableState();
        if (mIndicator != null && mIndicator.isStateful())
        {
            mIndicator.setState(state);
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void drawableHotspotChanged(float x, float y)
    {
        super.drawableHotspotChanged(x, y);

        if (mIndicator != null)
        {
            mIndicator.setHotspot(x, y);
        }
    }

    @Override
    protected void onAttachedToWindow()
    {
        super.onAttachedToWindow();
        startAnimation();
        removeCallbacks();
    }

    @Override
    protected void onDetachedFromWindow()
    {
        stopAnimation();

        super.onDetachedFromWindow();
        removeCallbacks();
    }

    private void removeCallbacks()
    {
        removeCallbacks(mDelayedHide);
        removeCallbacks(mDelayedShow);
    }

}
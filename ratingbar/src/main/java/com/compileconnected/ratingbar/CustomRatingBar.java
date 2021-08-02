package com.compileconnected.ratingbar;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RatingBar;

@SuppressLint("AppCompatCustomView")
public class CustomRatingBar extends RatingBar {
    private ColorStateList mStarColor;

    private ColorStateList mSubStarColor;

    private ColorStateList mBgColor;

    private int mStarDrawable;

    private int mBgDrawable;

    private boolean mKeepOriginColor;

    private float scaleFactor;

    private float starSpacing;

    private boolean right2Left;

    private StarDrawable mDrawable;

    private OnRatingChangeListener mOnRatingChangeListener;

    private float mTempRating;

    public CustomRatingBar(Context context) {
        this(context, null);
    }

    public CustomRatingBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0);
    }

    public CustomRatingBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.CustomRatingBar, defStyleAttr, 0);
        this.right2Left = typedArray.getBoolean(R.styleable.CustomRatingBar_right2Left, false);
        if (typedArray.hasValue(R.styleable.CustomRatingBar_starColor))
            if (this.right2Left) {
                this.mBgColor = typedArray.getColorStateList(R.styleable.CustomRatingBar_starColor);
            } else {
                this.mStarColor = typedArray.getColorStateList(R.styleable.CustomRatingBar_starColor);
            }

        if (typedArray.hasValue(R.styleable.CustomRatingBar_subStarColor) &&
                !this.right2Left)
            this.mSubStarColor = typedArray.getColorStateList(R.styleable.CustomRatingBar_subStarColor);

        if (typedArray.hasValue(R.styleable.CustomRatingBar_bgColor))
            if (this.right2Left) {
                this.mStarColor = typedArray.getColorStateList(R.styleable.CustomRatingBar_bgColor);
            } else {
                this.mBgColor = typedArray.getColorStateList(R.styleable.CustomRatingBar_bgColor);
            }
        this.mKeepOriginColor = typedArray.getBoolean(R.styleable.CustomRatingBar_keepOriginColor, false);
        this.scaleFactor = typedArray.getFloat(R.styleable.CustomRatingBar_scaleFactor, 1.0F);
        this.starSpacing = typedArray.getDimension(R.styleable.CustomRatingBar_starSpacing, 0.0F);
        this.mStarDrawable = typedArray.getResourceId(R.styleable.CustomRatingBar_starDrawable, R.drawable.ic_rating_star_solid);
        if (typedArray.hasValue(R.styleable.CustomRatingBar_bgDrawable)) {
            this.mBgDrawable = typedArray.getResourceId(R.styleable.CustomRatingBar_bgDrawable, R.drawable.ic_rating_star_solid);
        } else {
            this.mBgDrawable = this.mStarDrawable;
        }
        typedArray.recycle();
        this.mDrawable = new StarDrawable(context, this.mStarDrawable, this.mBgDrawable, this.mKeepOriginColor);
        this.mDrawable.setStarCount(getNumStars());
        setProgressDrawable((Drawable)this.mDrawable);
        if (this.right2Left)
            setRating(getNumStars() - getRating());
    }

    @Override
    public void setNumStars(int numStars) {
        super.setNumStars(numStars);
        if (mDrawable != null) {
            mDrawable.setStarCount(numStars);
        }
    }

    @Override
    protected synchronized void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int height = getMeasuredHeight();
        int width = Math.round(height * mDrawable.getTileRatio() * getNumStars() * scaleFactor) + (int) ((getNumStars() - 1) * starSpacing);
        setMeasuredDimension(View.resolveSizeAndState(width, widthMeasureSpec, 0), height);
    }

    @Override
    public void setProgressDrawable(Drawable d) {
        super.setProgressDrawable(d);
        applyProgressTints();
    }

    private void applyProgressTints() {
        if (getProgressDrawable() == null) {
            return;
        }
        applyPrimaryProgressTint();
        applyProgressBackgroundTint();
        applySecondaryProgressTint();
    }

    private void applyPrimaryProgressTint() {
        if (mStarColor != null) {
            Drawable target = getTintTargetFromProgressDrawable(android.R.id.progress, true);
            if (target != null) {
                applyTintForDrawable(target, mStarColor);
            }
        }
    }

    private void applySecondaryProgressTint() {
        if (mSubStarColor != null) {
            Drawable target = getTintTargetFromProgressDrawable(android.R.id.secondaryProgress,
                    false);
            if (target != null) {
                applyTintForDrawable(target, mSubStarColor);
            }
        }
    }

    private void applyProgressBackgroundTint() {
        if (mBgColor != null) {
            Drawable target = getTintTargetFromProgressDrawable(android.R.id.background, false);
            if (target != null) {
                applyTintForDrawable(target, mBgColor);
            }
        }
    }

    private Drawable getTintTargetFromProgressDrawable(int layerId, boolean shouldFallback) {
        Drawable progressDrawable = getProgressDrawable();
        if (progressDrawable == null) {
            return null;
        }
        progressDrawable.mutate();
        Drawable layerDrawable = null;
        if (progressDrawable instanceof LayerDrawable) {
            layerDrawable = ((LayerDrawable) progressDrawable).findDrawableByLayerId(layerId);
        }
        if (layerDrawable == null && shouldFallback) {
            layerDrawable = progressDrawable;
        }
        return layerDrawable;
    }

    // Progress drawables in this library has already rewritten tint related methods for
    // compatibility.
    private void applyTintForDrawable(Drawable drawable, ColorStateList tintList) {
        if (tintList != null) {
            if (drawable instanceof BaseDrawable) {
                ((BaseDrawable) drawable).setTintList(tintList);
            } else {
                drawable.setTintList(tintList);
            }
            // The drawable (or one of its children) may not have been
            // stateful before applying the tint, so let's try again.
            if (drawable.isStateful()) {
                drawable.setState(getDrawableState());
            }
        }
    }

    /**
     * Get the listener that is listening for rating change events.
     *
     * @return The listener, may be null.
     */
    public OnRatingChangeListener getOnRatingChangeListener() {
        return mOnRatingChangeListener;
    }

    /**
     * Sets the listener to be called when the rating changes.
     *
     * @param listener The listener.
     */
    public void setOnRatingChangeListener(OnRatingChangeListener listener) {
        mOnRatingChangeListener = listener;
        runRatingChangeListener(getRating());
    }

    @Override
    public synchronized void setSecondaryProgress(int secondaryProgress) {
        super.setSecondaryProgress(secondaryProgress);

        float rating = getRating();
        if (mOnRatingChangeListener != null && rating != mTempRating) {
            runRatingChangeListener(rating);
        }
        mTempRating = rating;
    }

    private void runRatingChangeListener(float rating) {
        if (right2Left) {
            mOnRatingChangeListener.onRatingChanged(this, getNumStars() - rating);
        } else {
            mOnRatingChangeListener.onRatingChanged(this, rating);
        }
    }


    /**
     * set the scale factor of the ratingbar
     *
     * @param scaleFactor
     */
    public void setScaleFactor(float scaleFactor) {
        this.scaleFactor = scaleFactor;
        requestLayout();
    }

    /**
     * set the spacing of the star
     *
     * @param starSpacing
     */
    public void setStarSpacing(float starSpacing) {
        this.starSpacing = starSpacing;
        requestLayout();
    }

    /**
     * A callback that notifies clients when the rating has been changed. This includes changes that
     * were initiated by the user through a touch gesture or arrow key/trackball as well as changes
     * that were initiated programmatically. This callback <strong>will</strong> be called
     * continuously while the user is dragging, different from framework's
     * {@link OnRatingBarChangeListener}.
     */
    @FunctionalInterface
    public interface OnRatingChangeListener {

        /**
         * Notification that the rating has changed. This <strong>will</strong> be called
         * continuously while the user is dragging, different from framework's
         * {@link OnRatingBarChangeListener}.
         *
         * @param ratingBar The RatingBar whose rating has changed.
         * @param rating    The current rating. This will be in the range 0..numStars.
         */
        void onRatingChanged(CustomRatingBar ratingBar, float rating);
    }
}
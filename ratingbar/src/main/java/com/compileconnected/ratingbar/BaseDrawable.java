package com.compileconnected.ratingbar;

import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

import androidx.annotation.ColorInt;
import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

@Keep
abstract class BaseDrawable extends Drawable {
    int mAlpha = 255;

    private ColorFilter mColorFilter;

    private ColorStateList mTintList;

    private PorterDuff.Mode mTintMode = PorterDuff.Mode.SRC_IN;

    private PorterDuffColorFilter mTintFilter;

    private final ConstantState mConstantState = new ConstantState();

    @Override
    public int getAlpha() {
        return this.mAlpha;
    }

    public void setAlpha(int alpha) {
        if (this.mAlpha != alpha) {
            this.mAlpha = alpha;
            invalidateSelf();
        }
    }

    @Override
    public ColorFilter getColorFilter() {
        return this.mColorFilter;
    }

    public void setColorFilter(@Nullable ColorFilter colorFilter) {
        this.mColorFilter = colorFilter;
        invalidateSelf();
    }

    @Override
    public void setTint(@ColorInt int tintColor) {
        setTintList(ColorStateList.valueOf(tintColor));
    }

    @Override
    public void setTintList(@Nullable ColorStateList tint) {
        this.mTintList = tint;
        if (updateTintFilter()) invalidateSelf();
    }

    @Override
    public void setTintMode(@Nullable PorterDuff.Mode tintMode) {
        this.mTintMode = tintMode;
        if (updateTintFilter()) invalidateSelf();
    }

    @Override
    public boolean isStateful() {
        return (this.mTintList != null && this.mTintList.isStateful());
    }

    @Override
    protected boolean onStateChange(int[] state) {
        return updateTintFilter();
    }

    private boolean updateTintFilter() {
        if (this.mTintList == null || this.mTintMode == null) {
            boolean hadTintFilter = (this.mTintFilter != null);
            this.mTintFilter = null;
            return hadTintFilter;
        }
        int tintColor = this.mTintList.getColorForState(getState(), 0);
        this.mTintFilter = new PorterDuffColorFilter(tintColor, this.mTintMode);
        return true;
    }

    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }

    protected abstract void onDraw(Canvas canvas, int width, int height);

    public void draw(Canvas canvas) {
        Rect bounds = getBounds();
        if (bounds.width() == 0 || bounds.height() == 0)
            return;
        int saveCount = canvas.save();
        canvas.translate(bounds.left, bounds.top);
        onDraw(canvas, bounds.width(), bounds.height());
        canvas.restoreToCount(saveCount);
    }

    protected ColorFilter getColorFilterForDrawing() {
        return (this.mColorFilter != null) ? this.mColorFilter : this.mTintFilter;
    }

    @Override
    public Drawable.ConstantState getConstantState() {
        return this.mConstantState;
    }

    private final  class ConstantState extends Drawable.ConstantState {
        public int getChangingConfigurations() {
            return 0;
        }

        @NonNull
        public Drawable newDrawable() {
            return BaseDrawable.this;
        }
    }
}
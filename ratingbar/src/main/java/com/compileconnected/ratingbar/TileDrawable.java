package com.compileconnected.ratingbar;


import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;

public class TileDrawable extends BaseDrawable {
    private Drawable mDrawable;

    private int mTileCount = -1;

    public TileDrawable(Drawable drawable) {
        this.mDrawable = drawable;
    }

    public Drawable getDrawable() {
        return this.mDrawable;
    }

    public int getTileCount() {
        return this.mTileCount;
    }

    public void setTileCount(int tileCount) {
        this.mTileCount = tileCount;
        invalidateSelf();
    }

    @NonNull
    @Override
    public Drawable mutate() {
        this.mDrawable = this.mDrawable.mutate();
        return this;
    }

    @Override
    protected void onDraw(Canvas canvas, int width, int height) {
        this.mDrawable.setAlpha(this.mAlpha);
        ColorFilter colorFilter = getColorFilterForDrawing();
        if (colorFilter != null)
            this.mDrawable.setColorFilter(colorFilter);
        int tileHeight = this.mDrawable.getIntrinsicHeight();
        float scale = height / tileHeight;
        canvas.scale(scale, scale);
        float scaledWidth = width / scale;
        if (this.mTileCount < 0) {
            int tileWidth = this.mDrawable.getIntrinsicWidth();
            int x;
            for (x = 0; x < scaledWidth; x += tileWidth) {
                this.mDrawable.setBounds(x, 0, x + tileWidth, tileHeight);
                this.mDrawable.draw(canvas);
            }
        } else {
            float tileWidth = scaledWidth / this.mTileCount;
            for (int i = 0; i < this.mTileCount; i++) {
                int drawableWidth = this.mDrawable.getIntrinsicWidth();
                float tileCenter = tileWidth * (i + 0.5F);
                float drawableWidthHalf = drawableWidth / 2.0F;
                this.mDrawable.setBounds(Math.round(tileCenter - drawableWidthHalf), 0,
                        Math.round(tileCenter + drawableWidthHalf), tileHeight);
                this.mDrawable.draw(canvas);
            }
        }
    }
}

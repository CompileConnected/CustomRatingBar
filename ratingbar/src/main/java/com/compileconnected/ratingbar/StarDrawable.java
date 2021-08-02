package com.compileconnected.ratingbar;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Build;
import android.view.Gravity;

import androidx.appcompat.content.res.AppCompatResources;
import java.lang.reflect.Field;

public class StarDrawable extends LayerDrawable {
    public StarDrawable(Context context, int starDrawable, int bgDrawable, boolean mKeepOriginColor) {
        super(new Drawable[] { createLayerDrawableWithTintAttrRes(bgDrawable, R.attr.colorControlHighlight, context, mKeepOriginColor),
                createClippedLayerDrawableWithTintColor(starDrawable, context),
                createClippedLayerDrawableWithTintAttrRes(starDrawable, R.attr.colorControlActivated, context, mKeepOriginColor) });
        setId(0, android.R.id.background);
        setId(1, android.R.id.secondaryProgress);
        setId(2, android.R.id.progress);
    }

    private static Drawable createLayerDrawableWithTintAttrRes(int tileRes, int tintAttrRes, Context context, boolean mKeepOriginColor) {
        int tintColor = -1;
        if (!mKeepOriginColor)
            tintColor = getColorFromAttrRes(tintAttrRes, context);
        return createLayerDrawableWithTintColor(tileRes, tintColor, context);
    }

    private static Drawable createClippedLayerDrawableWithTintColor(int tileResId, Context context) {
        return new ClipDrawable(createLayerDrawableWithTintColor(tileResId, 0, context), 3, 1);
    }

    private static Drawable createLayerDrawableWithTintColor(int tileRes, int tintColor, Context context) {
        TileDrawable drawable = new TileDrawable(AppCompatResources.getDrawable(context, tileRes));
        drawable.mutate();
        if (tintColor != -1)
            drawable.setTint(tintColor);
        return drawable;
    }

    private static Drawable createClippedLayerDrawableWithTintAttrRes(int tileResId, int tintAttrRes, Context context, boolean mKeepOriginColor) {
        return new ClipDrawable(createLayerDrawableWithTintAttrRes(tileResId, tintAttrRes, context, mKeepOriginColor), Gravity.LEFT, ClipDrawable.HORIZONTAL);
    }

    public float getTileRatio() {
        Drawable drawable = getTileDrawableByLayerId(android.R.id.progress).getDrawable();
        return drawable.getIntrinsicWidth() / drawable.getIntrinsicHeight();
    }

    public void setStarCount(int count) {
        getTileDrawableByLayerId(android.R.id.background).setTileCount(count);
        getTileDrawableByLayerId(android.R.id.secondaryProgress).setTileCount(count);
        getTileDrawableByLayerId(android.R.id.progress).setTileCount(count);
    }

    private TileDrawable getTileDrawableByLayerId(int id) {
        return  internalTileDrawableByLayerId(id, findDrawableByLayerId(id));
    }

    private TileDrawable internalTileDrawableByLayerId(int id, Drawable layerDrawable)  {
        ClipDrawable clipDrawable;
        if (id == android.R.id.background) {
            return (TileDrawable) layerDrawable;
        } else if (id == android.R.id.secondaryProgress || id == android.R.id.progress) {
            clipDrawable = (ClipDrawable) layerDrawable;
            if (Build.VERSION.SDK_INT >= 23) {
                return (TileDrawable) clipDrawable.getDrawable();
            } else {
                try {
                    String fieldState = (Build.VERSION.SDK_INT >= 22) ? "mState" : "mClipState";
                    Field mStateField = clipDrawable.getClass().getDeclaredField(fieldState);
                    mStateField.setAccessible(true);
                    Object clipState = mStateField.get(clipDrawable);
                    Field mDrawableField = clipState.getClass().getDeclaredField("mDrawable");
                    mDrawableField.setAccessible(true);
                    return (TileDrawable) mDrawableField.get(clipState);
                } catch (Exception e) {
                    /*no-op*/
                }
            }
        }
        throw new IllegalStateException();
    }

    private static int getColorFromAttrRes(int attrRes, Context context) {
        TypedArray a = context.obtainStyledAttributes(new int[] { attrRes });
        try {
            return a.getColor(0, 0);
        } finally {
            a.recycle();
        }
    }
}

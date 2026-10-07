package com.openpipe.app;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

/** Minimal wrapping layout used for chips. */
final class Flow extends ViewGroup {
    private final int gap = U.dp(8);

    Flow(Context c) {
        super(c);
    }

    @Override
    protected void onMeasure(int wSpec, int hSpec) {
        int maxW = MeasureSpec.getSize(wSpec);
        int avail = maxW - getPaddingLeft() - getPaddingRight();
        int x = 0, y = 0, rowH = 0, used = 0;
        for (int i = 0; i < getChildCount(); i++) {
            View v = getChildAt(i);
            if (v.getVisibility() == GONE) continue;
            v.measure(MeasureSpec.makeMeasureSpec(Math.max(avail, 0), MeasureSpec.AT_MOST),
                    MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
            int cw = v.getMeasuredWidth(), chh = v.getMeasuredHeight();
            if (x > 0 && x + cw > avail) {
                x = 0;
                y += rowH + gap;
                rowH = 0;
            }
            x += cw + gap;
            rowH = Math.max(rowH, chh);
            used = Math.max(used, x - gap);
        }
        int h = y + rowH + getPaddingTop() + getPaddingBottom();
        int w = used + getPaddingLeft() + getPaddingRight();
        setMeasuredDimension(resolveSize(w, wSpec), resolveSize(h, hSpec));
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int avail = r - l - getPaddingLeft() - getPaddingRight();
        int x = 0, y = 0, rowH = 0;
        for (int i = 0; i < getChildCount(); i++) {
            View v = getChildAt(i);
            if (v.getVisibility() == GONE) continue;
            int cw = v.getMeasuredWidth(), chh = v.getMeasuredHeight();
            if (x > 0 && x + cw > avail) {
                x = 0;
                y += rowH + gap;
                rowH = 0;
            }
            int left = getPaddingLeft() + x;
            int top = getPaddingTop() + y;
            v.layout(left, top, left + cw, top + chh);
            x += cw + gap;
            rowH = Math.max(rowH, chh);
        }
    }
}

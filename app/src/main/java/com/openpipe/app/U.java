package com.openpipe.app;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.os.Environment;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

/** Small UI helpers so the whole app can be built in code (no layout XML, no libraries). */
final class U {
    private U() {
    }

    static final int MATCH = ViewGroup.LayoutParams.MATCH_PARENT;
    static final int WRAP = ViewGroup.LayoutParams.WRAP_CONTENT;

    interface Chk {
        void on(boolean checked);
    }

    interface Str {
        void on(String s);
    }

    static int dp(float v) {
        return Math.round(v * Resources.getSystem().getDisplayMetrics().density);
    }

    static void toast(Context c, String s) {
        Toast.makeText(c.getApplicationContext(), s, Toast.LENGTH_SHORT).show();
    }

    static GradientDrawable round(int color, float rDp) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(color);
        g.setCornerRadius(dp(rDp));
        return g;
    }

    static GradientDrawable outline(int fill, int stroke, float rDp, float strokeDp) {
        GradientDrawable g = round(fill, rDp);
        g.setStroke(Math.max(1, dp(strokeDp)), stroke);
        return g;
    }

    static Drawable ripple(Drawable d) {
        return new RippleDrawable(ColorStateList.valueOf(0x33808080), d, null);
    }

    static LinearLayout.LayoutParams lp(int w, int h, float l, float t, float r, float b) {
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(w, h);
        p.setMargins(dp(l), dp(t), dp(r), dp(b));
        return p;
    }

    static void pad(View v, float l, float t, float r, float b) {
        v.setPadding(dp(l), dp(t), dp(r), dp(b));
    }

    static TextView text(Context c, CharSequence s, float sp, int color, boolean bold) {
        TextView t = new TextView(c);
        t.setText(s);
        t.setTextSize(sp);
        t.setTextColor(color);
        if (bold) t.setTypeface(Typeface.DEFAULT_BOLD);
        return t;
    }

    static LinearLayout row(Context c) {
        LinearLayout l = new LinearLayout(c);
        l.setOrientation(LinearLayout.HORIZONTAL);
        l.setGravity(Gravity.CENTER_VERTICAL);
        return l;
    }

    static LinearLayout col(Context c) {
        LinearLayout l = new LinearLayout(c);
        l.setOrientation(LinearLayout.VERTICAL);
        return l;
    }

    static LinearLayout card(Context c, Pal p) {
        LinearLayout l = col(c);
        l.setBackground(outline(p.card, p.line, 28, 1));
        pad(l, 18, 16, 18, 16);
        return l;
    }

    /** Filled pill button. */
    static TextView btn(Context c, String label, int bg, int fg, View.OnClickListener l) {
        TextView t = text(c, label, 15, fg, true);
        t.setGravity(Gravity.CENTER);
        pad(t, 18, 11, 18, 11);
        t.setBackground(ripple(round(bg, 24)));
        t.setOnClickListener(l);
        return t;
    }

    /** Plain text button. */
    static TextView link(Context c, String label, int color, View.OnClickListener l) {
        TextView t = text(c, label, 15, color, true);
        t.setGravity(Gravity.CENTER);
        pad(t, 12, 10, 12, 10);
        t.setBackground(ripple(round(0, 20)));
        t.setOnClickListener(l);
        return t;
    }

    static TextView chip(Context c, String label, int bg, int fg) {
        TextView t = text(c, label, 14, fg, false);
        t.setGravity(Gravity.CENTER);
        pad(t, 14, 7, 14, 7);
        t.setBackground(round(bg, 16));
        return t;
    }

    static Switch toggle(Context c, Pal p, boolean on, final Chk cb) {
        Switch s = new Switch(c);
        s.setChecked(on);
        int[][] st = {{android.R.attr.state_checked}, {}};
        s.setThumbTintList(new ColorStateList(st, new int[]{p.onAccent, p.sub}));
        s.setTrackTintList(new ColorStateList(st, new int[]{p.accent, p.line}));
        s.setOnCheckedChangeListener((b, v) -> cb.on(v));
        return s;
    }

    static String shortPath(String path) {
        String root = Environment.getExternalStorageDirectory().getAbsolutePath();
        if (path.equals(root)) return "Internal storage";
        if (path.startsWith(root + "/")) return path.substring(root.length() + 1);
        return path;
    }
}

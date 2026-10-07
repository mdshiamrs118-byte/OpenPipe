package com.openpipe.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;

/** Colour palette generated from a single hue, in light / dark / black variants. */
final class Pal {
    static final String[] MODES = {"System", "Light", "Dark", "Black"};
    static final String[] NAMES = {"Sunset", "Forest", "Ocean", "Violet", "Rose", "Mono"};
    static final int[] HUES = {24, 100, 205, 268, 338, 0};

    boolean dark;
    int bg, bgTop, card, line, text, sub, accent, onAccent, chip, onChip,
            barSel, onBarSel, fab, onFab, danger;

    private boolean mono;

    static Pal get(Context c) {
        SharedPreferences sp = Store.sp(c);
        int mode = sp.getInt("mode", 0);
        int hue = sp.getInt("hue", 24);
        boolean mono = sp.getBoolean("mono", false);
        int night = c.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        boolean dark = mode == 2 || mode == 3 || (mode == 0 && night == Configuration.UI_MODE_NIGHT_YES);
        return new Pal(dark, mode == 3, hue, mono);
    }

    private int hsv(float h, float s, float v) {
        return Color.HSVToColor(new float[]{h, mono ? 0f : s, v});
    }

    private Pal(boolean dark, boolean black, float h, boolean mono) {
        this.dark = dark;
        this.mono = mono;
        if (!dark) {
            bg = hsv(h, 0.05f, 0.99f);
            bgTop = hsv(h, 0.22f, 0.98f);
            card = hsv(h, 0.16f, 0.95f);
            line = hsv(h, 0.28f, 0.86f);
            text = hsv(h, 0.55f, 0.16f);
            sub = hsv(h, 0.35f, 0.42f);
            accent = hsv(h, 0.70f, 0.55f);
            onAccent = Color.WHITE;
            chip = hsv(h, 0.38f, 0.99f);
            onChip = hsv(h, 0.75f, 0.42f);
            barSel = hsv(h, 0.03f, 1f);
            onBarSel = accent;
            fab = hsv(h, 0.40f, 0.99f);
            onFab = hsv(h, 0.85f, 0.28f);
            danger = 0xFFC62828;
        } else {
            bg = hsv(h, 0.50f, 0.11f);
            bgTop = hsv(h, 0.55f, 0.17f);
            card = hsv(h, 0.40f, 0.21f);
            line = hsv(h, 0.35f, 0.32f);
            text = hsv(h, 0.08f, 0.93f);
            sub = hsv(h, 0.15f, 0.72f);
            accent = hsv(h, 0.45f, 0.85f);
            onAccent = hsv(h, 0.85f, 0.14f);
            chip = hsv(h, 0.60f, 0.34f);
            onChip = hsv(h, 0.30f, 0.95f);
            barSel = hsv(h, 0.60f, 0.08f);
            onBarSel = accent;
            fab = accent;
            onFab = onAccent;
            danger = 0xFFFF8A80;
            if (black) {
                bg = Color.BLACK;
                bgTop = Color.BLACK;
                card = hsv(h, 0.30f, 0.10f);
                line = hsv(h, 0.30f, 0.22f);
            }
        }
    }
}

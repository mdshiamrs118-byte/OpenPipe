package com.openpipe.app;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

/** Themed dialog builder (rounded card, matches the current palette). */
final class Dlg {
    final Activity a;
    final Pal p;
    final Dialog d;
    final LinearLayout box;

    Dlg(Activity a, Pal p) {
        this.a = a;
        this.p = p;
        d = new Dialog(a);
        d.requestWindowFeature(Window.FEATURE_NO_TITLE);
        box = U.col(a);
        box.setBackground(U.round(p.card, 28));
        U.pad(box, 20, 20, 20, 12);
        d.setContentView(box);
        Window w = d.getWindow();
        if (w != null) w.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }

    Dlg title(String s) {
        box.addView(U.text(a, s, 20, p.text, true));
        return this;
    }

    Dlg msg(String s) {
        TextView t = U.text(a, s, 15, p.sub, false);
        t.setPadding(0, U.dp(8), 0, U.dp(8));
        box.addView(t);
        return this;
    }

    Dlg view(View v) {
        box.addView(v);
        return this;
    }

    Dlg buttons(String neg, final Runnable onNeg, String pos, final Runnable onPos) {
        LinearLayout r = U.row(a);
        r.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
        if (neg != null) {
            r.addView(U.link(a, neg, p.sub, v -> {
                d.dismiss();
                if (onNeg != null) onNeg.run();
            }));
        }
        if (pos != null) {
            r.addView(U.btn(a, pos, p.accent, p.onAccent, v -> {
                d.dismiss();
                if (onPos != null) onPos.run();
            }), U.lp(U.WRAP, U.WRAP, 8, 0, 0, 0));
        }
        box.addView(r, U.lp(U.MATCH, U.WRAP, 0, 12, 0, 0));
        return this;
    }

    void show() {
        d.show();
        Window w = d.getWindow();
        if (w != null) {
            int width = (int) (a.getResources().getDisplayMetrics().widthPixels * 0.92f);
            w.setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT);
        }
    }

    static void input(Activity a, Pal p, String title, String hint, String init, String ok, final U.Str cb) {
        Dlg d = new Dlg(a, p).title(title);
        final EditText et = new EditText(a);
        et.setHint(hint);
        et.setText(init);
        et.setSingleLine();
        et.setTextColor(p.text);
        et.setHintTextColor(p.sub);
        et.setBackground(U.outline(0, p.line, 14, 1.5f));
        U.pad(et, 14, 12, 14, 12);
        d.box.addView(et, U.lp(U.MATCH, U.WRAP, 0, 12, 0, 0));
        d.buttons("Cancel", null, ok, () -> cb.on(et.getText().toString().trim()));
        d.show();
        et.requestFocus();
        Window w = d.d.getWindow();
        if (w != null) w.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }
}

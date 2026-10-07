package com.openpipe.app;

import android.app.Activity;
import android.graphics.drawable.GradientDrawable;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends Activity {
    private Pal p;
    private int tab = 0; // 0 rules, 1 history, 2 settings
    private LinearLayout body;
    private final Handler ui = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        if (b != null) tab = b.getInt("tab", 0);
        build();
    }

    @Override
    protected void onSaveInstanceState(Bundle o) {
        super.onSaveInstanceState(o);
        o.putInt("tab", tab);
    }

    @Override
    protected void onResume() {
        super.onResume();
        fill();
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onBackPressed() {
        if (tab != 0) {
            tab = 0;
            build();
        } else {
            super.onBackPressed();
        }
    }

    // ------------------------------------------------------------ chrome

    @SuppressWarnings("deprecation")
    private void build() {
        p = Pal.get(this);
        Window w = getWindow();
        w.setStatusBarColor(p.bgTop);
        w.setNavigationBarColor(p.bg);
        w.getDecorView().setSystemUiVisibility(p.dark ? 0
                : (View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR | View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR));

        FrameLayout root = new FrameLayout(this);
        root.setBackground(new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{p.bgTop, p.bg}));

        ScrollView scroll = new ScrollView(this);
        scroll.setVerticalScrollBarEnabled(false);
        body = U.col(this);
        U.pad(body, 16, 8, 16, 120);
        scroll.addView(body, new ViewGroup.LayoutParams(U.MATCH, U.WRAP));
        root.addView(scroll, new FrameLayout.LayoutParams(U.MATCH, U.MATCH));

        FrameLayout.LayoutParams bl = new FrameLayout.LayoutParams(U.WRAP, U.WRAP,
                Gravity.BOTTOM | Gravity.START);
        bl.setMargins(U.dp(16), 0, 0, U.dp(16));
        root.addView(buildBar(), bl);

        if (tab == 0) {
            FrameLayout fab = new FrameLayout(this);
            GradientDrawable g = new GradientDrawable();
            g.setShape(GradientDrawable.OVAL);
            g.setColor(p.fab);
            fab.setBackground(U.ripple(g));
            fab.setElevation(U.dp(6));
            fab.setOnClickListener(v -> startActivity(new Intent(this, RuleEditActivity.class)));
            fab.addView(new IconView(this, IconView.PLUS, p.onFab),
                    new FrameLayout.LayoutParams(U.dp(28), U.dp(28), Gravity.CENTER));
            FrameLayout.LayoutParams fl = new FrameLayout.LayoutParams(U.dp(60), U.dp(60),
                    Gravity.BOTTOM | Gravity.END);
            fl.setMargins(0, 0, U.dp(16), U.dp(16));
            root.addView(fab, fl);
        }

        setContentView(root);
        fill();
    }

    private View buildBar() {
        String[] names = {"Rules", "History", "Settings"};
        LinearLayout bar = U.row(this);
        bar.setBackground(U.round(p.accent, 40));
        U.pad(bar, 8, 8, 8, 8);
        bar.setElevation(U.dp(6));
        for (int i = 0; i < 3; i++) {
            final int idx = i;
            boolean sel = i == tab;
            LinearLayout t = U.row(this);
            t.setGravity(Gravity.CENTER);
            if (sel) t.setBackground(U.round(p.barSel, 32));
            U.pad(t, sel ? 18 : 14, 12, sel ? 20 : 14, 12);
            t.addView(new IconView(this, i, sel ? p.onBarSel : p.onAccent),
                    new LinearLayout.LayoutParams(U.dp(24), U.dp(24)));
            if (sel) {
                TextView tv = U.text(this, names[i], 15, p.onBarSel, true);
                tv.setPadding(U.dp(8), 0, 0, 0);
                t.addView(tv);
            }
            t.setOnClickListener(v -> {
                if (tab != idx) {
                    tab = idx;
                    build();
                }
            });
            bar.addView(t, U.lp(U.WRAP, U.WRAP, 2, 0, 2, 0));
        }
        return bar;
    }

    private void fill() {
        if (body == null) return;
        body.removeAllViews();
        if (tab == 0) fillRules();
        else if (tab == 1) fillHistory();
        else fillSettings();
    }

    private void header(String title, View action) {
        LinearLayout h = U.row(this);
        TextView t = U.text(this, title, 30, p.text, true);
        h.addView(t, new LinearLayout.LayoutParams(0, U.WRAP, 1f));
        if (action != null) h.addView(action);
        body.addView(h, U.lp(U.MATCH, U.WRAP, 6, 8, 6, 14));
    }

    private void add(View v) {
        body.addView(v, U.lp(U.MATCH, U.WRAP, 0, 0, 0, 12));
    }

    private void empty(String s) {
        TextView t = U.text(this, s, 16, p.sub, false);
        t.setGravity(Gravity.CENTER);
        U.pad(t, 24, 48, 24, 24);
        body.addView(t, U.lp(U.MATCH, U.WRAP, 0, 0, 0, 0));
    }

    // ------------------------------------------------------------- rules

    private void fillRules() {
        ArrayList<Rule> rules = Store.rules(this);
        View runAll = rules.isEmpty() ? null
                : U.btn(this, "Run all", p.accent, p.onAccent, v -> runAll());
        header("OpenPipe", runAll);

        if (!Perm.has(this)) {
            LinearLayout c = U.card(this, p);
            c.addView(U.text(this, "File access needed", 18, p.text, true));
            TextView d = U.text(this, "OpenPipe needs permission to see and move your files. Nothing ever leaves your device.", 14, p.sub, false);
            d.setPadding(0, U.dp(4), 0, U.dp(10));
            c.addView(d);
            c.addView(U.btn(this, "Grant access", p.accent, p.onAccent, v -> Perm.ask(this)));
            add(c);
        }

        if (rules.isEmpty()) {
            empty("No rules yet.\nTap + to create your first one.");
            return;
        }
        for (Rule r : rules) add(ruleCard(r));
    }

    private View ruleCard(final Rule r) {
        LinearLayout c = U.card(this, p);

        LinearLayout h = U.row(this);
        TextView ic = U.text(this, Rule.icon(r.icon), 22, p.text, false);
        ic.setGravity(Gravity.CENTER);
        ic.setBackground(U.round(p.chip, 16));
        h.addView(ic, new LinearLayout.LayoutParams(U.dp(46), U.dp(46)));
        TextView name = U.text(this, r.name, 22, p.text, false);
        name.setSingleLine();
        name.setEllipsize(TextUtils.TruncateAt.END);
        name.setPadding(U.dp(14), 0, U.dp(8), 0);
        h.addView(name, new LinearLayout.LayoutParams(0, U.WRAP, 1f));
        h.addView(U.toggle(this, p, r.enabled, on -> {
            r.enabled = on;
            Store.upsert(this, r);
            fill();
        }));
        c.addView(h);

        Flow f = new Flow(this);
        for (String e : r.exts) f.addView(U.chip(this, e.equals("*") ? "all files" : e, p.chip, p.onChip));
        c.addView(f, U.lp(U.MATCH, U.WRAP, 0, 12, 0, 0));

        StringBuilder from = new StringBuilder();
        for (int i = 0; i < r.sources.size() && i < 2; i++) {
            if (i > 0) from.append(", ");
            from.append(U.shortPath(r.sources.get(i)));
        }
        if (r.sources.size() > 2) from.append(", +").append(r.sources.size() - 2);
        String info = "From: " + from + "\nTo: " + (r.dest.isEmpty() ? "-" : U.shortPath(r.dest))
                + "\n" + (r.copy ? "Copy" : "Move") + (r.subfolders ? " · incl. subfolders" : "");
        TextView it = U.text(this, info, 14, p.sub, false);
        it.setLineSpacing(U.dp(3), 1f);
        c.addView(it, U.lp(U.MATCH, U.WRAP, 2, 12, 0, 0));

        LinearLayout a = U.row(this);
        a.addView(U.link(this, "Preview", p.accent, v -> preview(r)));
        a.addView(U.link(this, "Edit", p.accent, v -> {
            Intent i = new Intent(this, RuleEditActivity.class);
            i.putExtra("id", r.id);
            startActivity(i);
        }));
        a.addView(new View(this), new LinearLayout.LayoutParams(0, 1, 1f));

        LinearLayout run = U.row(this);
        run.setGravity(Gravity.CENTER);
        run.setBackground(U.ripple(U.round(p.chip, 24)));
        U.pad(run, 16, 10, 20, 10);
        run.addView(new IconView(this, IconView.PLAY, p.onChip), new LinearLayout.LayoutParams(U.dp(18), U.dp(18)));
        TextView rt = U.text(this, "Run now", 15, p.onChip, true);
        rt.setPadding(U.dp(8), 0, 0, 0);
        run.addView(rt);
        run.setOnClickListener(v -> runRule(r, v));
        a.addView(run);
        c.addView(a, U.lp(U.MATCH, U.WRAP, -8, 8, 0, 0));

        c.setAlpha(r.enabled ? 1f : 0.6f);
        return c;
    }

    private String summary(Run run) {
        if (run.error != null) return run.error;
        if (run.done == 0 && run.failed == 0) return "No matching files";
        String s = (run.copy ? "Copied " : "Moved ") + run.done + (run.done == 1 ? " file" : " files");
        if (run.failed > 0) s += ", " + run.failed + " failed";
        return s;
    }

    private boolean canRun(Rule r) {
        if (!r.enabled) {
            U.toast(this, "Turn the rule on first");
            return false;
        }
        if (!Perm.has(this)) {
            U.toast(this, "Allow file access first");
            Perm.ask(this);
            return false;
        }
        return true;
    }

    private void runRule(final Rule r, final View btn) {
        if (!canRun(r)) return;
        btn.setEnabled(false);
        btn.setAlpha(0.5f);
        new Thread(() -> {
            final Run run = Mover.run(getApplicationContext(), r, "Manual");
            ui.post(() -> {
                U.toast(this, summary(run));
                if (!isFinishing()) fill();
            });
        }).start();
    }

    private void runAll() {
        if (!Perm.has(this)) {
            U.toast(this, "Allow file access first");
            Perm.ask(this);
            return;
        }
        final ArrayList<Rule> rules = Store.rules(this);
        new Thread(() -> {
            int total = 0, failed = 0, used = 0;
            for (Rule r : rules) {
                if (!r.enabled) continue;
                used++;
                Run run = Mover.run(getApplicationContext(), r, "Manual");
                total += run.done;
                failed += run.failed;
            }
            final String msg = used == 0 ? "No rules are turned on"
                    : "Processed " + total + (total == 1 ? " file" : " files")
                    + (failed > 0 ? ", " + failed + " failed" : "");
            ui.post(() -> {
                U.toast(this, msg);
                if (!isFinishing()) fill();
            });
        }).start();
    }

    private void preview(final Rule r) {
        if (!Perm.has(this)) {
            U.toast(this, "Allow file access first");
            Perm.ask(this);
            return;
        }
        new Thread(() -> {
            final ArrayList<File> files = Mover.scan(r);
            ui.post(() -> {
                if (isFinishing()) return;
                Dlg d = new Dlg(this, p).title(r.name);
                d.msg(files.isEmpty() ? "No matching files right now."
                        : files.size() + (files.size() == 1 ? " file would be " : " files would be ")
                        + (r.copy ? "copied" : "moved") + ":");
                if (!files.isEmpty()) {
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < files.size() && i < 80; i++) {
                        if (i > 0) sb.append('\n');
                        sb.append(files.get(i).getName());
                    }
                    if (files.size() > 80) sb.append("\n… and ").append(files.size() - 80).append(" more");
                    TextView t = U.text(this, sb.toString(), 14, p.text, false);
                    t.setLineSpacing(U.dp(3), 1f);
                    ScrollView sv = new ScrollView(this);
                    sv.addView(t);
                    d.box.addView(sv, new LinearLayout.LayoutParams(U.MATCH,
                            files.size() > 8 ? U.dp(260) : U.WRAP));
                }
                d.buttons(null, null, "Close", null);
                d.show();
            });
        }).start();
    }

    // ----------------------------------------------------------- history

    private void fillHistory() {
        final ArrayList<Run> hs = Store.history(this);
        View clear = hs.isEmpty() ? null : U.link(this, "Clear", p.accent, v -> {
            Dlg d = new Dlg(this, p).title("Clear history?")
                    .msg("Undo will no longer be possible for these runs.");
            d.buttons("Cancel", null, "Clear", () -> {
                Store.clearHistory(this);
                fill();
            });
            d.show();
        });
        header("History", clear);
        if (hs.isEmpty()) {
            empty("Nothing has run yet.");
            return;
        }
        SimpleDateFormat fmt = new SimpleDateFormat("MMM d, h:mm a", Locale.getDefault());
        for (final Run run : hs) {
            LinearLayout c = U.card(this, p);
            c.setBackground(U.ripple(U.outline(p.card, p.line, 28, 1)));
            LinearLayout r = U.row(this);
            TextView ic = U.text(this, Rule.icon(run.icon), 20, p.text, false);
            ic.setGravity(Gravity.CENTER);
            ic.setBackground(U.round(p.chip, 14));
            r.addView(ic, new LinearLayout.LayoutParams(U.dp(42), U.dp(42)));

            LinearLayout col = U.col(this);
            col.setPadding(U.dp(12), 0, U.dp(8), 0);
            TextView n = U.text(this, run.ruleName, 17, p.text, true);
            n.setSingleLine();
            n.setEllipsize(TextUtils.TruncateAt.END);
            col.addView(n);
            col.addView(U.text(this, run.trigger + " · " + fmt.format(new Date(run.time)) + " · " + summary(run), 13, p.sub, false));
            r.addView(col, new LinearLayout.LayoutParams(0, U.WRAP, 1f));

            String st = run.status();
            int bg = p.chip, fg = p.onChip;
            if (st.equals("Success")) {
                bg = p.accent;
                fg = p.onAccent;
            } else if (st.equals("Failed")) {
                bg = p.danger;
                fg = p.dark ? 0xFF3B0000 : 0xFFFFFFFF;
            }
            r.addView(U.chip(this, st, bg, fg));
            c.addView(r);
            c.setOnClickListener(v -> showRun(run));
            add(c);
        }
    }

    private void showRun(final Run run) {
        SimpleDateFormat fmt = new SimpleDateFormat("MMM d, yyyy h:mm a", Locale.getDefault());
        Dlg d = new Dlg(this, p).title(run.ruleName);
        d.msg(run.status() + " · " + run.trigger + " · " + fmt.format(new Date(run.time)) + "\n" + summary(run));
        if (!run.entries.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < run.entries.size() && i < 100; i++) {
                File to = new File(run.entries.get(i)[1]);
                if (i > 0) sb.append('\n');
                sb.append(to.getName()).append("  →  ").append(U.shortPath(String.valueOf(to.getParent())));
            }
            TextView t = U.text(this, sb.toString(), 13, p.text, false);
            t.setLineSpacing(U.dp(3), 1f);
            ScrollView sv = new ScrollView(this);
            sv.addView(t);
            d.box.addView(sv, new LinearLayout.LayoutParams(U.MATCH,
                    run.entries.size() > 6 ? U.dp(240) : U.WRAP));
        }
        boolean canUndo = !run.undone && run.done > 0 && !run.entries.isEmpty();
        d.buttons("Close", null, canUndo ? "Undo" : null, () -> {
            if (!Perm.has(this)) {
                U.toast(this, "Allow file access first");
                Perm.ask(this);
                return;
            }
            new Thread(() -> {
                final int n = Mover.undo(getApplicationContext(), run);
                ui.post(() -> {
                    U.toast(this, "Restored " + n + (n == 1 ? " file" : " files"));
                    if (!isFinishing()) fill();
                });
            }).start();
        });
        d.show();
    }

    // ---------------------------------------------------------- settings

    private void fillSettings() {
        header("Settings", null);

        LinearLayout c = U.card(this, p);
        c.addView(U.text(this, "Appearance", 18, p.text, true));

        c.addView(U.text(this, "Mode", 13, p.sub, false), U.lp(U.WRAP, U.WRAP, 0, 12, 0, 6));
        Flow modes = new Flow(this);
        int mode = Store.sp(this).getInt("mode", 0);
        for (int i = 0; i < Pal.MODES.length; i++) {
            final int idx = i;
            TextView t = U.chip(this, Pal.MODES[i], i == mode ? p.accent : p.chip, i == mode ? p.onAccent : p.onChip);
            t.setOnClickListener(v -> {
                Store.sp(this).edit().putInt("mode", idx).apply();
                build();
            });
            modes.addView(t);
        }
        c.addView(modes);

        c.addView(U.text(this, "Color", 13, p.sub, false), U.lp(U.WRAP, U.WRAP, 0, 16, 0, 6));
        Flow colors = new Flow(this);
        boolean mono = Store.sp(this).getBoolean("mono", false);
        int hue = Store.sp(this).getInt("hue", 24);
        for (int i = 0; i < Pal.NAMES.length; i++) {
            final int idx = i;
            boolean isMono = i == Pal.NAMES.length - 1;
            boolean sel = isMono ? mono : (!mono && hue == Pal.HUES[i]);
            TextView t = U.chip(this, Pal.NAMES[i], sel ? p.accent : p.chip, sel ? p.onAccent : p.onChip);
            t.setOnClickListener(v -> {
                Store.sp(this).edit().putBoolean("mono", isMono)
                        .putInt("hue", isMono ? Store.sp(this).getInt("hue", 24) : Pal.HUES[idx]).apply();
                build();
            });
            colors.addView(t);
        }
        c.addView(colors);

        c.addView(U.text(this, "Custom hue", 13, p.sub, false), U.lp(U.WRAP, U.WRAP, 0, 16, 0, 0));
        SeekBar sb = new SeekBar(this);
        sb.setMax(360);
        sb.setProgress(hue);
        sb.setThumbTintList(ColorStateList.valueOf(p.accent));
        sb.setProgressTintList(ColorStateList.valueOf(p.accent));
        sb.setProgressBackgroundTintList(ColorStateList.valueOf(p.line));
        sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar s, int v, boolean user) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar s) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar s) {
                Store.sp(MainActivity.this).edit().putBoolean("mono", false)
                        .putInt("hue", s.getProgress()).apply();
                build();
            }
        });
        c.addView(sb, U.lp(U.MATCH, U.WRAP, -4, 4, -4, 0));
        add(c);

        LinearLayout perm = U.card(this, p);
        perm.addView(U.text(this, "File access", 18, p.text, true));
        boolean ok = Perm.has(this);
        TextView ps = U.text(this, ok ? "Granted ✓" : "Not granted – rules can't move files yet.", 14, p.sub, false);
        ps.setPadding(0, U.dp(4), 0, U.dp(ok ? 0 : 10));
        perm.addView(ps);
        if (!ok) perm.addView(U.btn(this, "Grant access", p.accent, p.onAccent, v -> Perm.ask(this)));
        add(perm);

        LinearLayout about = U.card(this, p);
        about.addView(U.text(this, "About", 18, p.text, true));
        TextView at = U.text(this, "OpenPipe 1.0\nMoves or copies files between folders using rules you create. "
                + "100% offline – the app has no internet permission.", 14, p.sub, false);
        at.setPadding(0, U.dp(4), 0, 0);
        at.setLineSpacing(U.dp(3), 1f);
        about.addView(at);
        add(about);
    }
}

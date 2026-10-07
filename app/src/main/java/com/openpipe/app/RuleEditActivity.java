package com.openpipe.app;

import android.app.Activity;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.Icon;
import android.os.Bundle;
import android.os.Environment;
import android.text.InputType;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;

import java.util.Locale;

public class RuleEditActivity extends Activity {
    private Pal p;
    private Rule r;
    private boolean isNew;

    private EditText nameEt;
    private TextView iconTv;
    private Flow extFlow;
    private LinearLayout srcBox;
    private TextView destTv;
    private Switch subSw;
    private TextView moveChip, copyChip;

    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        p = Pal.get(this);
        String id = getIntent().getStringExtra("id");
        r = Store.find(this, id);
        isNew = r == null;
        if (isNew) r = new Rule();

        Window w = getWindow();
        w.setStatusBarColor(p.bgTop);
        w.setNavigationBarColor(p.bg);
        w.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        w.getDecorView().setSystemUiVisibility(p.dark ? 0
                : (View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR | View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR));

        LinearLayout page = U.col(this);
        page.setBackground(new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{p.bgTop, p.bg}));

        // top bar
        LinearLayout top = U.row(this);
        FrameLayoutBack back = new FrameLayoutBack();
        top.addView(back.make(), new LinearLayout.LayoutParams(U.dp(48), U.dp(48)));
        TextView title = U.text(this, isNew ? "New rule" : "Edit rule", 24, p.text, true);
        title.setPadding(U.dp(8), 0, 0, 0);
        top.addView(title);
        page.addView(top, U.lp(U.MATCH, U.WRAP, 8, 8, 8, 4));

        ScrollView sv = new ScrollView(this);
        sv.setVerticalScrollBarEnabled(false);
        LinearLayout form = U.col(this);
        U.pad(form, 16, 8, 16, 16);
        sv.addView(form);
        page.addView(sv, new LinearLayout.LayoutParams(U.MATCH, 0, 1f));

        // name + icon
        LinearLayout nr = U.row(this);
        iconTv = U.text(this, Rule.icon(r.icon), 26, p.text, false);
        iconTv.setGravity(Gravity.CENTER);
        iconTv.setBackground(U.ripple(U.round(p.chip, 18)));
        iconTv.setOnClickListener(v -> pickIcon());
        nr.addView(iconTv, new LinearLayout.LayoutParams(U.dp(56), U.dp(56)));
        nameEt = new EditText(this);
        nameEt.setHint("Rule name");
        nameEt.setText(r.name);
        nameEt.setSingleLine();
        nameEt.setTextSize(18);
        nameEt.setTextColor(p.text);
        nameEt.setHintTextColor(p.sub);
        nameEt.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        nameEt.setBackground(U.outline(0, p.line, 16, 1.5f));
        U.pad(nameEt, 16, 14, 16, 14);
        nr.addView(nameEt, U.lp(0, U.WRAP, 12, 0, 0, 0));
        ((LinearLayout.LayoutParams) nameEt.getLayoutParams()).weight = 1f;
        form.addView(nr, U.lp(U.MATCH, U.WRAP, 0, 4, 0, 12));

        // extensions
        LinearLayout c1 = U.card(this, p);
        c1.addView(U.text(this, "File extensions", 18, p.text, true));
        c1.addView(U.text(this, "Which file types this rule applies to", 13, p.sub, false), U.lp(U.WRAP, U.WRAP, 0, 2, 0, 10));
        extFlow = new Flow(this);
        c1.addView(extFlow);
        form.addView(c1, U.lp(U.MATCH, U.WRAP, 0, 0, 0, 12));

        // sources
        LinearLayout c2 = U.card(this, p);
        c2.addView(U.text(this, "Source folders", 18, p.text, true));
        c2.addView(U.text(this, "Scan files from these folders", 13, p.sub, false), U.lp(U.WRAP, U.WRAP, 0, 2, 0, 8));
        srcBox = U.col(this);
        c2.addView(srcBox);
        c2.addView(U.btn(this, "＋  Add folder", p.chip, p.onChip, v -> pickSource()), U.lp(U.WRAP, U.WRAP, 0, 6, 0, 0));
        LinearLayout sr = U.row(this);
        sr.addView(U.text(this, "Scan subfolders", 16, p.text, false), new LinearLayout.LayoutParams(0, U.WRAP, 1f));
        subSw = U.toggle(this, p, r.subfolders, on -> {
        });
        sr.addView(subSw);
        c2.addView(sr, U.lp(U.MATCH, U.WRAP, 0, 12, 0, 0));
        form.addView(c2, U.lp(U.MATCH, U.WRAP, 0, 0, 0, 12));

        // destination
        LinearLayout c3 = U.card(this, p);
        c3.addView(U.text(this, "Destination folder", 18, p.text, true));
        c3.addView(U.text(this, "Matching files go here", 13, p.sub, false), U.lp(U.WRAP, U.WRAP, 0, 2, 0, 8));
        destTv = U.text(this, "", 15, p.text, false);
        c3.addView(destTv, U.lp(U.MATCH, U.WRAP, 2, 0, 0, 8));
        c3.addView(U.btn(this, "Pick folder", p.chip, p.onChip, v -> pickDest()));
        form.addView(c3, U.lp(U.MATCH, U.WRAP, 0, 0, 0, 12));

        // operation
        LinearLayout c4 = U.card(this, p);
        c4.addView(U.text(this, "Operation", 18, p.text, true));
        LinearLayout opr = U.row(this);
        moveChip = U.chip(this, "Move", p.chip, p.onChip);
        copyChip = U.chip(this, "Copy", p.chip, p.onChip);
        moveChip.setOnClickListener(v -> {
            r.copy = false;
            refreshOp();
        });
        copyChip.setOnClickListener(v -> {
            r.copy = true;
            refreshOp();
        });
        opr.addView(moveChip);
        opr.addView(copyChip, U.lp(U.WRAP, U.WRAP, 8, 0, 0, 0));
        c4.addView(opr, U.lp(U.MATCH, U.WRAP, 0, 10, 0, 6));
        c4.addView(U.text(this, "Move takes files out of the source folder. Copy leaves the originals in place.", 13, p.sub, false));
        form.addView(c4, U.lp(U.MATCH, U.WRAP, 0, 0, 0, 12));

        if (!isNew) {
            LinearLayout ex = U.row(this);
            ex.addView(U.link(this, "Add to home screen", p.accent, v -> pinShortcut()));
            ex.addView(new View(this), new LinearLayout.LayoutParams(0, 1, 1f));
            ex.addView(U.link(this, "Delete rule", p.danger, v -> confirmDelete()));
            form.addView(ex, U.lp(U.MATCH, U.WRAP, 0, 0, 0, 0));
        }

        // bottom buttons
        LinearLayout bot = U.row(this);
        TextView cancel = U.btn(this, "Cancel", p.card, p.text, v -> finish());
        cancel.setBackground(U.ripple(U.outline(p.card, p.line, 24, 1.5f)));
        bot.addView(cancel, new LinearLayout.LayoutParams(0, U.WRAP, 1f));
        bot.addView(U.btn(this, "Save", p.accent, p.onAccent, v -> save()), U.lp(0, U.WRAP, 12, 0, 0, 0));
        ((LinearLayout.LayoutParams) bot.getChildAt(1).getLayoutParams()).weight = 1f;
        page.addView(bot, U.lp(U.MATCH, U.WRAP, 16, 8, 16, 12));

        setContentView(page);
        refreshExt();
        refreshSrc();
        refreshDest();
        refreshOp();
    }

    /** Tiny helper so the back arrow gets a ripple + icon. */
    private final class FrameLayoutBack {
        View make() {
            android.widget.FrameLayout f = new android.widget.FrameLayout(RuleEditActivity.this);
            f.setBackground(U.ripple(U.round(0, 24)));
            f.setOnClickListener(v -> finish());
            f.addView(new IconView(RuleEditActivity.this, IconView.BACK, p.text),
                    new android.widget.FrameLayout.LayoutParams(U.dp(24), U.dp(24), Gravity.CENTER));
            return f;
        }
    }

    // ----------------------------------------------------------- refreshers

    private void refreshExt() {
        extFlow.removeAllViews();
        for (final String e : r.exts) {
            TextView t = U.chip(this, (e.equals("*") ? "all files" : e) + "  ×", p.chip, p.onChip);
            t.setOnClickListener(v -> {
                r.exts.remove(e);
                refreshExt();
            });
            extFlow.addView(t);
        }
        TextView add = U.chip(this, "＋ Add type", 0, p.text);
        add.setBackground(U.outline(0, p.line, 16, 1.5f));
        add.setOnClickListener(v -> Dlg.input(this, p, "Add file types", "e.g. jpg, png, pdf", "", "Add", s -> {
            for (String part : s.split("[,\\s]+")) {
                String x = part.trim().toLowerCase(Locale.ROOT);
                while (x.startsWith("*.") || x.startsWith(".")) x = x.substring(x.startsWith("*.") ? 2 : 1);
                if (x.isEmpty() || r.exts.contains(x)) continue;
                if (!x.equals("*") && !x.matches("[a-z0-9_+\\-]+")) continue;
                r.exts.add(x);
            }
            refreshExt();
        }));
        extFlow.addView(add);

        TextView tpl = U.chip(this, "✦ Use template", 0, p.text);
        tpl.setBackground(U.outline(0, p.line, 16, 1.5f));
        tpl.setOnClickListener(v -> pickTemplate());
        extFlow.addView(tpl);
    }

    private void refreshSrc() {
        srcBox.removeAllViews();
        if (r.sources.isEmpty()) {
            srcBox.addView(U.text(this, "No folders yet", 14, p.sub, false));
            return;
        }
        for (final String s : r.sources) {
            LinearLayout row = U.row(this);
            TextView t = U.text(this, "📁  " + U.shortPath(s), 15, p.text, false);
            t.setSingleLine();
            t.setEllipsize(TextUtils.TruncateAt.MIDDLE);
            row.addView(t, new LinearLayout.LayoutParams(0, U.WRAP, 1f));
            row.addView(U.link(this, "×", p.sub, v -> {
                r.sources.remove(s);
                refreshSrc();
            }));
            srcBox.addView(row);
        }
    }

    private void refreshDest() {
        destTv.setText(r.dest.isEmpty() ? "Not set" : "📁  " + U.shortPath(r.dest));
    }

    private void refreshOp() {
        moveChip.setBackground(U.round(r.copy ? p.chip : p.accent, 16));
        moveChip.setTextColor(r.copy ? p.onChip : p.onAccent);
        copyChip.setBackground(U.round(r.copy ? p.accent : p.chip, 16));
        copyChip.setTextColor(r.copy ? p.onAccent : p.onChip);
    }

    // -------------------------------------------------------------- pickers

    private boolean needAccess() {
        if (Perm.has(this)) return false;
        U.toast(this, "Allow file access first");
        Perm.ask(this);
        return true;
    }

    private void pickSource() {
        if (needAccess()) return;
        String start = r.sources.isEmpty() ? null : r.sources.get(r.sources.size() - 1);
        FolderPicker.show(this, p, start, path -> {
            if (!r.sources.contains(path)) r.sources.add(path);
            refreshSrc();
        });
    }

    private void pickDest() {
        if (needAccess()) return;
        FolderPicker.show(this, p, r.dest, path -> {
            r.dest = path;
            refreshDest();
        });
    }

    private void pickIcon() {
        final Dlg d = new Dlg(this, p).title("Rule icon");
        Flow f = new Flow(this);
        for (int i = 0; i < Rule.ICONS.length; i++) {
            final int idx = i;
            TextView t = U.text(this, Rule.ICONS[i], 26, p.text, false);
            t.setGravity(Gravity.CENTER);
            t.setBackground(U.ripple(U.round(idx == r.icon ? p.chip : 0, 16)));
            t.setOnClickListener(v -> {
                r.icon = idx;
                iconTv.setText(Rule.icon(idx));
                d.d.dismiss();
            });
            f.addView(t, new LinearLayout.LayoutParams(U.dp(52), U.dp(52)));
        }
        d.view(f);
        d.box.getChildAt(d.box.getChildCount() - 1).setPadding(0, U.dp(12), 0, U.dp(8));
        d.show();
    }

    private void pickTemplate() {
        final Dlg d = new Dlg(this, p).title("Use template");
        LinearLayout list = U.col(this);
        for (final String[] t : Rule.TEMPLATES) {
            LinearLayout row = U.col(this);
            row.setBackground(U.ripple(U.round(0, 14)));
            U.pad(row, 10, 10, 10, 10);
            row.addView(U.text(this, Rule.icon(Integer.parseInt(t[1])) + "  " + t[0], 17, p.text, true));
            row.addView(U.text(this, t[2].equals("*") ? "all files" : t[2].replace(",", ", "), 13, p.sub, false));
            row.setOnClickListener(v -> {
                r.exts.clear();
                for (String x : t[2].split(",")) r.exts.add(x);
                if (nameEt.getText().toString().trim().isEmpty()) {
                    nameEt.setText(t[0]);
                    r.icon = Integer.parseInt(t[1]);
                    iconTv.setText(Rule.icon(r.icon));
                }
                refreshExt();
                d.d.dismiss();
            });
            list.addView(row);
        }
        ScrollView sv = new ScrollView(this);
        sv.addView(list);
        d.box.addView(sv, new LinearLayout.LayoutParams(U.MATCH, U.dp(360)));
        d.buttons("Cancel", null, null, null);
        d.show();
    }

    // -------------------------------------------------------------- actions

    private static String norm(String s) {
        while (s.length() > 1 && s.endsWith("/")) s = s.substring(0, s.length() - 1);
        return s;
    }

    private void save() {
        r.name = nameEt.getText().toString().trim();
        r.subfolders = subSw.isChecked();
        if (r.exts.isEmpty()) {
            U.toast(this, "Add at least one file type");
            return;
        }
        if (r.sources.isEmpty()) {
            U.toast(this, "Add a source folder");
            return;
        }
        if (r.dest.isEmpty()) {
            U.toast(this, "Pick a destination folder");
            return;
        }
        for (String s : r.sources) {
            if (norm(s).equals(norm(r.dest))) {
                U.toast(this, "Destination can't be one of the source folders");
                return;
            }
        }
        if (r.name.isEmpty()) r.name = "Rule";
        Store.upsert(this, r);
        finish();
    }

    private void confirmDelete() {
        Dlg d = new Dlg(this, p).title("Delete rule?").msg("Files already moved stay where they are.");
        d.buttons("Cancel", null, "Delete", () -> {
            Store.delete(this, r.id);
            finish();
        });
        d.show();
    }

    private void pinShortcut() {
        // Save the latest edits first so the shortcut uses the current name/icon.
        r.name = nameEt.getText().toString().trim();
        if (r.name.isEmpty()) r.name = "Rule";
        r.subfolders = subSw.isChecked();
        Store.upsert(this, r);
        try {
            ShortcutManager sm = getSystemService(ShortcutManager.class);
            if (sm == null || !sm.isRequestPinShortcutSupported()) {
                U.toast(this, "Your launcher doesn't support home screen shortcuts");
                return;
            }
            Bitmap bm = Bitmap.createBitmap(192, 192, Bitmap.Config.ARGB_8888);
            Canvas cv = new Canvas(bm);
            Paint pt = new Paint(Paint.ANTI_ALIAS_FLAG);
            pt.setColor(p.accent);
            cv.drawRoundRect(0, 0, 192, 192, 56, 56, pt);
            pt.setTextSize(104);
            pt.setTextAlign(Paint.Align.CENTER);
            Paint.FontMetrics fm = pt.getFontMetrics();
            cv.drawText(Rule.icon(r.icon), 96, 96 - (fm.ascent + fm.descent) / 2f, pt);

            Intent i = new Intent(this, RunActivity.class);
            i.setAction(Intent.ACTION_VIEW);
            i.putExtra("id", r.id);
            ShortcutInfo info = new ShortcutInfo.Builder(this, "rule_" + r.id)
                    .setShortLabel(r.name)
                    .setLongLabel("Run " + r.name)
                    .setIcon(Icon.createWithBitmap(bm))
                    .setIntent(i)
                    .build();
            sm.requestPinShortcut(info, null);
        } catch (Exception e) {
            U.toast(this, "Couldn't create shortcut");
        }
    }
}

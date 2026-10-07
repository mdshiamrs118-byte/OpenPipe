package com.openpipe.app;

import android.app.Activity;
import android.os.Environment;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/** Tiny in-app folder browser (works with plain file paths, no SAF needed). */
final class FolderPicker {
    private FolderPicker() {
    }

    interface OnPick {
        void pick(String path);
    }

    static void show(final Activity a, final Pal p, String start, final OnPick cb) {
        File s = (start == null || start.isEmpty()) ? null : new File(start);
        if (s == null || !s.isDirectory()) s = Environment.getExternalStorageDirectory();
        final File[] cur = {s};

        final Dlg dlg = new Dlg(a, p).title("Choose folder");
        final TextView path = U.text(a, "", 13, p.sub, false);
        path.setPadding(0, U.dp(4), 0, U.dp(8));
        dlg.box.addView(path);

        final ScrollView sv = new ScrollView(a);
        final LinearLayout list = U.col(a);
        sv.addView(list);
        dlg.box.addView(sv, new LinearLayout.LayoutParams(U.MATCH, U.dp(320)));

        final Runnable[] reload = new Runnable[1];
        reload[0] = () -> {
            list.removeAllViews();
            File d = cur[0];
            path.setText(U.shortPath(d.getAbsolutePath()));

            list.addView(item(a, p, "＋  New folder", p.accent, v ->
                    Dlg.input(a, p, "New folder", "Folder name", "", "Create", name -> {
                        if (name.isEmpty() || name.contains("/")) return;
                        File nf = new File(cur[0], name);
                        if (nf.isDirectory() || nf.mkdirs()) {
                            cur[0] = nf;
                            reload[0].run();
                        } else {
                            U.toast(a, "Couldn't create folder");
                        }
                    })));

            final File parent = d.getParentFile();
            if (parent != null) {
                list.addView(item(a, p, "↑   ..", p.text, v -> {
                    cur[0] = parent;
                    reload[0].run();
                }));
            }

            File[] all = d.listFiles();
            ArrayList<File> dirs = new ArrayList<>();
            if (all != null) for (File f : all) if (f.isDirectory()) dirs.add(f);
            Collections.sort(dirs, new Comparator<File>() {
                @Override
                public int compare(File x, File y) {
                    return x.getName().compareToIgnoreCase(y.getName());
                }
            });
            for (final File f : dirs) {
                list.addView(item(a, p, "📁  " + f.getName(), p.text, v -> {
                    cur[0] = f;
                    reload[0].run();
                }));
            }
            if (dirs.isEmpty()) {
                TextView e = U.text(a, all == null ? "Can't open this folder (no access)" : "No subfolders", 14, p.sub, false);
                U.pad(e, 8, 12, 8, 12);
                list.addView(e);
            }
            sv.scrollTo(0, 0);
        };
        reload[0].run();

        dlg.buttons("Cancel", null, "Select", () -> cb.pick(cur[0].getAbsolutePath()));
        dlg.show();
    }

    private static TextView item(Activity a, Pal p, String label, int color, View.OnClickListener l) {
        TextView t = U.text(a, label, 16, color, false);
        U.pad(t, 8, 12, 8, 12);
        t.setSingleLine();
        t.setEllipsize(android.text.TextUtils.TruncateAt.END);
        t.setBackground(U.ripple(U.round(0, 12)));
        t.setOnClickListener(l);
        return t;
    }
}

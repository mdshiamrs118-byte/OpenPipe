package com.openpipe.app;

import android.content.Context;
import android.media.MediaScannerConnection;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** The whole "engine": find files for a rule, move/copy them, undo. */
final class Mover {
    private Mover() {
    }

    private static final int MAX_ENTRIES = 300;
    private static final int MAX_DEPTH = 12;

    // ------------------------------------------------------------- matching

    static boolean matches(Rule r, String fileName) {
        if (fileName.startsWith(".")) return false; // skip hidden / pending files
        for (String e : r.exts) if (e.equals("*")) return true;
        int dot = fileName.lastIndexOf('.');
        if (dot < 0 || dot == fileName.length() - 1) return false;
        String ext = fileName.substring(dot + 1).toLowerCase(Locale.ROOT);
        for (String e : r.exts) if (e.equals(ext)) return true;
        return false;
    }

    private static String canon(File f) {
        try {
            return f.getCanonicalPath();
        } catch (IOException e) {
            return f.getAbsolutePath();
        }
    }

    static ArrayList<File> scan(Rule r) {
        ArrayList<File> out = new ArrayList<>();
        String dest = r.dest.isEmpty() ? null : canon(new File(r.dest));
        for (String s : r.sources) {
            File dir = new File(s);
            if (dest != null && canon(dir).equals(dest)) continue;
            walk(dir, r, dest, out, 0);
        }
        return out;
    }

    private static void walk(File dir, Rule r, String dest, List<File> out, int depth) {
        File[] fs = dir.listFiles();
        if (fs == null) return;
        for (File f : fs) {
            if (f.isDirectory()) {
                if (!r.subfolders || depth >= MAX_DEPTH || f.getName().startsWith(".")) continue;
                if (dest != null && canon(f).equals(dest)) continue;
                walk(f, r, dest, out, depth + 1);
            } else if (f.isFile() && matches(r, f.getName())) {
                out.add(f);
            }
        }
    }

    // ----------------------------------------------------------- operations

    private static File unique(File dir, String name) {
        File f = new File(dir, name);
        if (!f.exists()) return f;
        int dot = name.lastIndexOf('.');
        String base = dot > 0 ? name.substring(0, dot) : name;
        String ext = dot > 0 ? name.substring(dot) : "";
        for (int i = 1; i < 10000; i++) {
            f = new File(dir, base + " (" + i + ")" + ext);
            if (!f.exists()) return f;
        }
        return f;
    }

    private static void copyFile(File s, File d) throws IOException {
        try (FileInputStream in = new FileInputStream(s); FileOutputStream out = new FileOutputStream(d)) {
            byte[] buf = new byte[64 * 1024];
            int n;
            while ((n = in.read(buf)) > 0) out.write(buf, 0, n);
        }
        //noinspection ResultOfMethodCallIgnored
        d.setLastModified(s.lastModified());
    }

    private static boolean copyOnly(File src, File dst) {
        try {
            copyFile(src, dst);
            return true;
        } catch (IOException e) {
            //noinspection ResultOfMethodCallIgnored
            dst.delete();
            return false;
        }
    }

    private static boolean moveFile(File src, File dst) {
        if (src.renameTo(dst)) return true;
        if (!copyOnly(src, dst)) return false;
        if (src.delete()) return true;
        //noinspection ResultOfMethodCallIgnored
        dst.delete();
        return false;
    }

    private static void rescan(Context c, List<String> paths) {
        if (paths.isEmpty()) return;
        try {
            MediaScannerConnection.scanFile(c.getApplicationContext(),
                    paths.toArray(new String[0]), null, null);
        } catch (Exception ignored) {
        }
    }

    /** Runs a rule synchronously (call from a background thread) and records it in history. */
    static Run run(Context c, Rule r, String trigger) {
        Run run = new Run();
        run.ruleName = r.name;
        run.icon = r.icon;
        run.trigger = trigger;
        run.copy = r.copy;
        try {
            if (r.sources.isEmpty() || r.dest.isEmpty()) {
                run.error = "Rule has no source or destination folder";
            } else {
                File dest = new File(r.dest);
                if (!dest.isDirectory() && !dest.mkdirs()) {
                    run.error = "Can't create the destination folder";
                } else {
                    ArrayList<String> touched = new ArrayList<>();
                    for (File f : scan(r)) {
                        File t = unique(dest, f.getName());
                        boolean ok;
                        try {
                            ok = r.copy ? copyOnly(f, t) : moveFile(f, t);
                        } catch (RuntimeException e) {
                            ok = false;
                        }
                        if (ok) {
                            run.done++;
                            if (run.entries.size() < MAX_ENTRIES)
                                run.entries.add(new String[]{f.getPath(), t.getPath()});
                            touched.add(t.getPath());
                            if (!r.copy) touched.add(f.getPath());
                        } else {
                            run.failed++;
                        }
                    }
                    rescan(c, touched);
                }
            }
        } catch (RuntimeException e) {
            run.error = String.valueOf(e.getMessage());
        }
        Store.addRun(c, run);
        return run;
    }

    /** Reverts a run. Returns the number of files restored. */
    static int undo(Context c, Run run) {
        int n = 0;
        ArrayList<String> touched = new ArrayList<>();
        for (int i = run.entries.size() - 1; i >= 0; i--) {
            File from = new File(run.entries.get(i)[0]);
            File to = new File(run.entries.get(i)[1]);
            if (!to.exists()) continue;
            if (run.copy) {
                if (to.delete()) {
                    n++;
                    touched.add(to.getPath());
                }
                continue;
            }
            File parent = from.getParentFile();
            if (parent == null) continue;
            //noinspection ResultOfMethodCallIgnored
            parent.mkdirs();
            File back = from.exists() ? unique(parent, from.getName()) : from;
            if (moveFile(to, back)) {
                n++;
                touched.add(to.getPath());
                touched.add(back.getPath());
            }
        }
        rescan(c, touched);
        run.undone = true;
        Store.updateRun(c, run);
        return n;
    }
}

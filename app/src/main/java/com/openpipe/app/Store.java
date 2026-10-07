package com.openpipe.app;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/** Tiny persistence layer: rules + settings in SharedPreferences, history in a JSON file. */
final class Store {
    private Store() {
    }

    static SharedPreferences sp(Context c) {
        return c.getApplicationContext().getSharedPreferences("openpipe", Context.MODE_PRIVATE);
    }

    // ---------------------------------------------------------------- rules

    static synchronized ArrayList<Rule> rules(Context c) {
        ArrayList<Rule> out = new ArrayList<>();
        try {
            JSONArray a = new JSONArray(sp(c).getString("rules", "[]"));
            for (int i = 0; i < a.length(); i++) out.add(Rule.fromJson(a.getJSONObject(i)));
        } catch (JSONException ignored) {
        }
        return out;
    }

    static synchronized void saveRules(Context c, List<Rule> list) {
        JSONArray a = new JSONArray();
        for (Rule r : list) a.put(r.toJson());
        sp(c).edit().putString("rules", a.toString()).apply();
    }

    static Rule find(Context c, String id) {
        if (id == null) return null;
        for (Rule r : rules(c)) if (r.id.equals(id)) return r;
        return null;
    }

    static synchronized void upsert(Context c, Rule rule) {
        ArrayList<Rule> l = rules(c);
        boolean found = false;
        for (int i = 0; i < l.size(); i++) {
            if (l.get(i).id.equals(rule.id)) {
                l.set(i, rule);
                found = true;
                break;
            }
        }
        if (!found) l.add(rule);
        saveRules(c, l);
    }

    static synchronized void delete(Context c, String id) {
        ArrayList<Rule> l = rules(c);
        for (int i = 0; i < l.size(); i++) {
            if (l.get(i).id.equals(id)) {
                l.remove(i);
                break;
            }
        }
        saveRules(c, l);
    }

    // -------------------------------------------------------------- history

    private static File hf(Context c) {
        return new File(c.getApplicationContext().getFilesDir(), "history.json");
    }

    static synchronized ArrayList<Run> history(Context c) {
        ArrayList<Run> out = new ArrayList<>();
        File f = hf(c);
        if (!f.exists()) return out;
        try (FileInputStream in = new FileInputStream(f)) {
            ByteArrayOutputStream bo = new ByteArrayOutputStream();
            byte[] buf = new byte[8192];
            int n;
            while ((n = in.read(buf)) > 0) bo.write(buf, 0, n);
            JSONArray a = new JSONArray(bo.toString("UTF-8"));
            for (int i = 0; i < a.length(); i++) out.add(Run.fromJson(a.getJSONObject(i)));
        } catch (IOException | JSONException ignored) {
        }
        return out;
    }

    static synchronized void saveHistory(Context c, List<Run> list) {
        JSONArray a = new JSONArray();
        for (Run r : list) a.put(r.toJson());
        try (FileOutputStream out = new FileOutputStream(hf(c))) {
            out.write(a.toString().getBytes("UTF-8"));
        } catch (IOException ignored) {
        }
    }

    static synchronized void addRun(Context c, Run run) {
        ArrayList<Run> l = history(c);
        l.add(0, run);
        while (l.size() > 40) l.remove(l.size() - 1);
        // Only the newest runs keep their file lists (needed for Undo) to keep storage tiny.
        for (int i = 8; i < l.size(); i++) l.get(i).entries.clear();
        saveHistory(c, l);
    }

    static synchronized void updateRun(Context c, Run run) {
        ArrayList<Run> l = history(c);
        for (int i = 0; i < l.size(); i++) {
            if (l.get(i).id.equals(run.id)) {
                l.set(i, run);
                break;
            }
        }
        saveHistory(c, l);
    }

    static synchronized void clearHistory(Context c) {
        hf(c).delete();
    }
}

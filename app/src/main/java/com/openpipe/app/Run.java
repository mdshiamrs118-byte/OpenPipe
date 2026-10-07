package com.openpipe.app;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.UUID;

/** One execution of a rule (kept in history, used for Undo). */
final class Run {
    String id = UUID.randomUUID().toString();
    String ruleName = "";
    int icon = 0;
    long time = System.currentTimeMillis();
    String trigger = "Manual";
    int done = 0;
    int failed = 0;
    boolean copy = false;
    boolean undone = false;
    String error = null;
    ArrayList<String[]> entries = new ArrayList<>(); // {from, to}

    String status() {
        if (error != null || (failed > 0 && done == 0)) return "Failed";
        if (undone) return "Undone";
        if (failed > 0) return "Partial";
        if (done == 0) return "No changes";
        return "Success";
    }

    JSONObject toJson() {
        JSONObject o = new JSONObject();
        try {
            o.put("id", id);
            o.put("name", ruleName);
            o.put("icon", icon);
            o.put("time", time);
            o.put("trig", trigger);
            o.put("done", done);
            o.put("failed", failed);
            o.put("copy", copy);
            o.put("undone", undone);
            if (error != null) o.put("err", error);
            JSONArray a = new JSONArray();
            for (String[] e : entries) {
                JSONArray p = new JSONArray();
                p.put(e[0]);
                p.put(e[1]);
                a.put(p);
            }
            o.put("entries", a);
        } catch (JSONException ignored) {
        }
        return o;
    }

    static Run fromJson(JSONObject o) {
        Run r = new Run();
        r.id = o.optString("id", r.id);
        r.ruleName = o.optString("name", "");
        r.icon = o.optInt("icon", 0);
        r.time = o.optLong("time", 0);
        r.trigger = o.optString("trig", "Manual");
        r.done = o.optInt("done", 0);
        r.failed = o.optInt("failed", 0);
        r.copy = o.optBoolean("copy", false);
        r.undone = o.optBoolean("undone", false);
        r.error = o.has("err") ? o.optString("err") : null;
        JSONArray a = o.optJSONArray("entries");
        if (a != null) {
            for (int i = 0; i < a.length(); i++) {
                JSONArray p = a.optJSONArray(i);
                if (p != null && p.length() >= 2) r.entries.add(new String[]{p.optString(0), p.optString(1)});
            }
        }
        return r;
    }
}

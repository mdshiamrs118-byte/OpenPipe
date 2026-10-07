package com.openpipe.app;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.UUID;

final class Rule {
    static final String[] ICONS = {"📁", "🖼️", "📸", "🎬", "🎵", "📦", "📄", "⬇️", "📚", "🎮", "⭐", "💡"};

    // name, icon index, extensions
    static final String[][] TEMPLATES = {
            {"Screenshots", "2", "png,jpg,jpeg"},
            {"Images", "1", "jpg,jpeg,png,gif,heic,webp,bmp"},
            {"Documents", "6", "pdf,docx,doc,txt,odt"},
            {"Downloads", "7", "jpg,jpeg,png,mp4,pdf"},
            {"Installables", "5", "apk,apkm,xapk,zip"},
            {"Music", "4", "mp3,flac,wav,aac,m4a,ogg,opus"},
            {"Videos", "3", "mp4,mov,mkv,avi,webm"},
            {"All files", "0", "*"},
    };

    String id = UUID.randomUUID().toString();
    String name = "";
    int icon = 0;
    ArrayList<String> exts = new ArrayList<>();
    ArrayList<String> sources = new ArrayList<>();
    String dest = "";
    boolean copy = false;
    boolean subfolders = false;
    boolean enabled = true;

    static String icon(int i) {
        return ICONS[(i >= 0 && i < ICONS.length) ? i : 0];
    }

    JSONObject toJson() {
        JSONObject o = new JSONObject();
        try {
            o.put("id", id);
            o.put("name", name);
            o.put("icon", icon);
            o.put("exts", new JSONArray(exts));
            o.put("src", new JSONArray(sources));
            o.put("dest", dest);
            o.put("copy", copy);
            o.put("sub", subfolders);
            o.put("on", enabled);
        } catch (JSONException ignored) {
        }
        return o;
    }

    static Rule fromJson(JSONObject o) {
        Rule r = new Rule();
        r.id = o.optString("id", r.id);
        r.name = o.optString("name", "");
        r.icon = o.optInt("icon", 0);
        r.dest = o.optString("dest", "");
        r.copy = o.optBoolean("copy", false);
        r.subfolders = o.optBoolean("sub", false);
        r.enabled = o.optBoolean("on", true);
        JSONArray e = o.optJSONArray("exts");
        if (e != null) for (int i = 0; i < e.length(); i++) r.exts.add(e.optString(i));
        JSONArray s = o.optJSONArray("src");
        if (s != null) for (int i = 0; i < s.length(); i++) r.sources.add(s.optString(i));
        return r;
    }
}

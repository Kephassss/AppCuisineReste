package com.repasdelaflemme.app.data;

import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PrefPantryStats {
    private static final String PREF = "pantry_stats";
    private static final String KEY_MAP = "stats"; // JSON object: id -> {count,last}

    private final SharedPreferences prefs;

    public PrefPantryStats(Context context) {
        this.prefs = context.getSharedPreferences(PREF, Context.MODE_PRIVATE);
    }

    private Map<String, Stat> load() {
        Map<String, Stat> map = new HashMap<>();
        try {
            String raw = prefs.getString(KEY_MAP, "{}");
            JSONObject obj = new JSONObject(raw);
            JSONArray names = obj.names();
            if (names != null) {
                for (int i = 0; i < names.length(); i++) {
                    String id = names.getString(i);
                    JSONObject s = obj.optJSONObject(id);
                    if (s != null) {
                        Stat st = new Stat();
                        st.count = s.optInt("count", 0);
                        st.last = s.optLong("last", 0L);
                        map.put(id, st);
                    }
                }
            }
        } catch (Throwable ignored) {}
        return map;
    }

    private void save(Map<String, Stat> map) {
        try {
            JSONObject obj = new JSONObject();
            for (Map.Entry<String, Stat> e : map.entrySet()) {
                JSONObject s = new JSONObject();
                s.put("count", e.getValue().count);
                s.put("last", e.getValue().last);
                obj.put(e.getKey(), s);
            }
            prefs.edit().putString(KEY_MAP, obj.toString()).apply();
        } catch (Throwable ignored) {}
    }

    public void bump(String id) {
        if (id == null) return;
        Map<String, Stat> m = load();
        Stat st = m.get(id);
        if (st == null) st = new Stat();
        st.count += 1;
        st.last = System.currentTimeMillis();
        m.put(id, st);
        save(m);
    }

    public int getCount(String id) {
        if (id == null) return 0;
        Map<String, Stat> m = load();
        Stat st = m.get(id);
        return st != null ? st.count : 0;
    }

    public List<String> topRecent(int limit) {
        Map<String, Stat> m = load();
        List<Map.Entry<String, Stat>> list = new ArrayList<>(m.entrySet());
        Collections.sort(list, Comparator.comparingLong((Map.Entry<String, Stat> e) -> e.getValue().last).reversed());
        List<String> out = new ArrayList<>();
        for (int i = 0; i < list.size() && out.size() < Math.max(0, limit); i++) {
            if (list.get(i).getValue().last > 0L) out.add(list.get(i).getKey());
        }
        return out;
    }

    public List<String> topFrequent(int limit) {
        Map<String, Stat> m = load();
        List<Map.Entry<String, Stat>> list = new ArrayList<>(m.entrySet());
        Collections.sort(list, Comparator.comparingInt((Map.Entry<String, Stat> e) -> e.getValue().count).reversed());
        List<String> out = new ArrayList<>();
        for (int i = 0; i < list.size() && out.size() < Math.max(0, limit); i++) {
            if (list.get(i).getValue().count > 0) out.add(list.get(i).getKey());
        }
        return out;
    }

    private static class Stat { int count; long last; }
}

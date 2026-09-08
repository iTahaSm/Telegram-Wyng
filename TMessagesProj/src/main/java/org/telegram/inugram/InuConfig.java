package org.telegram.inugram;

import android.content.Context;
import android.content.SharedPreferences;

import org.telegram.messenger.ApplicationLoader;

public final class InuConfig {

    private static volatile SharedPreferences prefs;

    private static SharedPreferences prefs() {
        if (prefs == null) {
            synchronized (InuConfig.class) {
                if (prefs == null) {
                    try {
                        prefs = ApplicationLoader.applicationContext.getSharedPreferences("inugram_config", Context.MODE_PRIVATE);
                    } catch (Throwable ignore) {
                        prefs = null;
                        return null;
                    }
                }
            }
        }
        return prefs;
    }

    private static boolean get(String key, boolean def) {
        SharedPreferences p = prefs();
        return p == null ? def : p.getBoolean(key, def);
    }

    private static float get(String key, float def) {
        SharedPreferences p = prefs();
        return p == null ? def : p.getFloat(key, def);
    }

    public static boolean showSeconds() {
        return get("show_seconds", true);
    }

    public static float animationMultiplier() {
        return get("animation_multiplier", 1.0f);
    }

    public static boolean disableBgParallax() {
        return get("disable_bg_parallax", false);
    }

    private InuConfig() {}
}

package com.chrober.lmsmaterial.nowplaying;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Material Skin's public theme/color identifiers and its built-in color palette.
 * Source: CDrummond/lms-material, ui-settings.js, misc/colors.json and CSS themes.
 * Upstream artwork/styles Copyright (c) Craig Drummond, MIT licensed.
 *
 * The WebView always uses the ORIGINAL CSS served by Material Skin; these
 * colors are for the surrounding native Android settings UI only.
 */
final class MaterialTheme {
    static final String DEFAULT_THEME = "dark";
    static final String DEFAULT_COLOR = "blue";
    static final int COLOR_FIXED = 0;
    static final int COLOR_COVER = 1;
    static final int COLOR_PLAYER = 2;

    static class Option {
        final String key;
        final String label;
        Option(String key, String label) { this.key = key; this.label = label; }
        @Override public String toString() { return label; }
    }

    static final class ColorOption extends Option {
        final String primary;
        final String accent;
        ColorOption(String key, String label, String primary, String accent) {
            super(key, label);
            this.primary = primary;
            this.accent = accent;
        }
    }

    static final class Palette {
        final int background;
        final int text;
        final int secondary;
        final int accent;
        Palette(int bg, int fg, int sub, int accent) {
            this.background=bg; this.text=fg; this.secondary=sub; this.accent=accent;
        }
    }

    static List<Option> builtInThemes() {
        List<Option> options = new ArrayList<>();
        options.add(new Option("auto", "Automatic"));
        options.add(new Option("light", "Light"));
        options.add(new Option("dark", "Dark"));
        options.add(new Option("black", "Black"));
        options.add(new Option("dark-lyrion", "Lyrion"));
        return options;
    }

    static List<ColorOption> builtInColors() {
        List<ColorOption> c = new ArrayList<>();
        c.add(new ColorOption("red", "Red", "#c62828", "#d84141"));
        c.add(new ColorOption("pink", "Pink", "#c2185b", "#fa5788"));
        c.add(new ColorOption("purple", "Purple", "#9c27b0", "#d05ce3"));
        c.add(new ColorOption("deeppurple", "Deep purple", "#512da8", "#8559da"));
        c.add(new ColorOption("indigo", "Indigo", "#303f9f", "#666ad1"));
        c.add(new ColorOption("blue", "Blue", "#1976d2", "#82b1ff"));
        c.add(new ColorOption("lightblue", "Light blue", "#039be5", "#63ccff"));
        c.add(new ColorOption("cyan", "Cyan", "#0097a7", "#56c8d8"));
        c.add(new ColorOption("teal", "Teal", "#00796b", "#48a999"));
        c.add(new ColorOption("green", "Green", "#43a047", "#76d275"));
        c.add(new ColorOption("lightgreen", "Light green", "#7cb342", "#8ec157"));
        c.add(new ColorOption("lime", "Lime", "#afb42b", "#c8cd32"));
        c.add(new ColorOption("yellow", "Yellow", "#e1a205", "#fbc02d"));
        c.add(new ColorOption("amber", "Amber", "#ff8f00", "#ffb24d"));
        c.add(new ColorOption("orange", "Orange", "#ef6c00", "#ff9d3f"));
        c.add(new ColorOption("deeporange", "Deep orange", "#f4511e", "#ff844c"));
        c.add(new ColorOption("brown", "Brown", "#8d6e63", "#be9c91"));
        c.add(new ColorOption("bluegrey", "Blue grey", "#455a64", "#718792"));
        c.add(new ColorOption("lyrion", "Lyrion", "#589e81", "#6cac92"));
        return c;
    }

    /**
     * Skin-defined theme/color identifiers only. User CSS names can contain
     * slashes but must not contain path traversal, query or fragment syntax.
     */
    static boolean safeKey(String value) {
        return value != null && !value.isEmpty() && value.length() <= 120
                && value.matches("[A-Za-z0-9_:/.-]+")
                && !value.contains("..") && !value.startsWith("/")
                && !value.contains("//");
    }

    static String effectiveTheme(String theme, boolean darkSystem) {
        String t = theme == null ? DEFAULT_THEME : theme;
        if (t.startsWith("auto")) return darkSystem ? "dark" : "light";
        return t;
    }

    static String urlTheme(String theme, boolean colorToolbars) {
        String t = safeKey(theme) ? theme : DEFAULT_THEME;
        return t + (colorToolbars ? "-colored" : "");
    }

    static int parseHex(String value, int fallback) {
        if (value == null || !value.matches("#(?:[0-9a-fA-F]{6}|[0-9a-fA-F]{3})")) return fallback;
        String digits = value.substring(1);
        if (digits.length() == 3) {
            digits = "" + digits.charAt(0)+digits.charAt(0)
                    +digits.charAt(1)+digits.charAt(1)
                    +digits.charAt(2)+digits.charAt(2);
        }
        return 0xff000000 | (int) Long.parseLong(digits, 16);
    }

    static Palette palette(String theme, String color, int mode, boolean darkSystem) {
        String active = effectiveTheme(theme, darkSystem);
        boolean light = active.equals("light") || active.contains("/light/");
        int bg = light ? 0xfffafafa : active.equals("black") ? 0xff000000
                : active.equals("dark-lyrion") ? 0xff081f22 : 0xff212121;
        int fg = light ? 0xff202020 : active.equals("dark-lyrion") ? 0xffe3fdc4 : 0xfff5f5f5;
        int sub = light ? 0xff555555 : 0xffbbbbbb;
        int accent = light ? 0xff1976d2 : 0xff82b1ff;
        if (mode == COLOR_FIXED) {
            for (ColorOption option : builtInColors()) {
                if (option.key.equals(color)) {
                    accent = parseHex(light ? option.primary : option.accent, accent);
                    break;
                }
            }
        }
        if (active.equals("dark-lyrion") && "lyrion".equals(color) && mode == COLOR_FIXED)
            accent = 0xffe3fdc4;
        return new Palette(bg, fg, sub, accent);
    }

    static List<Option> mergedThemes(List<Option> custom) {
        List<Option> merged = builtInThemes();
        if (custom != null) for (Option option : custom) {
            if (!safeKey(option.key)) continue;
            boolean exists = false;
            for (Option item : merged) if (item.key.equals(option.key)) { exists = true; break; }
            if (!exists) merged.add(option);
        }
        return merged;
    }

    private MaterialTheme() { }
}

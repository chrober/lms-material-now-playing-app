package com.chrober.lmsmaterial.nowplaying;

import android.content.Context;
import android.test.ActivityInstrumentationTestCase2;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

/** API 22+ native UI tests, running on a clean emulator profile. */
public final class SettingsSmokeTest extends ActivityInstrumentationTestCase2<MainActivity> {
    public SettingsSmokeTest() { super(MainActivity.class); }

    @Override protected void setUp() throws Exception {
        super.setUp();
        getInstrumentation().getTargetContext()
                .getSharedPreferences("material_now_playing", Context.MODE_PRIVATE)
                .edit().clear().commit();
    }

    private static View findText(View root, String text) {
        if (root instanceof TextView && text.contentEquals(((TextView) root).getText()))
            return root;
        if (root instanceof ViewGroup) {
            ViewGroup parent = (ViewGroup) root;
            for (int i = 0; i < parent.getChildCount(); i++) {
                View match = findText(parent.getChildAt(i), text);
                if (match != null) return match;
            }
        }
        return null;
    }

    private static EditText findInput(View root) {
        if (root instanceof EditText) return (EditText) root;
        if (root instanceof ViewGroup) {
            ViewGroup parent = (ViewGroup) root;
            for (int i = 0; i < parent.getChildCount(); i++) {
                EditText match = findInput(parent.getChildAt(i));
                if (match != null) return match;
            }
        }
        return null;
    }

    public void testFirstLaunchShowsNativeSettingsAndSetupActions() {
        MainActivity activity = getActivity();
        View root = activity.findViewById(android.R.id.content);
        assertNotNull(findText(root, "Lyrion Display"));
        assertNotNull(findText(root, "SERVER"));
        assertNotNull(findText(root, "PLAYER"));
        assertNotNull(findText(root, "Save and show Now Playing"));
        assertNotNull(findInput(root));
    }

    public void testInvalidServerCannotBeSaved() throws Throwable {
        MainActivity activity = getActivity();
        View root = activity.findViewById(android.R.id.content);
        EditText input = findInput(root);
        Button save = (Button) findText(root, "Save and show Now Playing");
        assertNotNull(input);
        assertNotNull(save);
        runTestOnUiThread(() -> {
            input.setText("ftp://not-a-lyrion-server");
            save.performClick();
        });
        getInstrumentation().waitForIdleSync();
        String configured = activity.getSharedPreferences(
                "material_now_playing", Context.MODE_PRIVATE).getString("server", "");
        assertEquals("", configured);
    }

    public void testSettingsDoNotKeepDisplayAwake() {
        MainActivity activity = getActivity();
        int flags = activity.getWindow().getAttributes().flags;
        assertEquals(0, flags & WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }
}

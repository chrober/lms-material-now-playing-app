package com.chrober.lmsmaterial.nowplaying;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.hardware.display.DisplayManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.EditorInfo;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/** Native, view-only wrapper for Material Skin's /material/np-only endpoint. */
public final class MainActivity extends Activity {
    private static final String PREFS_NAME = "material_now_playing";
    private static final String KEY_SERVER = "server";
    private static final String KEY_PLAYER = "player_id";
    private static final String KEY_KEEP_AWAKE = "keep_awake";

    private static final long GEAR_VISIBLE_MS = 5000L;
    private static final long GEAR_FADE_IN_MS = 230L;
    private static final long GEAR_FADE_OUT_MS = 700L;
    private static final int BG = Color.rgb(13, 18, 25);
    private static final int TEXT = Color.rgb(241, 246, 249);
    private static final int SUBTEXT = Color.rgb(169, 181, 193);
    private static final int ACCENT = Color.rgb(117, 201, 198);

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final List<LyrionPlayers.Player> playerOptions = new ArrayList<>();
    private final Runnable hideGear = this::fadeGearOut;
    private SharedPreferences prefs;
    private boolean displayVisible;
    private boolean resumed;
    private boolean supportsAwake;
    private int requestSerial;
    private String activeUrl;
    private WebView browser;
    private ImageButton gearButton;
    private EditText serverInput;
    private Spinner playerSpinner;
    private TextView playerStatus;
    private CheckBox awakeCheckbox;
    private Button startButton;

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        DisplayManager manager = (DisplayManager) getSystemService(DISPLAY_SERVICE);
        supportsAwake = getWindow() != null && manager != null
                && manager.getDisplays().length != 0;
        if (!acceptUrlOverride(getIntent())) {
            if (configured()) showDisplay(); else showSettings(null);
        }
    }

    @Override protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        acceptUrlOverride(intent);
    }

    /** Optional ADB convenience; no external URL is ever opened in a different app. */
    private boolean acceptUrlOverride(Intent intent) {
        String raw = intent == null ? null : intent.getStringExtra("url");
        if (TextUtils.isEmpty(raw)) return false;
        try {
            Uri uri = Uri.parse(raw);
            String server = ServerAddress.normalize(uri.getScheme() + "://"
                    + uri.getEncodedAuthority());
            String player = uri.getQueryParameter("player");
            if (server == null || TextUtils.isEmpty(player)) return false;
            prefs.edit().putString(KEY_SERVER, server).putString(KEY_PLAYER, player).apply();
            showDisplay();
            return true;
        } catch (RuntimeException ignored) { return false; }
    }

    @Override protected void onResume() {
        super.onResume();
        resumed = true;
        updateWakeFlag();
    }

    @Override protected void onPause() {
        resumed = false;
        updateWakeFlag();
        super.onPause();
    }

    @Override protected void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        destroyBrowser();
        super.onDestroy();
    }

    @Override public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus && displayVisible) setFullscreen(true);
    }

    private boolean configured() {
        return ServerAddress.normalize(prefs.getString(KEY_SERVER, "")) != null
                && !TextUtils.isEmpty(prefs.getString(KEY_PLAYER, ""));
    }

    private void updateWakeFlag() {
        if (resumed && displayVisible && supportsAwake
                && prefs.getBoolean(KEY_KEEP_AWAKE, true)) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    private void setFullscreen(boolean enabled) {
        getWindow().getDecorView().setSystemUiVisibility(enabled
                ? (View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION)
                : View.SYSTEM_UI_FLAG_VISIBLE);
    }

    private void showSettings(String error) {
        requestSerial++;
        handler.removeCallbacks(hideGear);
        gearButton = null;
        displayVisible = false;
        updateWakeFlag();
        setFullscreen(false);
        destroyBrowser();

        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(BG);
        scroll.setFillViewport(true);
        FrameLayout body = new FrameLayout(this);
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(28), dp(22), dp(28), dp(28));

        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int columnWidth = Math.min(screenWidth, dp(720));
        FrameLayout.LayoutParams columnParams =
                new FrameLayout.LayoutParams(columnWidth, ViewGroup.LayoutParams.WRAP_CONTENT,
                        Gravity.TOP | Gravity.CENTER_HORIZONTAL);
        body.addView(content, columnParams);
        scroll.addView(body);
        setContentView(scroll);

        content.addView(label("Material Now Playing", 28, TEXT, true));
        add(content, label("A dedicated, read-only display for Lyrion Music Server.",
                16, SUBTEXT, false), 6, 20);
        if (error != null)
            add(content, label(error, 16, Color.rgb(255, 170, 148), false), 0, 14);

        content.addView(label("LYRION SERVER", 15, ACCENT, true));
        serverInput = input(prefs.getString(KEY_SERVER, ""),
                "192.168.1.30:9000 or https://lyrion.local");
        add(content, serverInput, 5, 6);
        Button discoveryButton = button("Find Lyrion servers");
        add(content, discoveryButton, 0, 20);
        discoveryButton.setOnClickListener(v -> {
            discoveryButton.setEnabled(false);
            discoveryButton.setText("Searching the local network…");
            final EditText currentInput = serverInput;
            LyrionDiscovery.discover(this, servers -> {
                if (isFinishing() || displayVisible || serverInput != currentInput) return;
                discoveryButton.setEnabled(true);
                discoveryButton.setText("Find Lyrion servers");
                if (servers.isEmpty()) {
                    new AlertDialog.Builder(this)
                            .setMessage("No servers found. Ensure this device is on the same local network, or enter the server address manually.")
                            .setPositiveButton("OK", null).show();
                    return;
                }
                String[] choices = new String[servers.size()];
                for (int i = 0; i < servers.size(); i++)
                    choices[i] = servers.get(i).toString();
                new AlertDialog.Builder(this)
                        .setTitle("Choose Lyrion server")
                        .setItems(choices, (dialog, which) -> {
                            serverInput.setText(servers.get(which).url());
                            loadPlayers();
                        }).show();
            });
        });

        content.addView(label("PLAYER", 15, ACCENT, true));
        playerSpinner = new Spinner(this);
        add(content, playerSpinner, 8, 6);
        playerStatus = label("Choose a server to see its players.", 15, SUBTEXT, false);
        add(content, playerStatus, 0, 8);
        Button refreshButton = button("Refresh player list");
        add(content, refreshButton, 0, 20);
        refreshButton.setOnClickListener(v -> loadPlayers());

        content.addView(label("DISPLAY", 15, ACCENT, true));
        awakeCheckbox = new CheckBox(this);
        awakeCheckbox.setText("Keep screen awake");
        awakeCheckbox.setTextColor(TEXT);
        awakeCheckbox.setTextSize(17);
        awakeCheckbox.setChecked(prefs.getBoolean(KEY_KEEP_AWAKE, true));
        awakeCheckbox.setEnabled(supportsAwake);
        add(content, awakeCheckbox, 5, 4);
        add(content, label(supportsAwake
                ? "Prevents Android's normal display timeout while this app is showing Now Playing. Normal power management resumes when the app is in the background."
                : "Not supported on this device: no applicable Android display was detected.",
                14, SUBTEXT, false), 0, 18);

        startButton = button("Save and show Now Playing");
        startButton.setEnabled(false);
        startButton.setOnClickListener(v -> saveSettings());
        add(content, startButton, 0, 6);

        if (configured()) {
            Button cancel = button("Cancel and return to display");
            cancel.setOnClickListener(v -> showDisplay());
            add(content, cancel, 0, 6);
        }
        add(content, label("Fire TV: Menu opens settings; D-pad reveals the gear. "
                + "Touchscreen: tap the display, then tap the gear.", 14, SUBTEXT, false), 16, 0);

        serverInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                requestSerial++;
                playerOptions.clear();
                showPlayerEntries();
                playerStatus.setText("Refresh players for this server.");
            }
            @Override public void afterTextChanged(Editable s) { }
        });
        showPlayerEntries();
        if (ServerAddress.normalize(serverInput.getText().toString()) != null) {
            loadPlayers();
        } else {
            discoveryButton.requestFocus();
            discoveryButton.performClick();
        }
    }

    private void showPlayerEntries() {
        if (playerSpinner == null) return;
        List<String> labels = new ArrayList<>();
        labels.add("Select a player…");
        for (LyrionPlayers.Player p : playerOptions) labels.add(p.toString());
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, labels);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        playerSpinner.setAdapter(adapter);
        playerSpinner.setEnabled(!playerOptions.isEmpty());
        if (startButton != null) startButton.setEnabled(false);
        playerSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                if (startButton != null) startButton.setEnabled(pos > 0 && pos <= playerOptions.size());
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {
                if (startButton != null) startButton.setEnabled(false);
            }
        });
    }

    private void loadPlayers() {
        String server = ServerAddress.normalize(serverInput.getText().toString());
        if (server == null) {
            playerStatus.setText("Enter a valid server hostname or IP address.");
            return;
        }
        int serial = ++requestSerial;
        playerOptions.clear();
        showPlayerEntries();
        playerStatus.setText("Loading players…");
        final EditText currentInput = serverInput;
        LyrionPlayers.fetch(server, (players, error) -> {
            if (isFinishing() || displayVisible || serial != requestSerial
                    || serverInput != currentInput) return;
            if (error != null) {
                playerStatus.setText("Player lookup failed: " + error);
                return;
            }
            playerOptions.addAll(players);
            showPlayerEntries();
            if (players.isEmpty()) {
                playerStatus.setText("No players found on this server.");
                return;
            }
            playerStatus.setText(players.size() + " player(s) available");
            if (server.equals(ServerAddress.normalize(prefs.getString(KEY_SERVER, "")))) {
                String saved = prefs.getString(KEY_PLAYER, "");
                for (int i = 0; i < players.size(); i++) {
                    if (saved.equals(players.get(i).id)) {
                        playerSpinner.setSelection(i + 1);
                        break;
                    }
                }
            }
        });
    }

    private void saveSettings() {
        String server = ServerAddress.normalize(serverInput.getText().toString());
        int pos = playerSpinner.getSelectedItemPosition();
        if (server == null || pos <= 0 || pos > playerOptions.size()) {
            new AlertDialog.Builder(this)
                    .setMessage("Select a valid server and one of its available players.")
                    .setPositiveButton("OK", null).show();
            return;
        }
        prefs.edit().putString(KEY_SERVER, server)
                .putString(KEY_PLAYER, playerOptions.get(pos - 1).id)
                .putBoolean(KEY_KEEP_AWAKE, supportsAwake && awakeCheckbox.isChecked())
                .apply();
        showDisplay();
    }

    private void showDisplay() {
        if (!configured()) { showSettings("Configure a server and player first."); return; }
        requestSerial++;
        displayVisible = true;
        handler.removeCallbacks(hideGear);
        setFullscreen(true);
        updateWakeFlag();
        destroyBrowser();

        String server = ServerAddress.normalize(prefs.getString(KEY_SERVER, ""));
        String player = prefs.getString(KEY_PLAYER, "");
        activeUrl = server + "/material/np-only?player=" + Uri.encode(player);

        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(Color.BLACK);
        browser = new WebView(this);
        browser.setBackgroundColor(Color.BLACK);
        browser.setFocusable(false);
        browser.setFocusableInTouchMode(false);
        browser.setOnTouchListener((v, event) -> true); // Prevent any WebView interaction.
        browser.setLongClickable(false);
        browser.setVerticalScrollBarEnabled(false);
        browser.setHorizontalScrollBarEnabled(false);
        WebSettings settings = browser.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setSupportZoom(false);
        settings.setBuiltInZoomControls(false);
        settings.setDisplayZoomControls(false);
        settings.setAllowFileAccess(false);
        settings.setAllowContentAccess(false);
        settings.setAllowFileAccessFromFileURLs(false);
        settings.setAllowUniversalAccessFromFileURLs(false);
        settings.setJavaScriptCanOpenWindowsAutomatically(false);
        settings.setSupportMultipleWindows(false);
        root.addView(browser, new FrameLayout.LayoutParams(-1, -1));

        TextView loading = label("Connecting to Lyrion…", 18, TEXT, false);
        loading.setGravity(Gravity.CENTER);
        loading.setBackgroundColor(BG);
        root.addView(loading, new FrameLayout.LayoutParams(-1, -1));
        browser.setWebViewClient(new WebViewClient() {
            @Override public void onPageFinished(WebView view, String url) {
                loading.setVisibility(View.GONE);
            }
            @Override public void onReceivedError(WebView view, WebResourceRequest request,
                                                  WebResourceError error) {
                if (request != null && request.isForMainFrame())
                    connectionFailed("Unable to load Now Playing. Check the server address and network.");
            }
            @SuppressWarnings("deprecation")
            @Override public void onReceivedError(WebView view, int code, String message,
                                                  String failingUrl) {
                if (activeUrl != null && activeUrl.equals(failingUrl))
                    connectionFailed("Unable to connect to Lyrion: " + message);
            }
        });

        gearButton = new ImageButton(this);
        gearButton.setImageResource(R.drawable.ic_gear);
        gearButton.setColorFilter(TEXT);
        gearButton.setContentDescription(getString(R.string.open_settings));
        gearButton.setFocusable(true);
        gearButton.setClickable(true);
        GradientDrawable shape = new GradientDrawable();
        shape.setColor(Color.argb(192, 12, 17, 24));
        shape.setCornerRadius(dp(29));
        gearButton.setBackground(shape);
        gearButton.setAlpha(0f);
        gearButton.setScaleX(0.94f);
        gearButton.setScaleY(0.94f);
        gearButton.setTranslationY(-dp(8));
        gearButton.setVisibility(View.GONE);
        gearButton.setOnClickListener(v -> showSettings(null));
        gearButton.setOnFocusChangeListener((v, focused) -> {
            shape.setStroke(focused ? dp(2) : 0, ACCENT);
            if (focused) revealGear();
        });
        FrameLayout.LayoutParams gearParams = new FrameLayout.LayoutParams(
                dp(56), dp(56), Gravity.TOP | Gravity.START);
        gearParams.setMargins(dp(22), dp(22), 0, 0);
        root.addView(gearButton, gearParams);
        setContentView(root);
        browser.loadUrl(activeUrl);
    }

    private void connectionFailed(String message) {
        if (!displayVisible || isFinishing()) return;
        displayVisible = false;
        updateWakeFlag();
        new AlertDialog.Builder(this)
                .setTitle("Lyrion connection problem")
                .setMessage(message)
                .setPositiveButton("Retry", (d, which) -> showDisplay())
                .setNeutralButton("Settings", (d, which) -> showSettings(null))
                .setNegativeButton("Exit", (d, which) -> finish())
                .setCancelable(false).show();
    }

    /** Fast, softly decelerating fade-in; slower, unobtrusive fade-out. */
    private void revealGear() {
        if (!displayVisible || gearButton == null) return;
        handler.removeCallbacks(hideGear);
        ImageButton gear = gearButton;
        boolean hidden = gear.getVisibility() != View.VISIBLE;
        gear.animate().cancel();
        if (hidden) {
            gear.setAlpha(0f);
            gear.setTranslationY(-dp(8));
            gear.setScaleX(0.94f);
            gear.setScaleY(0.94f);
            gear.setVisibility(View.VISIBLE);
        }
        gear.animate().alpha(0.94f).translationY(0f).scaleX(1f).scaleY(1f)
                .setDuration(GEAR_FADE_IN_MS)
                .setInterpolator(new DecelerateInterpolator(1.7f)).start();
        handler.postDelayed(hideGear, GEAR_VISIBLE_MS);
    }

    private void fadeGearOut() {
        ImageButton gear = gearButton;
        if (gear == null || gear.getVisibility() != View.VISIBLE) return;
        gear.animate().cancel();
        gear.animate().alpha(0f).translationY(-dp(6))
                .scaleX(0.97f).scaleY(0.97f)
                .setDuration(GEAR_FADE_OUT_MS)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .withEndAction(() -> {
                    if (gearButton == gear && gear.getAlpha() < 0.01f)
                        gear.setVisibility(View.GONE);
                }).start();
    }

    @Override public boolean dispatchKeyEvent(KeyEvent event) {
        if (!displayVisible) return super.dispatchKeyEvent(event);
        int code = event.getKeyCode();
        boolean down = event.getAction() == KeyEvent.ACTION_DOWN;
        if (code == KeyEvent.KEYCODE_MENU) {
            if (down && event.getRepeatCount() == 0) showSettings(null);
            return true;
        }
        if (code == KeyEvent.KEYCODE_DPAD_UP || code == KeyEvent.KEYCODE_DPAD_DOWN
                || code == KeyEvent.KEYCODE_DPAD_LEFT || code == KeyEvent.KEYCODE_DPAD_RIGHT) {
            if (down && event.getRepeatCount() == 0) {
                revealGear();
                gearButton.requestFocus();
            }
            return true;
        }
        if (code == KeyEvent.KEYCODE_DPAD_CENTER || code == KeyEvent.KEYCODE_ENTER
                || code == KeyEvent.KEYCODE_BUTTON_A) {
            if (down && event.getRepeatCount() == 0) {
                if (gearButton != null && gearButton.getVisibility() == View.VISIBLE) {
                    showSettings(null);
                } else {
                    revealGear();
                    gearButton.requestFocus();
                }
            }
            return true;
        }
        // Prevent keyboard/media keys from reaching the view-only WebView.
        if (code != KeyEvent.KEYCODE_BACK && code != KeyEvent.KEYCODE_HOME
                && code != KeyEvent.KEYCODE_VOLUME_UP && code != KeyEvent.KEYCODE_VOLUME_DOWN
                && code != KeyEvent.KEYCODE_VOLUME_MUTE && code != KeyEvent.KEYCODE_POWER)
            return true;
        return super.dispatchKeyEvent(event);
    }

    @Override public boolean dispatchTouchEvent(MotionEvent event) {
        if (displayVisible && event.getActionMasked() == MotionEvent.ACTION_DOWN) revealGear();
        return super.dispatchTouchEvent(event);
    }

    @Override public boolean dispatchGenericMotionEvent(MotionEvent event) {
        if (displayVisible && (event.getActionMasked() == MotionEvent.ACTION_HOVER_MOVE
                || event.getActionMasked() == MotionEvent.ACTION_MOVE))
            revealGear();
        return super.dispatchGenericMotionEvent(event);
    }

    @Override public void onBackPressed() {
        if (displayVisible) finish();
        else if (configured()) showDisplay();
        else finish();
    }

    private void destroyBrowser() {
        if (browser == null) return;
        browser.stopLoading();
        browser.setWebViewClient(null);
        if (browser.getParent() instanceof ViewGroup)
            ((ViewGroup) browser.getParent()).removeView(browser);
        browser.destroy();
        browser = null;
    }

    private Button button(String text) {
        Button button = new Button(this);
        button.setText(text);
        button.setTextSize(16);
        button.setAllCaps(false);
        button.setMinHeight(dp(48));
        return button;
    }

    private TextView label(String value, int size, int color, boolean bold) {
        TextView label = new TextView(this);
        label.setText(value);
        label.setTextColor(color);
        label.setTextSize(size);
        if (bold) label.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        return label;
    }

    private EditText input(String value, String hint) {
        EditText input = new EditText(this);
        input.setSingleLine(true);
        input.setText(value);
        input.setHint(hint);
        input.setTextColor(TEXT);
        input.setHintTextColor(SUBTEXT);
        input.setTextSize(18);
        input.setSelectAllOnFocus(true);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI);
        input.setImeOptions(EditorInfo.IME_ACTION_DONE);
        return input;
    }

    private void add(LinearLayout parent, View child, int top, int bottom) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.topMargin = dp(top);
        params.bottomMargin = dp(bottom);
        parent.addView(child, params);
    }

    private int dp(float value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }
}

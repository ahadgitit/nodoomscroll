package com.nodoomscroll.shorts;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;

/** One-screen setup: shows whether the blocker is on and links to the Accessibility settings. */
public class MainActivity extends Activity {

    private TextView status;
    private TextView count;
    private Button enableButton;
    private Switch pauseSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout col = new LinearLayout(this);
        col.setOrientation(LinearLayout.VERTICAL);
        int pad = dp(24);
        col.setPadding(pad, pad, pad, pad);

        TextView title = text("NoDoomScroll", 26);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        col.addView(title);
        col.addView(text("Blocks YouTube Shorts in the YouTube app. Whenever the Shorts player opens, it's closed straight away.", 15));

        status = text("", 18);
        status.setTypeface(Typeface.DEFAULT_BOLD);
        status.setPadding(0, dp(24), 0, dp(8));
        col.addView(status);

        enableButton = new Button(this);
        enableButton.setText("Turn on in Accessibility settings");
        enableButton.setOnClickListener(v ->
                startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)));
        col.addView(enableButton);

        pauseSwitch = new Switch(this);
        pauseSwitch.setText("Pause blocking");
        pauseSwitch.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        pauseSwitch.setPadding(0, dp(16), 0, dp(8));
        pauseSwitch.setOnCheckedChangeListener((b, checked) -> {
            Prefs.setPaused(this, checked);
            refresh();
        });
        col.addView(pauseSwitch);

        count = text("", 15);
        col.addView(count);

        TextView help = text(
                "How to turn it on\n"
                + "1. Tap the button above.\n"
                + "2. Open \"Installed apps\" (or \"Downloaded apps\") and choose NoDoomScroll.\n"
                + "3. Switch it on and tap Allow.\n\n"
                + "Is the switch greyed out, with a message about a restricted setting? Android does this for apps installed outside the Play Store. "
                + "Go to Settings → Apps → NoDoomScroll, tap the ⋮ menu (top right), choose \"Allow restricted settings\", then try again.\n\n"
                + "Phone makers like Xiaomi, Samsung and Oppo may stop background apps to save battery. "
                + "If blocking stops working, set NoDoomScroll's battery use to \"Unrestricted\".", 14);
        help.setPadding(0, dp(24), 0, 0);
        col.addView(help);

        ScrollView scroll = new ScrollView(this);
        scroll.addView(col);
        setContentView(scroll);
    }

    @Override
    protected void onResume() {
        super.onResume();
        refresh();
    }

    private void refresh() {
        boolean enabled = isServiceEnabled();
        boolean paused = Prefs.isPaused(this);
        pauseSwitch.setChecked(paused);
        pauseSwitch.setEnabled(enabled);
        if (!enabled) {
            status.setText("● Off – not turned on yet");
            status.setTextColor(Color.rgb(0xCC, 0x00, 0x00));
            enableButton.setText("Turn on in Accessibility settings");
        } else if (paused) {
            status.setText("● Paused");
            status.setTextColor(Color.rgb(0xE0, 0x8A, 0x00));
            enableButton.setText("Open Accessibility settings");
        } else {
            status.setText("● On – Shorts are blocked");
            status.setTextColor(Color.rgb(0x1E, 0x8E, 0x3E));
            enableButton.setText("Open Accessibility settings");
        }
        int n = Prefs.blockedCount(this);
        count.setText("Shorts blocked so far: " + n);
    }

    private boolean isServiceEnabled() {
        String enabled = Settings.Secure.getString(getContentResolver(),
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
        if (TextUtils.isEmpty(enabled)) return false;
        ComponentName me = new ComponentName(this, ShortsBlockerService.class);
        TextUtils.SimpleStringSplitter split = new TextUtils.SimpleStringSplitter(':');
        split.setString(enabled);
        for (String s : split) {
            ComponentName c = ComponentName.unflattenFromString(s);
            if (me.equals(c)) return true;
        }
        return false;
    }

    private TextView text(String s, int sp) {
        TextView t = new TextView(this);
        t.setText(s);
        t.setTextSize(TypedValue.COMPLEX_UNIT_SP, sp);
        t.setGravity(Gravity.START);
        t.setLineSpacing(0, 1.15f);
        return t;
    }

    private int dp(int v) {
        return Math.round(v * getResources().getDisplayMetrics().density);
    }
}

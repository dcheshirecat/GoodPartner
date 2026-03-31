package com.goodpartner.app;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.WindowCompat;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    private WebView webView;
    public static final String CHANNEL_ID = "good_partner_reminders";

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Keep the app visible above the system bars on phones with gesture/navigation bars.
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.parseColor("#ff6fb8"));
            getWindow().setNavigationBarColor(Color.parseColor("#fffbf0"));
        }

        setContentView(R.layout.activity_main);

        createNotificationChannel();
        requestNotificationPermission();
        scheduleCheckInReminder();

        webView = findViewById(R.id.webview);
        setupWebView();
        webView.loadUrl("file:///android_asset/index.html");
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void setupWebView() {
        WebSettings s = webView.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setDatabaseEnabled(true);
        s.setAllowFileAccess(true);
        s.setAllowFileAccessFromFileURLs(true);
        s.setAllowUniversalAccessFromFileURLs(true);
        s.setCacheMode(WebSettings.LOAD_DEFAULT);
        s.setMediaPlaybackRequiresUserGesture(false);
        s.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);

        // Inject our native bridge
        webView.addJavascriptInterface(new NativeBridge(this), "NativeBridge");

        webView.setWebChromeClient(new WebChromeClient());
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                String url = request.getUrl().toString();
                if (url.startsWith("http://") || url.startsWith("https://")) {
                    // Open external links in browser
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(intent);
                    return true;
                }
                return false;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                // Hide the loading screen
                View splash = findViewById(R.id.splash);
                if (splash != null) {
                    splash.animate().alpha(0f).setDuration(400).withEndAction(() ->
                        splash.setVisibility(View.GONE)).start();
                }
            }
        });
    }

    // Bridge class: maps window.storage calls to Android SharedPreferences
    public class NativeBridge {
        private final Context ctx;

        NativeBridge(Context context) { this.ctx = context; }

        @JavascriptInterface
        public String storageGet(String key) {
            return ctx.getSharedPreferences("gp_store", MODE_PRIVATE)
                      .getString(key, null);
        }

        @JavascriptInterface
        public void storageSet(String key, String value) {
            ctx.getSharedPreferences("gp_store", MODE_PRIVATE)
               .edit().putString(key, value).apply();
        }

        @JavascriptInterface
        public void storageDelete(String key) {
            ctx.getSharedPreferences("gp_store", MODE_PRIVATE)
               .edit().remove(key).apply();
        }

        @JavascriptInterface
        public String storageList(String prefix) {
            java.util.Map<String, ?> all = ctx.getSharedPreferences("gp_store", MODE_PRIVATE).getAll();
            StringBuilder sb = new StringBuilder("[");
            boolean first = true;
            for (String k : all.keySet()) {
                if (prefix == null || prefix.isEmpty() || k.startsWith(prefix)) {
                    if (!first) sb.append(",");
                    sb.append("\"").append(k.replace("\"", "\\\"")).append("\"");
                    first = false;
                }
            }
            sb.append("]");
            return sb.toString();
        }

        @JavascriptInterface
        public void showToast(String msg) {
            runOnUiThread(() -> Toast.makeText(ctx, msg, Toast.LENGTH_SHORT).show());
        }

        @JavascriptInterface
        public void scheduleReminder(int hourOfDay, String message) {
            scheduleAlarm(hourOfDay, message);
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "Good Partner Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription("Daily check-in and task reminders");
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) manager.createNotificationChannel(channel);
        }
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }
    }

    private void scheduleCheckInReminder() {
        scheduleAlarm(8, "Good morning! Time to check in 🌈");
    }

    private void scheduleAlarm(int hour, String message) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        Intent intent = new Intent(this, NotificationReceiver.class);
        intent.putExtra("message", message);
        intent.putExtra("title", "Good Partner");

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
            this, hour,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        // If the time has already passed today, schedule for tomorrow
        if (calendar.getTimeInMillis() < System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,
                        calendar.getTimeInMillis(),
                        AlarmManager.INTERVAL_DAY,
                        pendingIntent);
                }
            } else {
                alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,
                    calendar.getTimeInMillis(),
                    AlarmManager.INTERVAL_DAY,
                    pendingIntent);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        if (webView != null && webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (webView != null) webView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (webView != null) webView.onPause();
    }
}

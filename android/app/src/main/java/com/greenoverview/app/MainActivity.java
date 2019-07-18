package com.greenoverview.app;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.DownloadManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.Settings;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.GeolocationPermissions;
import android.webkit.URLUtil;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.SslErrorHandler;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.core.app.NotificationCompat;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    //Permission variables
    static boolean App_JSCRIPT    = AppConfig.App_JSCRIPT;
    static boolean App_FUPLOAD    = AppConfig.App_FUPLOAD;
    static boolean App_CAMUPLOAD  = AppConfig.App_CAMUPLOAD;
    static boolean App_ONLYCAM      = AppConfig.App_ONLYCAM;
    static boolean App_MULFILE    = AppConfig.App_MULFILE;
    static boolean App_LOCATION   = AppConfig.App_LOCATION;
    static boolean App_RATINGS    = AppConfig.App_RATINGS;
    static boolean App_PULLFRESH    = AppConfig.App_PULLFRESH;
    static boolean App_PBAR       = AppConfig.App_PBAR;
    static boolean App_ZOOM       = AppConfig.App_ZOOM;
    static boolean App_SFORM      = AppConfig.App_SFORM;
    static boolean App_OFFLINE      = AppConfig.App_OFFLINE;
    static boolean App_EXTURL       = AppConfig.App_EXTURL;

    //Security variables
    static boolean App_CERT_VERIFICATION = AppConfig.App_CERT_VERIFICATION;

    //Configuration variables
    private static String APP_URL      = AppConfig.App_URL;
    private String CURR_URL              = APP_URL;
    private static String App_F_TYPE   = AppConfig.App_F_TYPE;

    public static String APP_HOST      = app_host(APP_URL);

    //Careful with these variable names if altering
    WebView app_web_view;
    ProgressBar app_progress;
    TextView app_loading_text;
    NotificationManager app_notification;
    Notification app_notification_new;

    private String app_cam_message;
    private ValueCallback<Uri> app_file_message;
    private ValueCallback<Uri[]> app_file_path;
    private final static int app_file_req = 1;

    private final static int loc_perm = 1;
    private final static int file_perm = 2;

    private SecureRandom random = new SecureRandom();

    private static final String TAG = MainActivity.class.getSimpleName();

    // Geolocation permission request code
    private static final int RP_ACCESS_LOCATION = 1001;

    // global variables for the origin for permission and interface used by the your application to set the Geolocation permission state for an origin
    private String AppGeolocationOrigin;
    private GeolocationPermissions.Callback AppGeolocationCallback;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimary));
            Uri[] results = null;
            if (resultCode == Activity.RESULT_OK) {
                if (requestCode == app_file_req) {
                    if (null == app_file_path) {
                        return;
                    }
                    if (intent == null || intent.getData() == null) {
                        if (app_cam_message != null) {
                            results = new Uri[]{Uri.parse(app_cam_message)};
                        }
                    } else {
                        String dataString = intent.getDataString();
                        if (dataString != null) {
                            results = new Uri[]{ Uri.parse(dataString) };
                        } else {
                            if(App_MULFILE) {
                                if (intent.getClipData() != null) {
                                    final int numSelectedFiles = intent.getClipData().getItemCount();
                                    results = new Uri[numSelectedFiles];
                                    for (int i = 0; i < numSelectedFiles; i++) {
                                        results[i] = intent.getClipData().getItemAt(i).getUri();
                                    }
                                }
                            }
                        }
                    }
                }
            }
            app_file_path.onReceiveValue(results);
            app_file_path = null;
        } else {
            if (requestCode == app_file_req) {
                if (null == app_file_message) return;
                Uri result = intent == null || resultCode != RESULT_OK ? null : intent.getData();
                app_file_message.onReceiveValue(result);
                app_file_message = null;
            }
        }
    }

    public class GeoWebChromeClient extends WebChromeClient {
        @Override
        public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
            final String permission = Manifest.permission.ACCESS_FINE_LOCATION;
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M ||
                    ContextCompat.checkSelfPermission(MainActivity.this, permission) == PackageManager.PERMISSION_GRANTED) {
                // that is you already implement, but it works only
                // we're on SDK < 23 OR user has ALREADY granted permission
                callback.invoke(origin, true, false);
            } else {
                if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, permission)) {
                    // user has denied this permission before and selected [/] DON'T ASK ME AGAIN
                    // TODO Best Practice: show an AlertDialog explaining why the user could allow this permission, then ask again
                } else {
                    // store
                    AppGeolocationOrigin = origin;
                    AppGeolocationCallback = callback;
                    // ask the user for permissions
                    ActivityCompat.requestPermissions(MainActivity.this, new String[] {permission}, RP_ACCESS_LOCATION);
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case RP_ACCESS_LOCATION:
                boolean allow = false;
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // user has allowed these permissions
                    allow = true;
                }
                if (AppGeolocationCallback != null) {
                    // use stored callback and origin for allowing Geolocation permission for WebView
                    AppGeolocationCallback.invoke(AppGeolocationOrigin, allow, false);
                }
                break;
        }
    }

    @SuppressLint({"SetJavaScriptEnabled", "WrongViewCast"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.w("READ_PERM = ",Manifest.permission.READ_EXTERNAL_STORAGE);
        Log.w("WRITE_PERM = ",Manifest.permission.WRITE_EXTERNAL_STORAGE);
        //Prevent the app from being started again when it is still alive in the background
        if (!isTaskRoot()) {
            finish();
            return;
        }

        setContentView(R.layout.activity_main);

        app_web_view = findViewById(R.id.msw_view);

        final SwipeRefreshLayout pullfresh = findViewById(R.id.pullfresh);
        if (App_PULLFRESH) {
            pullfresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    pull_fresh();
                    pullfresh.setRefreshing(false);
                }
            });
            app_web_view.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
                @Override
                public void onScrollChanged() {
                    if (app_web_view.getScrollY() == 0) {
                        pullfresh.setEnabled(true);
                    } else {
                        pullfresh.setEnabled(false);
                    }
                }
            });
        }else{
            pullfresh.setRefreshing(false);
            pullfresh.setEnabled(false);
        }

        if (App_PBAR) {
            app_progress = findViewById(R.id.msw_progress);
        } else {
            findViewById(R.id.msw_progress).setVisibility(View.GONE);
        }
        app_loading_text = findViewById(R.id.msw_loading_text);

        Handler handler = new Handler();

        //Launching app rating request
        if (App_RATINGS) {
            handler.postDelayed(new Runnable() { public void run() { get_rating(); }}, 1000 * 60); //running request after few moments
        }

        //Getting basic device information
        get_info();

        //Webview settings; defaults are customized for best performance
        WebSettings webSettings = app_web_view.getSettings();

        if(!App_OFFLINE){
            // Enable Javascript
            webSettings.setJavaScriptEnabled(App_JSCRIPT);
        }
        webSettings.setSaveFormData(App_SFORM);
        webSettings.setSupportZoom(App_ZOOM);
        webSettings.setGeolocationEnabled(App_LOCATION);
        webSettings.setAllowFileAccess(true);
        webSettings.setAllowFileAccessFromFileURLs(true);
        webSettings.setAllowUniversalAccessFromFileURLs(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setDomStorageEnabled(true);
        // Enable geolocation
        webSettings.setGeolocationEnabled(true);
        app_web_view.setWebChromeClient(new GeoWebChromeClient());

        app_web_view.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return true;
            }
        });
        app_web_view.setHapticFeedbackEnabled(false);

        app_web_view.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimeType, long contentLength) {

                if(!check_permission(2)){
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, file_perm);
                }else {
                    DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));

                    request.setMimeType(mimeType);
                    String cookies = CookieManager.getInstance().getCookie(url);
                    request.addRequestHeader("cookie", cookies);
                    request.addRequestHeader("User-Agent", userAgent);
                    request.setDescription(getString(R.string.dl_downloading));
                    request.setTitle(URLUtil.guessFileName(url, contentDisposition, mimeType));
                    request.allowScanningByMediaScanner();
                    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                    request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, URLUtil.guessFileName(url, contentDisposition, mimeType));
                    DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                    assert dm != null;
                    dm.enqueue(request);
                    Toast.makeText(getApplicationContext(), getString(R.string.dl_downloading2), Toast.LENGTH_LONG).show();
                }
            }
        });

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
        webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        app_web_view.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        app_web_view.setVerticalScrollBarEnabled(false);
        // Force links and redirects to open in the WebView instead of in a browser
        app_web_view.setWebViewClient(new Callback());

        //Rendering the default URL
        app_view(APP_URL, false);

        app_web_view.setWebChromeClient(new WebChromeClient() {
            //Handling input[type="file"]
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams){
                if(check_permission(2) && check_permission(3)) {
                    if (App_FUPLOAD) {
                        if (app_file_path != null) {
                            app_file_path.onReceiveValue(null);
                        }
                        app_file_path = filePathCallback;
                        Intent takePictureIntent = null;
                        if (App_CAMUPLOAD) {
                            takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                            if (takePictureIntent.resolveActivity(MainActivity.this.getPackageManager()) != null) {
                                File photoFile = null;
                                try {
                                    photoFile = create_image();
                                    takePictureIntent.putExtra("PhotoPath", app_cam_message);
                                } catch (IOException ex) {
                                    Log.e(TAG, "Image file creation failed", ex);
                                }
                                if (photoFile != null) {
                                    app_cam_message = "file:" + photoFile.getAbsolutePath();
                                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                                } else {
                                    takePictureIntent = null;
                                }
                            }
                        }
                        Intent contentSelectionIntent = new Intent(Intent.ACTION_GET_CONTENT);
                        if (!App_ONLYCAM) {
                            contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE);
                            contentSelectionIntent.setType(App_F_TYPE);
                            if (App_MULFILE) {
                                contentSelectionIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                            }
                        }
                        Intent[] intentArray;
                        if (takePictureIntent != null) {
                            intentArray = new Intent[]{takePictureIntent};
                        } else {
                            intentArray = new Intent[0];
                        }

                        Intent chooserIntent = new Intent(Intent.ACTION_CHOOSER);
                        chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent);
                        chooserIntent.putExtra(Intent.EXTRA_TITLE, getString(R.string.fl_chooser));
                        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray);
                        startActivityForResult(chooserIntent, app_file_req);
                    }
                    return true;
                }else{
                    get_file();
                    return false;
                }
            }


            //Getting webview rendering progress
            @Override
            public void onProgressChanged(WebView view, int p) {
                if (App_PBAR) {
                    app_progress.setProgress(p);
                    if (p == 100) {
                        app_progress.setProgress(0);
                    }
                }
            }

            // overload the geoLocations permissions prompt to always allow instantly as app permission was granted previously
            public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
                if(Build.VERSION.SDK_INT < 23 || check_permission(1)){
                    // location permissions were granted previously so auto-approve
                    callback.invoke(origin, true, false);
                } else {
                    // location permissions not granted so request them
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, loc_perm);
                }
            }
        });
        if (getIntent().getData() != null) {
            String path     = getIntent().getDataString();
            /*
            If you want to check or use specific directories or schemes or hosts

            Uri data        = getIntent().getData();
            String scheme   = data.getScheme();
            String host     = data.getHost();
            List<String> pr = data.getPathSegments();
            String param1   = pr.get(0);
            */
            app_view(path, false);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        app_web_view.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        app_web_view.onResume();
        //Coloring the "recent apps" tab header; doing it onResume, as an insurance
        if (Build.VERSION.SDK_INT >= 23) {
            Bitmap bm = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
            ActivityManager.TaskDescription taskDesc;
            taskDesc = new ActivityManager.TaskDescription(getString(R.string.app_name), bm, getColor(R.color.colorPrimary));
            MainActivity.this.setTaskDescription(taskDesc);
        }
        //get_location();
    }

    // Prevent the back-button from closing the app
    @Override
    public void onBackPressed() {
        if(app_web_view.canGoBack()) {
            app_web_view.goBack();
        } else {
            super.onBackPressed();
        }
    }

    //Setting activity layout visibility
    private class Callback extends WebViewClient {
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            //get_location();
        }

        public void onPageFinished(WebView view, String url) {
            findViewById(R.id.msw_welcome).setVisibility(View.GONE);
            findViewById(R.id.msw_view).setVisibility(View.VISIBLE);
        }
        //For android below API 23
        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            Toast.makeText(getApplicationContext(), getString(R.string.went_wrong), Toast.LENGTH_SHORT).show();
            app_view("file:///android_asset/error.html", false);
        }

        //Overriding webview URLs
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            CURR_URL = url;
            return url_actions(view, url);
        }

        //Overriding webview URLs for API 23+ [suggested by github.com/JakePou]
        @TargetApi(Build.VERSION_CODES.N)
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            CURR_URL = request.getUrl().toString();
            return url_actions(view, request.getUrl().toString());
        }

        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            if(App_CERT_VERIFICATION) {
                super.onReceivedSslError(view, handler, error);
            } else {
                handler.proceed(); // Ignore SSL certificate errors
            }
        }
    }

    //Random ID creation function to help get fresh cache every-time webview reloaded
    public String random_id() {
        return new BigInteger(130, random).toString(32);
    }

    //Opening URLs inside webview with request
    void app_view(String url, Boolean tab) {
        if (tab) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            startActivity(intent);
        } else {
            if(url.contains("?")){ // check to see whether the url already has query parameters and handle appropriately.
                url += "&";
            } else {
                url += "?";
            }
            url += "rid="+random_id();
            app_web_view.loadUrl(url);
        }
    }

    //Actions based on shouldOverrideUrlLoading
    public boolean url_actions(WebView view, String url){
        boolean a = true;
        //Show toast error if not connected to the network
        if (!App_OFFLINE && !DetectConnection.isInternetAvailable(MainActivity.this)) {
            Toast.makeText(getApplicationContext(), getString(R.string.check_connection), Toast.LENGTH_SHORT).show();

            //Use this in a hyperlink to redirect back to default URL :: href="refresh:android"
        } else if (url.startsWith("refresh:")) {
            String ref_sch = (Uri.parse(url).toString()).replace("refresh:","");
            if(ref_sch.matches("URL")){
                CURR_URL = APP_URL;
            }
            pull_fresh();

            //Use this in a hyperlink to launch default phone dialer for specific number :: href="tel:+919876543210"
        } else if (url.startsWith("tel:")) {
            Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse(url));
            startActivity(intent);

            //Use this to open your apps page on google play store app :: href="rate:android"
        } else if (url.startsWith("rate:")) {
            final String app_package = getPackageName(); //requesting app package name from Context or Activity object
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + app_package)));
            } catch (ActivityNotFoundException anfe) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + app_package)));
            }

            //Sharing content from your webview to external apps :: href="share:URL" and remember to place the URL you want to share after share:___
        } else if (url.startsWith("share:")) {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_SUBJECT, view.getTitle());
            intent.putExtra(Intent.EXTRA_TEXT, view.getTitle()+"\nVisit: "+(Uri.parse(url).toString()).replace("share:",""));
            startActivity(Intent.createChooser(intent, getString(R.string.share_w_friends)));

            //Use this in a hyperlink to exit your app :: href="exit:android"
        } else if (url.startsWith("exit:")) {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);

            //Getting location for offline files
        } else if (url.startsWith("offloc:")) {
            //String offloc = APP_URL+"?loc="+get_location();
            String offloc = APP_URL;
            app_view(offloc,false);
            Log.d("OFFLINE LOC REQ",offloc);

            //Opening external URLs in android default web browser
        } else if (App_EXTURL && !app_host(url).equals(APP_HOST)) {
            app_view(url,true);
        } else {
            a = false;
        }
        return a;
    }

    //Getting host name
    public static String app_host(String url){
        if (url == null || url.length() == 0) {
            return "";
        }
        int dslash = url.indexOf("//");
        if (dslash == -1) {
            dslash = 0;
        } else {
            dslash += 2;
        }
        int end = url.indexOf('/', dslash);
        end = end >= 0 ? end : url.length();
        int port = url.indexOf(':', dslash);
        end = (port > 0 && port < end) ? port : end;
        Log.w("URL Host: ",url.substring(dslash, end));
        return url.substring(dslash, end);
    }

    //Reloading current page
    public void pull_fresh(){
        app_view((!CURR_URL.equals("")?CURR_URL:APP_URL),false);
    }

    //Getting device basic information
    public void get_info(){
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        cookieManager.setCookie(APP_URL, "DEVICE=android");
        cookieManager.setCookie(APP_URL, "DEV_API=" + Build.VERSION.SDK_INT);
    }

    //Checking permission for storage and camera for writing and uploading images
    public void get_file(){
        String[] perms = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA};

        //Checking for storage permission to write images for upload
        if (App_FUPLOAD && App_CAMUPLOAD && !check_permission(2) && !check_permission(3)) {
            ActivityCompat.requestPermissions(MainActivity.this, perms, file_perm);

            //Checking for WRITE_EXTERNAL_STORAGE permission
        } else if (App_FUPLOAD && !check_permission(2)) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, file_perm);

            //Checking for CAMERA permissions
        } else if (App_CAMUPLOAD && !check_permission(3)) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA}, file_perm);
        }
    }

    /*
    //Using cookies to update user locations
    public String get_location(){
        String newloc = "0,0";
        //Checking for location permissions
        if (App_LOCATION && (Build.VERSION.SDK_INT < 23 || check_permission(1))) {
            GPSTrack gps;
            gps = new GPSTrack(MainActivity.this);
            double latitude = gps.getLatitude();
            double longitude = gps.getLongitude();
            if (gps.canGetLocation()) {
                if (latitude != 0 || longitude != 0) {
                    if(!App_OFFLINE) {
                        CookieManager cookieManager = CookieManager.getInstance();
                        cookieManager.setAcceptCookie(true);
                        cookieManager.setCookie(APP_URL, "lat=" + latitude);
                        cookieManager.setCookie(APP_URL, "long=" + longitude);
                    }
                    //Log.w("New Updated Location:", latitude + "," + longitude);  //enable to test dummy latitude and longitude
                    newloc = latitude+","+longitude;
                } else {
                    Log.w("New Updated Location:", "NULL");
                }
            } else {
                show_notification(1, 1);
                Log.w("New Updated Location:", "FAIL");
            }
        }
        return newloc;
    }
    */

    //Checking if particular permission is given or not
    public boolean check_permission(int permission){
        switch(permission){
            case 1:
                return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;

            case 2:
                return ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;

            case 3:
                return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;

        }
        return false;
    }

    //Creating image file for upload
    private File create_image() throws IOException {
        @SuppressLint("SimpleDateFormat")
        String file_name    = new SimpleDateFormat("yyyy_mm_ss").format(new Date());
        String new_name     = "file_"+file_name+"_";
        File sd_directory   = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(new_name, ".jpg", sd_directory);
    }

    //Launching app rating dialoge [developed by github.com/hotchemi]
    public void get_rating() {
        if (DetectConnection.isInternetAvailable(MainActivity.this)) {
            AppRate.with(this)
                    .setStoreType(StoreType.GOOGLEPLAY)     //default is Google Play, other option is Amazon App Store
                    .setInstallDays(AppConfig.ASWR_DAYS)
                    .setLaunchTimes(AppConfig.ASWR_TIMES)
                    .setRemindInterval(AppConfig.ASWR_INTERVAL)
                    .setTitle(R.string.rate_dialog_title)
                    .setMessage(R.string.rate_dialog_message)
                    .setTextLater(R.string.rate_dialog_cancel)
                    .setTextNever(R.string.rate_dialog_no)
                    .setTextRateNow(R.string.rate_dialog_ok)
                    .monitor();
            AppRate.showRateDialogIfMeetsConditions(this);
        }
        //for more customizations, look for AppRate and DialogManager
    }

    //Creating custom notifications with IDs
    public void show_notification(int type, int id) {
        long when = System.currentTimeMillis();
        app_notification = (NotificationManager) MainActivity.this.getSystemService(Context.NOTIFICATION_SERVICE);
        Intent i = new Intent();
        if (type == 1) {
            i.setClass(MainActivity.this, MainActivity.class);
        } else if (type == 2) {
            i.setAction(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        } else {
            i.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            i.addCategory(Intent.CATEGORY_DEFAULT);
            i.setData(Uri.parse("package:" + MainActivity.this.getPackageName()));
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            i.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        }
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(MainActivity.this, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);

        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(MainActivity.this, "");
        switch(type){
            case 1:
                builder.setTicker(getString(R.string.app_name));
                builder.setContentTitle(getString(R.string.loc_fail));
                builder.setContentText(getString(R.string.loc_fail_text));
                builder.setStyle(new NotificationCompat.BigTextStyle().bigText(getString(R.string.loc_fail_more)));
                builder.setVibrate(new long[]{350,350,350,350,350});
                builder.setSmallIcon(R.mipmap.ic_launcher);
                break;

            case 2:
                builder.setTicker(getString(R.string.app_name));
                builder.setContentTitle(getString(R.string.loc_perm));
                builder.setContentText(getString(R.string.loc_perm_text));
                builder.setStyle(new NotificationCompat.BigTextStyle().bigText(getString(R.string.loc_perm_more)));
                builder.setVibrate(new long[]{350, 700, 350, 700, 350});
                builder.setSound(alarmSound);
                builder.setSmallIcon(R.mipmap.ic_launcher);
                break;
        }
        builder.setOngoing(false);
        builder.setAutoCancel(true);
        builder.setContentIntent(pendingIntent);
        builder.setWhen(when);
        builder.setContentIntent(pendingIntent);
        app_notification_new = builder.build();
        app_notification.notify(id, app_notification_new);
    }

    /*
    //Checking if users allowed the requested permissions or not
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults){
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
               get_location();
            }
        }
    }
    */

    //Action on back key tap/click
    @Override
    public boolean onKeyDown(int keyCode, @NonNull KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                if (app_web_view.canGoBack()) {
                    app_web_view.goBack();
                } else {
                    finish();
                }
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState ){
        super.onSaveInstanceState(outState);
        app_web_view.saveState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState){
        super.onRestoreInstanceState(savedInstanceState);
        app_web_view.restoreState(savedInstanceState);
    }


}



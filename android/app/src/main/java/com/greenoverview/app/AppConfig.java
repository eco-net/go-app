package com.greenoverview.app;

class AppConfig {

    /* -- CONFIG VARIABLES -- */

    //complete URL of your website
    static String App_URL          = "https://grontoverblik.dk/";


    /* -- PERMISSION VARIABLES -- */

    // enable JavaScript for webview
    static boolean App_JSCRIPT     = true;

    // upload file from webview
    static boolean App_FUPLOAD     = true;

    // enable upload from camera for photos
    static boolean App_CAMUPLOAD   = true;

    // incase you want only camera files to upload
    static boolean App_ONLYCAM       = false;

    // upload multiple files in webview
    static boolean App_MULFILE     = true;

    // track GPS locations
    static boolean App_LOCATION    = true;

    // show ratings dialog; auto configured
    // edit method get_rating() for customizations
    static boolean App_RATINGS     = true;

    // pull refresh current url
    static boolean App_PULLFRESH     = true;

    // show progress bar in App
    static boolean App_PBAR        = true;

    // zoom control for webpages view
    static boolean App_ZOOM        = false;

    // save form cache and auto-fill information
    static boolean App_SFORM       = false;

    // whether the loading webpages are offline or online
    static boolean App_OFFLINE     = false;

    // open external url with default browser instead of App webview
    static boolean App_EXTURL      = true;


    /* -- SECURITY VARIABLES -- */

    // verify whether HTTPS port needs certificate verification
    static boolean App_CERT_VERIFICATION = true;

    //to upload any file type using "*/*"; check file type references for more
    static String App_F_TYPE       = "*/*";


    /* -- RATING SYSTEM VARIABLES -- */

    static int ASWR_DAYS            = 3;    // after how many days of usage would you like to show the dialoge
    static int ASWR_TIMES           = 10;  // overall request launch times being ignored
    static int ASWR_INTERVAL        = 2;   // reminding users to rate after days interval
}

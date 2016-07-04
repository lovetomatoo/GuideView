package com.ghx.guideviewdemo.utils;

import android.util.Log;

/**
 * Created by qmtv on 2016/7/4.
 */
public class LogUtils {

    private static final String TAG = "GHX";
    private static boolean isDebug = true;

    public static void debug(String str) {
        if (isDebug) {
            Log.d(TAG, str);
        }
    }

}

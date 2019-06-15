package viettel.cyberspace.speechrecognize.speedtotextcyberspace;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * Created by Toan on 3/27/2018.
 */

public class MySharePreferenceVoice {
    private static final String HOST = "HOST";
    private static final String PORT = "PORT";
    private static final String PARSE_JSON = "PARSE_JSON";
    private static final String LIEN_TUC = "LIEN_TUC";
    private static final String IS_16KHZ = "IS_16KHZ";

    private static String TAG = "MySharePreferenceVoice";

    public static void setHostAndPort(Context context, String host, int port, boolean lientuc, boolean parseJson, boolean is16kHz) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(HOST, host);
        editor.putInt(PORT, port);
        editor.putBoolean(LIEN_TUC, lientuc);
        editor.putBoolean(PARSE_JSON, parseJson);
        editor.putBoolean(IS_16KHZ, is16kHz);
        editor.commit();
        Log.d(TAG, "setHostAndPort: setup");
    }

    public static String getHost(Context context) {
        try {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
            Log.d(TAG, "getHost: ");
            return preferences.getString(HOST, UtilsVoice.HOST_DEFAULT);
        } catch (Exception e) {
            return UtilsVoice.HOST_DEFAULT;
        }

    }

    public static int getPort(Context context) {
        try {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
            return preferences.getInt(PORT, UtilsVoice.PORT_DEFAULT);
        } catch (Exception e) {
            return UtilsVoice.PORT_DEFAULT;
        }
    }

    public static boolean isParseJson(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getBoolean(PARSE_JSON, UtilsVoice.PARSE_JSON);
    }


    public static boolean isLienTuc(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getBoolean(LIEN_TUC, UtilsVoice.LIEN_TUC);
    }

    public static boolean is16kHz(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getBoolean(IS_16KHZ, UtilsVoice.IS_16KHZ);
    }
}

package marx.jr.fingerprintdemo;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Administrator on 2016/12/22.
 */

public class MApplication extends Application
{
    private static String FINGER_PRINT = "HasFingerPrint";
    private static boolean HasFingerPrint = false;

    @Override
    public void onCreate()
    {
        super.onCreate();


        SharedPreferences sp = this.getSharedPreferences(FINGER_PRINT, Context.MODE_PRIVATE);
        if (sp.contains(FINGER_PRINT))
        {
            HasFingerPrint = sp.getBoolean(FINGER_PRINT, false);
            return;
        }
        SharedPreferences.Editor editor = sp.edit();
        try
        {
            Class.forName("android.hardware.fingerprint.FingerprintManager");
            HasFingerPrint =true;
            editor.putBoolean(FINGER_PRINT,true);
        } catch (Exception e)
        {
            HasFingerPrint = false;
            editor.putBoolean(FINGER_PRINT,false);
        }
        editor.commit();

    }

    public static boolean hasFingerPrintHardware()
    {
        return HasFingerPrint;
    }
}

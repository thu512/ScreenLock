package com.gsitm.screenlock;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v4.hardware.fingerprint.FingerprintManagerCompat;

/**
 * Created by lcj on 2018. 9. 16..
 */

public class Util{
    private static final Util ourInstance = new Util();

    public static Util getInstance() {
        return ourInstance;
    }

    private Util() {
    }


    // 값 불러오기
    public boolean isLock(Context context){
        SharedPreferences pref = context.getSharedPreferences("pref", Context.MODE_PRIVATE);
        return pref.getBoolean("screenLock", false);
    }

    // 값 저장하기
    public void savePrefLock(Context context, boolean isLock){
        SharedPreferences pref = context.getSharedPreferences("pref", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean("screenLock", isLock);
        editor.commit();
    }


    //지문 인식
    public static boolean isFingerprintAuthAvailable(Context context) {
        FingerprintManagerCompat mFingerprintManager;
        mFingerprintManager = FingerprintManagerCompat.from(context);
        if (mFingerprintManager.isHardwareDetected() && mFingerprintManager.hasEnrolledFingerprints()) {
            return true;
        } else {
            return false;
        }
    }
    public static FingerprintManagerCompat getFingerprintManagerCompat(Context context){
        FingerprintManagerCompat mFingerprintManager;
        mFingerprintManager = FingerprintManagerCompat.from(context);
        return mFingerprintManager;
    }

}

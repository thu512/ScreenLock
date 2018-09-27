package com.gsitm.screenlock;

import android.os.Bundle;
import android.util.Log;

import com.lcj.forgerycheck.ForgeryCheckTest;


public class MainActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ForgeryCheckTest test = new ForgeryCheckTest();
        Log.d("Signiture: ",test.getSignaiture(this));

    }
}

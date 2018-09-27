package com.gsitm.screenlock;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by lcj on 2018. 9. 16..
 */

public class BaseActivity extends AppCompatActivity {



    @Override
    protected void onResume() {
        super.onResume();
        if(!Util.getInstance().isLock(this)){
            startActivity(new Intent(this, ScreenLockActivity.class));
        }


    }


    @Override
    protected void onStop() {
        super.onStop();
        Util.getInstance().savePrefLock(this,false);
    }
}

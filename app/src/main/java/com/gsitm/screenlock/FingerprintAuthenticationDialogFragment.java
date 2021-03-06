/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */

package com.gsitm.screenlock;

import android.app.DialogFragment;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.hardware.fingerprint.FingerprintManagerCompat;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import static android.os.Build.VERSION_CODES.LOLLIPOP;

/**
 * A dialog which uses fingerprint APIs to authenticate the user, and falls back to password
 * authentication if fingerprint is not available.
 */
public class FingerprintAuthenticationDialogFragment extends DialogFragment
        implements TextView.OnEditorActionListener, FingerprintUiHelper.Callback {

    private Button mCancelButton;
    private Button mSecondDialogButton;
    private View mFingerprintContent;
    private View mBackupContent;
    private EditText mPassword;
    private CheckBox mUseFingerprintFutureCheckBox;
    private TextView mPasswordDescriptionTextView;
    private TextView mNewFingerprintEnrolledTextView;

    private FingerprintManagerCompat.CryptoObject mCryptoObject;
    private FingerprintUiHelper mFingerprintUiHelper;
    private MainActivity mActivity;

    private InputMethodManager mInputMethodManager;

    private SecretAuthorize secretAuthorize;
    private int mStage = Constant.FINGERPRINT;
    private Animation vibrateAnim;
    private SharedPreferences mSharedPreferences;

    public FingerprintAuthenticationDialogFragment(){
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Do not create a new Fragment when the Activity is re-created such as orientation changes.
        setRetainInstance(true);

        if (Build.VERSION.SDK_INT > LOLLIPOP) {
            setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Material_Light_Dialog);
        } else {

        }
        vibrateAnim = AnimationUtils.loadAnimation(getActivity(),R.anim.vibrate_anim);
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        getDialog().setTitle(getString(R.string.sign_in));
        View v = inflater.inflate(R.layout.fingerprint_dialog_container, container, false);
        mCancelButton = (Button) v.findViewById(R.id.cancel_button);
        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });


        //다이얼로그 - Constant.FINGERPRINT-버튼 비밀번호 입력으로 변경 / Constant.PASSWORD - ok
        mSecondDialogButton = (Button) v.findViewById(R.id.second_dialog_button);
        mSecondDialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mStage == Constant.FINGERPRINT) {
                    goToBackup();
                } else {
                    verifyPassword();
                }
            }
        });
        mFingerprintContent = v.findViewById(R.id.fingerprint_container);
        mBackupContent = v.findViewById(R.id.backup_container);
        mPassword = (EditText) v.findViewById(R.id.password);
        mPassword.setOnEditorActionListener(this);
        mPasswordDescriptionTextView = (TextView) v.findViewById(R.id.password_description);
        mUseFingerprintFutureCheckBox = (CheckBox)
                v.findViewById(R.id.use_fingerprint_in_future_check);
        mNewFingerprintEnrolledTextView = (TextView)
                v.findViewById(R.id.new_fingerprint_enrolled_description);

        mFingerprintUiHelper = new FingerprintUiHelper(getActivity().getApplicationContext(),
                (ImageView) v.findViewById(R.id.fingerprint_icon),
                (TextView) v.findViewById(R.id.fingerprint_status), this);
        updateStage();

        // If fingerprint authentication is not available, switch immediately to the backup
        // (password) screen.
        if (!Util.isFingerprintAuthAvailable(getActivity().getApplicationContext())) {
            goToBackup();
        }
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mStage == Constant.FINGERPRINT) {
            mFingerprintUiHelper.startListening(mCryptoObject);
        }
    }


    @Override
    public void onPause() {
        super.onPause();
        mFingerprintUiHelper.stopListening();
    }


    /**
     * Sets the crypto object to be passed in when authenticating with fingerprint.
     */
    public void setCryptoObject(FingerprintManagerCompat.CryptoObject cryptoObject) {
        mCryptoObject = cryptoObject;
    }


    //비밀번호 입력 화면으로 변경
    private void goToBackup() {
        mStage = Constant.PASSWORD;
        updateStage();
        mPassword.requestFocus();


        // Fingerprint is not used anymore. Stop listening for it.
        mFingerprintUiHelper.stopListening();
    }


    //패스워드 확인
    private void verifyPassword() {

        if(mPassword.getText().toString().equals(mSharedPreferences.getString(Constant.PASSWORD_NUN ,"wrong" ))){
            dismiss();
            secretAuthorize.success();
            mStage = Constant.FINGERPRINT;
        }else{
            mBackupContent.startAnimation(vibrateAnim);
            mPassword.setText("");
        }
    }

    /**
     * @return true if {@code password} is correct, false otherwise
     */
    private boolean checkPassword(String password) {
        // Assume the password is always correct.
        // In the real world situation, the password needs to be verified in the server side.
        return password.length() > 0;
    }

    //다이얼로그 내용 변경 지문<>패스워드
    private void updateStage() {
        switch (mStage) {
            case Constant.FINGERPRINT:
                mCancelButton.setText(R.string.cancel);
                mSecondDialogButton.setText(R.string.use_password);
                mFingerprintContent.setVisibility(View.VISIBLE);
                mBackupContent.setVisibility(View.GONE);
                break;
            case Constant.PASSWORD:
                mCancelButton.setText(R.string.cancel);
                mSecondDialogButton.setText(R.string.ok);
                mFingerprintContent.setVisibility(View.GONE);
                mBackupContent.setVisibility(View.VISIBLE);
                break;
        }
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_GO) {
            verifyPassword();
            return true;
        }
        return false;
    }

    @Override
    public void onAuthenticated() {
        // Callback from FingerprintUiHelper. Let the activity know that authentication was
        // successful.
        dismiss();
        secretAuthorize.success();
    }

    @Override
    public void onError() {
        goToBackup();
    }


    public void setCallback(SecretAuthorize secretAuthorize){
        this.secretAuthorize = secretAuthorize;
    }

    /**
     * Enumeration to indicate which authentication method the user is trying to authenticate with.
     */
    public interface SecretAuthorize {
        void success();
        void fail();
    }
}

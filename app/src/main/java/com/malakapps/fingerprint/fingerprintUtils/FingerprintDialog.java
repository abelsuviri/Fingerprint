package com.malakapps.fingerprint.fingerprintUtils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.malakapps.fingerprint.MainActivity;
import com.malakapps.fingerprint.R;
import com.malakapps.fingerprint.interfaces.FingerprintCallback;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * @author Abel Suviri
 */

public class FingerprintDialog extends DialogFragment implements FingerprintCallback {

    @BindView(R.id.fingerprintMessage)
    TextView fingerprintMessage;

    @BindView(R.id.fingerprintIcon)
    ImageView fingerprintIcon;

    @BindView(R.id.secondButton)
    Button mSecondButton;

    @BindView(R.id.fingerprintEnrolledLayout)
    ConstraintLayout mEnrolledLayout;

    @BindView(R.id.fingerprintAdded)
    TextView mFingerprintAdded;

    private Stage mStage = Stage.FINGERPRINT;
    private FingerprintManager.CryptoObject mCryptoObject;
    private FingerprintHelper mFingerprintHelper;
    private Activity mActivity;
    private SharedPreferences mSharedPreferences;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().setTitle(getString(R.string.sign_in));
        View v = inflater.inflate(R.layout.fingerprint_dialog, container, false);
        ButterKnife.bind(this, v);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mFingerprintHelper = new FingerprintHelper(mActivity, mActivity.getSystemService(FingerprintManager.class),
                this, fingerprintIcon, fingerprintMessage);
        }

        if (!mFingerprintHelper.isFingerprintAvailable()) {
            dismiss();
        }

        updateStage();

        return  v;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mStage == Stage.FINGERPRINT) {
            mFingerprintHelper.startListening(mCryptoObject);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mFingerprintHelper.stopListening();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mActivity = (MainActivity) context;
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mActivity);
    }

    public void setCryptoObject(FingerprintManager.CryptoObject cryptoObject) {
        mCryptoObject = cryptoObject;
    }

    public void setStage(Stage stage) {
        mStage = stage;
    }

    private void updateStage() {
        switch (mStage) {
            case FINGERPRINT:
                mSecondButton.setText("");
                mSecondButton.setClickable(false);
                mEnrolledLayout.setVisibility(View.VISIBLE);
                mFingerprintAdded.setVisibility(View.INVISIBLE);
                break;

            case NEW_FINGERPRINT_ENROLLED:
                mSecondButton.setText(getString(R.string.ok));
                mSecondButton.setClickable(true);
                mFingerprintAdded.setVisibility(View.VISIBLE);
                mEnrolledLayout.setVisibility(View.INVISIBLE);
                break;
        }
    }

    @OnClick(R.id.cancelButton)
    public void cancelClick() {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putBoolean(MainActivity.FINGERPRINT_ENROLLED, false);
        editor.apply();
        dismiss();
    }

    @OnClick(R.id.secondButton)
    public void secondButtonClick() {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putBoolean(MainActivity.FINGERPRINT_ENROLLED, true);
        editor.apply();
        dismiss();
    }

    public enum Stage {
        FINGERPRINT,
        NEW_FINGERPRINT_ENROLLED
    }

    @Override
    public void onAuthenticated() {
        dismiss();
    }

    @Override
    public void onError() {

    }
}

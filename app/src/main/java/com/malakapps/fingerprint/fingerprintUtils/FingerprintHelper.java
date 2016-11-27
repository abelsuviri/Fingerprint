package com.malakapps.fingerprint.fingerprintUtils;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.hardware.fingerprint.FingerprintManager;
import android.os.CancellationSignal;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.widget.ImageView;
import android.widget.TextView;

import com.malakapps.fingerprint.R;
import com.malakapps.fingerprint.interfaces.FingerprintCallback;

/**
 * @author Abel Suviri
 */

@TargetApi(23)
public class FingerprintHelper extends FingerprintManager.AuthenticationCallback {

    private Context mContext;
    private FingerprintManager mFingerprintManager;
    private FingerprintCallback mCallback;
    private CancellationSignal mCancellationSignal;
    private boolean isCancelled;
    private ImageView mIcon;
    private TextView mError;
    private Resources mResources;

    private static final long ERROR_TIMEOUT_MILLIS = 1600;
    private static final long SUCCESS_DELAY_MILLIS = 1300;

    public FingerprintHelper(Context context, FingerprintManager fingerprintManager, FingerprintCallback callback, ImageView icon, TextView error) {
        mContext = context;
        mFingerprintManager = fingerprintManager;
        mCallback = callback;
        mIcon = icon;
        mError = error;
        mResources = mContext.getResources();
    }

    public boolean isFingerprintAvailable() {
        return mFingerprintManager.isHardwareDetected() && mFingerprintManager.hasEnrolledFingerprints();
    }

    public void startListening(FingerprintManager.CryptoObject cryptoObject) {
        if (!isFingerprintAvailable()) {
            return;
        }

        mCancellationSignal = new CancellationSignal();
        isCancelled = false;
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mFingerprintManager.authenticate(cryptoObject, mCancellationSignal, 0, this, null);
    }

    public void stopListening() {
        if (mCancellationSignal != null) {
            isCancelled = true;
            mCancellationSignal.cancel();
            mCancellationSignal = null;
        }
    }

    @Override
    public void onAuthenticationError(int errorMessage, CharSequence errorString) {
        if (isCancelled) {
            showError(errorString);
            mIcon.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mCallback.onError();
                }
            }, ERROR_TIMEOUT_MILLIS);
        }
    }

    @Override
    public void onAuthenticationHelp(int helpMessage, CharSequence helpString) {
        showError(helpString);
    }

    @Override
    public void onAuthenticationFailed() {
        showError(mResources.getString(R.string.fingerprint_error));
    }

    @Override
    public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
        mError.removeCallbacks(mResetError);
        mError.setTextColor(ResourcesCompat.getColor(mResources, R.color.successColor, null));
        mError.setText(mResources.getString(R.string.fingerprint_success));
        mIcon.setImageResource(R.drawable.ic_fingerprint_success);
        mIcon.postDelayed(new Runnable() {
            @Override
            public void run() {
                mCallback.onAuthenticated();
            }
        }, SUCCESS_DELAY_MILLIS);
    }

    private void showError(CharSequence error) {
        mError.setText(error);
        mError.setTextColor(ResourcesCompat.getColor(mResources, R.color.errorColor, null));
        mError.removeCallbacks(mResetError);
        mError.postDelayed(mResetError, ERROR_TIMEOUT_MILLIS);
        mIcon.setImageResource(R.drawable.ic_fingerprint_error);
    }

    private Runnable mResetError = new Runnable() {
        @Override
        public void run() {
            mError.setText(mResources.getString(R.string.fingerprint_message));
            mError.setTextColor(ResourcesCompat.getColor(mResources, R.color.blackColor, null));
            mIcon.setImageResource(R.drawable.ic_fingerprint);
        }
    };
}

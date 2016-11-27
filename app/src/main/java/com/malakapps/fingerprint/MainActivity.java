package com.malakapps.fingerprint;

import android.annotation.TargetApi;
import android.content.SharedPreferences;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;

import com.malakapps.fingerprint.fingerprintUtils.FingerprintDialog;
import com.malakapps.fingerprint.interfaces.LoginView;
import com.malakapps.fingerprint.presenter.LoginPresenter;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * @author Abel Suviri
 */

public class MainActivity extends AppCompatActivity implements LoginView {

    @BindView(R.id.userName)
    EditText mUserName;

    @BindView(R.id.password)
    EditText mPassword;

    @BindView(R.id.buttonLogin)
    Button mButtonLogin;

    private LoginPresenter loginPresenter;
    private KeyStore mKeyStore;
    private KeyGenerator mKeyGenerator;
    private Cipher mDefaultCipher;
    private SharedPreferences mSharedPreferences;

    private static final String FRAGMENT_TAG = "fragment";
    private static final String ANDROID_KEY_STORE = "AndroidKeyStore";
    public static final String DEFAULT_KEY_NAME = "default_key";
    public static final String FINGERPRINT_ENROLLED = "FINGERPRINT_ENROLLED";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        initializePresenter();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
            initKeys();
            if (mSharedPreferences.getBoolean(FINGERPRINT_ENROLLED, false)) {
                checkFingerprint(false);
            }
        }
    }

    private void initializePresenter() {
        loginPresenter = new LoginPresenter(this);
    }

    @TargetApi(23)
    private void initKeys() {
        String cipherTransformation = KeyProperties.KEY_ALGORITHM_AES + "/" + KeyProperties.BLOCK_MODE_CBC + "/" + KeyProperties.ENCRYPTION_PADDING_PKCS7;
        try {
            mKeyStore = KeyStore.getInstance(ANDROID_KEY_STORE);
        } catch (KeyStoreException e) {
            throw new RuntimeException("Failed to get an instance of KeyStore", e);
        }
        try {
            mKeyGenerator = KeyGenerator
                .getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEY_STORE);
        } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
            Log.e("Failed KeyGenerator", e.toString());
        }

        try {
            mDefaultCipher = Cipher.getInstance(cipherTransformation);
            createKey(DEFAULT_KEY_NAME, true);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            Log.e("Failed instance Cipher", e.toString());
        }
    }

    private boolean initCipher(String keyName) {
        try {
            mKeyStore.load(null);
            SecretKey key = (SecretKey) mKeyStore.getKey(keyName, null);
            mDefaultCipher.init(Cipher.ENCRYPT_MODE, key);
            return true;
        } catch (KeyStoreException | CertificateException | UnrecoverableKeyException | IOException | NoSuchAlgorithmException | InvalidKeyException e) {
            Log.e("Failed to init Cipher", e.toString());
            return false;
        }
    }

    @TargetApi(23)
    public void createKey(String keyName, boolean invalidatedByBiometricEnrollment) {
        try {
            mKeyStore.load(null);
            KeyGenParameterSpec.Builder builder = new KeyGenParameterSpec.Builder(keyName, KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                .setUserAuthenticationRequired(true)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                builder.setInvalidatedByBiometricEnrollment(invalidatedByBiometricEnrollment);
            }

            mKeyGenerator.init(builder.build());
            mKeyGenerator.generateKey();
        } catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException | CertificateException | IOException e) {
            Log.e("Failed create key", e.toString());
        }
    }

    @TargetApi(23)
    private void checkFingerprint(boolean hasLogin) {
        if (getSystemService(FingerprintManager.class).isHardwareDetected() && getSystemService(FingerprintManager.class).hasEnrolledFingerprints()) {
            if (initCipher(DEFAULT_KEY_NAME)) {
                FingerprintDialog fragment = new FingerprintDialog();
                fragment.setCryptoObject(new FingerprintManager.CryptoObject(mDefaultCipher));

                if (hasLogin) {
                    fragment.setStage(FingerprintDialog.Stage.NEW_FINGERPRINT_ENROLLED);
                } else {
                    fragment.setStage(FingerprintDialog.Stage.FINGERPRINT);
                }

                fragment.show(getSupportFragmentManager(), FRAGMENT_TAG);
            }
        }
    }

    @OnClick(R.id.buttonLogin)
    public void loginButtonClick() {
        loginPresenter.doLogin(mUserName.getText().toString(), mPassword.getText().toString());
    }

    @Override
    public void showErrorMessageForUserNamePassword() {
        Snackbar.make(mPassword, getResources().getString(R.string.wrong_user), Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void showErrorMessageForMaxLoginAttempt() {
        Snackbar.make(mButtonLogin, getResources().getString(R.string.attempt_exceeded), Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void showLoginSuccessMessage() {
        checkFingerprint(true);
        Snackbar.make(mButtonLogin, getResources().getString(R.string.login_success), Snackbar.LENGTH_LONG).show();
    }
}

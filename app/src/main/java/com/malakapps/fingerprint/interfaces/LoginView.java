package com.malakapps.fingerprint.interfaces;

/**
 * @author Abel Suviri
 */

public interface LoginView {

    void showErrorMessageForUserNamePassword();

    void showErrorMessageForMaxLoginAttempt();

    void showLoginSuccessMessage();
}

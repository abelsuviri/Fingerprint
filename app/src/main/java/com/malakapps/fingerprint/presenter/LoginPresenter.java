package com.malakapps.fingerprint.presenter;

import com.malakapps.fingerprint.interfaces.LoginView;

/**
 * @author Abel Suviri
 */

public class LoginPresenter {

    private static final int MAX_LOGIN_ATTEMPT = 3;
    private final LoginView loginView;
    private int loginAttempt;

    public LoginPresenter(LoginView loginView) {
        this.loginView = loginView;
    }

    public int incrementLoginAttempt() {
        loginAttempt++;
        return loginAttempt;
    }

    public boolean isLoginAttemptExceeded() {
        return loginAttempt >= MAX_LOGIN_ATTEMPT;
    }

    public void doLogin(String userName, String password) {
        if (isLoginAttemptExceeded()) {
            loginView.showErrorMessageForMaxLoginAttempt();
            return;
        }

        if (userName.equals("abel") && password.equals("test")) {
            loginView.showLoginSuccessMessage();
            return;
        }

        incrementLoginAttempt();
        loginView.showErrorMessageForUserNamePassword();
    }
}

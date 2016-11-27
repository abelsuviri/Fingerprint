package com.malakapps.fingerprint;

import android.content.Intent;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

@RunWith(AndroidJUnit4.class)
public class LoginActivityTest {

    private ActivityTestRule<MainActivity> activityTestRule = new ActivityTestRule<>(MainActivity.class);

    @Test
    public void checkUserNameEditTextIsDisplayed() {
        activityTestRule.launchActivity(new Intent());
        onView(withId(R.id.userName)).check(matches(isDisplayed()));
    }

    @Test
    public void checkErrorMessageIsDisplayedForEmptyData() {
        activityTestRule.launchActivity(new Intent());
        onView(withId(R.id.buttonLogin)).check(matches(isDisplayed())).perform(click());
        onView(withText("Wrong username or password")).check(matches(isDisplayed()));
    }

    @Test
    public void checkLoginSuccess() {
        activityTestRule.launchActivity(new Intent());
        onView(withId(R.id.userName)).perform(typeText("abel"), closeSoftKeyboard());
        onView(withId(R.id.password)).perform(typeText("test"), closeSoftKeyboard());
        onView(withId(R.id.buttonLogin)).check(matches(isDisplayed())).perform(click());
        onView(withText("Login successful")).check(matches(isDisplayed()));
    }

    @Test
    public void checkLoginError() {
        activityTestRule.launchActivity(new Intent());
        onView(withId(R.id.userName)).perform(typeText("abel"), closeSoftKeyboard());
        onView(withId(R.id.password)).perform(typeText("aaa"), closeSoftKeyboard());
        onView(withId(R.id.buttonLogin)).check(matches(isDisplayed())).perform(click());
        onView(withText("Wrong username or password")).check(matches(isDisplayed()));
    }
}

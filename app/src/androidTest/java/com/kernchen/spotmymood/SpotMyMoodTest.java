package com.kernchen.spotmymood;

import android.content.Intent;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.action.ViewActions;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.rule.ActivityTestRule;

import com.kernchen.spotmymood.spotmymood.EmotionDetectActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Testing class which currently only tests the select button functionality
 * @author Max Kernchen
 * @version 1.1 - 5/28/2018
 */

@RunWith(AndroidJUnit4.class)
public class SpotMyMoodTest {
    //Use ActivityTest rule to start up the application
    @Rule
    public ActivityTestRule<EmotionDetectActivity> activityRule
            = new ActivityTestRule<>(
            EmotionDetectActivity.class,
            true,
            false);

    /**
     * Simple test of the select image button's functionality, should open up a camera screen
     */
    @Test
    public void press_select_image(){
        activityRule.launchActivity(new Intent());
        Espresso.onView(ViewMatchers.withId(R.id.buttonSelectImage)).perform(ViewActions.click());

    }
}

package ca.ualberta.compileorcry;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.*;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.*;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.SystemClock;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class SearchFragmentTest {

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule = new ActivityScenarioRule<>(MainActivity.class);

    @BeforeClass
    public static void setup() {
        FirebaseFirestore.getInstance().useEmulator("10.0.2.2", 8080);
    }

    private void performLoginAndGoToFeed() {
        onView(withId(R.id.login_username_text)).perform(typeText("testuser"), closeSoftKeyboard());
        onView(withId(R.id.register_button)).perform(click());
        onView(withId(R.id.registration_username_text)).perform(typeText("testuser"), closeSoftKeyboard());
        onView(withId(R.id.registration_name_text)).perform(typeText("Test User"), closeSoftKeyboard());
        onView(withId(R.id.done_button)).perform(click());
        SystemClock.sleep(2000);
    }

    @Test
    public void testUserSearchWithResults() {
        performLoginAndGoToFeed();
        onView(withId(R.id.fabUserSearch)).perform(click());
        onView(withId(R.id.search_input)).perform(typeText("testuser"), closeSoftKeyboard());
        onView(withId(R.id.search_icon)).perform(click());
        SystemClock.sleep(2500);
        onView(withText("testuser")).check(matches(isDisplayed()));
    }

    @Test
    public void testUserSearchWithNoResults() {
        performLoginAndGoToFeed();
        onView(withId(R.id.fabUserSearch)).perform(click());
        onView(withId(R.id.search_input)).perform(typeText("nonexistentuser"), closeSoftKeyboard());
        onView(withId(R.id.search_icon)).perform(click());
        SystemClock.sleep(2500);
        onView(withId(R.id.empty_state_text)).check(matches(withText("No users with this username")));
    }

    @After
    public void tearDown() {
        try {
            URL url = new URL("http://10.0.2.2:8080/emulator/v1/projects/compile-or-cry-8c762/databases/(default)/documents");
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("DELETE");
            urlConnection.getResponseCode();
            urlConnection.disconnect();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
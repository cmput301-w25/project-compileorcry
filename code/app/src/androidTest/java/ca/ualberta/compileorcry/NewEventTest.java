package ca.ualberta.compileorcry;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.RootMatchers.isPlatformPopup;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;


@RunWith(AndroidJUnit4.class)
@LargeTest
public class NewEventTest {

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule =
            new ActivityScenarioRule<>(MainActivity.class);

    @BeforeClass
    public static void setup(){
        // Specific address for emulated device to access our localHost
        String androidLocalhost = "10.0.2.2";
        int portNumber = 8080;
        FirebaseFirestore.getInstance().useEmulator(androidLocalhost, portNumber);

    }

    private void clearCachedLogin() {
        // Clear SharedPreferences or any other cached data related to login
        SharedPreferences preferences =
                androidx.test.platform.app.InstrumentationRegistry.getInstrumentation().getTargetContext().getSharedPreferences("default", Context.MODE_PRIVATE);
        preferences.edit().clear().apply();
    }

    @Test
    public void testSuccessfulEventCreation() {
        performLogin();

        onView(withId(R.id.new_event_emotional_state_autocomplete))
                .perform(replaceText("Happiness"));

        onView(withId(R.id.new_event_date_text))
                .perform(replaceText("2020-10-01"));

        onView(withId(R.id.new_event_trigger_text))
                .perform(typeText("Music"), closeSoftKeyboard());

        onView(withId(R.id.new_event_social_situation_autocomplete))
                .perform(replaceText("Alone"));

        onView(withId(R.id.create_button)).perform(click());

        // Verify that the fields have been reset to their default state
        onView(withId(R.id.new_event_emotional_state_autocomplete))
                .check(matches(withText("")));

        onView(withId(R.id.new_event_date_text))
                .check(matches(withText("")));

        onView(withId(R.id.new_event_trigger_text))
                .check(matches(withText("")));

        onView(withId(R.id.new_event_social_situation_autocomplete))
                .check(matches(withText("")));
    }

    @Test
    public void testMissingEmotionalState() {
        performLogin();

        // Do NOT fill emotional state

        onView(withId(R.id.new_event_date_text))
                .perform(replaceText("2020-10-01"));

        onView(withId(R.id.new_event_trigger_text)).perform(typeText("Music"), closeSoftKeyboard());

        onView(withId(R.id.new_event_social_situation_autocomplete))
                .perform(replaceText("Alone"));

        onView(withId(R.id.create_button)).perform(click());

        // Verify error message
        onView(withText("This field is required")).check(matches(isDisplayed()));
    }

    @Test
    public void testInvalidDateFormat() {
        performLogin();

        onView(withId(R.id.new_event_emotional_state_autocomplete))
                .perform(replaceText("Happy"));

        // Enter an invalid date
        onView(withId(R.id.new_event_date_text))
                .perform(replaceText("Invalid"));

        onView(withId(R.id.new_event_trigger_text)).perform(typeText("Music"), closeSoftKeyboard());

        onView(withId(R.id.new_event_social_situation_autocomplete))
                .perform(replaceText("Alone"));

        onView(withId(R.id.create_button)).perform(click());

        // Verify error message
        onView(withText("Please enter a valid date")).check(matches(isDisplayed()));
    }

    private void performLogin() {

        // Navigate login screen
        onView(withId(R.id.login_username_text)).perform(typeText("testuser"), closeSoftKeyboard());
        onView(withId(R.id.register_button)).perform(click());

        // Create test user
        onView(withId(R.id.registration_username_text)).perform(typeText("testuser"), closeSoftKeyboard());
        onView(withId(R.id.registration_name_text)).perform(typeText("testuser"), closeSoftKeyboard());
        onView(withId(R.id.done_button)).perform(click());

        // Wait for navigation
        SystemClock.sleep(2000);

        // Navigate to new fragment
        onView(withId(R.id.navigation_new)).perform(click());
    }

    @After
    public void tearDown() {
        String projectId = "compile-or-cry-8c762";
        URL url = null;
        try {
            url = new URL("http://10.0.2.2:8080/emulator/v1/projects/" + projectId + "/databases/(default)/documents");
        } catch (MalformedURLException exception) {
            Log.e("URL Error", Objects.requireNonNull(exception.getMessage()));
        }
        HttpURLConnection urlConnection = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("DELETE");
            int response = urlConnection.getResponseCode();
            Log.i("Response Code", "Response Code: " + response);
        } catch (IOException exception) {
            Log.e("IO Error", Objects.requireNonNull(exception.getMessage()));
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
    }
}

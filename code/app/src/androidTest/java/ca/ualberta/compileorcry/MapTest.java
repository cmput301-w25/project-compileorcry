package ca.ualberta.compileorcry;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.UiDevice;

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
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.allOf;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
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
public class MapTest {

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

    @Before
    public void grantLocationPermission() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();

        // For ACCESS_FINE_LOCATION
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String permission = android.Manifest.permission.ACCESS_FINE_LOCATION;
            if (context.checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                try {
                    UiDevice device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
                    device.executeShellCommand("pm grant " + context.getPackageName() + " " + permission);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Test
    public void testOpenMapWithLocatableMood() {
        performLogin();

        onView(withId(R.id.new_event_emotional_state_autocomplete))
                .perform(replaceText("Happiness"));

        onView(withId(R.id.new_event_date_text))
                .perform(replaceText("2020-10-01 @ 12:01"));

        onView(withId(R.id.my_location_button)).perform(click());

        onView(withId(R.id.new_event_trigger_text))
                .perform(replaceText("Music"));

        onView(withId(R.id.new_event_social_situation_autocomplete))
                .perform(replaceText("Alone"));

        onView(withId(R.id.create_button)).perform(click());

        // Delay for mood creation
        SystemClock.sleep(2000);

        // Navigate to new fragment
        onView(withId(R.id.navigation_feed)).perform(click());

        onView(withId(R.id.feed_spinner))
                .perform(click());
        onData(allOf(is(instanceOf(String.class)), is("History")))
                .perform(click());

        // Delay for mood fetch
        SystemClock.sleep(2000);

        onView(withId(R.id.fabMap)).perform(click());

        // Delay for navigation
        SystemClock.sleep(5000);

        onView(withId(R.id.fab_exit_map)).perform(click());

        onView(withId(R.id.navigation_feed))
                .check(matches(isDisplayed()));
    }

    @Test
    public void testOpenMapWithNoLocatableMoods() {
        performLogin();

        onView(withId(R.id.new_event_emotional_state_autocomplete))
                .perform(replaceText("Happiness"));

        onView(withId(R.id.new_event_date_text))
                .perform(replaceText("2020-10-01 @ 12:01"));

        onView(withId(R.id.create_button)).perform(click());

        // Delay for mood creation
        SystemClock.sleep(2000);

        // Navigate to new fragment
        onView(withId(R.id.navigation_feed)).perform(click());

        onView(withId(R.id.fabMap)).perform(click());

        onView(withId(R.id.navigation_feed))
                .check(matches(isDisplayed()));
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
        SystemClock.sleep(5000);

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

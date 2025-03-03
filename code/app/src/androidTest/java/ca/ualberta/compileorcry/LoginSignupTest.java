package ca.ualberta.compileorcry;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasErrorText;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.util.Log;

import androidx.test.espresso.action.ViewActions;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.UiDevice;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class LoginSignupTest {
    @Rule
    public ActivityScenarioRule<MainActivity> scenario =
            new ActivityScenarioRule<MainActivity>(MainActivity.class);

    static UiDevice device;

    @BeforeClass
    public static void setup(){
        // Specific address for emulated device to access our localHost
        String androidLocalhost = "10.0.2.2";

        int portNumber = 8080;
        try {
            FirebaseFirestore.getInstance().useEmulator(androidLocalhost, portNumber);
        } catch (IllegalStateException ignored){
        }

        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
    }

    @Test
    public void registerValidUser() throws InterruptedException {
        onView(withId(R.id.register_button)).perform(click());
        device.waitForIdle();
        onView(withId(R.id.registration_username_text)).perform(ViewActions.typeText("newUser"));
        onView(withId(R.id.registration_name_text)).perform(ViewActions.typeText("New User"));
        onView(withId(R.id.done_button)).perform(click());
        device.waitForIdle();
        Thread.sleep(1000);

        // Verify on Profile Page
        onView(withId(R.id.profile_username)).check(matches(isDisplayed()));
        onView(withId(R.id.profile_username)).check(matches(withText("newUser")));
        onView(withId(R.id.profile_name)).check(matches(withText("New User")));
    }

    @Test
    public void registerExistingUsername() throws InterruptedException {
        addUser("existingUser", "New Existing User!");

        // Test
        onView(withId(R.id.register_button)).perform(click());
        device.waitForIdle();
        onView(withId(R.id.registration_username_text)).perform(ViewActions.typeText("existingUser"));
        onView(withId(R.id.registration_name_text)).perform(ViewActions.typeText("New Existing User!"));
        onView(withId(R.id.done_button)).perform(click());
        device.waitForIdle();
        Thread.sleep(1000);

        // Verify still on registration page
        onView(withId(R.id.login_username_layout)).check(matches(hasErrorText("Username already registered.")));
    }

    @Test
    public void loginExistingUsername() throws InterruptedException {
        addUser("existingUser", "New Existing User!");

        // Test
        onView(withId(R.id.login_username_text)).perform(ViewActions.typeText("existingUser"));
        onView(withId(R.id.login_button)).perform(click());
        device.waitForIdle();
        Thread.sleep(1000);

        // Verify navigated to profile
        onView(withId(R.id.profile_username)).check(matches(isDisplayed()));
        onView(withId(R.id.profile_username)).check(matches(withText("existingUser")));
        onView(withId(R.id.profile_name)).check(matches(withText("New Existing User!")));
    }

    @Test
    public void loginInvalidUsername() throws InterruptedException {
        // Test
        onView(withId(R.id.login_username_text)).perform(ViewActions.typeText("existingUser"));
        onView(withId(R.id.login_button)).perform(click());
        device.waitForIdle();
        Thread.sleep(200);

        // Verify still on login page
        onView(withId(R.id.login_username_layout)).check(matches(hasErrorText("User does not exist.")));
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

    public static void addUser(String username, String name) throws InterruptedException {
        // Add Initial User
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference usersRef = db.collection("users");
        Map<String, Object> userData = new HashMap<>();
        userData.put("username", username);
        userData.put("name", name);
        usersRef.document(username).set(userData);
        Thread.sleep(500);
    }
}

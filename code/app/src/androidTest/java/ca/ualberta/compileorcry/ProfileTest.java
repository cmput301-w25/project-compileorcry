package ca.ualberta.compileorcry;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.RootMatchers.isDialog;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import static org.junit.Assert.assertNull;
import static ca.ualberta.compileorcry.TestHelper.addUser;
import static ca.ualberta.compileorcry.TestHelper.resetFirebase;

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
import org.junit.Before;
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

import ca.ualberta.compileorcry.domain.models.User;

/**
 * UI Test to verify the functionality of the Profile fragment.
 * <p>
 * Requires firebase datastore emulator to be running on port 8080.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class ProfileTest {
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

    @Before
    public void testSetup() throws InterruptedException {
        // Add Initial User
        addUser("testUser", "Test User");
        Thread.sleep(500);

        // Log In
        onView(withId(R.id.login_username_text)).perform(ViewActions.typeText("testUser"));
        onView(withId(R.id.login_button)).perform(click());
        device.waitForIdle();
        Thread.sleep(1000);
    }

    @Test
    public void profileVisible(){
        // Navigate to profile and verify
        onView(withId(R.id.navigation_profile)).perform(click());
        onView(withId(R.id.profile_name)).check(matches(isDisplayed()));
    }

    @Test
    public void changeName() throws InterruptedException {
        // Navigate to profile
        onView(withId(R.id.navigation_profile)).perform(click());

        // Change display name
        onView(withId(R.id.edit_button)).perform(click());
        device.waitForIdle();

        // Verify
        onView(withId(R.id.editname_text)).check(matches(isDisplayed()));
        onView(withId(R.id.editname_text)).perform(ViewActions.replaceText("New Test User"));
        onView(withText("Save"))
                .inRoot(isDialog())
                .perform(click());
        device.waitForIdle();
        Thread.sleep(100);
        // Verify new name is displayed on profile
        onView(withId(R.id.profile_name)).check(matches(withText("New Test User")));
    }

    @Test
    public void logout(){
        // Navigate to profile
        onView(withId(R.id.navigation_profile)).perform(click());

        // Logout button
        onView(withId(R.id.logout_button)).check(matches(isDisplayed()));
        onView(withId(R.id.logout_button)).perform(click());

        // Verify logged out
        onView(withId(R.id.login_username_text)).check(matches(isDisplayed()));
        assertNull(User.getActiveUser());
    }

    @After
    public void tearDown() {
        resetFirebase();
    }

}

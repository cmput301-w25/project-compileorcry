package ca.ualberta.compileorcry;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasErrorText;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import static ca.ualberta.compileorcry.TestHelper.addUser;
import static ca.ualberta.compileorcry.TestHelper.resetFirebase;

import android.view.View;

import androidx.test.espresso.action.ViewActions;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.UiDevice;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.regex.Matcher;


/**
 * UI Test to verify the functionality of the Login and Register fragments.
 * <p>
 * Requires firebase datastore emulator to be running on port 8080.
 */
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

    /**
     * Test registering a new user with a unique username. Registration is expected to succeed.
     */
    @Test
    public void registerValidUser() throws InterruptedException {
        onView(withId(R.id.register_button)).perform(click());
        device.waitForIdle();
        onView(withId(R.id.registration_username_text)).perform(ViewActions.typeText("newUser"));
        onView(withId(R.id.registration_name_text)).perform(ViewActions.typeText("New User"));
        onView(withId(R.id.done_button)).perform(click());
        device.waitForIdle();
        Thread.sleep(1000);

        // Navigate to profile page and verify displayed name
        onView(withId(R.id.navigation_profile)).perform(click());
        onView(withId(R.id.profile_username)).check(matches(withText("@newUser")));
        onView(withId(R.id.profile_name)).check(matches(withText("New User")));
    }

    /**
     * Test registering a user with a pre-existing username. Registration is expected to fail and an error message is checked.
     */
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
        onView(withId(R.id.registration_username_text)).check(matches(isDisplayed()));
    }

    /**
     * Test logging in with a valid username. Test is expected to succeed.
     */
    @Test
    public void loginExistingUsername() throws InterruptedException {
        addUser("existingUser", "New Existing User!");

        // Test
        onView(withId(R.id.login_username_text)).perform(ViewActions.typeText("existingUser"));
        onView(withId(R.id.login_button)).perform(click());
        device.waitForIdle();
        Thread.sleep(1000);

        // Navigate to profile and verify names are as expected
        onView(withId(R.id.navigation_profile)).perform(click());
        onView(withId(R.id.profile_username)).check(matches(withText("@existingUser")));
        onView(withId(R.id.profile_name)).check(matches(withText("New Existing User!")));
    }

    /**
     * Test logging in with a non-existent username. Test is expected to fail and an error message is checked.
     */
    @Test
    public void loginInvalidUsername() throws InterruptedException {
        // Test
        onView(withId(R.id.login_username_text)).perform(ViewActions.typeText("existingUser"));
        onView(withId(R.id.login_button)).perform(click());
        device.waitForIdle();
        Thread.sleep(200);

        // Verify still on login page
        onView(withId(R.id.login_username_layout)).check(matches(isDisplayed()));
    }

    @After
    public void tearDown() {
        resetFirebase();
    }
}

package ca.ualberta.compileorcry;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.RootMatchers.isDialog;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertNull;
import static ca.ualberta.compileorcry.TestHelper.addUser;
import static ca.ualberta.compileorcry.TestHelper.resetFirebase;

import android.content.pm.ActivityInfo;

import androidx.test.espresso.action.ViewActions;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.UiDevice;

import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

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
        addUser("testUser", "Test User");
        Thread.sleep(500);

        onView(withId(R.id.login_username_text)).perform(ViewActions.typeText("testUser"));
        onView(withId(R.id.login_button)).perform(click());
        device.waitForIdle();
        Thread.sleep(1000);
    }

    /**
     * Test to verify the profile can be accessed after login
     */
    @Test
    public void profileVisible(){
        // Navigate to profile and verify
        onView(withId(R.id.navigation_profile)).perform(click());
        onView(withId(R.id.profile_name)).check(matches(isDisplayed()));
    }

    /**
     * Test to verify changing the user's display name
     */
    @Test
    public void changeName() throws InterruptedException {
        onView(withId(R.id.navigation_profile)).perform(click());
        onView(withId(R.id.edit_button)).perform(click());
        device.waitForIdle();
        onView(withId(R.id.editname_text)).check(matches(isDisplayed()));
        onView(withId(R.id.editname_text)).perform(ViewActions.replaceText("New Test User"));
        onView(withId(R.id.save_button)).perform(click());

        device.waitForIdle();
        Thread.sleep(100);
        onView(withId(R.id.profile_name)).check(matches(withText("New Test User")));
    }

    /**
     * Test to verify using the logout from the profile page
     */
    @Test
    public void logout(){
        onView(withId(R.id.navigation_profile)).perform(click());
        onView(withId(R.id.logout_button)).check(matches(isDisplayed()));
        onView(withId(R.id.logout_button)).perform(click());
        onView(withId(R.id.login_username_text)).check(matches(isDisplayed()));
        assertNull(User.getActiveUser());
    }

    /**
     * Test to verify the QR code dialog can be opened and dismissed
     */
    @Test
    public void qrCodeDialog() {
        onView(withId(R.id.navigation_profile)).perform(click());
        onView(withId(R.id.qrcode_button)).perform(click());
        device.waitForIdle();
        onView(withId(R.id.qrcode_imageview)).check(matches(isDisplayed()));
        onView(withId(R.id.dismiss_button)).perform(click());
        device.waitForIdle();
        onView(withId(R.id.profile_name)).check(matches(isDisplayed()));
    }

    /**
     * Test to verify the Friends list can be accessed
     */
    @Test
    public void openFriendsList() {
        onView(withId(R.id.navigation_profile)).perform(click());
        onView(withId(R.id.friends_button)).perform(click());
        device.waitForIdle();
        onView(withId(R.id.recyclerView_users)).check(matches(isDisplayed()));
        onView(withId(R.id.switch1)).check(matches(isDisplayed()));
        onView(withId(R.id.back_button)).perform(click());
        device.waitForIdle();
        onView(withId(R.id.profile_name)).check(matches(isDisplayed()));
    }

    /**
     * Test to verify the Requests bottom sheet can be opened
     */
    @Test
    public void openRequestsBottomSheet() {
        onView(withId(R.id.navigation_profile)).perform(click());
        onView(withId(R.id.requests_button)).perform(click());
        device.waitForIdle();
        onView(withText("Friend Requests")).check(matches(isDisplayed()));
        onView(withId(R.id.empty_message)).check(matches(isDisplayed()));
    }

    /**
     * Test to verify that all UI elements have proper content descriptions for accessibility
     */
    @Test
    public void accessibilityContentDescriptions() {
        onView(withId(R.id.navigation_profile)).perform(click());
        onView(withId(R.id.profile_image)).check(matches(withContentDescription(not(""))));
        onView(withId(R.id.requests_button)).check(matches(withContentDescription(not(""))));
        onView(withId(R.id.edit_button)).check(matches(withContentDescription(not(""))));
        onView(withId(R.id.friends_button)).check(matches(withContentDescription(not(""))));
        onView(withId(R.id.qrcode_button)).check(matches(withContentDescription(not(""))));
        onView(withId(R.id.logout_button)).check(matches(withContentDescription(not(""))));
    }

    /**
     * Helper method to rotate the device
     */
    private void rotateDevice(int orientation) {
        scenario.getScenario().onActivity(activity -> {
            activity.setRequestedOrientation(orientation);
        });
        device.waitForIdle();
    }

    @After
    public void tearDown() {
        resetFirebase();
    }
}
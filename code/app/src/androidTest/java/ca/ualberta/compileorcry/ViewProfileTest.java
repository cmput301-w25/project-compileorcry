package ca.ualberta.compileorcry;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static ca.ualberta.compileorcry.TestHelper.addUser;
import static ca.ualberta.compileorcry.TestHelper.resetFirebase;

import android.content.Intent;
import android.net.Uri;

import androidx.test.core.app.ActivityScenario;
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

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import ca.ualberta.compileorcry.domain.models.User;
import ca.ualberta.compileorcry.features.mood.model.FollowHelper;

/**
 * UI Test to verify the functionality of the View Profile Fragment.
 * Tests deeplink to view profile, and the follow button.
 * <p>
 * Requires firebase datastore emulator to be running on port 8080.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class ViewProfileTest {
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
        addUser("testUser2", "Test User 2");
        Thread.sleep(500);

        // Log In
        onView(withId(R.id.login_username_text)).perform(ViewActions.typeText("testUser"));
        onView(withId(R.id.login_button)).perform(click());
        device.waitForIdle();
    }

    public Intent generateViewProfileIntent(String username){
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(("compileorcry://profile/" + username)));
        intent.setPackage(InstrumentationRegistry.getInstrumentation().getTargetContext().getPackageName());
        return intent;
    }

    /**
     * Verify the deeplink intent launches the profile, and the correct user data is displayed
     */
    @Test
    public void intentProfileLoads(){
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(generateViewProfileIntent("testUser2"))) {
            onView(withId(R.id.view_profile_name)).check(matches(withText("Test User 2")));
            onView(withId(R.id.view_profile_username)).check(matches(withText("testUser2")));
        }
    }

    /**
     * Verify functionality of follow button on ViewProfile fragment to request to follow a user, and cancel the follow request.
     */
    @Test
    public void testFollowRequest(){
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(generateViewProfileIntent("testUser2"))) {
            onView(withId(R.id.follow_button)).check(matches(isDisplayed()));
            onView(withId(R.id.follow_button_text)).check(matches(withText(R.string.button_follow)));
            onView(withId(R.id.follow_button)).perform(click()); // Request to follow
            device.waitForIdle();
            // Verify
            assertTrue(FollowHelper.hasUserRequestedFollow(User.getActiveUser().getUsername(), "testUser2"));
            onView(withId(R.id.follow_button_text)).check(matches(withText(R.string.button_requested)));

            onView(withId(R.id.follow_button)).perform(click()); // Cancel request
            // Verify
            assertFalse(FollowHelper.hasUserRequestedFollow(User.getActiveUser().getUsername(), "testUser2"));
            onView(withId(R.id.follow_button_text)).check(matches(withText(R.string.button_follow)));

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * Verify functionality of follow button on ViewProfile fragment to unfollow a user.
     */
    @Test
    public void testUnfollow(){
        // Setup
        try {
            CountDownLatch latch = new CountDownLatch(1);
            FollowHelper.createFollowRequest(User.getActiveUser(), "testUser2");
            User.get_user("testUser2", (user ,error) -> {
                if(error != null)
                    throw new RuntimeException();

                try {
                    FollowHelper.handleFollowRequest(user, "testUser", true); // User2 accept User1 follow request
                    latch.countDown();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });
            assertTrue(latch.await(5, TimeUnit.SECONDS));
        } catch (InterruptedException e){
            throw new RuntimeException(e);
        }
        // Run Test
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(generateViewProfileIntent("testUser2"))) {
            onView(withId(R.id.follow_button)).check(matches(isDisplayed()));
            assertTrue(FollowHelper.isUserFollowing("testUser", "testUser2"));
            onView(withId(R.id.follow_button_text)).check(matches(withText(R.string.button_unfollow)));
            onView(withId(R.id.follow_button)).perform(click());
            device.waitForIdle();
            // Verify
            assertFalse(FollowHelper.isUserFollowing("testUser", "testUser2"));
            onView(withId(R.id.follow_button_text)).check(matches(withText(R.string.button_follow)));
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @After
    public void tearDown() {
        resetFirebase();
    }
}

package ca.ualberta.compileorcry;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static ca.ualberta.compileorcry.TestHelper.resetFirebase;

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
 * UI Test to verify the functionality of the Login and Register fragments.
 * <p>
 * Requires firebase datastore emulator to be running on port 8080.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class OfflineDataTest {

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

        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation()) ;
    }

    @Before
    public void testSetup(){
        TestHelper.addUser("testUser", "Test User");
    }

    /**
     * Test to verify the logged in user's username is stored in persistent data and can be retrieved
     */
    @Test
    public void resumeActiveUserTest(){
        scenario.getScenario().onActivity(activity -> {
            User.get_user("testUser", (User user, String error) -> {
                assertNull(error);

                User.setActiveUserPersist(user, activity); // Set active user which is stored in persistent data

                try {
                    User.class.getDeclaredField("activeUser").set(User.class, null); // Force clear the active user variable
                } catch (IllegalAccessException | NoSuchFieldException e) {
                    throw new RuntimeException(e);
                }

                User.checkActiveUser(activity, (resumed, error1) ->{ // Resume and verify
                    assertTrue(resumed);
                    assertNull(error1);
                    assertEquals(User.getActiveUser(), user);
                });
            });
        });
    }

    @After
    public void tearDown() {
        resetFirebase();
    }
}

package ca.ualberta.compileorcry;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isRoot;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.os.SystemClock;
import android.view.View;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.PerformException;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.util.HumanReadables;
import androidx.test.espresso.util.TreeIterables;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.uiautomator.UiDevice;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import ca.ualberta.compileorcry.domain.models.User;

/**
 * Tests for the FeedFragment
 * */
public class FeedTest {
    @Rule
    public ActivityScenarioRule<MainActivity> scenario =
            new ActivityScenarioRule<MainActivity>(MainActivity.class);

    static UiDevice device;

    /**
     * Sets up Firebase emulator for testing
     * */
    @BeforeClass
    public static void setup(){
        // Specific address for emulated device to access our localHost
        String androidLocalhost = "10.0.2.2";
        int portNumber = 8080;
        FirebaseFirestore.getInstance().useEmulator(androidLocalhost, portNumber);

        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
    }

    /**
     * Seeds the database ... duh
     * User1 follows 2 other users. Each user has 1 mood each of different emotional state
     * */
    @Before
    public void seedDatabase() throws InterruptedException {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Define user references
        DocumentReference user1Ref = db.collection("users").document("user1");
        DocumentReference user2Ref = db.collection("users").document("user2");
        DocumentReference user3Ref = db.collection("users").document("user3");

        // Set up user profiles
        user1Ref.set(Map.of("username", "user1", "name", "user1"));
        user2Ref.set(Map.of("username", "user2", "name", "user2"));
        user3Ref.set(Map.of("username", "user3", "name", "user3"));

        // User1 follows user2 and user3
        user1Ref.collection("following").document("user2").set(Map.of("username", "user2"));
        user1Ref.collection("following").document("user3").set(Map.of("username", "user3"));
        user2Ref.collection("followers").document("user1").set(Map.of("username", "user1"));
        user3Ref.collection("followers").document("user1").set(Map.of("username", "user1"));

        // Create Mood Events
        Map<String, Object> mood1 = createEvent(1, "Studying", Timestamp.now(), "Alone", null);
        Map<String, Object> mood2 = createEvent(2, "Coding", Timestamp.now(), "Online", null);
        Map<String, Object> mood3 = createEvent(3, "Gaming", Timestamp.now(), "Friends", null);

        // Assign moods to respective users
        user1Ref.collection("mood_events").document("mood1").set(mood1); // user1's history
        user2Ref.collection("mood_events").document("mood2").set(mood2); // user2's post
        user3Ref.collection("mood_events").document("mood3").set(mood3); // user3's post

        Map<String, Object> recMood1 = createRecentEvent(1, "Studying", Timestamp.now(), "Alone", null,"user1","user1Event");
        Map<String, Object> recMood2 = createRecentEvent(2, "Coding", Timestamp.now(), "Online", null,"user2","user2Event");
        Map<String, Object> recMood3 = createRecentEvent(3, "Gaming", Timestamp.now(), "Friends", null,"user3","user3Event");

        // Assign moods to respective users
        db.collection("most_recent_moods").document("user1").collection("recent_moods").document("user1Event").set(recMood1); // user1's history
        db.collection("most_recent_moods").document("user2").collection("recent_moods").document("user2Event").set(recMood2); // user2's post
        db.collection("most_recent_moods").document("user3").collection("recent_moods").document("user3Event").set(recMood3); // user3's post

        // Wait for user1 to be set as active user
        CountDownLatch latch = new CountDownLatch(1);
        User.get_user("user1", (user, error) -> {
            if (user != null) {
                User.setActiveUser(user);
            }
            latch.countDown();
        });
        latch.await(10, TimeUnit.SECONDS);

        Thread.sleep(2000); // Wait for Firestore to propagate data
    }

    /**
     * Tests that the user's own moods show up in their history when History is selcted on the spinner
     * */
    @Test
    public void appShouldDisplayUserMoodHistory() throws InterruptedException {
        // Step 1: Launch Activity
        ActivityScenario.launch(MainActivity.class);
        // Ensure FeedFragment is open
        onView(withId(R.id.navigation_feed)).perform(click());

        // Wait for UI to load
        onView(isRoot()).perform(waitForView(R.id.feed_spinner, 5000));

        // Step 3: Select "Following" from feed spinner
        onView(withId(R.id.feed_spinner)).perform(click());
        onView(withText("History")).perform(click());

        // Wait for UI to load
        onView(isRoot()).perform(waitForView(R.id.feed_spinner, 5000));
        // Step 4: Assert moods from mood history appears
        onView(withText("Anger")).check(matches(isDisplayed()));
    }

    /**
     * Tests that moods of followed users show up in Following feed when FOllowing is selected in spinner
     * */
    @Test
    public void appShouldDisplayFollowedMoods() throws InterruptedException {
        // Step 1: Launch Activity
        ActivityScenario.launch(MainActivity.class);
        // Ensure FeedFragment is open
        onView(withId(R.id.navigation_feed)).perform(click());

        // Wait for UI to load
        onView(isRoot()).perform(waitForView(R.id.feed_spinner, 5000));

        // Step 3: Select "Following" from feed spinner
        onView(withId(R.id.feed_spinner)).perform(click());
        onView(withText("Following")).perform(click());
        // Wait for UI to load
        onView(isRoot()).perform(waitForView(R.id.feed_spinner, 5000));
        // Step 4: Assert moods from followed users appear (usernames shown on moodevent items)
        onView(withText("@user2's Confusion Event")).check(matches(isDisplayed()));
        onView(withText("@user3's Disgust Event")).check(matches(isDisplayed()));
    }

    /**
     * Added delay for loading UI stuff
     * */
    public static ViewAction waitForView(final int viewId, final long millis) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return isRoot();
            }

            @Override
            public String getDescription() {
                return "Wait for " + millis + "ms for view with ID " + viewId;
            }

            @Override
            public void perform(UiController uiController, View view) {
                final long startTime = System.currentTimeMillis();
                final long endTime = startTime + millis;
                do {
                    for (View child : TreeIterables.breadthFirstViewTraversal(view)) {
                        if (child.getId() == viewId) {
                            return;
                        }
                    }
                    uiController.loopMainThreadForAtLeast(50);
                } while (System.currentTimeMillis() < endTime);
                throw new PerformException.Builder()
                        .withActionDescription(this.getDescription())
                        .withViewDescription(HumanReadables.describe(view))
                        .build();
            }
        };
    }

    /**
     * Copied from MoodListTest.java
     * */
    private static Map<String, Object> createEvent(Object emotionalState, Object trigger, Object date, Object socialSituation, Object location) {
        Map<String, Object> event = new HashMap<>();
        event.put("emotional_state", emotionalState);
        event.put("trigger", trigger);
        event.put("date", date);
        event.put("social_situation", socialSituation);
        event.put("location", location);
        event.put("is_public", false);
        return event;
    }

    /**
     * borrowed from MoodListTest
     */
    private static Map<String, Object> createRecentEvent(Object emotionalState, Object trigger, Object date, Object socialSituation, Object location, String username, String id) {
        Map<String, Object> event = new HashMap<>();
        event.put("emotional_state", emotionalState);
        event.put("trigger", trigger);
        event.put("date", date);
        event.put("social_situation", socialSituation);
        event.put("location", location);
        event.put("username", username);
        event.put("mood_id", id);
        event.put("is_public", true);
        return event;
    }

}
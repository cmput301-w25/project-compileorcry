package ca.ualberta.compileorcry;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isRoot;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.os.SystemClock;
import android.util.Log;
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

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import ca.ualberta.compileorcry.domain.models.User;
import ca.ualberta.compileorcry.features.mood.model.EmotionalState;
import ca.ualberta.compileorcry.features.mood.model.MoodEvent;

public class HistoryTest {
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
        // TODO: Test Filtering, rn only checks feeds shown

        @Before
        public void seedDatabaseAndLogin() throws InterruptedException {
            // Create Moods for User1
            List<MoodEvent> moodsForUser1 = Arrays.asList(
                    new MoodEvent(EmotionalState.HAPPINESS, TimestampHelper.createTimestamp("2025-03-01"), "Won a game", "With friends", null),
                    new MoodEvent(EmotionalState.SADNESS, TimestampHelper.createTimestamp("2025-03-17"), "Lost a bet", "Alone", null)
            );

            // Add User1 to Database
            addUserWithMoodsAndFollowing("user1", "Alice", moodsForUser1, Arrays.asList());
            User.setTestUser("user1");
            // Delay
            SystemClock.sleep(2000);
        }

    @Test
    public void appShouldDisplayUserMoodHistory() throws InterruptedException {
        // Step 1: Launch Activity
        ActivityScenario.launch(MainActivity.class);
        // Ensure FeedFragment is open
        onView(withId(R.id.navigation_feed)).perform(click());

        // Wait for UI to load
        onView(isRoot()).perform(waitForView(R.id.feed_spinner, 5000));

        // Click on feed spinner
        onView(withId(R.id.feed_spinner)).perform(click());
        onView(withText("Feed...")).perform(click());

        // Step 3: Select "Following" from feed spinner
        onView(withId(R.id.feed_spinner)).perform(click());
        onView(withText("History")).perform(click());

        // Wait for UI to load
        onView(isRoot()).perform(waitForView(R.id.feed_spinner, 5000));

        // Step 4: Assert moods from followed users appear
        onView(withText("Happiness")).check(matches(isDisplayed()));
        onView(withText("Sadness")).check(matches(isDisplayed()));
    }

    public static void addUserWithMoodsAndFollowing(String username, String name, List<MoodEvent> moods, List<String> followingUsernames) throws InterruptedException {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference userDocRef = db.collection("users").document(username);

        // Create User Document
        Map<String, Object> userData = new HashMap<>();
        userData.put("username", username);
        userData.put("name", name);
        userDocRef.set(userData);
        Thread.sleep(500); // Let Firestore sync

        // Add Mood Events to 'mood_events' Subcollection
        CollectionReference moodsRef = userDocRef.collection("mood_events");
        for (MoodEvent mood : moods) {
            Map<String, Object> moodData = mood.toFireStoreMap(); // Assuming you have this method in MoodEvent
            moodsRef.document(mood.getId()).set(moodData);
        }
        Thread.sleep(500);

        // Add Followed Users to 'following' Subcollection
        CollectionReference followingRef = userDocRef.collection("following");
        for (String followedUser : followingUsernames) {
            Map<String, Object> followingData = new HashMap<>();
            followingData.put("username", followedUser);
            followingRef.document(followedUser).set(followingData);
        }
        Thread.sleep(500);
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

}

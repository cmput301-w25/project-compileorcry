package ca.ualberta.compileorcry;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.*;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.*;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.instanceOf;


import android.os.SystemClock;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class MoodInfoDialogTest {

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule = new ActivityScenarioRule<>(MainActivity.class);

    @BeforeClass
    public static void setup() {
        FirebaseFirestore.getInstance().useEmulator("10.0.2.2", 8080);
    }

    private void performLoginAndGoToFeed() {
        onView(withId(R.id.login_username_text)).perform(typeText("testuser"), closeSoftKeyboard());
        onView(withId(R.id.register_button)).perform(click());
        onView(withId(R.id.registration_username_text)).perform(typeText("testuser"), closeSoftKeyboard());
        onView(withId(R.id.registration_name_text)).perform(typeText("Test User"), closeSoftKeyboard());
        onView(withId(R.id.done_button)).perform(click());
        SystemClock.sleep(2000);
    }

    private void createMoodEvent(String state, String trigger, String situation) {
        onView(withId(R.id.navigation_new)).perform(click());



        // Set emotional state directly
        onView(withId(R.id.new_event_emotional_state_autocomplete))
                .perform(replaceText(state), closeSoftKeyboard());

        onView(withId(R.id.new_event_date_text)).perform(click());
        onView(withText("OK")).perform(click());
        onView(withText("OK")).perform(click());

        // Set trigger text
        onView(withId(R.id.new_event_trigger_text))
                .perform(replaceText(trigger), closeSoftKeyboard());

        // Set social situation directly
        onView(withId(R.id.new_event_social_situation_autocomplete))
                .perform(replaceText(situation), closeSoftKeyboard());

        // Submit
        onView(withId(R.id.create_button)).perform(click());

        SystemClock.sleep(1500); // Wait for navigation back
    }

    @Test
    public void testViewMoodEventFieldsVisibleInFollowing() {
        performLoginAndGoToFeed();
        createMoodEvent("Anger", "sunlight", "Alone");
        onView(withId(R.id.back_button)).perform(click());
        onView(withId(R.id.feed_spinner)).perform(click());
        onData(allOf(is(instanceOf(String.class)), is("History"))).perform(click());


        // Switch to Following (read-only view)
        onView(withId(R.id.feed_spinner)).perform(click());
        onData(allOf(is(instanceOf(String.class)), is("Following"))).perform(click());
        SystemClock.sleep(1000);

        onView(withText("Anger")).perform(click());

        // Assert that the mood details are correctly displayed
        onView(withId(R.id.moodinfo_state_text)).check(matches(withText("Anger")));
        onView(withId(R.id.moodinfo_trigger_display)).check(matches(withText("sunlight")));
        onView(withId(R.id.moodinfo_situation_text)).check(matches(withText("Alone")));
    }

    @Test
    public void testEditMoodEventUpdatesProperly() {
        performLoginAndGoToFeed();
        createMoodEvent("Anger", "sunlight", "Alone");
        onView(withId(R.id.back_button)).perform(click());
        onView(withId(R.id.feed_spinner)).perform(click());
        onData(allOf(is(instanceOf(String.class)), is("History"))).perform(click());


        onView(withText("Anger")).perform(click());

        onView(withId(R.id.moodinfo_state_auto_complete)).perform(replaceText("Happy"));
        onView(withId(R.id.moodinfo_trigger_text)).perform(replaceText("TestTrigger"));
        onView(withId(R.id.moodinfo_situation_auto_complete)).perform(replaceText("Alone"));
        onView(withId(R.id.save_button)).perform(click());

        SystemClock.sleep(2000);

        onView(withText("Anger")).perform(click());

        // Assert that the mood details are correctly displayed
        onView(withId(R.id.moodinfo_state_text)).check(matches(withText("Happy")));
        onView(withId(R.id.moodinfo_trigger_display)).check(matches(withText("TestTrigger")));
        onView(withId(R.id.moodinfo_situation_text)).check(matches(withText("Alone")));
    }

    @Test
    public void testDeleteMoodEventRemovesFromHistory() {
        performLoginAndGoToFeed();

        onView(withId(R.id.feed_spinner)).perform(click());
        onData(allOf(is(instanceOf(String.class)), is("History"))).perform(click());
        createMoodEvent("Scared", "loud sound", "Alone");

        onView(withId(R.id.recyclerViewMoodHistory)).perform(click());
        onView(withId(R.id.delete_button)).perform(click());

        SystemClock.sleep(1500);
    }

    @After
    public void tearDown() {
        try {
            URL url = new URL("http://10.0.2.2:8080/emulator/v1/projects/compile-or-cry-8c762/databases/(default)/documents");
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("DELETE");
            urlConnection.getResponseCode();
            urlConnection.disconnect();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

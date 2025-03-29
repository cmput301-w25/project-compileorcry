package ca.ualberta.compileorcry;


import static org.junit.Assert.fail;

import android.util.Log;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import ca.ualberta.compileorcry.domain.models.User;
import ca.ualberta.compileorcry.features.mood.data.MoodList;
import ca.ualberta.compileorcry.features.mood.data.QueryType;
import ca.ualberta.compileorcry.features.mood.model.Comment;
import ca.ualberta.compileorcry.features.mood.model.EmotionalState;
import ca.ualberta.compileorcry.features.mood.model.MoodEvent;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class MoodEventTest {
    private DocumentReference moodRef;
    private MoodEvent testEvent;

    @Rule
    public ActivityScenarioRule<MainActivity> scenario = new
            ActivityScenarioRule<MainActivity>(MainActivity.class);

    @BeforeClass
    public static void setup() {
        // Specific address for emulated device to access our localHost
        String androidLocalhost = "10.0.2.2";

        int portNumber = 8080;
        FirebaseFirestore.getInstance().useEmulator(androidLocalhost, portNumber);
    }

    @Before
    public void seedDatabase() throws InterruptedException {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference userDocRef = db.collection("users").document("test");
        DocumentReference moodEventRef = userDocRef.collection("mood_events").document("test");
        this.moodRef = moodEventRef;
        CollectionReference commentsRef = moodEventRef.collection("comments");
        Map<String,Map<String,Object>> moodEvents = new HashMap<>();
        Map<String, Object> testEventMap = createEvent((long) 1, "test", Timestamp.now(), "With friends", "s00twy");
        commentsRef.document("test1").set(createComment("testing comment 1", "testUser1",Timestamp.now()));
        commentsRef.document("test2").set(createComment("testing comment 2", "testUser2",Timestamp.now()));
        commentsRef.document("test3").set(createComment("testing comment 3", "testUser3",Timestamp.now()));
        moodEventRef.set(testEventMap);
        CountDownLatch latch = new CountDownLatch(1);
        User.register_user("test", "test", (user, error) -> {
            User.setActiveUser(user);
            latch.countDown();
        });
        MoodEvent moodEvent = new MoodEvent(EmotionalState.ANGER, Timestamp.now(), "test", "test", "test", true);
        moodEvent.setUsername("test");
        moodEvent.setIdFromDocRef(this.moodRef);
        this.testEvent = moodEvent;
        latch.await(10, TimeUnit.SECONDS);
    }

    @Test
    public void testGetComments() throws InterruptedException {
        this.testEvent.reloadComments("test");
        ArrayList<Comment> comments = this.testEvent.getComments("test");
        Set<String> ids = Set.of("test1","test2","test3");
        Set<String> loadedIds = new HashSet<>();
        for(Comment comment : comments){
            loadedIds.add(comment.getId());
        }
        Boolean test = loadedIds.containsAll(ids);
        Assert.assertTrue(test);
    }

    @Test
    public void testAddComment() throws InterruptedException {
        DocumentReference moodRef = this.moodRef;
        CountDownLatch latch = new CountDownLatch(1);
        MoodEvent moodEvent = this.testEvent;
        Comment comment = new Comment(this.testEvent,"test","test4",Timestamp.now(),"test4 msg");
        this.testEvent.addComment(comment, new MoodEvent.AddCommentCallback() {
            @Override
            public void onSuccess() throws InterruptedException {
                Boolean success = false;
                String commentID = comment.getId();
                moodEvent.reloadComments("user");
                ArrayList<Comment> comments = moodEvent.getComments("user");
                for(Comment comment : comments){
                    if(comment.getCommentMsg().equals("test4 msg")){
                        success = true;
                    }
                }
                if(!success){
                    moodRef.collection("comments").document(commentID).delete();
                    fail();
                }
                moodRef.collection("comments").document(commentID).delete();
                latch.countDown();
            }

            @Override
            public void onFailure(Exception e) {

            }
        }, "test");
        boolean completed = latch.await(10, TimeUnit.SECONDS); // Wait with timeout
        if (!completed) {
            fail("Test timed out waiting for callback.");
        }
    }

    private static Map<String, Object> createEvent(Object emotionalState, Object trigger, Object date, Object socialSituation, Object location) {
        Map<String, Object> event = new HashMap<>();
        event.put("emotional_state", emotionalState);
        event.put("trigger", trigger);
        event.put("date", date);
        event.put("social_situation", socialSituation);
        event.put("location", location);
        return event;
    }

    private static Map<String, Object> createComment(Object msg, Object username, Object date) {
        Map<String, Object> event = new HashMap<>();
        event.put("comment_message", msg);
        event.put("username", username);
        event.put("date_made", date);
        return event;
    }
}
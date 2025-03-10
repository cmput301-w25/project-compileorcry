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

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
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
import ca.ualberta.compileorcry.features.mood.model.EmotionalState;
import ca.ualberta.compileorcry.features.mood.model.MoodEvent;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class MoodListTest {
    @Rule
    public ActivityScenarioRule<MainActivity> scenario = new
            ActivityScenarioRule<MainActivity>(MainActivity.class);

    @BeforeClass
    public static void setup(){
        // Specific address for emulated device to access our localHost
        String androidLocalhost = "10.0.2.2";

        int portNumber = 8080;
        FirebaseFirestore.getInstance().useEmulator(androidLocalhost, portNumber);
    }

    @Before
    public void seedDatabase() throws InterruptedException {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference userDocRef = db.collection("users").document("test");
        CollectionReference moodEventsRef = userDocRef.collection("mood_events");
        CollectionReference followingRef = userDocRef.collection("following");
        CollectionReference recentsRef = db.collection("most_recent_moods");
        recentsRef.document("test").delete();
        Date twoWeeksAgo = new Date(System.currentTimeMillis() - 14 * 24 * 60 * 60 * 1000);
        Timestamp twoWeeksAgoTimestamp = new Timestamp(twoWeeksAgo);
        Map<String,Map<String,Object>> moodEvents = new HashMap<>();
        moodEvents.put("base", createEvent((long) 1, "test", Timestamp.now(), "With friends", "s00twy"));
        moodEvents.put("emotion", createEvent((long) 2, "ntestn", Timestamp.now(), "With friends", "s00twy"));
        moodEvents.put("trigger", createEvent((long) 1, "dummyData", Timestamp.now(), "With friends", "s00twy"));
        moodEvents.put("triggerNull", createEvent((long) 1, null, Timestamp.now(), "With friends", "s00twy"));
        moodEvents.put("date", createEvent((long) 1, "test", twoWeeksAgoTimestamp, "With friends", "s00twy"));
        moodEvents.put("locationNull", createEvent((long) 1, "testing", Timestamp.now(), "With friends", null));
        for (Map.Entry<String, Map<String, Object>> entry : moodEvents.entrySet()) {
            String eventId = entry.getKey();
            Map<String, Object> eventData = entry.getValue();
            moodEventsRef.document(eventId).set(eventData);
        }
        List<String> following = List.of("base","emotion","trigger","triggerNull","date","locationNull","locationFar");
        for(String string : following){
            followingRef.document(string).set(Map.of("username",string));
        }
        Map<String,Map<String,Object>> recentMoodEvents = new HashMap<>();
        recentMoodEvents.put("base", createRecemtEvent((long) 1, "test", Timestamp.now(), "With friends", "s00twy", "base", "test"));
        recentMoodEvents.put("emotion", createRecemtEvent((long) 2, "ntestn", Timestamp.now(), "With friends", "s00twy", "emotion", "test"));
        recentMoodEvents.put("trigger", createRecemtEvent((long) 1, "dummyData", Timestamp.now(), "With friends", "s00twy", "trigger", "test"));
        recentMoodEvents.put("triggerNull", createRecemtEvent((long) 1, null, Timestamp.now(), "With friends", "s00twy", "triggerNull", "test"));
        recentMoodEvents.put("date", createRecemtEvent((long) 1, "test", twoWeeksAgoTimestamp, "With friends", "s00twy", "date", "test"));
        recentMoodEvents.put("locationNull", createRecemtEvent((long) 1, "testing", Timestamp.now(), "With friends", null, "locationNull", "test"));
        recentMoodEvents.put("locationFar", createRecemtEvent((long) 1, "testing", Timestamp.now(), "With friends", "s10twy", "locationNull", "locationFar"));
        for (Map.Entry<String, Map<String, Object>> entry : recentMoodEvents.entrySet()) {
            String eventId = entry.getKey();
            Map<String, Object> eventData = entry.getValue();
            recentsRef.document(eventId).set(eventData);
        }
        CountDownLatch latch = new CountDownLatch(1);
        User.register_user("test","test", (user, error) -> {
            User.setActiveUser(user);
            latch.countDown();
        });
        latch.await(10, TimeUnit.SECONDS);
    }

    @Test
    public void personalHistory() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1); // Synchronization mechanism

        User.get_user("test", (user, error) -> {
            User.setActiveUser(user);
            MoodList.createMoodList(User.getActiveUser(), QueryType.HISTORY_MODIFIABLE,
                    new MoodList.MoodListListener() {
                        @Override
                        public void returnMoodList(MoodList moodList) {
                            ArrayList<MoodEvent> moodEvents = moodList.getMoodEvents();
                            Set<String> eventIds = Set.of("base", "emotion", "trigger", "triggerNull", "locationNull","date");
                            Set<String> eventIdsLoaded = new HashSet<>();
                            for (MoodEvent moodEvent : moodEvents) {
                                eventIdsLoaded.add(moodEvent.getId());
                            }
                            if(!eventIdsLoaded.equals(eventIds)){
                                fail();
                            }
                            latch.countDown(); // Signal completion
                        }

                        @Override
                        public void updatedMoodList() {
                            // Handled automatically
                        }

                        @Override
                        public void onError(Exception e) {
                            Log.e("DataList", e.getMessage());
                            latch.countDown(); // Avoid test hanging on failure
                        }
                    }, null);
        });

        boolean completed = latch.await(10, TimeUnit.SECONDS); // Wait with timeout
        if (!completed) {
            fail("Test timed out waiting for callback.");
        }
    }

    @Test
    public void personalHistoryRecent() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1); // Synchronization mechanism

        User.get_user("test", (user, error) -> {
            User.setActiveUser(user);
            MoodList.createMoodList(User.getActiveUser(), QueryType.HISTORY_RECENT,
                    new MoodList.MoodListListener() {
                        @Override
                        public void returnMoodList(MoodList moodList) {
                            ArrayList<MoodEvent> moodEvents = moodList.getMoodEvents();
                            Set<String> eventIds = Set.of("base", "emotion", "trigger", "triggerNull", "locationNull");
                            Set<String> eventIdsLoaded = new HashSet<>();
                            for (MoodEvent moodEvent : moodEvents) {
                                eventIdsLoaded.add(moodEvent.getId());
                            }
                            if(!eventIdsLoaded.equals(eventIds)){
                                fail();
                            }
                            latch.countDown(); // Signal completion
                        }

                        @Override
                        public void updatedMoodList() {
                            // Handled automatically
                        }

                        @Override
                        public void onError(Exception e) {
                            Log.e("DataList", e.getMessage());
                            latch.countDown(); // Avoid test hanging on failure
                        }
                    }, null);
        });

        boolean completed = latch.await(10, TimeUnit.SECONDS); // Wait with timeout
        if (!completed) {
            fail("Test timed out waiting for callback.");
        }
    }

    @Test
    public void personalHistoryEmotion() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1); // Synchronization mechanism

        User.get_user("test", (user, error) -> {
            User.setActiveUser(user);
            MoodList.createMoodList(User.getActiveUser(), QueryType.HISTORY_STATE,
                    new MoodList.MoodListListener() {
                        @Override
                        public void returnMoodList(MoodList moodList) {
                            ArrayList<MoodEvent> moodEvents = moodList.getMoodEvents();
                            Set<String> eventIds = Set.of("base", "trigger", "triggerNull", "locationNull","date");
                            Set<String> eventIdsLoaded = new HashSet<>();
                            for (MoodEvent moodEvent : moodEvents) {
                                eventIdsLoaded.add(moodEvent.getId());
                            }
                            if(!eventIdsLoaded.equals(eventIds)){
                                fail();
                            }
                            latch.countDown(); // Signal completion
                        }

                        @Override
                        public void updatedMoodList() {
                            // Handled automatically
                        }

                        @Override
                        public void onError(Exception e) {
                            Log.e("DataList", e.getMessage());
                            latch.countDown(); // Avoid test hanging on failure
                        }
                    }, EmotionalState.ANGER);
        });

        boolean completed = latch.await(10, TimeUnit.SECONDS); // Wait with timeout
        if (!completed) {
            fail("Test timed out waiting for callback.");
        }
    }

    @Test
    public void personalHistoryReason() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1); // Synchronization mechanism

        User.get_user("test", (user, error) -> {
            User.setActiveUser(user);
            MoodList.createMoodList(User.getActiveUser(), QueryType.HISTORY_REASON,
                    new MoodList.MoodListListener() {
                        @Override
                        public void returnMoodList(MoodList moodList) {
                            ArrayList<MoodEvent> moodEvents = moodList.getMoodEvents();
                            Set<String> eventIds = Set.of("base", "emotion", "locationNull","date");
                            Set<String> eventIdsLoaded = new HashSet<>();
                            for (MoodEvent moodEvent : moodEvents) {
                                eventIdsLoaded.add(moodEvent.getId());
                            }
                            if(!eventIdsLoaded.equals(eventIds)){
                                fail();
                            }
                            latch.countDown(); // Signal completion
                        }

                        @Override
                        public void updatedMoodList() {
                            // Handled automatically
                        }

                        @Override
                        public void onError(Exception e) {
                            Log.e("DataList", e.getMessage());
                            latch.countDown(); // Avoid test hanging on failure
                        }
                    }, "test");
        });

        boolean completed = latch.await(10, TimeUnit.SECONDS); // Wait with timeout
        if (!completed) {
            fail("Test timed out waiting for callback.");
        }
    }

    @Test
    public void following() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1); // Synchronization mechanism

        User.get_user("test", (user, error) -> {
            User.setActiveUser(user);
            MoodList.createMoodList(User.getActiveUser(), QueryType.FOLLOWING,
                    new MoodList.MoodListListener() {
                        @Override
                        public void returnMoodList(MoodList moodList) {
                            ArrayList<MoodEvent> moodEvents = moodList.getMoodEvents();
                            Set<String> eventIds = Set.of("base", "emotion", "trigger", "triggerNull", "locationNull","date","locationFar");
                            Set<String> eventIdsLoaded = new HashSet<>();
                            for (MoodEvent moodEvent : moodEvents) {
                                eventIdsLoaded.add(moodEvent.getId());
                            }
                            if(!eventIdsLoaded.equals(eventIds)){
                                fail();
                            }
                            latch.countDown(); // Signal completion
                        }

                        @Override
                        public void updatedMoodList() {
                            // Handled automatically
                        }

                        @Override
                        public void onError(Exception e) {
                            Log.e("DataList", e.getMessage());
                            latch.countDown(); // Avoid test hanging on failure
                        }
                    }, null);
        });

        boolean completed = latch.await(10, TimeUnit.SECONDS); // Wait with timeout
        if (!completed) {
            fail("Test timed out waiting for callback.");
        }
    }

    @Test
    public void followingRecent() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1); // Synchronization mechanism

        User.get_user("test", (user, error) -> {
            User.setActiveUser(user);
            MoodList.createMoodList(User.getActiveUser(), QueryType.FOLLOWING_RECENT,
                    new MoodList.MoodListListener() {
                        @Override
                        public void returnMoodList(MoodList moodList) {
                            ArrayList<MoodEvent> moodEvents = moodList.getMoodEvents();
                            Set<String> eventIds = Set.of("base", "emotion", "trigger", "triggerNull", "locationNull","locationFar");
                            Set<String> eventIdsLoaded = new HashSet<>();
                            for (MoodEvent moodEvent : moodEvents) {
                                eventIdsLoaded.add(moodEvent.getId());
                            }
                            if(!eventIdsLoaded.equals(eventIds)){
                                fail();
                            }
                            latch.countDown(); // Signal completion
                        }

                        @Override
                        public void updatedMoodList() {
                            // Handled automatically
                        }

                        @Override
                        public void onError(Exception e) {
                            Log.e("DataList", e.getMessage());
                            latch.countDown(); // Avoid test hanging on failure
                        }
                    }, null);
        });

        boolean completed = latch.await(10, TimeUnit.SECONDS); // Wait with timeout
        if (!completed) {
            fail("Test timed out waiting for callback.");
        }
    }

    @Test
    public void followingEmotion() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1); // Synchronization mechanism

        User.get_user("test", (user, error) -> {
            User.setActiveUser(user);
            MoodList.createMoodList(User.getActiveUser(), QueryType.FOLLOWING_STATE,
                    new MoodList.MoodListListener() {
                        @Override
                        public void returnMoodList(MoodList moodList) {
                            ArrayList<MoodEvent> moodEvents = moodList.getMoodEvents();
                            Set<String> eventIds = Set.of("base", "trigger", "triggerNull", "locationNull","date","locationFar");
                            Set<String> eventIdsLoaded = new HashSet<>();
                            for (MoodEvent moodEvent : moodEvents) {
                                eventIdsLoaded.add(moodEvent.getId());
                            }
                            if(!eventIdsLoaded.equals(eventIds)){
                                fail();
                            }
                            latch.countDown(); // Signal completion
                        }

                        @Override
                        public void updatedMoodList() {
                            // Handled automatically
                        }

                        @Override
                        public void onError(Exception e) {
                            Log.e("DataList", e.getMessage());
                            latch.countDown(); // Avoid test hanging on failure
                        }
                    }, EmotionalState.ANGER);
        });

        boolean completed = latch.await(10, TimeUnit.SECONDS); // Wait with timeout
        if (!completed) {
            fail("Test timed out waiting for callback.");
        }
    }

    @Test
    public void followingReason() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1); // Synchronization mechanism

        User.get_user("test", (user, error) -> {
            User.setActiveUser(user);
            MoodList.createMoodList(User.getActiveUser(), QueryType.FOLLOWING_REASON,
                    new MoodList.MoodListListener() {
                        @Override
                        public void returnMoodList(MoodList moodList) {
                            ArrayList<MoodEvent> moodEvents = moodList.getMoodEvents();
                            Set<String> eventIds = Set.of("base", "emotion", "locationNull","date","locationFar");
                            Set<String> eventIdsLoaded = new HashSet<>();
                            for (MoodEvent moodEvent : moodEvents) {
                                eventIdsLoaded.add(moodEvent.getId());
                            }
                            if(!eventIdsLoaded.equals(eventIds)){
                                fail();
                            }
                            latch.countDown(); // Signal completion
                        }

                        @Override
                        public void updatedMoodList() {
                            // Handled automatically
                        }

                        @Override
                        public void onError(Exception e) {
                            Log.e("DataList", e.getMessage());
                            latch.countDown(); // Avoid test hanging on failure
                        }
                    }, "test");
        });

        boolean completed = latch.await(10, TimeUnit.SECONDS); // Wait with timeout
        if (!completed) {
            fail("Test timed out waiting for callback.");
        }
    }

    @Test
    public void mapPersonal() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1); // Synchronization mechanism

        User.get_user("test", (user, error) -> {
            User.setActiveUser(user);
            MoodList.createMoodList(User.getActiveUser(), QueryType.MAP_PERSONAL,
                    new MoodList.MoodListListener() {
                        @Override
                        public void returnMoodList(MoodList moodList) {
                            ArrayList<MoodEvent> moodEvents = moodList.getMoodEvents();
                            Set<String> eventIds = Set.of("base", "emotion", "trigger", "triggerNull","date");
                            Set<String> eventIdsLoaded = new HashSet<>();
                            for (MoodEvent moodEvent : moodEvents) {
                                eventIdsLoaded.add(moodEvent.getId());
                            }
                            if(!eventIdsLoaded.equals(eventIds)){
                                fail();
                            }
                            latch.countDown(); // Signal completion
                        }

                        @Override
                        public void updatedMoodList() {
                            // Handled automatically
                        }

                        @Override
                        public void onError(Exception e) {
                            Log.e("DataList", e.getMessage());
                            latch.countDown(); // Avoid test hanging on failure
                        }
                    }, null);
        });

        boolean completed = latch.await(10, TimeUnit.SECONDS); // Wait with timeout
        if (!completed) {
            fail("Test timed out waiting for callback.");
        }
    }

    @Test
    public void mapFollowing() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1); // Synchronization mechanism

        User.get_user("test", (user, error) -> {
            User.setActiveUser(user);
            MoodList.createMoodList(User.getActiveUser(), QueryType.MAP_FOLLOWING,
                    new MoodList.MoodListListener() {
                        @Override
                        public void returnMoodList(MoodList moodList) {
                            ArrayList<MoodEvent> moodEvents = moodList.getMoodEvents();
                            Set<String> eventIds = Set.of("base", "emotion", "trigger", "triggerNull","date","locationFar");
                            Set<String> eventIdsLoaded = new HashSet<>();
                            for (MoodEvent moodEvent : moodEvents) {
                                eventIdsLoaded.add(moodEvent.getId());
                            }
                            if(!eventIdsLoaded.equals(eventIds)){
                                fail();
                            }
                            latch.countDown(); // Signal completion
                        }

                        @Override
                        public void updatedMoodList() {
                            // Handled automatically
                        }

                        @Override
                        public void onError(Exception e) {
                            Log.e("DataList", e.getMessage());
                            latch.countDown(); // Avoid test hanging on failure
                        }
                    }, null);
        });

        boolean completed = latch.await(10, TimeUnit.SECONDS); // Wait with timeout
        if (!completed) {
            fail("Test timed out waiting for callback.");
        }
    }

    @Test
    public void mapClose() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1); // Synchronization mechanism

        User.get_user("test", (user, error) -> {
            User.setActiveUser(user);
            MoodList.createMoodList(User.getActiveUser(), QueryType.MAP_CLOSE,
                    new MoodList.MoodListListener() {
                        @Override
                        public void returnMoodList(MoodList moodList) {
                            ArrayList<MoodEvent> moodEvents = moodList.getMoodEvents();
                            Set<String> eventIds = Set.of("base", "emotion", "trigger", "triggerNull","date");
                            Set<String> eventIdsLoaded = new HashSet<>();
                            for (MoodEvent moodEvent : moodEvents) {
                                eventIdsLoaded.add(moodEvent.getId());
                            }
                            if(!eventIdsLoaded.equals(eventIds)){
                                fail();
                            }
                            latch.countDown(); // Signal completion
                        }

                        @Override
                        public void updatedMoodList() {
                            // Handled automatically
                        }

                        @Override
                        public void onError(Exception e) {
                            Log.e("DataList", e.getMessage());
                            latch.countDown(); // Avoid test hanging on failure
                        }
                    }, "s00twy");
        });

        boolean completed = latch.await(10, TimeUnit.SECONDS); // Wait with timeout
        if (!completed) {
            fail("Test timed out waiting for callback.");
        }
    }

    @Test
    public void addAndDeleteMoodEvent() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1); // Synchronization mechanism

        User.get_user("test", (user, error) -> {
            User.setActiveUser(user);
            MoodList.createMoodList(User.getActiveUser(), QueryType.HISTORY_MODIFIABLE,
                    new MoodList.MoodListListener() {
                        MoodList moodListOuter;
                        MoodEvent moodEvent = new MoodEvent(EmotionalState.DISGUST, Timestamp.now(), "trigger", "Alone", "picture");
                        boolean addSuccess = false;
                        @Override
                        public void returnMoodList(MoodList moodList) {
                            moodList.addMoodEvent(moodEvent);
                            moodListOuter = moodList;
                        }

                        @Override
                        public void updatedMoodList() {
                            if(moodListOuter != null){
                                for(MoodEvent event : moodListOuter.getMoodEvents()) {
                                    if (moodEvent.getId() == event.getId()) {
                                        moodListOuter.deleteMoodEvent(moodEvent);
                                        addSuccess = true;
                                    }
                                }
                                if(addSuccess){
                                    boolean deleteSuccess = true;
                                    for(MoodEvent event : moodListOuter.getMoodEvents()) {
                                        if (moodEvent.getId() == event.getId()) {
                                            deleteSuccess = false;
                                            break;
                                        }
                                    }
                                    if(deleteSuccess){
                                        latch.countDown();
                                    }
                                }
                            }
                        }

                        @Override
                        public void onError(Exception e) {
                            Log.e("DataList", e.getMessage());
                            latch.countDown(); // Avoid test hanging on failure
                        }
                    }, null);
        });
        boolean completed = latch.await(10, TimeUnit.SECONDS); // Wait with timeout
        if (!completed) {
            fail("Test timed out waiting for callback.");
        }
    }
    @Test
    public void editMoodEvent() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1); // Synchronization mechanism

        User.get_user("test", (user, error) -> {
            User.setActiveUser(user);
            MoodList.createMoodList(User.getActiveUser(), QueryType.HISTORY_MODIFIABLE,
                    new MoodList.MoodListListener() {
                        MoodList moodListOuter;
                        Map<String,Object> editMap;
                        @Override
                        public void returnMoodList(MoodList moodList) {
                            moodListOuter = moodList;
                            MoodEvent baseEvent = new MoodEvent("base");
                            for(MoodEvent event: moodList.getMoodEvents()){
                                if(Objects.equals(event.getId(), "base")){
                                    baseEvent=event;
                                }
                            }
                            editMap = new HashMap<>();
                            editMap.put("emotional_state",EmotionalState.FEAR);
                            moodList.editMoodEvent(baseEvent,editMap);
                        }

                        @Override
                        public void updatedMoodList() {
                            if(moodListOuter != null){
                                for(MoodEvent event: moodListOuter.getMoodEvents()){
                                    if(event.getId().equals("base")){
                                        if(event.getEmotionalState().equals(EmotionalState.FEAR)){
                                            latch.countDown();
                                        }
                                    }
                                }
                            }
                        }

                        @Override
                        public void onError(Exception e) {
                            Log.e("DataList", e.getMessage());
                            latch.countDown(); // Avoid test hanging on failure
                        }
                    }, null);
        });
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
    private static Map<String, Object> createRecemtEvent(Object emotionalState, Object trigger, Object date, Object socialSituation, Object location, String username, String id) {
        Map<String, Object> event = new HashMap<>();
        event.put("emotional_state", emotionalState);
        event.put("trigger", trigger);
        event.put("date", date);
        event.put("social_situation", socialSituation);
        event.put("location", location);
        event.put("username", username);
        event.put("mood_id", id);
        return event;
    }
}
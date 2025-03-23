package ca.ualberta.compileorcry.features.mood.data;

import android.util.Log;

import androidx.annotation.NonNull;

import com.firebase.geofire.GeoFireUtils;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQueryBounds;
import com.firebase.geofire.core.GeoHash;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

import ca.ualberta.compileorcry.domain.models.User;
import ca.ualberta.compileorcry.features.mood.model.EmotionalState;
import ca.ualberta.compileorcry.features.mood.model.MoodEvent;


//Comment creation assisted using deepseek
//From: deepseek.com
//Prompt: suggest a class comment for this
//When: March 2nd 2025

/**
 * The MoodList class manages a collection of MoodEvent objects and interacts with Firestore to perform CRUD operations.
 * It supports various query types to filter and retrieve mood events based on different criteria such as emotional state,
 * reason, recency, and geographical proximity. The class also handles real-time updates through Firestore listeners,
 * ensuring that the mood list is always synchronized with the database.
 *
 * Key Features:
 * - Supports multiple query types defined in QueryType
 * - Allows adding, deleting, and editing mood events while ensuring data integrity and validity.
 * - Handles real-time updates through Firestore listeners, notifying the listener when changes occur.
 * - Manages the most recent mood event for the user and updates it automatically when new events are added or deleted.
 * - Supports geospatial queries to retrieve mood events within a specified radius of a location.
 * - Ensures that mood events are sorted by timestamp in descending order.
 *
 * Usage:
 * - Use the `createMoodList` factory method to initialize a MoodList instance with the desired query type and filter.
 * - Implement the `MoodListListener` interface to receive callbacks when the mood list is initialized or updated.
 * - Use the provided methods (`addMoodEvent`, `deleteMoodEvent`, `editMoodEvent`) to modify the mood list.
 *
 * Note:
 * - Certain operations (e.g., adding or editing events) are restricted based on the query type and write permissions.
 * - The class performs extensive validation to ensure that mood events and their associated data are valid before
 *   performing any Firestore operations.
 * @see MoodEvent
 * @see QueryType
 * @see MoodListListener
 */
public class MoodList {
    private final ArrayList<MoodEvent> moodEvents;  //a list of moodEvents, don't modify it outside of this class
    private final MoodList ptrToSelf;   //a reference to itself
    private boolean writeAllowed = false;   //internal boolean that prevents usage of add/delete/edit methods when using filtered queries
    private boolean dontUpdate = false;     //boolean that can be set true to prevent it from calling the update in the listener
    private boolean isMade = false;     //internal boolean to track if initialization is complete
    private boolean followingLoaded = false;    //internal boolean to determine if a following list has been initialized
    private boolean recentsType, mapType;       //booleans that are true if the query is from recents collection or require location respectively
    private final User user;        //the user that is logged in
    private final DocumentReference userDocRef;     //docref to the user
    private final CollectionReference followingRef;     //colref to the following subcollection
    private final CollectionReference moodEventsRef;    //colref to the mood_events subcollection
    private final CollectionReference moodEventsRecentRef;      //colref to the most_recent_moods collection
    private final QueryType queryType;      //the query type of this moodlist
    private Query query;        //firestore query for loading data
    private final MoodListListener listener;        //listener for callbacks
    private final FirebaseFirestore db;     //reference to the database
    private final ArrayList<String> followings;     //list of username of who the user follows
    private static final EnumSet<QueryType> reasonQueryTypes = EnumSet.of(QueryType.FOLLOWING_REASON,QueryType.HISTORY_REASON);     //a EnumSet of the reason query types
    private Object filter;      //the criteria for filtering in state and reason query types
    Semaphore recentSemi = new Semaphore(1,true);
    /**
     * Callback listener to handle returning data from asyn events
     */
    public interface MoodListListener {
        void returnMoodList(MoodList initalizedMoodList);

        void updatedMoodList();

        void onError(Exception e);
    }

    /**
     * Factory method to create a MoodList instance based on the specified query type and filter.
     * This method initializes a MoodList object with the appropriate configuration for the given query type.
     *
     * @param user     The user associated with the MoodList.
     * @param queryType The type of query to execute (e.g., HISTORY_MODIFIABLE, FOLLOWING_RECENT, etc.).
     * @param listener The listener to handle MoodList initialization and updates.
     * @param filter   An optional filter to apply to the query (e.g., EmotionalState, String, etc.).
     * @throws IllegalArgumentException If the filter is invalid for the specified query type.
     */
    public static void createMoodList(User user, QueryType queryType, MoodListListener listener, Object filter) {
        switch (queryType) {
            case HISTORY_MODIFIABLE:
                // Handle HISTORY_MODIFIABLE query type
                new MoodList(user, queryType, listener);
                break;
            case HISTORY_RECENT:
                // Handle HISTORY_RECENCY query type
                new MoodList(user, queryType, listener);
                break;
            case HISTORY_STATE:
                // Handle HISTORY_STATE query type
                if (filter instanceof EmotionalState) {
                    new MoodList(user, queryType, listener, filter);
                } else {
                    throw new IllegalArgumentException("filter needs to be of type Emotional State");
                }
                break;
            case HISTORY_REASON:
                // Handle HISTORY_REASON query type
                if (filter instanceof String) {
                    new MoodList(user, queryType, listener, filter);
                } else {
                    throw new IllegalArgumentException("filter needs to be of type Emotional String");
                }
                break;
            case FOLLOWING:
                // Handle FOLLOWING query type
                new MoodList(user, queryType, listener);
                break;
            case FOLLOWING_RECENT:
                // Handle FOLLOWING_RECENT query type
                new MoodList(user, queryType, listener);
                break;
            case FOLLOWING_STATE:
                // Handle FOLLOWING_STATE query type
                if (filter instanceof EmotionalState) {
                    new MoodList(user, queryType, listener, filter);
                } else {
                    throw new IllegalArgumentException("filter needs to be of type Emotional State");
                }
                break;
            case FOLLOWING_REASON:
                // Handle FOLLOWING_REASON query type
                if (filter instanceof String) {
                    new MoodList(user, queryType, listener, filter);
                } else {
                    throw new IllegalArgumentException("filter needs to be of type String");
                }
                break;
            case MAP_PERSONAL:
                // Handle MAP_PERSONAL query type
                new MoodList(user, queryType, listener);
                break;
            case MAP_FOLLOWING:
                // Handle MAP_FOLLOWED query type
                new MoodList(user, queryType, listener);
                break;
            case MAP_CLOSE:
                // Handle MAP_CLOSE query type
                new MoodList(user, queryType, listener, filter);
                break;
            default:
                // Handle unexpected query types
                throw new IllegalArgumentException("unsupported query type: " + queryType);
        }
    }

    private MoodList(User user, QueryType queryType, MoodListListener initListener) {
        this.user = user;
        this.queryType = queryType;
        this.listener = initListener;
        this.moodEvents = new ArrayList<MoodEvent>();
        this.followings = new ArrayList<String>();
        this.userDocRef = user.getUserDocRef();
        this.db = FirebaseFirestore.getInstance();
        this.followingRef = userDocRef.collection("following");
        this.moodEventsRef = userDocRef.collection("mood_events");
        this.moodEventsRecentRef = db.collection("most_recent_moods");
        this.ptrToSelf = this;
        switch (queryType) {
            case HISTORY_MODIFIABLE:
                getQuery();
                this.writeAllowed = true;
                attachMoodEventsListener(this.query);
                break;
            case HISTORY_RECENT:
                getQuery();
                attachMoodEventsListener(this.query);
                break;
            case FOLLOWING:
                this.recentsType = true;
                attachFollowersListener();
                break;
            case FOLLOWING_RECENT:
                this.recentsType = true;
                attachFollowersListener();
                break;
            case MAP_PERSONAL:
                getQuery();
                this.mapType = true;
                attachMoodEventsListener(this.query);
                break;
            case MAP_FOLLOWING:
                this.recentsType = true;
                this.mapType = true;
                attachFollowersListener();
                break;
            default:
                throw new IllegalArgumentException("unsupported query type: " + queryType);
        }
    }

    private MoodList(User user, QueryType queryType, MoodListListener initListener, Object filter) {
        this.user = user;
        this.queryType = queryType;
        this.listener = initListener;
        this.moodEvents = new ArrayList<MoodEvent>();
        this.followings = new ArrayList<String>();
        this.userDocRef = user.getUserDocRef();
        this.db = FirebaseFirestore.getInstance();
        this.followingRef = userDocRef.collection("following");
        this.moodEventsRef = userDocRef.collection("mood_events");
        this.moodEventsRecentRef = db.collection("most_recent_moods");
        this.ptrToSelf = this;
        this.filter = filter;
        switch (queryType) {
            case HISTORY_STATE:
                getQuery();
                attachMoodEventsListener(this.query);
                break;
            case FOLLOWING_STATE:
                this.recentsType = true;
                attachFollowersListener();
                break;
            case HISTORY_REASON:
                attachFollowersListener();
                break;
            case FOLLOWING_REASON:
                this.recentsType = true;
                attachFollowersListener();
                break;
            case MAP_CLOSE:
                this.recentsType = true;
                this.mapType = true;
                executeGeoQuery();
                break;
            default:
                throw new IllegalArgumentException("unsupported query type: " + queryType);
        }

    }

    /**
     * Adds a new MoodEvent to the list and updates Firestore.
     * This method ensures the event is valid, assigns it a unique ID, and updates the most recent event if necessary.
     *
     * @param event The MoodEvent to add.
     * @throws IllegalArgumentException If the MoodList is read-only or the event's username does not match the user.
     * @throws RuntimeException If the event cannot be stored due to invalid data or Firestore errors.
     */
    public void addMoodEvent(MoodEvent event) {
        if(!this.writeAllowed){
            throw new IllegalArgumentException("cannot add events to read only MoodList");
        }
        if(event.getUsername() != null && !event.getUsername().equals(user.getUsername())){
            throw new IllegalArgumentException("username of event does not match username of user");
        }
        String id = this.moodEventsRef.document().getId();
        DocumentReference moodEventDocRef = moodEventsRef.document(id);
        event.setIdFromDocRef(moodEventDocRef);
        Map<String,Object> eventMap = event.toFireStoreMap();
        if(!this.isPersonalEventMapValid(eventMap)){
            throw new IllegalArgumentException("this event has invalid date or emotional_state");
        } else {
            moodEventDocRef.set(eventMap);
        }
        if(event.getPublic()){
            ExecutorService executor = Executors.newSingleThreadExecutor();
            executor.execute(() -> {
                try {
                    recentSemi.acquire();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                CollectionReference recentEventDocRef = this.moodEventsRecentRef.document(this.user.getUsername()).collection("recent_moods");
                recentEventDocRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        Map<String, Timestamp> current_recents = new HashMap<>();
                        boolean allValid = true;
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                // Access the document data
                                String documentId = document.getId();
                                Object timestamp = document.get("date");
                                if (timestamp != null && timestamp instanceof Timestamp) {
                                    current_recents.put(documentId, (Timestamp) timestamp);
                                } else {
                                    allValid = false;
                                    break;
                                }
                            }
                            if (current_recents.size() >= 3) {
                                if (allValid) {
                                    Timestamp leastRecentTime = null;
                                    String leastRecentKey = null;
                                    for (Map.Entry<String, Timestamp> entry : current_recents.entrySet()) {
                                        Timestamp currentTimestamp = entry.getValue();
                                        // If leastRecentTimestamp is null or currentTimestamp is older than leastRecentTimestamp
                                        if (leastRecentTime == null || currentTimestamp.compareTo(leastRecentTime) < 0) {
                                            leastRecentTime = currentTimestamp;
                                            leastRecentKey = entry.getKey();
                                        }
                                    }
                                    //if the new event is more recent than the old recent one
                                    if (event.getTimestamp().compareTo(leastRecentTime) > 0) {
                                        Map<String, Object> eventMap = event.toFireStoreMap();
                                        eventMap.put("username", user.getUsername());
                                        eventMap.put("mood_id", id);
                                        if (!ptrToSelf.isRecentEventMapValid(eventMap)) {
                                            //this error should only occur under extreme circumstances
                                            //if this becomes an issue, a clone method on the event should be used
                                            listener.onError(new RuntimeException("recent event is invalid"));
                                            return;
                                        }
                                        try {
                                            Tasks.await(recentEventDocRef.document(leastRecentKey).delete());
                                        } catch (Exception e) {
                                            listener.onError(e);
                                        }
                                        try {
                                            Tasks.await(recentEventDocRef.document(event.getId()).set(eventMap));
                                        } catch (Exception e) {
                                            listener.onError(e);
                                        }
                                        ptrToSelf.recentSemi.release();
                                    }
                                } else {
                                    ptrToSelf.recentSemi.release();
                                    listener.onError(new IllegalArgumentException("the recent document for the user has an invalid timestamp"));
                                }

                            } else {
                                // Document does not exist, thus should be set
                                Map<String, Object> eventMap = event.toFireStoreMap();
                                eventMap.put("username", user.getUsername());
                                eventMap.put("mood_id", id);
                                if (!ptrToSelf.isRecentEventMapValid(eventMap)) {
                                    //this error should only occur under extreme circumstances
                                    //if this becomes an issue, a clone method on the event should be used
                                    throw new RuntimeException("the event map was incorrectly formatted");
                                }
                                try {
                                    Tasks.await(recentEventDocRef.document(event.getId()).set(eventMap));
                                } catch (Exception e) {
                                    listener.onError(e);
                                }
                                ptrToSelf.recentSemi.release();
                            }
                        } else {
                            // Handle any errors that occurred while fetching the document
                            Exception e = task.getException();
                            if (e != null) {
                                ptrToSelf.recentSemi.release();
                                e.printStackTrace();
                            }
                        }
                    }
                });
            });
        }
        Collections.sort(moodEvents, new Comparator<MoodEvent>() {
            @Override
            public int compare(MoodEvent o1, MoodEvent o2) {
                return o2.getTimestamp().compareTo(o1.getTimestamp());
            }
        });
        if(!dontUpdate) {
            listener.updatedMoodList();
        }
    }
    /**
     * Deletes a MoodEvent from the list and Firestore.
     * If the deleted event is the most recent, it updates the most recent event in Firestore.
     *
     * @param event The MoodEvent to delete.
     * @throws IllegalArgumentException If the MoodList is read-only or the event does not have an ID.
     * @throws RuntimeException If Firestore operations fail or the event cannot be deleted.
     */
    public void deleteMoodEvent(MoodEvent event) {
        if (!this.writeAllowed) {
            throw new IllegalArgumentException("Cannot delete events from a read-only MoodList.");
        }
        if (event.getId() == null) {
            throw new IllegalArgumentException("Event must have an ID to be deleted.");
        }

        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                recentSemi.acquire();

                // Remove from local list first to reflect deletion immediately
                moodEvents.remove(event);

                // Reference to recent_moods collection
                CollectionReference recentEventDocRef = this.moodEventsRecentRef
                        .document(this.user.getUsername())
                        .collection("recent_moods");

                // Check if the event is in recent_moods and handle accordingly
                boolean isRecent = false;
                HashSet<String> recentIds = new HashSet<>();
                try {
                    QuerySnapshot recentSnapshot = Tasks.await(recentEventDocRef.get());
                    for (QueryDocumentSnapshot doc : recentSnapshot) {
                        recentIds.add(doc.getId());
                        if (doc.getId().equals(event.getId())) {
                            isRecent = true;
                        }
                    }
                } catch (Exception e) {
                    listener.onError(e);
                }

                // If the event is a recent event, replace it before deleting
                if (isRecent) {
                    if (!moodEvents.isEmpty()) {
                        // Sort to find the new most recent event
                        Collections.sort(moodEvents, (o1, o2) -> o2.getTimestamp().compareTo(o1.getTimestamp()));
                        int index = -1;
                        for(int i = 0; i < moodEvents.size(); i++){
                            if(recentIds.contains(moodEvents.get(i).getId()) || !moodEvents.get(i).getPublic()){
                                continue;
                            } else {
                                index = i;
                                break;
                            }
                        }
                        if(index == -1){
                            try {
                                Tasks.await(recentEventDocRef.document(event.getId()).delete());
                            } catch (Exception e) {
                                listener.onError(e);
                            }
                        } else {
                            MoodEvent newMostRecent = moodEvents.get(index);
                            Map<String, Object> eventMap = newMostRecent.toFireStoreMap();
                            eventMap.put("username", user.getUsername());
                            eventMap.put("mood_id", newMostRecent.getId());

                            // Update recent_moods with the new event and delete the old one
                            try {
                                Tasks.await(recentEventDocRef.document(newMostRecent.getId()).set(eventMap));
                                Tasks.await(recentEventDocRef.document(event.getId()).delete());
                            } catch (Exception e) {
                                listener.onError(e);
                            }
                        }
                    } else {
                        // No events left, delete from recent_moods
                        try {
                            Tasks.await(recentEventDocRef.document(event.getId()).delete());
                        } catch (Exception e) {
                            listener.onError(e);
                        }
                    }
                }

                // Delete from the main collection regardless of recent processing
                try {
                    Tasks.await(moodEventsRef.document(event.getId()).delete());
                } catch (Exception e) {
                    listener.onError(e);
                }

                // Update the local list and notify listener
                Collections.sort(moodEvents, (o1, o2) -> o2.getTimestamp().compareTo(o1.getTimestamp()));
                if (!dontUpdate) {
                    listener.updatedMoodList();
                }

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                listener.onError(e);
            } finally {
                recentSemi.release();
            }
        });
    }
    /**
     * Edits an existing MoodEvent and updates Firestore.
     * This method applies the specified changes to the event and ensures the updates are valid.
     *
     * @param event   The MoodEvent to edit.
     * @param changes A map of key-value pairs representing the changes to apply.
     *                The usable keys are: "picture", "social_situation", "date", "location", "trigger", "emotional_state"
     *                They must be paired with the corresponding datatype for the attribute in MoodEvent
     * @throws IllegalArgumentException If the MoodList is read-only, the event is invalid, or the changes are invalid.
     * @throws RuntimeException If Firestore operations fail or the event cannot be updated.
     */
    public void editMoodEvent(MoodEvent event, Map<String, Object> changes) {
        if(!this.writeAllowed){
            throw new IllegalArgumentException("cannot add events to read only MoodList");
        }
        if(event.getId() == null){
            throw new IllegalArgumentException("mood event does not have an Id");
        }
        if(!containsMoodEvent(event)){
            throw new IllegalArgumentException("mood event is not in MoodList");
        }
        Map<String,Object> map = event.toFireStoreMap();
        final List<Task<DocumentSnapshot>> tasks = new ArrayList<>();
        tasks.add(this.moodEventsRef.document(event.getId()).get());
        tasks.add(this.moodEventsRecentRef.document(user.getUsername()).collection("recent_moods").document(event.getId()).get());
        DocumentReference recentMoodDocRef = this.moodEventsRecentRef.document(user.getUsername()).collection("recent_moods").document(event.getId());
        updateEventFromMap(event,changes);
        Tasks.whenAllComplete(tasks).addOnCompleteListener(new OnCompleteListener<List<Task<?>>>() {
            @Override
            public void onComplete(@NonNull Task<List<Task<?>>> t) {
                Map<DocumentReference, DocumentSnapshot> docMap = new HashMap<>();

                // Process the results here if needed
                for (Task<?> task : tasks) {
                    if (task.isSuccessful() && task.getResult() instanceof DocumentSnapshot) {
                        DocumentSnapshot doc = (DocumentSnapshot) task.getResult();
                        if (doc.getReference().equals(ptrToSelf.moodEventsRef.document(event.getId()))) {
                            docMap.put(ptrToSelf.moodEventsRef.document(event.getId()), doc);
                        } else if (doc.getReference().equals(recentMoodDocRef)) {
                            docMap.put((recentMoodDocRef), doc);
                        }
                    }
                }
                DocumentSnapshot personalDoc = docMap.get(ptrToSelf.moodEventsRef.document(event.getId()));
                DocumentSnapshot recentDoc = docMap.get(recentMoodDocRef);
                if(personalDoc == null){
                    listener.onError(new IllegalArgumentException("no document related to this mood event"));
                    return;
                }
                Map<String,Object> eventMap  = event.toFireStoreMap();
                if(!ptrToSelf.isPersonalEventMapValid(eventMap)){
                    listener.onError(new IllegalArgumentException("the eventMap is bad value(s)"));
                    return;
                }
                eventMap.remove("username");
                ptrToSelf.moodEventsRef.document(event.getId()).set(eventMap);
                if (personalDoc != null && personalDoc.exists() && recentDoc != null && recentDoc.exists()){
                    if(personalDoc.get("mood_id").equals(recentDoc.get("mood_id"))){
                        eventMap.put("username", user.getUsername());
                        eventMap.put("mood_id", event.getId());
                        if(!ptrToSelf.isRecentEventMapValid(eventMap)){
                            listener.onError(new IllegalArgumentException("the eventMap has bad value(s)"));
                            return;
                        }
                        recentMoodDocRef.set(eventMap);
                    }

                }
                if(!ptrToSelf.dontUpdate){
                    listener.updatedMoodList();
                }
            }
        });
    }
    //Set this to true if you don't want the update listener to be called
    public void setDontUpdate(boolean bool){
        this.dontUpdate = bool;
    }

    public ArrayList<MoodEvent> getMoodEvents() {
        return moodEvents;
    }
    /**
     * Attaches a Firestore listener to the user's "following" collection.
     * This method updates the list of followed users and triggers a query for their MoodEvents.
     *
     * @throws RuntimeException If the Firestore listener fails to attach or the user is not following anyone.
     */
    private void attachFollowersListener() {
        followingRef.addSnapshotListener((value, error) -> {
            if (error != null) {
                Log.e("Firestore", error.toString());
                listener.onError(new RuntimeException("followers didn't attach"));
                return;
            }
            if (value != null) {
                followings.clear();
                for (QueryDocumentSnapshot snapshot : value) {
                    String username = snapshot.getString("username");
                    followings.add(username);
                }
            }
            if (followings.isEmpty()) {
                listener.onError(new RuntimeException("user is following nobody"));
                return;
            }
            if (!followingLoaded) {
                ptrToSelf.getQuery();
                attachMoodEventsListener(ptrToSelf.query);
                followingLoaded = true;
            } else {
                ptrToSelf.getQuery();
                //if needed a listener call here for update to followers
            }
        });
    }
    /**
     * Attaches a Firestore listener to the specified query to fetch MoodEvents.
     * This method processes the query results, creates MoodEvent objects, and updates the MoodList.
     *
     * @param query The Firestore query to listen to.
     * @throws RuntimeException If the Firestore listener fails to attach or the query results are invalid.
     */
    private void attachMoodEventsListener(Query query) {
        Log.d("Firestore", "attachMoodEventsListener() called");
        query.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot queryDocumentSnapshots, FirebaseFirestoreException e) {
                if (e != null) {
                    // Handle errors
                    Log.w("Firestore", "Listen failed.", e);
                    listener.onError(new RuntimeException("moodEvents didn't attach"));
                    return;
                }
                if (queryDocumentSnapshots != null) {
                    moodEvents.clear();
                }
                Log.d("Firestore", "Number of documents retrieved: " + queryDocumentSnapshots.size());
                // Process the documents
                for (DocumentSnapshot document : queryDocumentSnapshots) {
                    if (document.exists()) {
                        Map<String, Object> documentData = document.getData();
                        String id = document.getId();
                        MoodEvent moodEvent = new MoodEvent(id);
                        if (isValidKeyPairDatatype(documentData, "emotional_state", Long.class)) {
                            moodEvent.setEmotionalState(EmotionalState.fromCode((Long) documentData.get("emotional_state")));
                        } else {
                            listener.onError(new IllegalArgumentException("emotional state cannot be null"));
                            return;
                        }
                        if (isValidKeyPairDatatype(documentData, "date", Timestamp.class)) {
                            moodEvent.setTimestamp((Timestamp) documentData.get("date"));
                        } else {
                            listener.onError(new IllegalArgumentException("date cannot be null"));
                            return;
                        }
                        if (isValidKeyPairDatatype(documentData, "username", String.class)) {
                            moodEvent.setUsername((String) documentData.get("username"));
                        } else if (recentsType) {
                            listener.onError(new IllegalArgumentException("username cannot be null for querys of recentMoods or is not a String"));
                            return;
                        }
                        if (isValidKeyPairDatatype(documentData, "trigger", String.class)) {
                            moodEvent.setTrigger((String) documentData.get("trigger"));
                        }
                        if (isValidKeyPairDatatype(documentData, "social_situation", String.class)) {
                            moodEvent.setSocialSituation((String) documentData.get("social_situation"));
                        }
                        if (isValidKeyPairDatatype(documentData, "location", String.class)) {
                            GeoHash geoHash = new GeoHash((String) documentData.get("location"));
                            moodEvent.setLocation(geoHash);
                        } else if (mapType) {
                            listener.onError(new IllegalArgumentException("location cannot be null for map query or is not a the correct datatype"));
                            return;
                        }
                        //todo: picture datatype and retrieving the picture as the firestore cannot store pictures in a document
                        if (isValidKeyPairDatatype(documentData, "picture", String.class)) {
                            moodEvent.setPicture((String) documentData.get("picture"));
                        }
                        if (isValidKeyPairDatatype(documentData, "is_public", Boolean.class)) {
                            moodEvent.setPublic((Boolean) documentData.get("is_public"));
                        }
                        if(!containsMoodEvent(moodEvent)) {
                            moodEvents.add(moodEvent);
                        }
                    }
                }
                //runs the function to remove moodEvents that don't contain the reasonString
                if (reasonQueryTypes.contains(ptrToSelf.queryType)){
                    //filter should cast to String as the constructor ensures instanceOf
                    reasonStringSearch((String) filter);
                }
                if (!isMade) {
                    isMade = true;
                    listener.returnMoodList(ptrToSelf);
                }
                if (moodEvents.isEmpty()) {
                    Log.d("Firestore", "No moods found for this query.");
                }
                if (!dontUpdate) {
                    listener.updatedMoodList();
                }
            }
        });
    }

    /**
     * Checks if a key-value pair in a map is valid for a specific data type.
     *
     * @param data The map containing the data.
     * @param key  The key to check.
     * @param type The expected data type.
     * @return True if the key exists and the value is of the expected type, false otherwise.
     */
    private static <T> boolean isValidKeyPairDatatype(Map<String, Object> data, String key, Class<T> type) {
        return data.containsKey(key) && type.isInstance(data.get(key));// Return false if key doesn't exist or type mismatch
    }
    /**
     * Constructs a Firestore query based on the current query type and filter.
     * This method configures the query to fetch MoodEvents according to the specified criteria.
     *
     * @throws IllegalArgumentException If the query type is unsupported or the filter is invalid.
     */
    private void getQuery() {
        Date lastWeekDate = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(lastWeekDate);
        calendar.add(Calendar.DAY_OF_YEAR, -7);
        lastWeekDate = calendar.getTime();
        Timestamp lastWeek = new Timestamp(lastWeekDate);

        EmotionalState filterState;
        Query query = this.moodEventsRef
                .orderBy("date", Query.Direction.DESCENDING);
        switch (this.queryType) {
            case HISTORY_MODIFIABLE:
                break;
            case HISTORY_RECENT:
                query = query.endAt(lastWeek)
                        .startAt(Timestamp.now());
                // Handle HISTORY_RECENCY query type
                break;
            case HISTORY_STATE:
                filterState = (EmotionalState) filter;
                query = query.whereEqualTo("emotional_state", filterState.getCode());
                // Handle HISTORY_STATE query type
                break;
            case HISTORY_REASON:
                // Handle HISTORY_REASON query type
                break;
            case FOLLOWING:
                query = db.collectionGroup("recent_moods").whereIn("username", followings)
                        .orderBy("username", Query.Direction.DESCENDING);
                // Handle FOLLOWING query type
                break;
            case FOLLOWING_RECENT:
                query = db.collectionGroup("recent_moods").whereIn("username", followings)
                        .orderBy("date", Query.Direction.DESCENDING)
                        .endAt(lastWeek)
                        .startAt(Timestamp.now());

                // Handle FOLLOWING_RECENT query type
                break;
            case FOLLOWING_STATE:
                filterState = (EmotionalState) filter;
                query = db.collectionGroup("recent_moods").whereIn("username", followings)
                        .whereEqualTo("emotional_state", filterState.getCode())
                        .orderBy("date", Query.Direction.DESCENDING);
                // Handle FOLLOWING_STATE query type
                break;
            case FOLLOWING_REASON:
                query = db.collectionGroup("recent_moods").whereIn("username", followings)
                        .orderBy("date", Query.Direction.DESCENDING);
                // Handle FOLLOWING_REASON query type
                break;
            case MAP_PERSONAL:
                query = query.whereNotEqualTo("location", null);
                break;
            case MAP_FOLLOWING:
                query = db.collectionGroup("recent_moods").whereIn("username", followings)
                        .whereNotEqualTo("location", null)
                        .orderBy("date", Query.Direction.DESCENDING);
                // Handle MAP_FOLLOWED query type
                break;
            case MAP_CLOSE:
                // Handle MAP_CLOSE query type
                break;
            default:
                // Handle unexpected query types
                throw new IllegalArgumentException("unsupported query type: " + queryType);
        }
        this.query = query;
    }

    //https://firebase.google.com/docs/firestore/solutions/geoqueries#java
    //borrowed from Firebase
    //At 01000 20 02 2025
    /**
     * Executes a geospatial query to fetch MoodEvents within a specified radius of a location.
     * This method uses GeoHash to filter events based on their proximity to the given location.
     *
     * @throws IllegalArgumentException If the location filter is invalid or the query results are invalid.
     * @throws RuntimeException If Firestore operations fail or the geospatial query cannot be executed.
     */
    private void executeGeoQuery() {

        GeoLocation location = GeoHash.locationFromHash((String)this.filter);
        List<GeoQueryBounds> bounds = GeoFireUtils.getGeoHashQueryBounds(location, 5000);
        final List<Task<QuerySnapshot>> tasks = new ArrayList<>();
        for (GeoQueryBounds b : bounds) {
            Query q = this.moodEventsRecentRef
                    .whereNotEqualTo("location",null)
                    .orderBy("location")
                    .startAt(b.startHash)
                    .endAt(b.endHash);

            tasks.add(q.get());
        }
        Tasks.whenAllComplete(tasks)
                .addOnCompleteListener(new OnCompleteListener<List<Task<?>>>() {
                    @Override
                    public void onComplete(@NonNull Task<List<Task<?>>> t) {
                        List<DocumentSnapshot> matchingDocs = new ArrayList<>();

                        for (Task<QuerySnapshot> task : tasks) {
                            QuerySnapshot snap = task.getResult();
                            for (DocumentSnapshot doc : snap.getDocuments()) {
                                Map<String, Object> documentData = doc.getData();
                                String id = doc.getId();
                                MoodEvent moodEvent = new MoodEvent(id);
                                if (isValidKeyPairDatatype(documentData, "location", String.class)) {
                                    GeoHash geoHash = new GeoHash((String) documentData.get("location"));
                                    moodEvent.setLocation(geoHash);
                                } else if (mapType) {
                                    listener.onError(new IllegalArgumentException("location cannot be null for map query or is not a the correct datatype"));
                                    return;
                                }
                                GeoLocation docLocation = GeoHash.locationFromHash(moodEvent.getLocation().getGeoHashString());
                                // We have to filter out a few false positives due to GeoHash
                                // accuracy, but most will match
                                double distanceInM = GeoFireUtils.getDistanceBetween(docLocation, location);
                                if (distanceInM <= 5000) {
                                    matchingDocs.add(doc);
                                }
                                if (isValidKeyPairDatatype(documentData, "emotional_state", Long.class)) {
                                    moodEvent.setEmotionalState(EmotionalState.fromCode((Long) documentData.get("emotional_state")));
                                } else {
                                    listener.onError(new IllegalArgumentException("emotional state cannot be null"));
                                    return;
                                }
                                if (isValidKeyPairDatatype(documentData, "date", Timestamp.class)) {
                                    moodEvent.setTimestamp((Timestamp) documentData.get("date"));
                                } else {
                                    listener.onError(new IllegalArgumentException("date cannot be null"));
                                    return;
                                }
                                if (isValidKeyPairDatatype(documentData, "username", String.class)) {
                                    moodEvent.setUsername((String) documentData.get("username"));
                                } else if (recentsType) {
                                    listener.onError(new IllegalArgumentException("username cannot be null for querys of recentMoods or is not a String"));
                                    return;
                                }
                                if (isValidKeyPairDatatype(documentData, "trigger", String.class)) {
                                    moodEvent.setTrigger((String) documentData.get("trigger"));
                                }
                                if (isValidKeyPairDatatype(documentData, "social_situation", String.class)) {
                                    moodEvent.setSocialSituation((String) documentData.get("social_situation"));
                                }
                                if (isValidKeyPairDatatype(documentData, "picture", String.class)) {
                                    moodEvent.setPicture((String) documentData.get("picture"));
                                }
                                if (isValidKeyPairDatatype(documentData, "is_public", Boolean.class)) {
                                    moodEvent.setPublic((Boolean) documentData.get("is_public"));
                                }
                                if(!containsMoodEvent(moodEvent)) {
                                    moodEvents.add(moodEvent);
                                }
                            }
                        }
                        listener.returnMoodList(ptrToSelf);
                    }
                });
    }
    /**
     * Validates a map of data to ensure it is suitable for storing a personal MoodEvent in Firestore.
     * todo: any data validation for reason if reason has a char limit.
     * @param map The map of data to validate.
     * @return True if the data is valid, false otherwise.
     */
    private boolean isPersonalEventMapValid(Map<String,Object> map){
        if(!isValidKeyPairDatatype(map,"date", Timestamp.class)){
            return false;
        }
        if(!isValidKeyPairDatatype(map,"emotional_state", Long.class)){
            return false;
        }
        if(!isValidKeyPairDatatype(map,"mood_id", String.class)){
            return false;
        }
        if(!isValidKeyPairDatatype(map,"is_public", Boolean.class)){
            return false;
        }
        map.remove("username");
        return true;
    }
    /**
     * Validates a map of data to ensure it is suitable for storing a recent MoodEvent in Firestore.
     * todo: any data validation for reason if reason has a char limit.
     * @param map The map of data to validate.
     * @return True if the data is valid, false otherwise.
     */
    private boolean isRecentEventMapValid(Map<String,Object> map){
        if(!isValidKeyPairDatatype(map,"date", Timestamp.class)){
            return false;
        }
        if(!isValidKeyPairDatatype(map,"emotional_state", Long.class)){
            return false;
        }
        if(!isValidKeyPairDatatype(map,"username", String.class)){
            return false;
        }
        if(!isValidKeyPairDatatype(map,"mood_id", String.class)){
            return false;
        }
        if(!isValidKeyPairDatatype(map,"is_public", Boolean.class)){
            return false;
        }
        return true;
    }
    /**
     * Validates a map of data to ensure it is suitable for editing a MoodEvent in Firestore.
     *
     * @param map The map of data to validate.
     * @return True if the data is valid, false otherwise.
     */
    private boolean isValidEditMap(Map<String,Object> map){
        if(map.containsKey("picture") && !isValidKeyPairDatatype(map, "picture", Object.class)){
            return false;
        }
        if(map.containsKey("social_situation") && !isValidKeyPairDatatype(map, "social_situation", String.class)){
            return false;
        }
        if(map.containsKey("date") && !isValidKeyPairDatatype(map, "date", Timestamp.class)){
            return false;
        }
        if(map.containsKey("location") && !isValidKeyPairDatatype(map, "location", GeoHash.class)){
            return false;
        }
        if(map.containsKey("trigger") && !isValidKeyPairDatatype(map, "trigger", String.class)){
            return false;
        }
        if(map.containsKey("emotional_state") && !isValidKeyPairDatatype(map, "emotional_state", EmotionalState.class)){
            return false;
        }
        return true;
    }
    /**
     * Updates a MoodEvent with the specified changes from a map.
     * This method ensures the changes are valid and applies them to the event.
     *
     * @param toBeUpdated The MoodEvent to update.
     * @param updateMap   key-value pairs representing the changes to apply.
     * @throws IllegalArgumentException If the update map contains invalid data.
     */
    private void updateEventFromMap(MoodEvent toBeUpdated, Map<String, Object> updateMap){
        if(!isValidEditMap(updateMap)){
            throw new IllegalArgumentException("updateMap contains incorrect datatype(s)");
        }
        ArrayList<String> updatableKeys = new ArrayList<>(Arrays.asList("picture", "social_situation", "date", "location", "trigger", "emotional_state"));
        Set<String> keySet = updateMap.keySet();
        for (String key : keySet) {
            if (updatableKeys.contains(key)) {
                switch (key) {
                    case "picture":
                        toBeUpdated.setPicture((String) updateMap.get(key)); // Set the picture
                        break;
                    case "social_situation":
                        toBeUpdated.setSocialSituation((String) updateMap.get(key)); // Set the social situation
                        break;
                    case "date":
                        toBeUpdated.setTimestamp((Timestamp) updateMap.get(key)); // Set the date (timestamp)
                        break;
                    case "location":
                        toBeUpdated.setLocation((GeoHash) updateMap.get(key)); // Set the location
                        break;
                    case "trigger":
                        toBeUpdated.setTrigger((String) updateMap.get(key)); // Set the trigger
                        break;
                    case "emotional_state":
                        toBeUpdated.setEmotionalState((EmotionalState) updateMap.get(key)); // Set the emotional state
                        break;
                    case "is_public":
                        toBeUpdated.setPublic((Boolean) updateMap.get(key)); // Set the emotional state
                        break;
                    default:
                        // Handle unexpected keys (if any)
                        break;
                }
            }
        }
    }
    //firestore doesn't have a feature to do this using queries.
    //The only options are to do it serverside which we cant, use a third party software which can cost $$, or do filtering clientside
    /**
     * removes all moodEvents that do not contain the reasonString
     *
     * @param reasonString The substring to search
     */
    private void reasonStringSearch(String reasonString){
        Iterator<MoodEvent> iter = moodEvents.iterator();
        while(iter.hasNext()) {
            MoodEvent event = iter.next();
            if(event.getTrigger() == null){
                iter.remove();
                continue;
            }
            if(!event.getTrigger().contains(reasonString)) {
                iter.remove(); // Removes the 'current' item
            }
        }
    }

    public void clearMoodEvents() {
        Log.d("MoodList", "clearMoodEvents() called, but delaying actual clearing until new data is fetched.");

        if (query != null) {
            attachMoodEventsListener(query);
        } else {
            Log.d("MoodList", "Query is null, cannot re-fetch.");
        }
    }
    /**
     * See if a moodEvent with the same ID as the mood event is passed in.
     * Note that MoodEvents from different users can share the same ID.
     * This is intended for internal use but I've left it public if someone finds use of it.
     *
     * @param event The event to check if it's contained in the MoodList
     * @return Returns true if the moodList contains the event and false if not, the ID is the comparison
     *
     */
    public boolean containsMoodEvent(MoodEvent event){
        for(MoodEvent containedEvent: this.moodEvents){
            if(event.getId().equals(containedEvent.getId())){
                return true;
            }
        }
        return false;
    }


}
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ca.ualberta.compileorcry.domain.models.User;
import ca.ualberta.compileorcry.features.mood.model.EmotionalState;
import ca.ualberta.compileorcry.features.mood.model.MoodEvent;

public class MoodList {
    private final ArrayList<MoodEvent> moodEvents;
    private final MoodList prtToSelf;
    private boolean writeAllowed = false;
    private boolean dontUpdate = false;
    private boolean isMade = false;
    private boolean followingLoaded = false;
    private boolean recentsType, mapType;
    private final User user;
    private final DocumentReference userDocRef;
    private final CollectionReference followingReference;
    private final CollectionReference moodEventsReference;
    private final CollectionReference moodEventsRecentRef;
    private final QueryType queryType;
    private Query query;
    private final MoodListListener listener;
    private final FirebaseFirestore db;
    private final ArrayList<String> followings;
    private Object filter;

    public interface MoodListListener {
        void returnMoodList(MoodList initalizedMoodList);

        void updatedMoodList();
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
        this.followingReference = userDocRef.collection("following");
        this.moodEventsReference = userDocRef.collection("mood_events");
        this.moodEventsRecentRef = db.collection("most_recent_moods");
        this.prtToSelf = this;
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
        this.followingReference = userDocRef.collection("following");
        this.moodEventsReference = userDocRef.collection("mood_events");
        this.moodEventsRecentRef = db.collection("most_recent_moods");
        this.prtToSelf = this;
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
                this.recentsType = true;
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
        String id = this.moodEventsReference.document().getId();
        DocumentReference moodEventDocRef = moodEventsReference.document(id);
        event.setIdFromDocRef(moodEventDocRef);
        Map<String,Object> eventMap = event.toFireStoreMap();
        if(!this.isPersonalEventMapValid(eventMap)){
            throw new IllegalArgumentException("this event has invalid date or emotional_state");
        } else {
            moodEventDocRef.set(eventMap);
        }
        DocumentReference recentEventDocRef = this.moodEventsRecentRef.document(this.user.getUsername());
        recentEventDocRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        if (document.get("date") != null && document.get("date") instanceof Timestamp){
                            Timestamp recentTime = (Timestamp) document.get("date");
                            //if the new event is more recent than the old recent one
                            if(event.getTimestamp().compareTo(recentTime) > 0){
                                Map<String,Object> eventMap = event.toFireStoreMap();
                                eventMap.put("username", user.getUsername());
                                eventMap.put("mood_id", id);
                                if(!prtToSelf.isRecentEventMapValid(eventMap)){
                                    //this error should only occur under extreme circumstances
                                    //if this becomes an issue, a clone method on the event should be used
                                    throw new RuntimeException("this error shouldn't occur, if this is happening it likey the moodevent was modified improperly before the onComplete listener finished");
                                }
                                recentEventDocRef.set(eventMap);
                            }
                        } else {
                            throw new IllegalArgumentException("the recent document for the user has an invalid timestampe");
                        }

                    } else {
                        // Document does not exist, thus should be set
                        Map<String,Object> eventMap = event.toFireStoreMap();
                        eventMap.put("username", user.getUsername());
                        eventMap.put("mood_id", id);
                        if(!prtToSelf.isRecentEventMapValid(eventMap)){
                            //this error should only occur under extreme circumstances
                            //if this becomes an issue, a clone method on the event should be used
                            throw new RuntimeException("this error shouldn't occur, if this is happening it likely the moodevent was modified improperly before the onComplete listener finished");
                        }
                        recentEventDocRef.set(eventMap);

                        }
                } else {
                    // Handle any errors that occurred while fetching the document
                    Exception e = task.getException();
                    if (e != null) {
                        e.printStackTrace();
                    }
                }
            }
        });
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
        if(!this.writeAllowed){
            throw new IllegalArgumentException("cannot add events to read only MoodList");
        }
        if (event.getId() == null){
            throw new IllegalArgumentException("a event needs a Id to be deleted");
        } else {
            DocumentReference recentEventDocRef = this.moodEventsRecentRef.document(user.getUsername());
            recentEventDocRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            //The SDK was telling me to do it this way thater than putting it in the if statement
                            //This is a check to see if the document we're deleting is the most recent.
                            boolean equals = document.get("mood_id").equals(event.getId());
                            if(equals){
                                // Replace the most recent with the new most recent before deleting it
                                Collections.sort(moodEvents, new Comparator<MoodEvent>() {
                                    @Override
                                    public int compare(MoodEvent o1, MoodEvent o2) {
                                        return o2.getTimestamp().compareTo(o1.getTimestamp());
                                    }
                                });
                                //Subtract one because we haven't deleted the mood event
                                if(moodEvents.size()-1 > 0){
                                    Map<String,Object> eventMap = moodEvents.get(0).toFireStoreMap();
                                    MoodEvent newEvent = moodEvents.get(0);
                                    if(moodEvents.get(0).getId().equals(event.getId())){
                                        //this is likely true, as the most recent event should still be most recent
                                        //This exist in the odd chance it isn't anymore.
                                        newEvent = moodEvents.get(1);
                                        eventMap = moodEvents.get(1).toFireStoreMap();
                                    }
                                    eventMap.put("username", user.getUsername());
                                    //this shouldn't be empty as the MoodList assigns an ID to every moodEvent in it
                                    eventMap.put("mood_id", newEvent.getId());
                                    if(!prtToSelf.isRecentEventMapValid(eventMap)){
                                        //this error should only occur under extreme circumstances
                                        //If this happened it's likely that some bad dummy data found it's way into the db
                                        throw new RuntimeException("the event that tried to replace most recnet was misformated");
                                    }
                                    recentEventDocRef.set(eventMap);
                                } else {
                                    recentEventDocRef.delete();
                                }
                            }
                        } else {
                            // Document should exist, if it doesn't that should be rectified by setting one
                            // First ensure moodEvents are sorted so we can grab the most recent.
                            Collections.sort(moodEvents, new Comparator<MoodEvent>() {
                                @Override
                                public int compare(MoodEvent o1, MoodEvent o2) {
                                    return o2.getTimestamp().compareTo(o1.getTimestamp());
                                }
                            });
                            Map<String,Object> eventMap = moodEvents.get(0).toFireStoreMap();
                            eventMap = event.toFireStoreMap();
                            eventMap.put("username", user.getUsername());
                            //this shouldn't be empty as the MoodList assigns an ID to every moodEvent in it
                            eventMap.put("mood_id", event.getId());
                            if(!prtToSelf.isRecentEventMapValid(eventMap)){
                                //this error should only occur under extreme circumstances
                                //If this happened it's likely that some bad dummy data found it's way into the db
                                throw new RuntimeException("this error shouldn't occur, if this is happening it likely the moodevent was modified improperly before the onComplete listener finished");
                            }
                            recentEventDocRef.set(eventMap);
                            }
                        moodEventsReference.document(event.getId()).delete();
                        moodEvents.remove(event);
                        Collections.sort(moodEvents, new Comparator<MoodEvent>() {
                            @Override
                            public int compare(MoodEvent o1, MoodEvent o2) {
                                return o2.getTimestamp().compareTo(o1.getTimestamp());
                            }
                        });
                        if(!dontUpdate) {
                            listener.updatedMoodList();
                        }
                    } else {
                        Exception e = task.getException();
                        if (e != null) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        }
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
            throw new IllegalArgumentException("mood event does not have an id");
        }
        if(!moodEvents.contains(event)){
            throw new IllegalArgumentException("mood event is not in moodList");
        }
        Map<String,Object> map = event.toFireStoreMap();
        final List<Task<DocumentSnapshot>> tasks = new ArrayList<>();
        tasks.add(this.moodEventsReference.document(event.getId()).get());
        tasks.add(this.moodEventsRecentRef.document(user.getUsername()).get());
        updateEventFromMap(event,changes);
        Tasks.whenAllComplete(tasks).addOnCompleteListener(new OnCompleteListener<List<Task<?>>>() {
            @Override
            public void onComplete(@NonNull Task<List<Task<?>>> t) {
                Map<DocumentReference, DocumentSnapshot> docMap = new HashMap<>();

                // Process the results here if needed
                for (Task<?> task : tasks) {
                    if (task.isSuccessful() && task.getResult() instanceof DocumentSnapshot) {
                        DocumentSnapshot doc = (DocumentSnapshot) task.getResult();
                        if (doc.getReference().equals(prtToSelf.moodEventsReference.document(event.getId()))) {
                            docMap.put(prtToSelf.moodEventsReference.document(event.getId()), doc);
                        } else if (doc.getReference().equals(prtToSelf.moodEventsRecentRef.document(user.getUsername()))) {
                            docMap.put(prtToSelf.moodEventsRecentRef.document(user.getUsername()), doc);
                        }
                    }
                }
                DocumentSnapshot personalDoc = docMap.get(prtToSelf.moodEventsReference.document(event.getId()));
                DocumentSnapshot recentDoc = docMap.get(prtToSelf.moodEventsRecentRef.document(user.getUsername()));
                if(personalDoc == null){
                    throw new IllegalArgumentException("no document related to this mood event");
                }
                Map<String,Object> eventMap  = event.toFireStoreMap();
                if(!prtToSelf.isPersonalEventMapValid(eventMap)){
                    throw new IllegalArgumentException("the eventMap is bad value(s)");
                }
                eventMap.remove("username");
                prtToSelf.moodEventsReference.document(event.getId()).set(eventMap);
                if (personalDoc != null && recentDoc != null){
                    if(personalDoc.get("mood_id").equals(recentDoc.get("mood_id"))){
                        eventMap.put("username", user.getUsername());
                        eventMap.put("mood_id", event.getId());
                        if(!prtToSelf.isRecentEventMapValid(eventMap)){
                            throw new IllegalArgumentException("the eventMap is bad value(s)");
                        }
                        prtToSelf.moodEventsRecentRef.document(user.getUsername()).set(eventMap);
                    }

                }
                if(!prtToSelf.dontUpdate){
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
        followingReference.addSnapshotListener((value, error) -> {
            if (error != null) {
                Log.e("Firestore", error.toString());
                throw new RuntimeException("followers didn't attach");
            }
            if (value != null) {
                followings.clear();
                for (QueryDocumentSnapshot snapshot : value) {
                    String username = snapshot.getString("username");
                    followings.add(username);
                }
            }
            if (followings.isEmpty()) {
                throw new RuntimeException("user is following nobody");
            }
            if (!followingLoaded) {
                prtToSelf.getQuery();
                attachMoodEventsListener(prtToSelf.query);
                followingLoaded = true;
            } else {
                prtToSelf.getQuery();
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
        query.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot queryDocumentSnapshots, FirebaseFirestoreException e) {
                if (e != null) {
                    // Handle errors
                    Log.w("Firestore", "Listen failed.", e);
                    throw new RuntimeException("moodEvents didn't attach");
                }
                moodEvents.clear();
                // Process the documents
                for (DocumentSnapshot document : queryDocumentSnapshots) {
                    if (document.exists()) {
                        Map<String, Object> documentData = document.getData();
                        String id = document.getId();
                        MoodEvent moodEvent = new MoodEvent(id);
                        if (isValidKeyPairDatatype(documentData, "emotional_state", Long.class)) {
                            moodEvent.setEmotionalState(EmotionalState.fromCode((Long) documentData.get("emotional_state")));
                        } else {
                            throw new IllegalArgumentException("emotional state cannot be null");
                        }
                        if (isValidKeyPairDatatype(documentData, "date", Timestamp.class)) {
                            moodEvent.setTimestamp((Timestamp) documentData.get("date"));
                        } else {
                            throw new IllegalArgumentException("date cannot be null");
                        }
                        if (isValidKeyPairDatatype(documentData, "username", String.class)) {
                            moodEvent.setUsername((String) documentData.get("username"));
                        } else if (recentsType) {
                            throw new IllegalArgumentException("username cannot be null for querys of recentMoods or is not a String");
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
                            throw new IllegalArgumentException("location cannot be null for map query or is not a the correct datatype");
                        }
                        //todo: picture datatype and retrieving the picture as the firestore cannot store pictures in a document
                        if (isValidKeyPairDatatype(documentData, "picture", Object.class)) {
                            Object picture = new Object();
                            moodEvent.setPicture(picture);
                        }
                        if(!moodEvents.contains(moodEvent)) {
                            moodEvents.add(moodEvent);
                        }
                    }
                }
                if (!isMade) {
                    isMade = true;
                    listener.returnMoodList(prtToSelf);
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
        Query query = this.moodEventsReference
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
                query = this.moodEventsRecentRef.whereIn("username", followings)
                        .orderBy("username", Query.Direction.DESCENDING);
                // Handle FOLLOWING query type
                break;
            case FOLLOWING_RECENT:
                query = this.moodEventsRecentRef.whereIn("username", followings)
                        .orderBy("date", Query.Direction.DESCENDING)
                        .endAt(lastWeek)
                        .startAt(Timestamp.now());

                // Handle FOLLOWING_RECENT query type
                break;
            case FOLLOWING_STATE:
                filterState = (EmotionalState) filter;
                query = this.moodEventsRecentRef.whereIn("username", followings)
                        .whereEqualTo("emotional_state", filterState.getCode())
                        .orderBy("date", Query.Direction.DESCENDING);
                // Handle FOLLOWING_STATE query type
                break;
            case FOLLOWING_REASON:
                query = this.moodEventsRecentRef.whereIn("username", followings)
                        .orderBy("date", Query.Direction.DESCENDING);
                // Handle FOLLOWING_REASON query type
                break;
            case MAP_PERSONAL:
                query = query.whereNotEqualTo("location", null);
                break;
            case MAP_FOLLOWING:
                query = this.moodEventsRecentRef.whereIn("username", followings)
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
                                    throw new IllegalArgumentException("location cannot be null for map query or is not a the correct datatype");
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
                                    throw new IllegalArgumentException("emotional state cannot be null");
                                }
                                if (isValidKeyPairDatatype(documentData, "date", Timestamp.class)) {
                                    moodEvent.setTimestamp((Timestamp) documentData.get("date"));
                                } else {
                                    throw new IllegalArgumentException("date cannot be null");
                                }
                                if (isValidKeyPairDatatype(documentData, "username", String.class)) {
                                    moodEvent.setUsername((String) documentData.get("username"));
                                } else if (recentsType) {
                                    throw new IllegalArgumentException("username cannot be null for querys of recentMoods or is not a String");
                                }
                                if (isValidKeyPairDatatype(documentData, "trigger", String.class)) {
                                    moodEvent.setTrigger((String) documentData.get("trigger"));
                                }
                                if (isValidKeyPairDatatype(documentData, "social_situation", String.class)) {
                                    moodEvent.setSocialSituation((String) documentData.get("social_situation"));
                                }

                                //todo: picture datatype and retrieving the picture as the firestore cannot store pictures in a document
                                if (isValidKeyPairDatatype(documentData, "picture", Object.class)) {
                                    Object picture = new Object();
                                    moodEvent.setPicture(picture);
                                }
                                if(!moodEvents.contains(moodEvent)) {
                                    moodEvents.add(moodEvent);
                                }
                            }
                        }
                        listener.returnMoodList(prtToSelf);
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
        return true;
    }
    /**
     * Validates a map of data to ensure it is suitable for editing a MoodEvent in Firestore.
     *
     * @param map The map of data to validate.
     * @return True if the data is valid, false otherwise.
     */
    private boolean checkIfGoodToStoreEdit(Map<String,Object> map){
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
        if(!checkIfGoodToStoreEdit(updateMap)){
            throw new IllegalArgumentException("updateMap contains incorrect datatype(s)");
        }
        ArrayList<String> updatableKeys = new ArrayList<>(Arrays.asList("picture", "social_situation", "date", "location", "trigger", "emotional_state"));
        Set<String> keySet = updateMap.keySet();
        for (String key : keySet) {
            if (updatableKeys.contains(key)) {
                switch (key) {
                    case "picture":
                        toBeUpdated.setPicture(updateMap.get(key)); // Set the picture
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
                    default:
                        // Handle unexpected keys (if any)
                        break;
                }
            }
        }
    }
}
package ca.ualberta.compileorcry.features.mood.model;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Transaction;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import ca.ualberta.compileorcry.domain.models.User;
//Comment creation assisted using deepseek
//From: deepseek.com
//Prompt: suggest a class comment for this
//When: March 24nd 2025
/**
 * A helper class for managing user follow relationships and follow requests using Firebase Firestore.
 * This class provides functionality to retrieve followers, followings, and follow requests,
 * as well as handle follow request creation, acceptance, and denial.
 *
 * <p>All methods in this class execute Firestore operations asynchronously using an {@link ExecutorService}
 * and return {@code null} in case of executor failure or timeout (10 seconds). It is crucial for callers
 * to check for {@code null} returns and handle potential {@link InterruptedException}s.</p>
 *
 * <p>Typical usage includes:</p>
 * <ul>
 *   <li>Retrieving lists of followers/followings for profile display</li>
 *   <li>Managing follow requests for private accounts</li>
 *   <li>Updating relationship status between users</li>
 * </ul>
 *
 * <p>This class automatically cleans up invalid follower/following documents that don't contain
 * valid username strings.</p>
 *
 * @see FirebaseFirestore
 * @see ExecutorService
 * @see User
 */

public class FollowHelper {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    /**
     * Used to get a list of all followers of a user.
     * Check if the return is null, as it returns null in the event of a executor failure.
     *
     * @param username  the username of the user who you want to get their followers
     * @return          a list of username of the people who follow the user
     * @throws InterruptedException
     */
    public static ArrayList<String> getFollowers(String username) throws InterruptedException {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        ArrayList<String> followers = new ArrayList<>();
        //Executor service to prevent deadlock on main
        executor.execute(() -> {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            QuerySnapshot followersSnapshot;
            try {
                //await the collection of all docs in the users following
                followersSnapshot = Tasks.await(db.collection("users").document(username).collection("followers").get());
                for (DocumentSnapshot doc : followersSnapshot.getDocuments()) {
                    //loop over each document in the collection
                    Map<String,Object> docData = doc.getData();
                    if(docData == null){
                        //sanity check to prevent nulls from going into the next if statement
                        continue;
                    }
                    if(docData.containsKey("username") && docData.get("username") instanceof String){
                        //ensure that username is in the doc before adding it into the followers
                        followers.add((String) docData.get("username"));
                    } else {
                        //this should only occur if a bad document is made via the firestore console
                        doc.getReference().delete();
                    }
                }
            } catch (ExecutionException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        //initiates the shutdown sequence which causes the executor to terminate onces its finished
        executor.shutdown();
        //timeout set to 10s, I've never had this take longer than 200ms, if this becomes an issue it can be increased
        Boolean success = executor.awaitTermination(10, TimeUnit.SECONDS);
        if(!success){
            return null;
        } else {
            return followers;
        }
    }
    /**
     * Used to get a list of all followings of a user.
     * Check if the return is null, as it returns null in the event of a executor failure.
     *
     * @param username  the username of the user who you want to get their followings
     * @return          a list of username of the people who follow the user
     * @throws InterruptedException
     */
    public static ArrayList<String> getFollowings(String username) throws InterruptedException {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        ArrayList<String> following = new ArrayList<>();
        //Executor service to prevent deadlock on main
        executor.execute(() -> {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            QuerySnapshot followersSnapshot;
            try {
                //await the collection of all docs in the users following
                followersSnapshot = Tasks.await(db.collection("users").document(username).collection("following").get());
                for (DocumentSnapshot doc : followersSnapshot.getDocuments()) {
                    //loop over each document in the collection
                    Map<String,Object> docData = doc.getData();
                    if(docData == null){
                        //sanity check to prevent nulls from going into the next if statement
                        continue;
                    }
                    if(docData.containsKey("username") && docData.get("username") instanceof String){
                        //ensure that username is in the doc before adding it into the followings
                        following.add((String) docData.get("username"));
                    } else {
                        //this should only occur if a bad document is made via the firestore console
                        doc.getReference().delete();
                    }
                }
            } catch (ExecutionException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        //initiates the shutdown sequence which causes the executor to terminate onces its finished
        executor.shutdown();
        //timeout set to 10s, I've never had this take longer than 200ms, if this becomes an issue it can be increased
        Boolean success = executor.awaitTermination(10, TimeUnit.SECONDS);
        if(!success){
            return null;
        } else {
            return following;
        }
    }
    /**
     * Used to get a list of all follow requests for a user.
     * Check if the return is null, as it returns null in the event of a executor failure.
     *
     * @param username  the username of the user who you want to get their followings
     * @return          a list of username of the people who follow requests the user
     * @throws InterruptedException
     */
    public static ArrayList<String> getFollowRequest(String username) throws InterruptedException {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        ArrayList<String> followRequests = new ArrayList<>();
        //Executor service to prevent deadlock on main
        executor.execute(() -> {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            QuerySnapshot followersSnapshot;
            try {
                //await the collection of all docs in the users follow_requests
                followersSnapshot = Tasks.await(db.collection("users").document(username).collection("follow_requests").get());
                for (DocumentSnapshot doc : followersSnapshot.getDocuments()) {
                    //loop over each document in the collection
                    Map<String,Object> docData = doc.getData();
                    if(docData == null){
                        //sanity check to prevent nulls from going into the next if statement
                        continue;
                    }
                    if(docData.containsKey("username") && docData.get("username") instanceof String){
                        //ensure that username is in the doc before adding it into the requests
                        followRequests.add((String) docData.get("username"));
                    } else {
                        //this should only occur if a bad document is made via the firestore console
                        doc.getReference().delete();
                    }
                }
            } catch (ExecutionException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        //initiates the shutdown sequence which causes the executor to terminate onces its finished
        executor.shutdown();
        //timeout set to 10s, I've never had this take longer than 200ms, if this becomes an issue it can be increased
        Boolean success = executor.awaitTermination(10, TimeUnit.SECONDS);
        if(!success){
            return null;
        } else {
            return followRequests;
        }
    }

    /**
     * This is used to accept or deny follow requests
     *
     * @param user      The user who is accepting the follow request
     * @param requester     The username of the person who sent the follow request
     * @param accept    True to accept, False to deny
     * @return
     * @throws InterruptedException
     */
    public static boolean handleFollowRequest(User user, String requester, boolean accept) throws InterruptedException {
        boolean success = false;
        ExecutorService executor = Executors.newSingleThreadExecutor();
        //Executor service to prevent deadlock on main
        executor.execute(() -> {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            try {
                //Await transaction success, transaction allows us to ensure none all operations occur at once
                Tasks.await(db.runTransaction(new Transaction.Function<Void>() {
                    @Override
                    public Void apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {
                        //follow request is deleted
                        transaction.delete(db.collection("users").document(user.getUsername()).collection("follow_requests").document(requester));
                        if(accept) {
                            //if the request is accepted then then following and followers tables indicate the new relationship
                            Map<String, Object> followerMap = Map.of("username", requester, "date", Timestamp.now());
                            Map<String, Object> followingMap = Map.of("username", user.getUsername(), "date", Timestamp.now());
                            transaction.set(db.collection("users").document(user.getUsername()).collection("followers").document(requester), followerMap);
                            transaction.set(db.collection("users").document(requester).collection("following").document(user.getUsername()), followingMap);
                        }
                        return null;
                    }
                }));
            } catch (ExecutionException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        //initiates the shutdown sequence which causes the executor to terminate onces its finished
        executor.shutdown();
        //timeout set to 10s, I've never had this take longer than 200ms, if this becomes an issue it can be increased
        success = executor.awaitTermination(10, TimeUnit.SECONDS);
        if(!success){
            return false;
        } else {
            return true;
        }
    }

    /**
     * This is create follow requests
     *
     * @param user      The user who is creating the follow request
     * @param requestee     The username of the person who is sent the follow request
     * @return
     * @throws InterruptedException
     */
    public static boolean createFollowRequest(User user, String requestee) throws InterruptedException {
        boolean success = false;
        ExecutorService executor = Executors.newSingleThreadExecutor();
        //Executor service to prevent deadlock on main
        executor.execute(() -> {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            Map<String,Object> requestMap = Map.of("username", user.getUsername(), "date", Timestamp.now());
            try {
                //attempts to add the follow_request
                Tasks.await(db.collection("users").document(requestee).collection("follow_requests").document(user.getUsername()).set(requestMap));
            } catch (ExecutionException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        //initiates the shutdown sequence which causes the executor to terminate onces its finished
        executor.shutdown();
        //timeout set to 10s, I've never had this take longer than 200ms, if this becomes an issue it can be increased
        success = executor.awaitTermination(10, TimeUnit.SECONDS);
        if(!success){
            return false;
        } else {
            return true;
        }
    }
}

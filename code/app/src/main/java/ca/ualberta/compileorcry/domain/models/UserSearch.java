package ca.ualberta.compileorcry.domain.models;

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

//Comment creation assisted using deepseek
//From: deepseek.com
//Prompt: suggest a class comment for this
//When: March 24th 2025
/**
 * A utility class for searching user usernames in a Firestore database based on a substring match.
 * This class performs asynchronous database operations to retrieve all usernames and filters them
 * to include only those containing the specified search substring. The search is executed with
 * a timeout constraint to prevent prolonged blocking.
 *
 * <p>Key features:
 * <ul>
 *   <li>Uses Firestore to fetch user data asynchronously in a background thread.</li>
 *   <li>Filters usernames to match those containing the provided search substring (case-sensitive).</li>
 *   <li>Enforces a 10-second timeout for database operations to ensure responsiveness.</li>
 *   <li>Returns {@code null} if the database operation exceeds the allowed execution time.</li>
 * </ul>
 *
 * <p>Example usage: Searching for users with "john" in their username would return "john_doe", "ajohnson", etc.
 *
 * @throws InterruptedException If the thread is interrupted while waiting for the database operation to complete.
 */
public class UserSearch {
    /**
     * Use this to find all users with the username or the substring
     *
     * @param searchString  the user we're looking to find, returns anything with the substring
     * @return      the usernames of all users in the db with the name
     * @throws InterruptedException
     */
    static ArrayList<String> findUser(String searchString) throws InterruptedException {
        ArrayList<String> array = new ArrayList<>();
        ExecutorService executor = Executors.newSingleThreadExecutor();

        //Runs the firestore stuff
        executor.execute(() -> {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            QuerySnapshot userSnapshot;
            try {
                userSnapshot = Tasks.await(db.collection("users").get());
                for (DocumentSnapshot doc : userSnapshot.getDocuments()) {
                    array.add(doc.getReference().getId());
                }
            } catch (ExecutionException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        executor.shutdown();
        Boolean success = executor.awaitTermination(10, TimeUnit.SECONDS);
        if(!success){
            return null;
        } else {
            UserSearch.reasonStringSearch(searchString,array);
            return array;
        }
    }


    /**
     * removes all moodEvents that do not contain the reasonString
     *
     * @param reasonString The substring to search
     */
    private static void reasonStringSearch(String reasonString, ArrayList<String> array){
        Iterator<String> iter = array.iterator();
        while(iter.hasNext()) {
            String username = iter.next();
            if(username == null){
                iter.remove();
                continue;
            }
            if(!username.contains(reasonString)) {
                iter.remove(); // Removes the 'current' item
            }
        }
    }
}

package ca.ualberta.compileorcry;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ca.ualberta.compileorcry.domain.models.UserSearch;

@RunWith(AndroidJUnit4.class)
@LargeTest

public class UserSearchTest {
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
        CollectionReference userColRef = db.collection("users");
        Set<String> usernames = Set.of("test","TEST","TeSt","nothing","12test12","something test something","sstestss","taeasat");
        for(String name : usernames){
            userColRef.document(name).set(Map.of("username",name));
        }
    }

    @Test
    public void testSearch() throws InterruptedException {
        Set<String> usernames = Set.of("test","TEST","TeSt","12test12","something test something","sstestss");
        ArrayList<String> searchResult = UserSearch.findUser("test");
        Set<String> searchResultSet = new HashSet<>(searchResult);
        Assert.assertEquals(searchResultSet, usernames);
    }
}

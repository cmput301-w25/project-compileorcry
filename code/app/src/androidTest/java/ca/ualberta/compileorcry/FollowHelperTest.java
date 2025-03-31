package ca.ualberta.compileorcry;

import static org.junit.Assert.fail;
import static ca.ualberta.compileorcry.TestHelper.resetFirebase;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.After;
import org.junit.Assert;
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
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import ca.ualberta.compileorcry.domain.models.User;
import ca.ualberta.compileorcry.features.mood.model.FollowHelper;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class FollowHelperTest {
    CollectionReference userColRef;
    DocumentReference user1Ref;
    DocumentReference user2Ref;
    DocumentReference user3Ref;
    DocumentReference user4Ref;

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
        this.userColRef = db.collection("users");
        this.user1Ref = this.userColRef.document("user1");
        this.user1Ref.set(Map.of("username","user1","name","user1"));
        this.user1Ref.collection("following").document("user2").set(Map.of("username","user2"));
        this.user1Ref.collection("following").document("user3").set(Map.of("username","user3"));
        this.user1Ref.collection("follow_requests").document("user4").set(Map.of("username","user4"));
        this.user2Ref = this.userColRef.document("user2");
        this.user2Ref.collection("followers").document("user1").set(Map.of("username","user1"));
        this.user2Ref.set(Map.of("username","user2","name","user2"));
        this.user3Ref = this.userColRef.document("user3");
        this.user3Ref.collection("followers").document("user1").set(Map.of("username","user1"));
        this.user3Ref.set(Map.of("username","user3","name","user3"));
        this.user4Ref = this.userColRef.document("user4");
        this.user4Ref.set(Map.of("username","user4","name","user4"));
        CountDownLatch latch = new CountDownLatch(1);
        User.get_user("user1", new User.OnUserLoadedListener() {
                    @Override
                    public void onUserLoaded(User user, String error) {
                        User.setActiveUser(user);
                        latch.countDown();
                    }
                });
        latch.await(10, TimeUnit.SECONDS);
    }

    @Test
    public void getFollowers() throws InterruptedException {
        Set<String> expectedFollowers = Set.of("user1");
        ArrayList<String> followers = FollowHelper.getFollowers("user2");
        Set<String> followersSet = new HashSet<>(followers);
        Assert.assertEquals(expectedFollowers,followersSet);
    }

    @Test
    public void getFollowing() throws InterruptedException {
        Set<String> expectedFollowings = Set.of("user2","user3");
        ArrayList<String> followings = FollowHelper.getFollowings("user1");
        Set<String> followingsSet = new HashSet<>(followings);
        Assert.assertEquals(expectedFollowings,followingsSet);
    }

    @Test
    public void unFollow() throws InterruptedException {
        Set<String> expectedFollowings = Set.of("user3");
        FollowHelper.unfollowUser("user1","user2");
        ArrayList<String> followerings = FollowHelper.getFollowings("user1");
        Set<String> followingSet = new HashSet<>(followerings);
        Assert.assertEquals(expectedFollowings,followingSet);
        Assert.assertTrue(FollowHelper.getFollowers("user2").isEmpty());
    }

    @Test
    public void isFollowing() throws InterruptedException {
        Assert.assertTrue(FollowHelper.isUserFollowing("user1","user2"));
        Assert.assertTrue(FollowHelper.isUserFollowing("user1","user3"));
        Assert.assertFalse(FollowHelper.isUserFollowing("user1","user4"));
        Assert.assertFalse(FollowHelper.isUserFollowing("user2","user1"));
    }

    @Test
    public void getFollowRequest() throws InterruptedException {
        Set<String> expectedFollowRequests = Set.of("user4");
        ArrayList<String> followRequests = FollowHelper.getFollowRequest("user1");
        Set<String> followRequestSet = new HashSet<>(followRequests);
        Assert.assertEquals(expectedFollowRequests,followRequestSet);
        Assert.assertTrue(FollowHelper.getFollowRequest("user2").isEmpty());
    }

    @Test
    public void hasUserRequestedFollow() throws InterruptedException {
        Assert.assertTrue(FollowHelper.hasUserRequestedFollow("user4","user1"));
        Assert.assertFalse(FollowHelper.hasUserRequestedFollow("user1","user2"));
    }

    @Test
    public void handleFollowRequestAccept() throws InterruptedException {
        Assert.assertTrue(FollowHelper.handleFollowRequest(User.getActiveUser(),"user4",true));
        Set<String> expectedFollowings = Set.of("user1");
        ArrayList<String> followings = FollowHelper.getFollowings("user4");
        Set<String> followingsSet = new HashSet<>(followings);
        Assert.assertEquals(expectedFollowings,followingsSet);
        Set<String> expectedFollowers = Set.of("user4");
        ArrayList<String> followers = FollowHelper.getFollowers("user1");
        Set<String> followersSet = new HashSet<>(followers);
        Assert.assertEquals(expectedFollowers,followersSet);
        Assert.assertTrue(FollowHelper.getFollowRequest("user1").isEmpty());
    }

    @Test
    public void handleFollowRequestDeny() throws InterruptedException {
        Assert.assertTrue(FollowHelper.handleFollowRequest(User.getActiveUser(),"user4",false));
        Assert.assertTrue(FollowHelper.getFollowRequest("user1").isEmpty());
        Assert.assertTrue(FollowHelper.getFollowers("user1").isEmpty());
        Assert.assertTrue(FollowHelper.getFollowings("user4").isEmpty());
    }

    @Test
    public void createFollowRequest() throws InterruptedException {
        Assert.assertTrue(FollowHelper.createFollowRequest(User.getActiveUser(),"user2"));
        Set<String> expectedFollowRequests = Set.of("user1");
        ArrayList<String> followRequests = FollowHelper.getFollowRequest("user2");
        Set<String> followRequestSet = new HashSet<>(followRequests);
        Assert.assertEquals(expectedFollowRequests,followRequestSet);
    }

    @After
    public void tearDown() {
        resetFirebase();
    }

}

package ca.ualberta.compileorcry;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import ca.ualberta.compileorcry.domain.data.OfflineHelper;
import ca.ualberta.compileorcry.domain.models.User;

/**
 * Main activity that serves as the entry point for the application.
 * This class sets up the navigation controller, manages the bottom navigation bar,
 * and handles the initial navigation based on user authentication status.
 *
 * Features:
 * - Configures the navigation components
 * - Hides the default action bar for custom UI
 * - Handles authentication state by showing appropriate initial screen
 * - Controls visibility of the bottom navigation bar
 * - Provides a method for showing navigation after successful login
 */

public class MainActivity extends AppCompatActivity {
    /** for logging purposes */
    private static final String TAG = "MainActivity";
    /** Navigation controller for managing navigation between fragments */
    private NavController navController;
    /** Bottom navigation view for main app navigation */
    private BottomNavigationView navView;

    /**
     * Initializes the activity, sets up the navigation components,
     * and navigates to the appropriate starting screen.
     *
     * @param savedInstanceState If non-null, this activity is being re-constructed from a previous saved state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Hide the action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Find the nav host fragment
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment_activity_main);

        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();

            // Set up bottom navigation
            navView = findViewById(R.id.nav_view);

            // Just connect the nav view to controller, no action bar setup
            NavigationUI.setupWithNavController(navView, navController);

            // Handle login navigation
            if (User.getActiveUser() == null) {
                User.checkActiveUser(this, (resumed, error) -> {
                    if(error != null){
                        Log.e("UserResume", error);
                    }

                    if(resumed){
                        Log.i("UserResume", "ActiveUser Resumed");
                        navController.navigate(R.id.navigation_feed);
                        navView.setSelectedItemId(R.id.navigation_feed);
                        navView.setVisibility(View.VISIBLE);
                    } else {
                        navController.navigate(R.id.navigation_login);
                        navView.setVisibility(View.GONE);
                    }
                });
            } else {
                // If user is already logged in, navigate to feed
                navController.navigate(R.id.navigation_feed);
                // Make sure the correct menu item is selected
                navView.setSelectedItemId(R.id.navigation_feed);
                navView.setVisibility(View.VISIBLE);
            }
        }
    }

    /**
     * Shows the bottom navigation bar and navigates to the feed screen.
     * This method is typically called after a successful login or registration.
     */
    public void showNavigation() {
        if (navView != null) {
            navView.setVisibility(View.VISIBLE);
            navController.navigate(R.id.navigation_feed);
            navView.setSelectedItemId(R.id.navigation_feed);
        }
    }
}
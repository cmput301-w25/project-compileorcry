package ca.ualberta.compileorcry;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import ca.ualberta.compileorcry.domain.models.User;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private NavController navController;
    private BottomNavigationView navView;

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

            // Handle login navigation if needed
            if (User.getActiveUser() == null) {
                navController.navigate(R.id.navigation_login);
                navView.setVisibility(View.GONE);
            }else {
                // If user is already logged in, navigate to feed
                navController.navigate(R.id.navigation_feed);
                // Make sure the correct menu item is selected
                navView.setSelectedItemId(R.id.navigation_feed);
                navView.setVisibility(View.VISIBLE);
            }
        }
    }

    // Method to show navigation after login
    public void showNavigation() {
        if (navView != null) {
            navView.setVisibility(View.VISIBLE);
            navController.navigate(R.id.navigation_feed);
            navView.setSelectedItemId(R.id.navigation_feed);
        }
    }
}
package ca.ualberta.compileorcry;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private NavController navController;
    private BottomNavigationView navView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        navView = findViewById(R.id.nav_view);
        navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);

        // Set up default navigation behavior for other items
        NavigationUI.setupWithNavController(navView, navController);

        navController.navigate(R.id.navigation_login);
        findViewById(R.id.nav_view).setVisibility(View.GONE);
        //TODO: Add check to see if logged in and redirect to profile
    }
}
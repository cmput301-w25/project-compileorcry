package ca.ualberta.compileorcry.features.mood.ui;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import ca.ualberta.compileorcry.R; // Adjust the import as needed
import ca.ualberta.compileorcry.features.mood.data.MoodRepository;
import ca.ualberta.compileorcry.features.mood.model.MoodEvent;

public class MoodHistoryActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private MoodAdapter adapter;
    private FloatingActionButton fabAddMood;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mood_history);  // Create this layout in res/layout

        recyclerView = findViewById(R.id.recycler_view_mood_history);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        fabAddMood = findViewById(R.id.fab_add_mood);
        fabAddMood.setOnClickListener(v -> {
            // Navigate to the AddMoodActivity
            Intent intent = new Intent(MoodHistoryActivity.this, AddMoodActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh mood history on resume (for example, after adding a new event)
        List<MoodEvent> moodEvents = MoodRepository.getInstance().getMoodHistory();
        adapter = new MoodAdapter(moodEvents);
        recyclerView.setAdapter(adapter);
    }
}
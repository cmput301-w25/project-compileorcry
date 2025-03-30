package ca.ualberta.compileorcry.ui.feed;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;

import java.util.List;

import ca.ualberta.compileorcry.R;
import ca.ualberta.compileorcry.domain.models.User;
import ca.ualberta.compileorcry.features.mood.model.Comment;
import ca.ualberta.compileorcry.features.mood.model.MoodEvent;

/**
 * Fragment responsible for displaying and managing comments related to a mood event.
 * Has buttons for returning to the mood (exit the fragment) and a textbox and post button for adding comments.
 * Uses CommentAdapter to display comments in a RecyclerView.
 * Uses CommentViewModel (LiveData) to handle data and UI updates.
 */
public class CommentFragment extends Fragment {

    private RecyclerView recyclerViewComments;
    private EditText editTextComment;
    private Button buttonAddComment;
    private Button buttonBack;
    private CommentAdapter commentAdapter;
    private String moodEventId;
    private MoodEvent moodEvent;
    private User commenter;
    private String username;
    private CommentViewModel commentViewModel;

    /**
     * Constructor for CommentFragment.
     * Required empty
     */
    public CommentFragment() {
    }

    /**
     * Called when the fragment is first created.
     * Get moodID passed in from moodInfoDialogFragment and active user viewing comment fragment
     * */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // pass in the mood id of mood clicked on from feed, needed for adding comments
        if (getArguments() != null) {
            moodEventId = getArguments().getString("moodEventId");
            Log.d("CommentFragment", "Received moodEventId in onCreate: " + moodEventId);
        }
        // viewmodel needs factory method to be initialized
        commentViewModel = new ViewModelProvider(this, new CommentViewModelFactory(moodEventId))
                .get(CommentViewModel.class);
        // get currently logged in user's username for posting comments and fetching moods
        username = commenter.getActiveUser().getUsername();
    }

    /**
     * Inflates the fragment's view and sets up UI elements.
     * Initializes RecyclerView, EditText, and buttons. Sets up CommentAdapter and RecyclerView.
     * Sets up observers for commentsLiveData and buttons for posting comments/exiting comment fragment
     * */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_comments, container, false);

        // Initialize views
        recyclerViewComments = view.findViewById(R.id.recyclerViewComments);
        editTextComment = view.findViewById(R.id.editTextComment);
        buttonAddComment = view.findViewById(R.id.buttonAddComment);
        buttonBack = view.findViewById(R.id.buttonBack);

        // Set up RecyclerView (commentAdapter)
        recyclerViewComments.setLayoutManager(new LinearLayoutManager(getContext()));
        commentAdapter = new CommentAdapter();
        recyclerViewComments.setAdapter(commentAdapter);

        // Observe commentsLiveData
        commentViewModel.getCommentsLiveData().observe(getViewLifecycleOwner(),
                new Observer<List<Comment>>() {
                    @Override
                    public void onChanged(List<Comment> comments) {
                        if (comments != null) {
                            if(commentAdapter.getCurrentList().isEmpty()){
                                // tell commentadapter (recyclerview) to update with diffutil
                                commentAdapter.submitList(comments);
                            }
                            commentAdapter.notifyDataSetChanged();
                        }
                    }
                });

        // Button listeners
        buttonAddComment.setOnClickListener(v -> addComment());

        buttonBack.setOnClickListener(v -> {
            Log.d("CommentFragment", "Back button clicked");
            goBack();
        });
        return view;
    }

    /**
     * Called when the fragment's view is created
     * Loads comments from mood passed in
     * */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d("CommentFragment", "onViewCreated called");
        // username of commenter passed in, correct username and mood handled by MoodEvent.getComments()
        commentViewModel.loadComments(username);
    }

    /**
     * Navigates back to the previous fragment.
     * Uses NavController to handle fragment navigation.
     */
    private void goBack() {
        NavController navController = NavHostFragment.findNavController(this);
        navController.navigateUp();
    }

    /**
     * Button logic for posting a comment. Creates a new comment object,
     * calls the viewmodel to handle the data, and clears text field after posting.
     * Reloads feed immediately to show new comment in fragment
     */
    private void addComment() {
        String text = editTextComment.getText().toString().trim();
        if (text.isEmpty() || moodEventId == null) return;

        Comment newComment = new Comment(commentViewModel.getMoodEvent(), username, "", Timestamp.now(), text);

        commentViewModel.addComment(newComment); // Use ViewModel to handle it

        editTextComment.setText(""); // Clear input field
        commentViewModel.reloadComments(username);
    }


}
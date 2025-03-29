package ca.ualberta.compileorcry.ui.feed;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

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
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

import ca.ualberta.compileorcry.R;
import ca.ualberta.compileorcry.domain.models.User;
import ca.ualberta.compileorcry.features.mood.model.Comment;
import ca.ualberta.compileorcry.features.mood.model.MoodEvent;
import ca.ualberta.compileorcry.ui.moodEvent.CommentAdapter;
import ca.ualberta.compileorcry.ui.feed.CommentViewModel;

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

    public CommentFragment() {
        // Required empty public constructor
    }

    public static CommentFragment newInstance(MoodEvent moodEvent) {
        CommentFragment fragment = new CommentFragment();
        Bundle args = new Bundle();
        args.putSerializable("moodEvent", moodEvent);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            moodEventId = getArguments().getString("moodEventId");
            Log.d("CommentFragment", "Received moodEventId in onCreate: " + moodEventId);
        }

        //commentViewModel = new CommentViewModel(moodEvent);
        commentViewModel = new ViewModelProvider(this, new CommentViewModelFactory(moodEventId))
                .get(CommentViewModel.class);
        username = commenter.getActiveUser().getUsername();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_comments, container, false);

        recyclerViewComments = view.findViewById(R.id.recyclerViewComments);
        editTextComment = view.findViewById(R.id.editTextComment);
        buttonAddComment = view.findViewById(R.id.buttonAddComment);
        buttonBack = view.findViewById(R.id.buttonBack);

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
                                commentAdapter.submitList(comments);
                            }
                            commentAdapter.notifyDataSetChanged();
                        }
                    }
                });

        // Load comments
        // commentViewModel.loadComments(username);

        buttonAddComment.setOnClickListener(v -> addComment());

        // TODO: Fix navigation
        buttonBack.setOnClickListener(v -> {
            Log.d("CommentFragment", "Back button clicked");
            goBack();
        });
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d("CommentFragment", "onViewCreated called");

        if (getArguments() != null) {
            // moodEventId = getArguments().getParcelable("moodEvent");
            //Log.d("CommentFragment", "Received moodEventId: " + moodEventId);
        }else {
            Log.e("CommentFragment", "getArguments() is NULL!");
        }
        // need to pass username of mood poster to fetch this mood's comments
          // moodEvent = new MoodEvent(moodEventId);
//        MyParcelableObject obj = getArguments().getParcelable("key");
//        moodEvent = getArguments().getParcelable("moodEvent");
        commentViewModel.loadComments(username);
    }

    private void goBack() {
        NavController navController = NavHostFragment.findNavController(this);
        navController.navigateUp();
        //navController.navigate(R.id.action_navigation_feed_to_commentFragment, bundle);
    }
    private void addComment() {
        String text = editTextComment.getText().toString().trim();
        if (text.isEmpty() || moodEventId == null) return;

        // need to pass in username of commenter to make new comment
        Comment newComment = new Comment(commentViewModel.getMoodEvent(), username, "", Timestamp.now(), text);

        commentViewModel.addComment(newComment); // Use ViewModel to handle it

        editTextComment.setText(""); // Clear input field
        commentViewModel.reloadComments(username);
    }


}
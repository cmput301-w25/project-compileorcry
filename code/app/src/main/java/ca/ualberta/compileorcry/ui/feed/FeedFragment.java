package ca.ualberta.compileorcry.ui.feed;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import ca.ualberta.compileorcry.databinding.FragmentFeedBinding;
import ca.ualberta.compileorcry.domain.models.User;
import ca.ualberta.compileorcry.features.mood.data.MoodList;
import ca.ualberta.compileorcry.features.mood.data.QueryType;

public class FeedFragment extends Fragment {
    // private FragmentFeedBinding binding;
    private FeedViewModel feedViewModel;
    private MoodListAdapter adapter;
    private RecyclerView recyclerView;
    private Spinner filterSpinner;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_feed, container, false);
        Spinner filterSpinner = view.findViewById(R.id.filter_spinner);
        User user = User.getActiveUser();
        recyclerView = view.findViewById(R.id.feed_recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext())); // get vs require context?
//        adapter = new MoodListAdapter(new MoodList(user, ));
//        recyclerView.setAdapter(adapter);

        FeedViewModel feedViewModel = new ViewModelProvider(this).get(FeedViewModel.class);
        loadFeed();

        return view;
    }

    private void loadFeed() {
        if (User.getActiveUser() != null) {
            MoodList.createMoodList(User.getActiveUser(), QueryType.FOLLOWING, new MoodList.MoodListListener() {
                @Override
                public void returnMoodList(MoodList moodList) {
                    if (feedViewModel != null) {
                        feedViewModel.setMoodEvents(moodList.getMoodEvents());
                    }
                }

                @Override
                public void updatedMoodList() {
                    // Handled automatically
                }
            }, null);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // binding = null;
    }
}
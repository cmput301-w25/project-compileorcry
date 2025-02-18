package ca.ualberta.compileorcry.ui.add;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import ca.ualberta.compileorcry.databinding.FragmentNewBinding;


public class NewFragment extends Fragment {

    private FragmentNewBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        NewViewModel newViewModel =
                new ViewModelProvider(this).get(NewViewModel.class);

        binding = FragmentNewBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textNew;
        newViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
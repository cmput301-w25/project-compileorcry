package ca.ualberta.compileorcry.ui.map;

import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapColorScheme;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

import ca.ualberta.compileorcry.R;
import ca.ualberta.compileorcry.features.mood.model.MoodEvent;

public class MapFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap googleMap;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
        FloatingActionButton fabExit = view.findViewById(R.id.fab_exit_map);
        fabExit.setOnClickListener(v -> requireActivity().getSupportFragmentManager().popBackStack());
    }

    @Override
    public void onMapReady(GoogleMap gMap) {
        googleMap = gMap;
        googleMap.setMapColorScheme(MapColorScheme.DARK);

        // Apply custom style to hide POIs
        try {
            boolean success = googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(requireContext(), R.raw.map_style)
            );
            if (!success) {
                Log.e("MapStyle", "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e("MapStyle", "Can't find style. Error: ", e);
        }

        // Get passed data
        Bundle bundle = getArguments();
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        boolean hasMoodEvents = false;

        if (bundle != null && bundle.containsKey("moodEvents")) {
            ArrayList<MoodEvent> moodEvents = (ArrayList<MoodEvent>) bundle.getSerializable("moodEvents");

            if (moodEvents != null) {
                for (MoodEvent mood : moodEvents) {
                    // Get mood location
                    LatLng moodLocation = mood.getDecodedLocation();
                    // Get mood color
                    int pinColor = ContextCompat.getColor(requireContext(), mood.getEmotionalState().getColorResId());
                    // Check for username (history moods will have null username and will not be displayed)
                    String username = mood.getUsername() != null ? "@" + mood.getUsername() : null;
                    // Add marker
                    googleMap.addMarker(new MarkerOptions()
                            .position(moodLocation)
                            .title(username)
                            .icon(BitmapDescriptorFactory.defaultMarker(getHueFromColor(pinColor)))
                    );
                    // Include the mood location in the bounds builder
                    builder.include(moodLocation);
                }
                hasMoodEvents = true;
            }
        }

        if (!hasMoodEvents) {
            LatLng defaultLocation = new LatLng(53.5461, -113.4938);
            googleMap.addMarker(new MarkerOptions().position(defaultLocation).title("Edmonton"));
            builder.include(defaultLocation);
        }

        // Calculate the bounds and frame camera
        LatLngBounds bounds = builder.build();
        int padding = 250;
        googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding));
    }

    private float getHueFromColor(int color) {
        // Convert to hsv
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);

        // Return hue
        return hsv[0];
    }
}

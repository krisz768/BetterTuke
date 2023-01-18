package hu.krisz768.bettertuke;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link GPSLoadFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class GPSLoadFragment extends Fragment {

    public GPSLoadFragment() {
        // Required empty public constructor
    }

    public static GPSLoadFragment newInstance() {

        return new GPSLoadFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_g_p_s_load, container, false);
    }
}
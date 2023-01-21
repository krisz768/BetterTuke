package hu.krisz768.bettertuke;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class GPSLoadFragment extends Fragment {

    public GPSLoadFragment() {

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
        return inflater.inflate(R.layout.fragment_g_p_s_load, container, false);
    }
}
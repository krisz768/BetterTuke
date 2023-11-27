package hu.krisz768.bettertuke.UpdateAndOnBoardScreen;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import hu.krisz768.bettertuke.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AdDisableSetting#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AdDisableSetting extends Fragment {


    public AdDisableSetting() {
        // Required empty public constructor
    }

    public static AdDisableSetting newInstance(String param1, String param2) {
        AdDisableSetting fragment = new AdDisableSetting();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_ad_disable_setting, container, false);
    }
}
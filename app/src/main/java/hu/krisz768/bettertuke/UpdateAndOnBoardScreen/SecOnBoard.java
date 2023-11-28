package hu.krisz768.bettertuke.UpdateAndOnBoardScreen;

import android.content.res.Configuration;
import android.os.Bundle;

import androidx.constraintlayout.helper.widget.Flow;
import androidx.core.content.OnConfigurationChangedProvider;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.internal.FlowLayout;

import hu.krisz768.bettertuke.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SecOnBoard#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SecOnBoard extends Fragment {

    View view;

    public SecOnBoard() {
        // Required empty public constructor
    }

    public static SecOnBoard newInstance(String param1, String param2) {
        SecOnBoard fragment = new SecOnBoard();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_sec_on_board, container, false);
        return view;
    }

    @Override
    public void onConfigurationChanged (Configuration config) {
        super.onConfigurationChanged(config);
        ((Flow)view.findViewById(R.id.flow2)).setWrapMode(Flow.WRAP_CHAIN);
    }
}
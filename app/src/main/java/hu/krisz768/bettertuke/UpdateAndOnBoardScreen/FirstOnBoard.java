package hu.krisz768.bettertuke.UpdateAndOnBoardScreen;

import android.content.res.Configuration;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.constraintlayout.helper.widget.Flow;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import hu.krisz768.bettertuke.R;

public class FirstOnBoard extends Fragment {
    View view;

    public FirstOnBoard() {

    }

    public static FirstOnBoard newInstance(String param1, String param2) {
        FirstOnBoard fragment = new FirstOnBoard();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_first_on_board, container, false);
        return view;
    }

    @Override
    public void onConfigurationChanged (@NonNull Configuration config) {
        super.onConfigurationChanged(config);
        ((Flow)view.findViewById(R.id.flow)).setWrapMode(Flow.WRAP_CHAIN);
    }
}
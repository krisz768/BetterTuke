package hu.krisz768.bettertuke.UpdateAndOnBoardScreen;

import android.content.res.Configuration;
import android.os.Bundle;

import androidx.constraintlayout.helper.widget.Flow;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import hu.krisz768.bettertuke.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FinishOnBoard#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FinishOnBoard extends Fragment {
    View view;

    public FinishOnBoard() {
        // Required empty public constructor
    }
    public static FinishOnBoard newInstance(String param1, String param2) {
        FinishOnBoard fragment = new FinishOnBoard();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_finish_on_board, container, false);
        return  view;
    }

    @Override
    public void onConfigurationChanged (Configuration config) {
        super.onConfigurationChanged(config);
        ((Flow)view.findViewById(R.id.flow3)).setWrapMode(Flow.WRAP_CHAIN);
    }
}
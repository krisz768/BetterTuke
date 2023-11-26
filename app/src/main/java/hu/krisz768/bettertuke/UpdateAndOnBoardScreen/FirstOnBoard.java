package hu.krisz768.bettertuke.UpdateAndOnBoardScreen;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import hu.krisz768.bettertuke.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FirstOnBoard#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FirstOnBoard extends Fragment {

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
        return inflater.inflate(R.layout.fragment_first_on_board, container, false);
    }
}
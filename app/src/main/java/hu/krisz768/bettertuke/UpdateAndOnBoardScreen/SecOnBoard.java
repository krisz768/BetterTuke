package hu.krisz768.bettertuke.UpdateAndOnBoardScreen;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import hu.krisz768.bettertuke.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SecOnBoard#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SecOnBoard extends Fragment {

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
        return inflater.inflate(R.layout.fragment_sec_on_board, container, false);
    }
}
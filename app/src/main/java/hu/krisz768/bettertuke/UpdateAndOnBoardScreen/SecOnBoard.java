package hu.krisz768.bettertuke.UpdateAndOnBoardScreen;

import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.constraintlayout.helper.widget.Flow;
import androidx.fragment.app.Fragment;

import hu.krisz768.bettertuke.R;

public class SecOnBoard extends Fragment {
    View view;

    public SecOnBoard() {

    }

    public static SecOnBoard newInstance() {
        return new SecOnBoard();
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
    public void onConfigurationChanged (@NonNull Configuration config) {
        super.onConfigurationChanged(config);
        ((Flow)view.findViewById(R.id.flow2)).setWrapMode(Flow.WRAP_CHAIN);
    }
}
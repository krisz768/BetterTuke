package hu.krisz768.bettertuke;

import android.graphics.Color;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link InfoFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class InfoFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String TEXT = "Text";
    private static final String COLOR = "Color";

    // TODO: Rename and change types of parameters
    private String mText;
    private int mColor;

    public InfoFragment() {
        // Required empty public constructor
    }

    public static InfoFragment newInstance(String Text, int Color) {
        InfoFragment fragment = new InfoFragment();
        Bundle args = new Bundle();
        args.putString(TEXT, Text);
        args.putInt(COLOR, Color);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mText = getArguments().getString(TEXT);
            mColor = getArguments().getInt(COLOR);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_info, container, false);

        TextView InfoText = view.findViewById(R.id.InfoText);
        InfoText.setText(mText);
        if (mColor != -1) {
            InfoText.setTextColor(mColor);
        }


        return view;
    }
}
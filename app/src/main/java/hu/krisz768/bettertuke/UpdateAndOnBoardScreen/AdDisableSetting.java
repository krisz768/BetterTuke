package hu.krisz768.bettertuke.UpdateAndOnBoardScreen;

import android.content.DialogInterface;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import hu.krisz768.bettertuke.R;
import hu.krisz768.bettertuke.UserDatabase.UserDatabase;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AdDisableSetting#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AdDisableSetting extends Fragment {
    View view;

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

        view = inflater.inflate(R.layout.fragment_ad_disable_setting, container, false);

        UserDatabase userDatabase = new UserDatabase(getContext());
        String AdEnabled = userDatabase.GetPreference("AdEnabled");

        if (AdEnabled == null || (AdEnabled != null && AdEnabled.equals("true"))) {
            ((Switch)view.findViewById(R.id.AdSwitch)).setChecked(true);
        }else {
            ((Switch)view.findViewById(R.id.AdSwitch)).setChecked(false);
        }

        view.findViewById(R.id.SaveButton).setOnClickListener(v -> ConfirmWindow());

        return view;
    }

    private void ConfirmWindow() {
        MaterialAlertDialogBuilder ConfirmAlert  = new MaterialAlertDialogBuilder(getContext());
        ConfirmAlert.setMessage(R.string.SaveWarningText);
        ConfirmAlert.setTitle(R.string.SaveWarningTitle);
        ConfirmAlert.setPositiveButton(R.string.Ok, (dialog, which) -> SaveDataAndExit());
        ConfirmAlert.setCancelable(false);
        ConfirmAlert.create().show();
    }

    private void SaveDataAndExit() {
        UserDatabase userDatabase = new UserDatabase(getContext());
        userDatabase.SetPreference("AdEnabled", Boolean.toString(((Switch)view.findViewById(R.id.AdSwitch)).isChecked()));

        getActivity().finish();
    }
}
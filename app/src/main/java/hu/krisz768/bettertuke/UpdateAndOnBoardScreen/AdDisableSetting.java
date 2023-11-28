package hu.krisz768.bettertuke.UpdateAndOnBoardScreen;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import hu.krisz768.bettertuke.R;
import hu.krisz768.bettertuke.UserDatabase.UserDatabase;

public class AdDisableSetting extends Fragment {
    View view;

    public AdDisableSetting() {

    }

    public static AdDisableSetting newInstance() {
        return new AdDisableSetting();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_ad_disable_setting, container, false);

        Context context = getContext();

        if (context != null) {
            UserDatabase userDatabase = new UserDatabase(context);
            String AdEnabled = userDatabase.GetPreference("AdEnabled");

            ((Switch)view.findViewById(R.id.AdSwitch)).setChecked(AdEnabled == null || AdEnabled.equals("true"));
        }

        view.findViewById(R.id.SaveButton).setOnClickListener(v -> ConfirmWindow());

        return view;
    }

    private void ConfirmWindow() {
        Context context = getContext();

        if (context != null) {
            MaterialAlertDialogBuilder ConfirmAlert = new MaterialAlertDialogBuilder(context);
            ConfirmAlert.setMessage(R.string.SaveWarningText);
            ConfirmAlert.setTitle(R.string.SaveWarningTitle);
            ConfirmAlert.setPositiveButton(R.string.Ok, (dialog, which) -> SaveDataAndExit());
            ConfirmAlert.setCancelable(false);
            ConfirmAlert.create().show();
        }
    }

    private void SaveDataAndExit() {
        Activity activity = getActivity();

        if (activity != null) {
            UserDatabase userDatabase = new UserDatabase(activity);
            userDatabase.SetPreference("AdEnabled", Boolean.toString(((Switch)view.findViewById(R.id.AdSwitch)).isChecked()));

            activity.finish();
        }
    }
}
package hu.krisz768.bettertuke.UpdateAndOnBoardScreen;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Date;

import hu.krisz768.bettertuke.MainActivity;
import hu.krisz768.bettertuke.R;
import hu.krisz768.bettertuke.UserDatabase.UserDatabase;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link OnBoardMainFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class OnBoardMainFragment extends Fragment {

    private static final int NUM_PAGES = 4;
    private ViewPager2 viewPager;

    private View view;
    private FragmentStateAdapter pagerAdapter;

    private int CurrentPage = 0;

    private FloatingActionButton NextButton;
    private FloatingActionButton PrevButton;
    private TextView PageText;
    private ProgressBar PageProgress;

    private DatabaseUpdate databaseUpdate;

    private boolean IsDatabaseDownloaded = false;

    public OnBoardMainFragment() {

    }

    public static OnBoardMainFragment newInstance() {
        OnBoardMainFragment fragment = new OnBoardMainFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_on_board_main, container, false);

        PageText = view.findViewById(R.id.PageText);
        PageProgress = view.findViewById(R.id.PageBar);

        viewPager = view.findViewById(R.id.OnBoardViewPager);
        pagerAdapter = new ScreenSlidePagerAdapter(getActivity());
        viewPager.setAdapter(pagerAdapter);

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels);
            }

            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                CurrentPage = position;
                OnPageChanged();
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                super.onPageScrollStateChanged(state);
            }
        });

        NextButton = view.findViewById(R.id.next_button);
        NextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (CurrentPage == NUM_PAGES-1) {
                    StartMain();
                } else {
                    ScrollTo(CurrentPage+1);
                }
            }
        });

        PrevButton = view.findViewById(R.id.prev_button);
        PrevButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ScrollTo(CurrentPage-1);
            }
        });

        OnPageChanged();

        return view;
    }

    private void OnPageChanged() {
        if (CurrentPage == 0) {
            PrevButton.setVisibility(View.GONE);
            NextButton.setVisibility(View.VISIBLE);
        } else if (CurrentPage == NUM_PAGES-1) {
            PrevButton.setVisibility(View.VISIBLE);
            NextButton.setVisibility(View.VISIBLE);
        } else {
            PrevButton.setVisibility(View.VISIBLE);
            NextButton.setVisibility(View.VISIBLE);
        }

        PageText.setText((CurrentPage+1) + "/" + NUM_PAGES);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            PageProgress.setProgress((100/(NUM_PAGES-1))*(CurrentPage), true);
        } else {
            PageProgress.setProgress((100/(NUM_PAGES-1))*(CurrentPage));
        }

        if (CurrentPage == 2 && !IsDatabaseDownloaded) {
            LockButtons();
            databaseUpdate.StartUpdate();
        }
    }

    private void ScrollTo(int Position) {
        viewPager.setCurrentItem(Position, true);
        CurrentPage = Position;
        OnPageChanged();
    }

    private class ScreenSlidePagerAdapter extends FragmentStateAdapter {
        public ScreenSlidePagerAdapter(FragmentActivity fa) {
            super(fa);
        }

        @Override
        public Fragment createFragment(int position) {
            switch (position) {
                case 1:
                    return new SecOnBoard();
                case 2:
                    databaseUpdate = DatabaseUpdate.newInstance(true, true, false,() -> {
                        UnlockButtons();
                        IsDatabaseDownloaded = true;
                        ScrollTo(CurrentPage + 1);
                    }, () -> OnUpdateFail());
                    return databaseUpdate;
                case 3:
                    return new FinishOnBoard();
                default:
                    return new FirstOnBoard();
            }
        }

        @Override
        public int getItemCount() {
            return NUM_PAGES;
        }
    }

    private void OnUpdateFail() {
        MaterialAlertDialogBuilder dlgAlert  = new MaterialAlertDialogBuilder(getContext());
        dlgAlert.setMessage(R.string.FirstLaunchInternetError);
        dlgAlert.setTitle(R.string.Error);
        dlgAlert.setPositiveButton(R.string.Ok, (dialogInterface, i) -> ExitApp());
        dlgAlert.setCancelable(false);
        dlgAlert.create().show();
    }

    private void ExitApp() {
        getActivity().finish();
    }

    private void LockButtons() {
        NextButton.setEnabled(false);
        PrevButton.setEnabled(false);
        viewPager.setUserInputEnabled(false);
    }

    private void UnlockButtons() {
        NextButton.setEnabled(true);
        PrevButton.setEnabled(true);
        viewPager.setUserInputEnabled(true);
    }

    private void StartMain() {
        UserDatabase userDatabase = new UserDatabase(getContext());
        userDatabase.SetPreference("FirstStartComplete", "true");

        Intent mainIntent = new Intent(getContext(), MainActivity.class);
        startActivity(mainIntent);
    }
}
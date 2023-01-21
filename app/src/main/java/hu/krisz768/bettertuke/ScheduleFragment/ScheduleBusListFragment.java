package hu.krisz768.bettertuke.ScheduleFragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import hu.krisz768.bettertuke.Database.BusNum;
import hu.krisz768.bettertuke.Database.DatabaseManager;
import hu.krisz768.bettertuke.R;
import hu.krisz768.bettertuke.ScheduleActivity;
import hu.krisz768.bettertuke.UserDatabase.Favorite;
import hu.krisz768.bettertuke.UserDatabase.UserDatabase;

public class ScheduleBusListFragment extends Fragment {
    private static final String STOPID= "StopId";
    private static final String SCROLLPOSITION= "ScrollPosition";

    private int mStopId;
    private Parcelable mScrollPosition;

    private RecyclerView.LayoutManager mLayoutManager;

    public ScheduleBusListFragment() {

    }

    public static ScheduleBusListFragment newInstance(int StopId, Parcelable ScrollPosition) {
        ScheduleBusListFragment fragment = new ScheduleBusListFragment();
        Bundle args = new Bundle();
        args.putInt(STOPID, StopId);
        args.putParcelable(SCROLLPOSITION, ScrollPosition);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mStopId = getArguments().getInt(STOPID);
            mScrollPosition = getArguments().getParcelable(SCROLLPOSITION);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_schedule_bus_list, container, false);

        if(getContext() == null) {
            return view;
        }

        DatabaseManager Dm = new DatabaseManager(getContext());
        BusNum[] busNums;
        if (mStopId == -1) {
            busNums = Dm.GetActiveBusLines();
        } else{
            busNums = Dm.GetActiveBusLinesFromStop(mStopId);
            String StopName = Dm.GetStopName(mStopId);
            String StopNum = Dm.GetStopNum(mStopId);

            ((TextView)view.findViewById(R.id.ScheduleTargetText)).setText(getString(R.string.BusStopNameWithNum, StopName.trim(), StopNum));
        }

        UserDatabase userDatabase = new UserDatabase(getContext());

        Favorite[] favorites = userDatabase.GetFavorites(UserDatabase.FavoriteType.Line);

        List<BusNum> favoriteBusNumList = new ArrayList<>();

        for (Favorite favorite : favorites) {
            for (BusNum busNum : busNums) {
                if (favorite.getData().equals(busNum.getLineName())) {
                    favoriteBusNumList.add(busNum);
                    break;
                }
            }
        }

        BusNum[] favoriteBusNum = new BusNum[favoriteBusNumList.size()];
        favoriteBusNumList.toArray(favoriteBusNum);

        ScheduleBusListAdapter Sbla = new ScheduleBusListAdapter(busNums, favoriteBusNum, getContext(), this);

        RecyclerView BusLineRecv = view.findViewById(R.id.ScheduleBusLineRec);
        mLayoutManager = new LinearLayoutManager(getActivity());
        BusLineRecv.setLayoutManager(mLayoutManager);
        BusLineRecv.setAdapter(Sbla);
        mLayoutManager.onRestoreInstanceState(mScrollPosition);

        ItemTouchHelper.Callback callback = new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                if (target.getAdapterPosition() < favoriteBusNum.length+1 && target.getAdapterPosition() != 0) {
                    Sbla.notifyItemMoved(viewHolder.getAdapterPosition(), target.getAdapterPosition());

                    int FirstId = userDatabase.GetId(((ScheduleBusListAdapter.ViewHolderLine)viewHolder).GetName(), UserDatabase.FavoriteType.Line);
                    int SecId = userDatabase.GetId(((ScheduleBusListAdapter.ViewHolderLine)target).GetName(), UserDatabase.FavoriteType.Line);

                    userDatabase.SwapId(FirstId, SecId);
                    return true;
                } else {
                    return false;
                }
            }

            @Override
            public int getDragDirs(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                return viewHolder.getAdapterPosition() < favoriteBusNum.length+1 && viewHolder.getAdapterPosition() != 0 ? super.getDragDirs(recyclerView, viewHolder) : 0;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {

            }

            @Override
            public boolean isLongPressDragEnabled() {

                return favoriteBusNum.length > 0;
            }
        };

        ItemTouchHelper mItemTouchHelper = new ItemTouchHelper(callback);
        mItemTouchHelper.attachToRecyclerView(BusLineRecv);

        return view;
    }

    public void OnLineClick(String Line) {
        mLayoutManager.onSaveInstanceState();
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> ((ScheduleActivity)getActivity()).selectLine(Line, mLayoutManager.onSaveInstanceState()));
        }
    }
}
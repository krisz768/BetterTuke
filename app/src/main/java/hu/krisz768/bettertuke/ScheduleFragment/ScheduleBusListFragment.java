package hu.krisz768.bettertuke.ScheduleFragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import hu.krisz768.bettertuke.Database.BusLine;
import hu.krisz768.bettertuke.Database.DatabaseManager;
import hu.krisz768.bettertuke.R;
import hu.krisz768.bettertuke.ScheduleActivity;
import hu.krisz768.bettertuke.UserDatabase.Favorite;
import hu.krisz768.bettertuke.UserDatabase.UserDatabase;

public class ScheduleBusListFragment extends Fragment {

    private static final String STOPID= "StopId";

    private int mStopId;

    public ScheduleBusListFragment() {
        // Required empty public constructor
    }

    public static ScheduleBusListFragment newInstance(int StopId) {
        ScheduleBusListFragment fragment = new ScheduleBusListFragment();
        Bundle args = new Bundle();
        args.putInt(STOPID, StopId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mStopId = getArguments().getInt(STOPID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_schedule_bus_list, container, false);

        DatabaseManager Dm = new DatabaseManager(getContext());
        BusLine[] BusLines;
        if (mStopId == -1) {
            BusLines = Dm.GetActiveBusLines();
        } else{
            BusLines = Dm.GetActiveBusLinesFromStop(mStopId);
            String StopName = Dm.GetStopName(mStopId);
            String StopNum = Dm.GetStopNum(mStopId);

            ((TextView)view.findViewById(R.id.ScheduleTargetText)).setText(StopName.trim() + " (" + StopNum + ")");
        }

        UserDatabase userDatabase = new UserDatabase(getContext());

        Favorite[] favorites = userDatabase.GetFavorites(UserDatabase.FavoriteType.Line);

        List<BusLine> FavoriteBusLineList = new ArrayList<>();

        for (int i = 0; i < favorites.length; i++) {
            for (int j = 0; j < BusLines.length; j++) {
                if (favorites[i].getData().equals(BusLines[j].getLineName())) {
                    FavoriteBusLineList.add(BusLines[j]);
                    break;
                }
            }
        }

        BusLine[] FavoriteBusLine = new BusLine[FavoriteBusLineList.size()];
        FavoriteBusLineList.toArray(FavoriteBusLine);

        ScheduleBusListAdapter Sbla = new ScheduleBusListAdapter(BusLines, FavoriteBusLine, getContext(), this);

        RecyclerView BusLineRecv = view.findViewById(R.id.ScheduleBusLineRec);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        BusLineRecv.setLayoutManager(mLayoutManager);
        BusLineRecv.setAdapter(Sbla);

        ItemTouchHelper.Callback callback = new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                if (target.getAdapterPosition() < FavoriteBusLine.length+1 && target.getAdapterPosition() != 0) {
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
                return viewHolder.getAdapterPosition() < FavoriteBusLine.length+1 && viewHolder.getAdapterPosition() != 0 ? super.getDragDirs(recyclerView, viewHolder) : 0;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {

            }

            @Override
            public boolean isLongPressDragEnabled() {

                return FavoriteBusLine.length > 0;
            }
        };

        ItemTouchHelper mItemTouchHelper = new ItemTouchHelper(callback);
        mItemTouchHelper.attachToRecyclerView(BusLineRecv);

        return view;
    }

    public void OnLineClick(String Line) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ((ScheduleActivity)getActivity()).selectLine(Line);
                }
            });
        }
    }
}
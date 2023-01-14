package hu.krisz768.bettertuke.SearchFragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import hu.krisz768.bettertuke.R;
import hu.krisz768.bettertuke.models.SearchResult;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SearchViewFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SearchViewFragment extends Fragment {

    private static final String ALLITEM = "AllItem";

    private SearchResult[] mAllItem;

    private RecyclerView recyclerView;
    private SearchAdapter searchAdapter;


    public SearchViewFragment() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static SearchViewFragment newInstance(SearchResult[] AllItem) {
        SearchViewFragment fragment = new SearchViewFragment();
        Bundle args = new Bundle();
        args.putSerializable(ALLITEM, AllItem);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mAllItem = (SearchResult[]) getArguments().getSerializable(ALLITEM);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_search_view, container, false);

        recyclerView = view.findViewById(R.id.SearchResultRecView);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(mLayoutManager);
        searchAdapter = new SearchAdapter(new SearchResult[0], getContext());
        recyclerView.setAdapter(searchAdapter);

        return view;
    }

    public void OnSearchTextChanged(String text) {
        if (searchAdapter != null) {
            if (text.equals("")) {
                searchAdapter.UpdateResults(new SearchResult[0]);
                searchAdapter.notifyDataSetChanged();
                return;
            }
            List<SearchResult> ResultsList = new ArrayList<>();

            for (int i = 0; i < mAllItem.length; i++) {
                if (mAllItem[i].getSearchText().toLowerCase().contains(text.toLowerCase())){
                    ResultsList.add(mAllItem[i]);
                }
            }

            SearchResult[] Results = new SearchResult[ResultsList.size()];
            ResultsList.toArray(Results);

            searchAdapter.UpdateResults(Results);
            searchAdapter.notifyDataSetChanged();
        }
    }
}
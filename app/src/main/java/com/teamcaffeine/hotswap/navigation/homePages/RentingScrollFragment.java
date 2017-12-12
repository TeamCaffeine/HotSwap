package com.teamcaffeine.hotswap.navigation.homePages;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.github.florent37.hollyviewpager.HollyViewPagerBus;
import com.github.ksoichiro.android.observablescrollview.ObservableScrollView;
import com.teamcaffeine.hotswap.R;
import com.teamcaffeine.hotswap.navigation.homeAdapters.OwnedItemsCardAdapter;
import com.teamcaffeine.hotswap.navigation.homeAdapters.RentingPendingItemsCardAdapter;
import com.teamcaffeine.hotswap.swap.Item;
import com.teamcaffeine.hotswap.swap.ItemTransactions;

public class RentingScrollFragment extends Fragment {

    private String TAG = "OwnedScrollFragment";

    private TextView title;
    private ObservableScrollView scrollView;
    private ListView cardListView;

    private static RentingPendingItemsCardAdapter adapter;

    public static RentingScrollFragment newInstance(String title, RentingPendingItemsCardAdapter listAdapter){
        Bundle args = new Bundle();
        args.putString("title",title);
        RentingScrollFragment fragment = new RentingScrollFragment();
        fragment.setArguments(args);
        adapter = listAdapter;
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView");
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_card_scroll, container, false);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        title = view.findViewById(R.id.pageTitle);
        title.setText(getArguments().getString("title"));

        scrollView = view.findViewById(R.id.obsScrollView);
        HollyViewPagerBus.registerScrollView(getActivity(), scrollView);

        cardListView = view.findViewById(R.id.cardListView);
        cardListView.setAdapter(adapter);

        cardListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // TODO: maybe add some detail view for rented items
            }
        });
    }
}

package com.teamcaffeine.hotswap.navigation.homePages;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.github.florent37.hollyviewpager.HollyViewPagerBus;
import com.github.ksoichiro.android.observablescrollview.ObservableScrollView;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.teamcaffeine.hotswap.R;
import com.teamcaffeine.hotswap.navigation.homeAdapters.OwnedItemsAdapter;
import com.teamcaffeine.hotswap.navigation.homeAdapters.OwnedItemsCardAdapter;
import com.teamcaffeine.hotswap.swap.Item;
import com.teamcaffeine.hotswap.swap.ItemTransactions;

public class OwnedScrollFragment extends Fragment {

    private String TAG = "OwnedScrollFragment";

    private TextView title;
    private ObservableScrollView scrollView;
    private ListView cardListView;

    private static OwnedItemsCardAdapter adapter;

    public static OwnedScrollFragment newInstance(String title, OwnedItemsCardAdapter listAdapter){
        Bundle args = new Bundle();
        args.putString("title",title);
        OwnedScrollFragment fragment = new OwnedScrollFragment();
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

        TextView emptyView = view.findViewById(R.id.emptyView);
        cardListView.setEmptyView(emptyView);
        cardListView.setAdapter(adapter);

        cardListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Item item = (Item) parent.getItemAtPosition(position);
                Intent itemTransactionsIntent = new Intent(getActivity(), ItemTransactions.class);
                itemTransactionsIntent.putExtra("itemID", item.getItemID());
                itemTransactionsIntent.putExtra("itemName", item.getName());
                startActivity(itemTransactionsIntent);
            }
        });
    }
}

package com.teamcaffeine.hotswap.navigation;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.teamcaffeine.hotswap.ListItemActivity;
import com.teamcaffeine.hotswap.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment {

    private Button btnListItem;
    private HomeFragmentListener HFL;
    private int LIST_ITEM_REQUEST_CODE = 999;

    public HomeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        btnListItem = view.findViewById(R.id.btnListItem);

        btnListItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getActivity(), ListItemActivity.class);
                startActivityForResult(i, LIST_ITEM_REQUEST_CODE);
            }
        });
    }

    public interface HomeFragmentListener {
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        HFL = (HomeFragment.HomeFragmentListener) context;  //context is a handle to the main activity, let's bind it to our interface.
    }
}

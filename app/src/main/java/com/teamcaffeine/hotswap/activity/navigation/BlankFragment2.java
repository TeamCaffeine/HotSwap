package com.teamcaffeine.hotswap.activity.navigation;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.teamcaffeine.hotswap.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class BlankFragment2 extends Fragment {


    public BlankFragment2() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_blank_fragment2, container, false);
    }

    BlankFragment2Listener BF2L;

    public interface BlankFragment2Listener {
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        BF2L = (BlankFragment2Listener) context;  //context is a handle to the main activity, let's bind it to our interface.
    }


}

package com.teamcaffeine.hotswap.navigation;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.teamcaffeine.hotswap.R;
import com.teamcaffeine.hotswap.messaging.ConvoAdapter;

public class InboxFragment extends Fragment {
    private ListView conversations;
    private ListAdapter convoAdapter;
    InboxFragmentListener IFL;


    public InboxFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_inbox, container, false);
        conversations = (ListView) view.findViewById(R.id.conversations);
        convoAdapter = new ConvoAdapter(getActivity()); //TODO: Double check if this is correct
        conversations.setAdapter(convoAdapter);

        return view;
    }

    public interface InboxFragmentListener {
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        IFL = (InboxFragment.InboxFragmentListener) context;
    }

}



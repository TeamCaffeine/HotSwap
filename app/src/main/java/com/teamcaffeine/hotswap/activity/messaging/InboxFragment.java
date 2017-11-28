package com.teamcaffeine.hotswap.activity.messaging;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.teamcaffeine.hotswap.R;

import java.util.ArrayList;

public class InboxFragment extends Fragment {
    private ListView conversations;
    private ListAdapter convoAdapter;

    public InboxFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_inbox, container, false);
        conversations = (ListView) view.findViewById(R.id.conversations);
        convoAdapter = new convoAdapter(getActivity()); //TODO: Double check if this is correct
        conversations.setAdapter(convoAdapter);

        return view;
    }

}

class convoAdapter extends BaseAdapter {
    private ArrayList<conversation> conversationList;
    private Context context;

    public convoAdapter(Context aContext) {
        context = aContext;
        conversationList = new ArrayList<conversation>();

        // Pull in information from database
        // Create an array of conversation objects from it

        // Mock information
        conversation test1 = new conversation(R.drawable.jake, "Jake", "I fixed everything.");
        conversation test2 = new conversation(R.drawable.william, "William", "I ate everything.");
        conversationList.add(test1);
        conversationList.add(test2);
    }

    @Override
    public int getCount() {
        return conversationList.size();
    }

    @Override
    public Object getItem(int i) {
        return conversationList.get(i);
    }

    // Dummy function
    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        View row;
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(R.layout.conversation, viewGroup, false);
        } else {
            row = view;
        }

        ImageView convoProfilePic = (ImageView) row.findViewById(R.id.convoProfilePic);
        final TextView convoName = (TextView) row.findViewById(R.id.convoName);
        TextView convoLastMessage = (TextView) row.findViewById(R.id.convoLastMessage);

        convoProfilePic.setImageResource(conversationList.get(i).getConvoProfilePic());
        convoName.setText(conversationList.get(i).getConvoName());
        convoLastMessage.setText(conversationList.get(i).getConvoLastMessage());

        // Where we want to start an intent to start the instant messaging between two particpants
        row.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, "Test " + convoName.getText(), Toast.LENGTH_LONG).show();
            }
        });
        return row;
    }
}

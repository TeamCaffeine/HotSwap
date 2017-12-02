package com.teamcaffeine.hotswap.messaging;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.teamcaffeine.hotswap.R;

import java.util.ArrayList;

/**
 * Created by william on 27/11/2017.
 */

public class ConvoAdapter extends BaseAdapter {
    private ArrayList<Conversation> conversationList;
    private Context context;

    public ConvoAdapter(Context aContext) {
        context = aContext;
        conversationList = new ArrayList<Conversation>();

        // Pull in information from database
        // Create an array of Conversation objects from it

        // Mock information
        Conversation test1 = new Conversation(R.drawable.jake, "Jake", "I fixed everything.");
        Conversation test2 = new Conversation(R.drawable.william, "William", "I ate everything.");
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
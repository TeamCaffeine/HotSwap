package com.teamcaffeine.hotswap.swap;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.teamcaffeine.hotswap.R;
import com.teamcaffeine.hotswap.login.User;

import java.util.ArrayList;

/**
 * Created by Tkixi on 11/28/17.
 */

public class Users extends BaseAdapter {
    private
    ArrayList<User> users;

    Context context;

    public Users(Context aContext) {
        context = aContext;  //saving the context we'll need it again (for intents)
        users = new ArrayList<User>();
    }
    @Override
    public int getCount() {
        return users.size();   //all of the arrays are same length
    }

    @Override
    public Object getItem(int position) {
        return users.get(position);
    }

    public void putUser(User user) {
        if (user != null) {
            this.users.add(user);
        }
    }

    public void nuke() {
        this.users = new ArrayList<User>();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View row;
        if(convertView == null){
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(R.layout.listview_row, parent, false);
        }
        else
        {
            row = convertView;
        }

        TextView userTitle = (TextView) row.findViewById(R.id.itemTitle);
        TextView userSince = (TextView) row.findViewById(R.id.itemDescription);

        userTitle.setText(users.get(position).getName());
        userSince.setText(users.get(position).getMemberSince());
        return row;

    }
}

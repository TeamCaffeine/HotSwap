package com.teamcaffeine.hotswap.swap;

/**
 * Created by Tkixi on 12/11/17.
 */

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

public class TransactionsAdapter extends BaseAdapter {
    private
    ArrayList<Transaction> Transactions;

    Context context;

    public TransactionsAdapter(Context aContext) {
        context = aContext;  //saving the context we'll need it again (for intents)
        Transactions = new ArrayList<Transaction>();
    }
    @Override
    public int getCount() {
        return Transactions.size();   //all of the arrays are same length
    }

    @Override
    public Object getItem(int position) {
        return Transactions.get(position);
    }

    public void putTransaction(Transaction transaction) {
        if (transaction != null) {
            this.Transactions.add(transaction);
        }
    }

    public void nuke() {
        this.Transactions = new ArrayList<Transaction>();
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

        TextView transDist = (TextView) row.findViewById(R.id.itemTitle);
        TextView transDates = (TextView) row.findViewById(R.id.itemDescription);

        transDist.setText("User is " + String.format("%.2f",Transactions.get(position).getDistance()/ 1000.0) + "KM Away");
        transDates.setText(Transactions.get(position).getRequestedDates().get(0).toString() +" to " + Transactions.get(position).getRequestedDates().get(Transactions.get(position).getRequestedDates().size()-1).toString());
        return row;

    }
}
package com.teamcaffeine.hotswap.navigation.homeAdapters;


/**
 * IMPORTANT FILE FOR THIS PR
 */


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.teamcaffeine.hotswap.R;
import com.teamcaffeine.hotswap.login.User;
import com.teamcaffeine.hotswap.swap.ActiveTransactionInfo;
import com.teamcaffeine.hotswap.swap.Item;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class RentingPendingItemsAdapter extends BaseAdapter {

    Context context;
    private HashMap<String, ActiveTransactionInfo> items;
    private List<Map.Entry<String, ActiveTransactionInfo>> itemList;

    public RentingPendingItemsAdapter(Context aContext) {
        context = aContext;
        items = new HashMap<String, ActiveTransactionInfo>();
        itemList = new ArrayList(items.entrySet());
    }

    public void putItems(HashMap items){
        this.items = items;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row;

        if(convertView == null){
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(R.layout.listview_row, parent, false);
        }

        else
        {
            row = convertView;
        }

        TextView itemName = (TextView) row.findViewById(R.id.itemTitle);
        TextView swapDates = (TextView) row.findViewById(R.id.itemDescription);

        itemName.setText(getActiveTransactionInfo(position).getItem().getName());
        swapDates.setText(getActiveTransactionInfo(position).getDate().toString());
        return row;
    }

    public String getTransactionKey(int position){
        Map.Entry<String, ActiveTransactionInfo> info = itemList.get(position);
        return info.getKey();
    }

    public ActiveTransactionInfo getActiveTransactionInfo (int position) {
        Map.Entry<String, ActiveTransactionInfo> info = itemList.get(position);
        return info.getValue();
    }

    public ActiveTransactionInfo getActiveTransactionInfo(String key) {
        return items.get(key);
    }
}

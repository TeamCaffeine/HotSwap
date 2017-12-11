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
import com.teamcaffeine.hotswap.swap.Item;

import java.util.ArrayList;



public class OwnedItemsAdapter extends BaseAdapter {

    private ArrayList<Item> items;
    private Context context;

    public OwnedItemsAdapter(Context aContext) {
        context = aContext;  //saving the context we'll need it again (for intents)
        items = new ArrayList<Item>();
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Item getItem(int position) {
        return items.get(position);
    }

    public void putItem(Item item) {
        if (item != null) {
            this.items.add(item);
        }
    }

    public void clear() {
        this.items = new ArrayList<Item>();
    }

    @Override
    public long getItemId(int position) {
        return position;
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
        TextView itemDescription = (TextView) row.findViewById(R.id.itemDescription);

        itemName.setText(items.get(position).getName());
        itemDescription.setText(items.get(position).getDescription());
        return row;
    }

    public ArrayList<String> getListOfItemNames() {
        ArrayList<String> nameList = new ArrayList<String>();
        for (Item i : items) {
            nameList.add(i.getName());
        }

        return nameList;
    }
}

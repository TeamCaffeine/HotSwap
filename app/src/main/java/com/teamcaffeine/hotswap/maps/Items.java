package com.teamcaffeine.hotswap.maps;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.teamcaffeine.hotswap.R;
import com.teamcaffeine.hotswap.swap.Item;

import java.util.ArrayList;

/**
 * Created by Tkixi on 11/28/17.
 */

public class Items extends BaseAdapter {
    private
    ArrayList<Item> items;

    Context context;

    public Items(Context aContext) {
        context = aContext;  //saving the context we'll need it again (for intents)
        items = new ArrayList<Item>();

    }
    @Override
    public int getCount() {
        return items.size();   //all of the arrays are same length
    }

    @Override
    public Object getItem(int position) {
        return items.get(position);
    }

    public void putItem(Item item) {
        if (item != null) {
            this.items.add(item);
        }
    }

    public void nuke() {
        this.items = new ArrayList<Item>();
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

//        ImageView imgItem = (ImageView) row.findViewById(R.id.imgItem);
        TextView itemTitle = (TextView) row.findViewById(R.id.itemTitle);
        TextView itemDescriptions = (TextView) row.findViewById(R.id.itemDescription);

        itemTitle.setText(items.get(position).getName());
        itemDescriptions.setText(items.get(position).getDescription());
        //      imgItem.setImageResource(itemImages.get(position).intValue());



        return row;

    }
}

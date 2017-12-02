package com.teamcaffeine.hotswap.maps;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.teamcaffeine.hotswap.R;


import java.util.ArrayList;

/**
 * Created by Tkixi on 11/28/17.
 */

public class Items extends BaseAdapter {
    private
    String items[];
    String itemDescription[];
    ArrayList<Integer> itemImages;

    Context context;

    public Items(Context aContext) {
        context = aContext;  //saving the context we'll need it again (for intents)

        items = aContext.getResources().getStringArray(R.array.items);
        itemDescription = aContext.getResources().getStringArray(R.array.item_descriptions);

        itemImages = new ArrayList<Integer>();
    }
    @Override
    public int getCount() {
        return items.length;   //all of the arrays are same length
    }

    @Override
    public Object getItem(int position) {
        return items[position];
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

        itemTitle.setText(items[position]);
        itemDescriptions.setText(itemDescription[position]);
        //      imgItem.setImageResource(itemImages.get(position).intValue());



        return row;

    }
}

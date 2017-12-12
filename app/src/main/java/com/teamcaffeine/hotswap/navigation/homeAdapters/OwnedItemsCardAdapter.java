package com.teamcaffeine.hotswap.navigation.homeAdapters;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.common.base.Strings;
import com.squareup.picasso.Picasso;
import com.teamcaffeine.hotswap.R;
import com.teamcaffeine.hotswap.navigation.NavigationActivity;
import com.teamcaffeine.hotswap.swap.Item;

import java.util.ArrayList;

public class OwnedItemsCardAdapter extends BaseAdapter {

    private ArrayList<Item> items;
    private Context context;

    public OwnedItemsCardAdapter(Context aContext) {
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
            row = inflater.inflate(R.layout.content_card, parent, false);
        }

        else
        {
            row = convertView;
        }

        ImageView circleImage = (ImageView) row.findViewById(R.id.circleImage);
        TextView itemName = (TextView) row.findViewById(R.id.cardItemTitle);
        TextView itemDescription = (TextView) row.findViewById(R.id.cardItemDescription);

        Item item = items.get(position);
        String picture = item.getHeaderPicture();
        if (!Strings.isNullOrEmpty(picture)) {
            Picasso.with(circleImage.getContext()).load(picture).into(circleImage);
        }
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

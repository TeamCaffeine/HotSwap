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
import com.teamcaffeine.hotswap.swap.ActiveTransactionInfo;

import java.util.ArrayList;
import java.util.HashMap;

public class RentingPendingItemsCardAdapter extends BaseAdapter {

    private Context context;
    private HashMap<String, ActiveTransactionInfo> items;
    private ArrayList<ActiveTransactionInfo> itemList;

    public RentingPendingItemsCardAdapter(Context aContext) {
        context = aContext;
        items = new HashMap<>();
        itemList = new ArrayList<ActiveTransactionInfo> (items.values());
    }

    public void putItems(HashMap<String, ActiveTransactionInfo> items) {
        this.items = items;
        this.itemList = new ArrayList<>(items.values());
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

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(R.layout.content_card, parent, false);
        } else {
            row = convertView;
        }

        ImageView circleImage = (ImageView) row.findViewById(R.id.circleImage);
        TextView itemName = (TextView) row.findViewById(R.id.cardItemTitle);
        TextView swapDates = (TextView) row.findViewById(R.id.cardItemDescription);

        ActiveTransactionInfo activeTransactionInfo = itemList.get(position);
        String picture = activeTransactionInfo.getItem().getHeaderPicture();
        if (!Strings.isNullOrEmpty(picture)) {
            Picasso.with(circleImage.getContext()).load(picture).into(circleImage);
        }
        itemName.setText(itemList.get(position).getItem().getName());
        swapDates.setText(itemList.get(position).getDate().toString());
        return row;
    }

    public ActiveTransactionInfo getActiveTransactionInfoAtPosition(int position) {
        return itemList.get(position);
    }
}

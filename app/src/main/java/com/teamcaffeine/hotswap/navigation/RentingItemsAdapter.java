package com.teamcaffeine.hotswap.navigation;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.teamcaffeine.hotswap.swap.Item;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;


public class RentingItemsAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<HashMap> itemList;

    public RentingItemsAdapter(Context aContext) {
        context = aContext;  //saving the context we'll need it again (for intents)
        itemList = new ArrayList<HashMap>();
    }

    @Override
    public int getCount() {
        return itemList.size();
    }

    @Override
    public HashMap getItem(int position) {
        return itemList.get(position);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        return null;
    }
}

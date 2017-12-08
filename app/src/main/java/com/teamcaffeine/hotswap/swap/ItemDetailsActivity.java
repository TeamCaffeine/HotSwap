package com.teamcaffeine.hotswap.swap;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.CalendarView;
import android.widget.ListView;
import android.widget.TextView;

import com.squareup.timessquare.CalendarPickerView;
import com.teamcaffeine.hotswap.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class ItemDetailsActivity extends AppCompatActivity {
    private String TAG = "ItemDetailsActivity";

    private TextView txtItemName;
    private TextView txtItemDescription;
    private TextView txtItemTags;
    private TextView txtItemPrice;
    private TextView txtItemLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_details);

        Bundle extras = getIntent().getExtras();
        Item item = (Item) extras.getParcelable("item");
        String currentCity = (String) extras.get("currentCity");

        txtItemName = (TextView) findViewById(R.id.txtItemName);
        txtItemDescription = (TextView) findViewById(R.id.txtItemDescription);
        txtItemTags = (TextView) findViewById(R.id.txtItemTags);
        txtItemPrice = (TextView) findViewById(R.id.txtItemPrice);
        txtItemLocation = (TextView) findViewById(R.id.txtItemLocation);

        String itemName = item.getName();
        String itemDescription = item.getDescription();
        String itemTags = getTagsAsString(item.getTags());
        String itemPrice = "$" + item.getRentPrice();
        String itemLocation;

        try {
            itemLocation = item.getAddress().split(",")[1];
        } catch (ArrayIndexOutOfBoundsException e) {
            Log.e(TAG, "Out of bounds when getting city from address: " + item.getAddress(), e);
            itemLocation = currentCity;
        }

        txtItemName.setText(itemName);
        txtItemDescription.setText(itemDescription);
        txtItemTags.setText(itemTags);
        txtItemPrice.setText(itemPrice);
        txtItemLocation.setText(itemLocation);
    }

    public String getTagsAsString(List<String> tagsList){
        StringBuilder tagsStringBuilder = new StringBuilder();
        for (int i = 0; i < tagsList.size(); i++) {
            tagsStringBuilder.append(i == 0 ? tagsList.get(i) : ", " + tagsList.get(i));
        }
        return tagsStringBuilder.toString();
    }
}

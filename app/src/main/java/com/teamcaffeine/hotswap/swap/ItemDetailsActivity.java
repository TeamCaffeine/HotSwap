package com.teamcaffeine.hotswap.swap;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.TextView;

import com.teamcaffeine.hotswap.R;

import java.util.Date;
import java.util.List;

public class ItemDetailsActivity extends AppCompatActivity {
    private String TAG = "ItemDetailsActivity";

    private TextView txtItemName;
    private TextView txtItemDescription;
    private TextView txtItemTags;
    private TextView txtItemPrice;
    private TextView txtItemLocation;
    private ListView listviewAvailableDates;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_details);

        Bundle extras = getIntent().getExtras();
        Item item = (Item) extras.getParcelable("item");

        txtItemName = (TextView) findViewById(R.id.txtItemName);
        txtItemDescription = (TextView) findViewById(R.id.txtItemDescription);
        txtItemTags = (TextView) findViewById(R.id.txtItemTags);
        txtItemPrice = (TextView) findViewById(R.id.txtItemPrice);
        txtItemLocation = (TextView) findViewById(R.id.txtItemLocation);

        String itemName = item.getName();
        String itemDescription = item.getDescription();
        String itemTags = getTagsAsString(item.getTags());
        String itemPrice = "$" + item.getRentPrice();
        String itemLocation = item.getAddress().split(",")[1];

        txtItemName.setText(itemName);
        txtItemDescription.setText(itemDescription);
        txtItemTags.setText(itemTags);
        txtItemPrice.setText(itemPrice);
        txtItemLocation.setText(itemLocation);
    }

    public String getTagsAsString(List<String> tagsList){
        List<String> itemTags = tagsList;
        String tagsString = itemTags.get(0);
        itemTags.remove(0);

        for (String tag : itemTags) {
            tagsString.concat(", ");
            tagsString.concat(tag);
        }

        return tagsString;
    }
}

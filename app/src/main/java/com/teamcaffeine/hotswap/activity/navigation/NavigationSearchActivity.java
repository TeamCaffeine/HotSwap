package com.teamcaffeine.hotswap.activity.navigation;

import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.SearchView;

import com.teamcaffeine.hotswap.R;

public class NavigationSearchActivity extends NavigationActivity {

    int searchIndex = 1;

    SearchView searchView;
    ListView lstSearchResults;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View wizard = getLayoutInflater().inflate(R.layout.activity_navigation_search, null);
        dynamicContent.addView(wizard);
        navigation.getMenu().getItem(searchIndex).setChecked(true);

        lstSearchResults = findViewById(R.id.lstSearchResults);

        searchView = findViewById(R.id.searchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                //TODO search database and populate the list view
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                //TODO decide what functionality we want here. Probably do nothing.
                return false;
            }
        });
    }

}

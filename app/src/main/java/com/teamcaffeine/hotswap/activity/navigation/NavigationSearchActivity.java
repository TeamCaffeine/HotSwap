package com.teamcaffeine.hotswap.activity.navigation;

import android.os.Bundle;
import android.view.View;

import com.teamcaffeine.hotswap.R;

public class NavigationSearchActivity extends NavigationActivity {

    int searchIndex = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View wizard = getLayoutInflater().inflate(R.layout.activity_navigation_search, null);
        dynamicContent.addView(wizard);
        navigation.getMenu().getItem(searchIndex).setChecked(true);
    }

}

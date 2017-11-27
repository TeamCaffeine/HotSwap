package com.teamcaffeine.hotswap.activity.navigation;

import android.os.Bundle;
import android.view.View;

import com.teamcaffeine.hotswap.R;

public class NavigationInboxActivity extends NavigationActivity {

    int inboxIndex = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View wizard = getLayoutInflater().inflate(R.layout.activity_navigation_inbox, null);
        dynamicContent.addView(wizard);
        navigation.getMenu().getItem(inboxIndex).setChecked(true);
    }
}

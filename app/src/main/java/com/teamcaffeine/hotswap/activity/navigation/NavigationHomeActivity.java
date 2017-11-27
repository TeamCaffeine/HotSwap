package com.teamcaffeine.hotswap.activity.navigation;

import android.os.Bundle;
import android.view.View;

import com.teamcaffeine.hotswap.R;

public class NavigationHomeActivity extends NavigationActivity {

    int homeIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View wizard = getLayoutInflater().inflate(R.layout.activity_navigation_home, null);
        dynamicContent.addView(wizard);
        navigation.getMenu().getItem(homeIndex).setChecked(true);
    }
}

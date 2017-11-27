package com.teamcaffeine.hotswap.activity.navigation;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.teamcaffeine.hotswap.R;

public class NavigationProfileActivity extends NavigationActivity {

    int profileIndex = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View wizard = getLayoutInflater().inflate(R.layout.activity_navigation_profile, null);
        dynamicContent.addView(wizard);
        navigation.getMenu().getItem(profileIndex).setChecked(true);
    }
}

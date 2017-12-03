package com.teamcaffeine.hotswap.navigation;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.squareup.timessquare.CalendarPickerView;
import com.teamcaffeine.hotswap.R;

import java.util.Calendar;
import java.util.Date;

public class ListItemFragment extends Fragment {

    private CalendarPickerView calendar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View root = inflater.inflate(R.layout.fragment_list_item, null);
        return root;
    }

    @SuppressLint("ClickableViewAccessibility")
    public void onViewCreated(View view, Bundle savedInstanceState) {
        Calendar nextYear = Calendar.getInstance();
        nextYear.add(Calendar.YEAR, 1);

        calendar = (CalendarPickerView) getView().findViewById(R.id.calendar_view);
        Date today = new Date();
        calendar.init(today, nextYear.getTime())
                .withSelectedDate(today)
                .inMode(CalendarPickerView.SelectionMode.MULTIPLE);

        calendar.setOnTouchListener(new View.OnTouchListener() {

            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        // Disallow ScrollView to intercept touch events.
                        v.getParent().requestDisallowInterceptTouchEvent(true);
                        break;

                    case MotionEvent.ACTION_UP:
                        // Allow ScrollView to intercept touch events.
                        v.getParent().requestDisallowInterceptTouchEvent(false);
                        break;
                }

                v.onTouchEvent(event);
                return true;
            }
        });
    }

    ListItemFragment.ListItemFragmentListener LIFL;

    public interface ListItemFragmentListener {
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        LIFL = (ListItemFragment.ListItemFragmentListener) context;  //context is a handle to the main activity, let's bind it to our interface.
    }
}

 package com.haanhgs.shoppingtest;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Spinner;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import com.google.firebase.firestore.Query;
import com.haanhgs.shoppingtest.model.Restaurant;
import com.haanhgs.shoppingtest.repo.Filters;


 public class FilterDialogFragment extends DialogFragment implements View.OnClickListener {

    interface FilterListener {
        void onFilter(Filters filters);
    }

     private FilterListener filterListener;

    public static final String TAG = "FilterDialog";
    private View fragmentView;
    private Spinner spinnerCategory;
    private Spinner spinnerCity;
    private Spinner spinnerSort;
    private Spinner spinnerPrice;


    private void initViews(View view){
        spinnerCategory = view.findViewById(R.id.sp_category);
        spinnerCity = view.findViewById(R.id.sp_city);
        spinnerSort = view.findViewById(R.id.sp_sort);
        spinnerPrice = view.findViewById(R.id.sp_price);

        //init buttons
        fragmentView.findViewById(R.id.bn_search).setOnClickListener(this);
        fragmentView.findViewById(R.id.bn_cancel).setOnClickListener(this);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        fragmentView = inflater.inflate(R.layout.dialog_filters, container, false);
        initViews(fragmentView);
        return fragmentView;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        if (context instanceof FilterListener) {
            filterListener = (FilterListener) context;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getDialog() != null && getDialog().getWindow() != null){
            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
//        if (getDialog() != null && getDialog().getWindow() != null){
//            getDialog().getWindow().setLayout(
//                    ViewGroup.LayoutParams.MATCH_PARENT,
//                    ViewGroup.LayoutParams.WRAP_CONTENT);
//        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bn_search:
                onSearchClicked();
                break;
            case R.id.bn_cancel:
                onCancelClicked();
                break;
        }
    }

    public void onSearchClicked() {
        if (filterListener != null) {
            filterListener.onFilter(getFilters());
        }
        dismiss();
    }

    public void onCancelClicked() {
        dismiss();
    }

    @Nullable
    private String getSelectedCategory() {
        String selected = (String) spinnerCategory.getSelectedItem();
        if (getString(R.string.value_any_category).equals(selected)) {
            return null;
        } else {
            return selected;
        }
    }

    @Nullable
    private String getSelectedCity() {
        String selected = (String) spinnerCity.getSelectedItem();
        if (getString(R.string.value_any_city).equals(selected)) {
            return null;
        } else {
            return selected;
        }
    }

    private int getSelectedPrice() {
        String selected = (String) spinnerPrice.getSelectedItem();
        if (selected.equals(getString(R.string.price_1))) {
            return 1;
        } else if (selected.equals(getString(R.string.price_2))) {
            return 2;
        } else if (selected.equals(getString(R.string.price_3))) {
            return 3;
        } else {
            return -1;
        }
    }

    @Nullable
    private String getSelectedSortBy() {
        String selected = (String) spinnerSort.getSelectedItem();
        if (getString(R.string.sort_by_rating).equals(selected)) {
            return Restaurant.FIELD_AVG_RATING;
        } if (getString(R.string.sort_by_price).equals(selected)) {
            return Restaurant.FIELD_PRICE;
        } if (getString(R.string.sort_by_popularity).equals(selected)) {
            return Restaurant.FIELD_POPULARITY;
        }
        return null;
    }

    @Nullable
    private Query.Direction getSortDirection() {
        String selected = (String) spinnerSort.getSelectedItem();
        if (getString(R.string.sort_by_rating).equals(selected)) {
            return Query.Direction.DESCENDING;
        } if (getString(R.string.sort_by_price).equals(selected)) {
            return Query.Direction.ASCENDING;
        } if (getString(R.string.sort_by_popularity).equals(selected)) {
            return Query.Direction.DESCENDING;
        }

        return null;
    }

    public void resetFilters() {
        if (fragmentView != null) {
            spinnerCategory.setSelection(0);
            spinnerCity.setSelection(0);
            spinnerPrice.setSelection(0);
            spinnerSort.setSelection(0);
        }
    }

    public Filters getFilters() {
        Filters filters = new Filters();

        if (fragmentView != null) {
            filters.setCategory(getSelectedCategory());
            filters.setCity(getSelectedCity());
            filters.setPrice(getSelectedPrice());
            filters.setSortBy(getSelectedSortBy());
            filters.setSortDirection(getSortDirection());
        }

        return filters;
    }
}

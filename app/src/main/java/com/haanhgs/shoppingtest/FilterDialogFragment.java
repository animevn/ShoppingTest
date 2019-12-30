package com.haanhgs.shoppingtest;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import com.google.firebase.firestore.Query;
import com.haanhgs.shoppingtest.model.Restaurant;
import com.haanhgs.shoppingtest.repo.Filters;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class FilterDialogFragment extends DialogFragment{

    @BindView(R.id.tv_filter_title)
    TextView tvFilterTitle;
    @BindView(R.id.iv_category)
    ImageView ivCategory;
    @BindView(R.id.sp_category)
    Spinner spCategory;
    @BindView(R.id.iv_city)
    ImageView ivCity;
    @BindView(R.id.sp_city)
    Spinner spCity;
    @BindView(R.id.iv_price)
    ImageView ivPrice;
    @BindView(R.id.sp_price)
    Spinner spPrice;
    @BindView(R.id.iv_sort)
    ImageView ivSort;
    @BindView(R.id.sp_sort)
    Spinner spSort;
    @BindView(R.id.bn_cancel)
    Button bnCancel;
    @BindView(R.id.bn_search)
    Button bnSearch;

    interface FilterListener {
        void onFilter(Filters filters);
    }

    private FilterListener filterListener;

    public static final String TAG = "FilterDialog";
    private View fragmentView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        fragmentView = inflater.inflate(R.layout.dialog_filters, container, false);
        ButterKnife.bind(this, fragmentView);
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
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
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

    @OnClick({R.id.bn_cancel, R.id.bn_search})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.bn_cancel:
                onCancelClicked();
                break;
            case R.id.bn_search:
                onSearchClicked();
                break;
        }
    }

    @Nullable
    private String getSelectedCategory() {
        String selected = (String) spCategory.getSelectedItem();
        if (getString(R.string.value_any_category).equals(selected)) {
            return null;
        } else {
            return selected;
        }
    }

    @Nullable
    private String getSelectedCity() {
        String selected = (String) spCity.getSelectedItem();
        if (getString(R.string.value_any_city).equals(selected)) {
            return null;
        } else {
            return selected;
        }
    }

    private int getSelectedPrice() {
        String selected = (String) spPrice.getSelectedItem();
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
        String selected = (String) spSort.getSelectedItem();
        if (getString(R.string.sort_by_rating).equals(selected)) {
            return Restaurant.FIELD_AVG_RATING;
        }
        if (getString(R.string.sort_by_price).equals(selected)) {
            return Restaurant.FIELD_PRICE;
        }
        if (getString(R.string.sort_by_popularity).equals(selected)) {
            return Restaurant.FIELD_POPULARITY;
        }
        return null;
    }

    @Nullable
    private Query.Direction getSortDirection() {
        String selected = (String) spSort.getSelectedItem();
        if (getString(R.string.sort_by_rating).equals(selected)) {
            return Query.Direction.DESCENDING;
        }
        if (getString(R.string.sort_by_price).equals(selected)) {
            return Query.Direction.ASCENDING;
        }
        if (getString(R.string.sort_by_popularity).equals(selected)) {
            return Query.Direction.DESCENDING;
        }

        return null;
    }

    public void resetFilters() {
        if (fragmentView != null) {
            spCategory.setSelection(0);
            spCity.setSelection(0);
            spPrice.setSelection(0);
            spSort.setSelection(0);
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

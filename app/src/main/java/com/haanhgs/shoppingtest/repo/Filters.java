package com.haanhgs.shoppingtest.repo;

import android.content.Context;
import android.text.TextUtils;
import com.google.firebase.firestore.Query;
import com.haanhgs.shoppingtest.R;
import com.haanhgs.shoppingtest.model.Restaurant;


public class Filters {

    private String category = null;
    private String city = null;
    private int price = -1;
    private String sortBy = null;
    private Query.Direction sortDirection = null;

    public Filters() {}

    public static Filters getDefault() {
        Filters filters = new Filters();
        filters.setSortBy(Restaurant.FIELD_AVG_RATING);
        filters.setSortDirection(Query.Direction.DESCENDING);

        return filters;
    }

    public boolean hasCategory() {
        return !(TextUtils.isEmpty(category));
    }

    public boolean hasCity() {
        return !(TextUtils.isEmpty(city));
    }

    public boolean hasPrice() {
        return (price > 0);
    }

    public boolean hasSortBy() {
        return !(TextUtils.isEmpty(sortBy));
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public String getSortBy() {
        return sortBy;
    }

    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }

    public Query.Direction getSortDirection() {
        return sortDirection;
    }

    public void setSortDirection(Query.Direction sortDirection) {
        this.sortDirection = sortDirection;
    }

    public String getSearchDescription(Context context) {
        StringBuilder stringBuilder = new StringBuilder();

        if (category == null && city == null) {
            stringBuilder.append("<b>");
            stringBuilder.append(context.getString(R.string.all_restaurants));
            stringBuilder.append("</b>");
        }

        if (category != null) {
            stringBuilder.append("<b>");
            stringBuilder.append(category);
            stringBuilder.append("</b>");
        }

        if (category != null && city != null) {
            stringBuilder.append(" in ");
        }

        if (city != null) {
            stringBuilder.append("<b>");
            stringBuilder.append(city);
            stringBuilder.append("</b>");
        }

        if (price > 0) {
            stringBuilder.append(" for ");
            stringBuilder.append("<b>");
            stringBuilder.append(RestaurantRepo.getPriceString(price));
            stringBuilder.append("</b>");
        }

        return stringBuilder.toString();
    }

    public String getOrderDescription(Context context) {
        if (Restaurant.FIELD_PRICE.equals(sortBy)) {
            return context.getString(R.string.sorted_by_price);
        } else if (Restaurant.FIELD_POPULARITY.equals(sortBy)) {
            return context.getString(R.string.sorted_by_popularity);
        } else {
            return context.getString(R.string.sorted_by_rating);
        }
    }
}
